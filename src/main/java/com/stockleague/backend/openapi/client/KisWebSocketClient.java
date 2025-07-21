package com.stockleague.backend.openapi.client;

import com.stockleague.backend.infra.redis.OpenApiTokenRedisService;
import com.stockleague.backend.infra.redis.StockOrderBookRedisService;
import com.stockleague.backend.infra.redis.StockPriceRedisService;
import com.stockleague.backend.openapi.parser.KisWebSocketResponseParser;
import com.stockleague.backend.stock.dto.response.stock.StockOrderBookDto;
import com.stockleague.backend.stock.dto.response.stock.StockPriceDto;
import jakarta.annotation.PreDestroy;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletionStage;

@Slf4j
@Component
@RequiredArgsConstructor
public class KisWebSocketClient {

    private final StockPriceRedisService stockPriceRedisService;
    private final OpenApiTokenRedisService openApiTokenRedisService;
    private final StockOrderBookRedisService stockOrderBookRedisService;
    private final KisWebSocketResponseParser parser;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final SimpMessagingTemplate messagingTemplate;

    private static final String WS_URL = "ws://ops.koreainvestment.com:31000";
    private static final List<String> TICKERS = List.of("005930");

    private WebSocket webSocket;
    private boolean isConnected = false;

    private int reconnectAttempts = 0;

    /**
     * 평일 오전 8시 59분 20초에 WebSocket 연결 시도
     */
    @Scheduled(cron = "20 59 8 * * MON-FRI")
    public void scheduledConnect() {
        log.info("[스케줄러] 오전 8:59 WebSocket 연결 시작");
        disconnect();
        connect();
    }

    /**
     * 평일 오후 3시 30분 31초에 WebSocket 연결 종료
     */
    @Scheduled(cron = "31 30 15 * * MON-FRI")
    public void scheduledDisconnect() {
        log.info("[스케줄러] 오후 15:30 WebSocket 연결 종료 요청");
        disconnect();
    }

    /**
     * 서버 시작 시 장중이면 자동 연결 수행
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        log.info("[WebSocket] 서버 초기화 - 실시간 연결 준비");

        if (isMarketTime()) {
            connect();
        } else {
            log.info("[WebSocket] 장시간 외 - 초기 연결 생략");
        }
    }

    /**
     * WebSocket 연결 로직 (이미 연결 상태면 중복 연결 방지)
     */
    public void connect() {
        if (isConnected) {
            log.info("이미 WebSocket에 연결되어 있습니다.");
            return;
        }

        String approvalKey = openApiTokenRedisService.getRealTimeKey();
        if (approvalKey == null || approvalKey.isEmpty()) {
            log.error("실시간 키가 존재하지 않습니다. WebSocket 연결 중단.");
            return;
        }

        initWebSocket(approvalKey);
    }

    /**
     * WebSocket 연결 초기화 및 비동기 리스너 등록
     */
    private void initWebSocket(String approvalKey) {
        HttpClient client = HttpClient.newHttpClient();
        client.newWebSocketBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .buildAsync(URI.create(WS_URL), createWebSocketListener(approvalKey))
                .thenAccept(ws -> {
                    this.webSocket = ws;
                    this.isConnected = true;
                    reconnectAttempts = 0;
                })
                .exceptionally(ex -> {
                    log.error("WebSocket 연결 예외 발생", ex);
                    return null;
                });
    }

    /**
     * WebSocket 연결 종료
     */
    public void disconnect() {
        if (webSocket != null) {
            try {
                webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "Market closed");
                log.info("WebSocket 정상 종료 요청 전송");
            } catch (Exception e) {
                log.warn("WebSocket 종료 중 예외", e);
            }
        } else {
            log.info("WebSocket 객체가 null이므로 종료 요청 생략");
        }

        isConnected = false;
        webSocket = null;
    }

    /**
     * 애플리케이션 종료 시 WebSocket 연결 종료
     */
    @PreDestroy
    public void onShutdown() {
        disconnect();
        scheduler.shutdownNow();
    }

    /**
     * WebSocket 리스너 생성 (평문 메시지 수신 처리)
     */
    private WebSocket.Listener createWebSocketListener(String approvalKey) {
        return new WebSocket.Listener() {
            @Override
            public void onOpen(WebSocket webSocket) {
                log.info("WebSocket 연결 성공");
                TICKERS.forEach(ticker -> sendApprovalAndSubscribe(webSocket, approvalKey, "H0STCNT0", ticker));
                TICKERS.forEach(ticker -> sendApprovalAndSubscribe(webSocket, approvalKey, "H0STASP0", ticker));
                WebSocket.Listener.super.onOpen(webSocket);
            }

            @Override
            public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
                String message = data.toString();
                handlePlainMessage(message);
                return WebSocket.Listener.super.onText(webSocket, data, last);
            }

            @Override
            public void onError(WebSocket webSocket, Throwable error) {
                log.error("WebSocket 오류 발생", error);
                KisWebSocketClient.this.webSocket = null;
                retryWithBackoff();
            }

            @Override
            public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
                log.info("WebSocket 종료: [{}] {}", statusCode, reason);
                isConnected = false;
                KisWebSocketClient.this.webSocket = null;
                retryWithBackoff();
                return WebSocket.Listener.super.onClose(webSocket, statusCode, reason);
            }
        };
    }

    /**
     * 재연결을 위한 백오프 전략 (최대 5분 지연)
     */
    private void retryWithBackoff() {
        if (isConnected || webSocket != null) {
            return;
        }

        reconnectAttempts++;
        long delay = Math.min((1L << reconnectAttempts), 300);
        log.warn("재연결 시도 예정 ({}회차, {}초 후)", reconnectAttempts, delay);

        scheduler.schedule(this::connect, delay, TimeUnit.SECONDS);
    }

    /**
     * 실시간 키를 포함한 구독 요청 메시지 전송
     */
    private void sendApprovalAndSubscribe(WebSocket webSocket, String approvalKey, String trId, String ticker) {
        String message = buildSubscribeMessage(approvalKey, trId, ticker);
        webSocket.sendText(message, true);
        log.info("인증 + 구독 요청 전송: {} / {}", trId, ticker);
    }

    /**
     * 구독 메시지 JSON 생성
     */
    private String buildSubscribeMessage(String approvalKey, String trId, String trKey) {
        return String.format("""
                {
                  "header": {
                    "approval_key": "%s",
                    "custtype": "P",
                    "tr_type": "1",
                    "content-type": "utf-8"
                  },
                  "body": {
                    "input": {
                      "tr_id": "%s",
                      "tr_key": "%s"
                    }
                  }
                }
                """, approvalKey, trId, trKey);
    }

    /**
     * <p>평문 메시지를 파싱</p>
     * <p>주가는 {@link StockPriceDto}로 파싱</p>
     * <p>호가는 {@link StockOrderBookDto}로 파싱</p>
     */
    private void handlePlainMessage(String message) {
        try {
            String[] parts = message.split("\\|");
            if (parts.length < 4) {
                log.warn("잘못된 평문 메시지: {}", message);
                return;
            }

            String trId = parts[1];
            String body = parts[3];

            if (trId.startsWith("H0STCNT0")) {
                List<StockPriceDto> dtos = parser.parsePlainText(trId, body);
                for (StockPriceDto dto : dtos) {
                    stockPriceRedisService.save(dto);
                    messagingTemplate.convertAndSend("/topic/stocks/" + dto.ticker(), dto);
                }
            } else if (trId.startsWith("H0STASP0")) {
                StockOrderBookDto orderBookDto = parser.parseOrderBook(body);
                if(orderBookDto != null) {
                    stockOrderBookRedisService.save(orderBookDto);
                }
                messagingTemplate.convertAndSend("/topic/orderbook/" + orderBookDto.ticker(), orderBookDto);
            }
        } catch (Exception e) {
            log.error("평문 메시지 처리 중 예외 발생", e);
        }
    }

    /**
     * 현재 시간이 장중인지 여부 확인 (평일 9:00~15:30)
     */
    private boolean isMarketTime() {
        LocalDateTime now = LocalDateTime.now();
        DayOfWeek day = now.getDayOfWeek();
        if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY) {
            return false;
        }

        LocalTime time = now.toLocalTime();
        return !time.isBefore(LocalTime.of(9, 0)) && !time.isAfter(LocalTime.of(15, 30));
    }
}

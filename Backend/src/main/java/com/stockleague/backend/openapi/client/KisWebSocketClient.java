package com.stockleague.backend.openapi.client;

import static com.stockleague.backend.global.util.MarketTimeUtil.isMarketOpen;
import static com.stockleague.backend.global.util.MarketTimeUtil.shouldCollectOrderbookNow;

import com.stockleague.backend.infra.redis.OpenApiTokenRedisService;
import com.stockleague.backend.infra.redis.StockOrderBookRedisService;
import com.stockleague.backend.infra.redis.StockOrderBookSnapshotRedisService;
import com.stockleague.backend.infra.redis.StockPriceRedisService;
import com.stockleague.backend.openapi.parser.KisWebSocketResponseParser;
import com.stockleague.backend.stock.dto.response.stock.StockOrderBookDto;
import com.stockleague.backend.stock.dto.response.stock.StockPriceDto;
import jakarta.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
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
public class KisWebSocketClient {
    private volatile String currentApprovalKey;

    private static final int SUBSCRIBE_DELAY_SECONDS = 3;
    private static final int TICKER_BATCH_SIZE = 5;
    private int expectedSubscribeCount = 0;
    private int subscribeSuccessCount = 0;

    private final StockPriceRedisService stockPriceRedisService;
    private final OpenApiTokenRedisService openApiTokenRedisService;
    private final StockOrderBookRedisService stockOrderBookRedisService;
    private final KisWebSocketResponseParser parser;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final SimpMessagingTemplate messagingTemplate;

    private final StockOrderBookSnapshotRedisService snapshotRedisService;

    private static final String WS_URL = "ws://ops.koreainvestment.com:31000";
    private final List<String> tickers;

    private WebSocket webSocket;
    private boolean isConnected = false;
    private int reconnectAttempts = 0;
    private final ConcurrentMap<String, Long> lastSnapshotMillis = new ConcurrentHashMap<>();

    public KisWebSocketClient(
            StockPriceRedisService stockPriceRedisService,
            OpenApiTokenRedisService openApiTokenRedisService,
            StockOrderBookRedisService stockOrderBookRedisService,
            KisWebSocketResponseParser parser,
            SimpMessagingTemplate messagingTemplate,
            List<String> tickers,
            StockOrderBookSnapshotRedisService snapshotRedisService
    ) {
        this.stockPriceRedisService = stockPriceRedisService;
        this.openApiTokenRedisService = openApiTokenRedisService;
        this.stockOrderBookRedisService = stockOrderBookRedisService;
        this.parser = parser;
        this.messagingTemplate = messagingTemplate;
        this.tickers = tickers;
        this.snapshotRedisService = snapshotRedisService;
    }

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
     * 평일 오후 3시 30분 10초에 WebSocket 연결 종료
     */
    @Scheduled(cron = "10 30 15 * * MON-FRI")
    public void scheduledDisconnect() {
        log.info("[스케줄러] 오후 15:30 WebSocket 연결 종료 요청");
        disconnect();
    }

    /** 평일 15:00:00에 호가만 해지 */
    @Scheduled(cron = "0 0 15 * * MON-FRI", zone = "Asia/Seoul")
    public void scheduledUnsubscribeOrderbookAt15() {
        if (webSocket == null || !isConnected) {
            log.info("[WebSocket] 15:00 호가 해제: 연결 없음 → 스킵");
            return;
        }
        log.info("[WebSocket] 15:00 호가(H0STASP0) 일괄 해제 시작");
        for (String ticker : tickers) {
            sendUnsubscribe(webSocket, "H0STASP0", ticker);
        }
        log.info("[WebSocket] 15:00 호가 해제 완료");
        lastSnapshotMillis.clear();
    }

    /**
     * 서버 시작 시 장중이면 자동 연결 수행
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        log.info("[WebSocket] 서버 초기화 - 실시간 연결 준비");
        if (isMarketOpen()) {
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
        this.currentApprovalKey = approvalKey;
        HttpClient client = HttpClient.newHttpClient();
        client.newWebSocketBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .buildAsync(URI.create(WS_URL), createWebSocketListener())
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
    private WebSocket.Listener createWebSocketListener() {
        return new WebSocket.Listener() {
            @Override
            public void onOpen(WebSocket webSocket) {
                log.info("WebSocket 연결 성공");

                final boolean collectOrderbookNow = shouldCollectOrderbookNow();
                expectedSubscribeCount = tickers.size() * (1 + (collectOrderbookNow ? 1 : 0));
                subscribeSuccessCount = 0;

                List<List<String>> batches = partitionTickers(tickers, TICKER_BATCH_SIZE);
                for (int i = 0; i < batches.size(); i++) {
                    List<String> batch = batches.get(i);
                    scheduler.schedule(
                            () -> subscribeBatch(webSocket, batch, collectOrderbookNow),
                            (long) i * SUBSCRIBE_DELAY_SECONDS, TimeUnit.SECONDS
                    );
                }

                WebSocket.Listener.super.onOpen(webSocket);
            }

            private final StringBuilder partialMessage = new StringBuilder();

            @Override
            public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
                partialMessage.append(data);
                if (last) {
                    String fullMessage = partialMessage.toString();
                    partialMessage.setLength(0);
                    try {
                        handlePlainMessage(fullMessage);
                    } catch (Exception e) {
                        log.error("[WebSocket] 평문 처리 예외", e);
                    }
                }
                return WebSocket.Listener.super.onText(webSocket, data, last);
            }

            @Override
            public void onError(WebSocket webSocket, Throwable error) {
                log.error("WebSocket 오류", error);
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

    private void subscribeBatch(
            WebSocket webSocket, List<String> batch, boolean collectOrderbookNow) {
        for (String ticker : batch) {
            sendApprovalAndSubscribe(webSocket, "H0STCNT0", ticker);
            if (collectOrderbookNow) {
                sendApprovalAndSubscribe(webSocket, "H0STASP0", ticker);
            } else {
                log.info("호가 구독 생략(15:00 이후): {}", ticker);
            }
        }
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
    private void sendApprovalAndSubscribe(WebSocket webSocket, String trId, String ticker) {
        if (!hasApprovalKey()) {
            log.error("approval_key 없음: 구독 메시지 전송 중단 trId={}, ticker={}", trId, ticker);
            return;
        }

        String message = buildSubscribeMessage(trId, ticker);
        webSocket.sendText(message, true);
        log.info("인증 + 구독 요청 전송: {} / {}", trId, ticker);
    }

    /** 해지 요청 */
    private void sendUnsubscribe(WebSocket webSocket, String trId, String ticker) {
        if (!hasApprovalKey()) {
            log.error("approval_key 없음: 해지 메시지 전송 중단 trId={}, ticker={}", trId, ticker);
            return;
        }

        String message = buildUnsubscribeMessage(trId, ticker);
        webSocket.sendText(message, true);
        log.info("구독 해지 요청 전송: {} / {}", trId, ticker);
    }

    /**
     * 구독 메시지 JSON 생성
     */
    private String buildSubscribeMessage(String trId, String trKey) {
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
                """, this.currentApprovalKey, trId, trKey);
    }

    /** 해지 메시지 */
    private String buildUnsubscribeMessage(String trId, String trKey) {
        return String.format("""
            {
              "header": {
                "approval_key": "%s",
                "custtype": "P",
                "tr_type": "2",
                "content-type": "utf-8"
              },
              "body": {
                "input": {
                  "tr_id": "%s",
                  "tr_key": "%s"
                }
              }
            }
            """, this.currentApprovalKey, trId, trKey);
    }

    /**
     * <p>평문 메시지를 파싱</p>
     * <p>주가는 {@link StockPriceDto}로 파싱</p>
     * <p>호가는 {@link StockOrderBookDto}로 파싱</p>
     */
    private void handlePlainMessage(String message) {
        try {
            if (message.contains("\"msg1\":\"SUBSCRIBE SUCCESS\"")) {
                subscribeSuccessCount++;
                log.debug("구독 성공 응답 수신 ({}/{})", subscribeSuccessCount, expectedSubscribeCount);

                if (subscribeSuccessCount == expectedSubscribeCount) {
                    log.info("모든 종목 구독 성공 완료!");
                }

                return;
            }

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
                if (!shouldCollectOrderbookNow()) {
                    log.debug("호가 프레임 무시(15:00 이후)");
                    return;
                }

                StockOrderBookDto orderBookDto = parser.parseOrderBook(body);
                if (orderBookDto != null) {
                    stockOrderBookRedisService.save(orderBookDto);
                    messagingTemplate.convertAndSend("/topic/orderbook/" + orderBookDto.ticker(), orderBookDto);

                    final String ticker = orderBookDto.ticker();
                    lastSnapshotMillis.compute(ticker, (t, lastMs) -> {
                        long now = System.currentTimeMillis();
                        if (lastMs != null && (now - lastMs) < 1000L) {
                            return lastMs;
                        }
                        try {
                            long ver = snapshotRedisService.writeSnapshot(orderBookDto);
                            log.debug("[Snapshot] {} ver={} (throttled <= 1/sec)", ticker, ver);
                        } catch (Exception e) {
                            log.warn("[Snapshot] write 실패: {}", e.getMessage(), e);
                            return lastMs;
                        }
                        return now;
                    });
                }
            }
        } catch (Exception e) {
            log.error("평문 메시지 처리 중 예외 발생", e);
        }
    }

    private List<List<String>> partitionTickers(List<String> tickers, int size) {
        List<List<String>> partitioned = new ArrayList<>();
        for (int i = 0; i < tickers.size(); i += size) {
            partitioned.add(tickers.subList(i, Math.min(i + size, tickers.size())));
        }
        return partitioned;
    }

    private boolean hasApprovalKey() {
        return this.currentApprovalKey != null && !this.currentApprovalKey.isBlank();
    }
}
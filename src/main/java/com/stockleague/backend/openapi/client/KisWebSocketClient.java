package com.stockleague.backend.openapi.client;

import com.stockleague.backend.infra.redis.OpenApiTokenRedisService;
import jakarta.annotation.PostConstruct;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletionStage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KisWebSocketClient {

    private final OpenApiTokenRedisService openApiTokenRedisService;

    private static final String WS_URL = "ws://ops.koreainvestment.com:21000";
    private static final List<String> TICKERS = List.of("005930", "000660");

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        log.info("[WebSocket] 서버 완전 초기화 후 WebSocket 연결 시도");
        connect();
    }

    public void connect() {
        log.info("[DEBUG] KisWebSocketClient 실행 시작됨 ✅");

        String approvalKey = openApiTokenRedisService.getRealTimeKey();
        log.info("현재 approvalKey: {}", approvalKey);

        if (approvalKey == null || approvalKey.isEmpty()) {
            log.error("실시간 키가 존재하지 않습니다. WebSocket 연결 중단.");
            return;
        }

        HttpClient client = HttpClient.newHttpClient();
        client.newWebSocketBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .buildAsync(URI.create(WS_URL), new WebSocket.Listener() {
                    @Override
                    public void onOpen(WebSocket webSocket) {
                        log.info("WebSocket 연결 성공");
                        sendApprovalKey(webSocket, approvalKey);
                        TICKERS.forEach(ticker -> {
                            subscribe(webSocket, "H0STCNT0", ticker); // 현재가
                            subscribe(webSocket, "H0STASP0", ticker); // 호가
                        });
                        WebSocket.Listener.super.onOpen(webSocket);
                    }

                    @Override
                    public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
                        log.info("수신: {}", data);
                        return WebSocket.Listener.super.onText(webSocket, data, last);
                    }

                    @Override
                    public void onError(WebSocket webSocket, Throwable error) {
                        log.error("WebSocket 오류 발생", error);
                        WebSocket.Listener.super.onError(webSocket, error);
                    }

                    @Override
                    public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
                        log.info("WebSocket 종료: [{}] {}", statusCode, reason);
                        return WebSocket.Listener.super.onClose(webSocket, statusCode, reason);
                    }

                    private void sendApprovalKey(WebSocket webSocket, String approvalKey) {
                        String message = String.format("""
                            {
                                "header": {
                                    "approval_key": "%s",
                                    "custtype": "P",
                                    "tr_type": "1",
                                    "content-type": "utf-8"
                                }
                            }
                        """, approvalKey);
                        webSocket.sendText(message, true);
                        log.info("인증 메시지 전송 완료");
                    }

                    private void subscribe(WebSocket webSocket, String trId, String ticker) {
                        String message = String.format("""
                            {
                                "header": {
                                    "tr_id": "%s",
                                    "tr_key": "%s"
                                }
                            }
                        """, trId, ticker);
                        webSocket.sendText(message, true);
                        log.info("구독 요청: {} / {}", trId, ticker);
                    }
                })
                .exceptionally(ex -> {
                    log.error("[❌] WebSocket 연결 예외 발생", ex);
                    return null;
                });
    }
}

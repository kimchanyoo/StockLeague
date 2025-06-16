package com.stockleague.backend.openapi.client;

import com.stockleague.backend.infra.redis.OpenApiTokenRedisService;
import com.stockleague.backend.openapi.parser.KisWebSocketResponseParser;
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
import org.json.JSONObject;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.time.Duration;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class KisWebSocketClient {

    private final OpenApiTokenRedisService openApiTokenRedisService;
    private final KisWebSocketResponseParser parser;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    private static final String WS_URL = "ws://ops.koreainvestment.com:31000";
    private static final List<String> TICKERS = List.of("005930");

    private WebSocket webSocket;
    private boolean isConnected = false;

    private final Map<String, String> encryptionKeyMap = new ConcurrentHashMap<>();
    private final Map<String, String> encryptionIvMap = new ConcurrentHashMap<>();

    private static final String MSG_CD = "msg_cd";
    private static final String OUTPUT = "output";
    private static final String TR_ID = "tr_id";
    private static final String TR_KEY = "tr_key";
    private static final String ENCRYPT = "encrypt";
    private static final String CIPHER_TEXT = "cipher_text";
    private static final String SUCCESS_MSG_CD = "OPSP0000";
    private static final String ENCRYPTED_FLAG_Y = "Y";

    private int reconnectAttempts = 0;

    @Scheduled(cron = "0 55 8 * * MON-FRI")
    public void scheduledConnect() {
        log.info("[스케줄러] 오전 8:55 WebSocket 연결 시작");
        connect();
    }

    @Scheduled(cron = "0 40 15 * * MON-FRI")
    public void scheduledDisconnect() {
        log.info("[스케줄러] 오후 15:40 WebSocket 연결 종료 요청");
        disconnect();
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        log.info("[WebSocket] 서버 초기화 - 실시간 연결 준비");

        if (isMarketTime()) {
            connect();
        } else {
            log.info("[WebSocket] 장시간 외 - 초기 연결 생략");
        }
    }

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

    public void disconnect() {
        if (webSocket != null && isConnected) {
            webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "Market closed");
            log.info("WebSocket 정상 종료 요청 전송");
            isConnected = false;
            webSocket = null;
        }
    }

    @PreDestroy
    public void onShutdown() {
        disconnect();
    }

    private WebSocket.Listener createWebSocketListener(String approvalKey) {
        return new WebSocket.Listener() {

            @Override
            public void onOpen(WebSocket webSocket) {
                log.info("WebSocket 연결 성공");
                TICKERS.forEach(ticker -> {
                    sendApprovalAndSubscribe(webSocket, approvalKey, "H0STCNT0", ticker);
//                    sendApprovalAndSubscribe(webSocket, approvalKey, "H0STASP0", ticker);
                });
                WebSocket.Listener.super.onOpen(webSocket);
            }

            @Override
            public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
                String message = data.toString();

                // JSON 포맷이면 기존 로직 사용
                if (message.trim().startsWith("{")) {
                    try {
                        JSONObject json = new JSONObject(message);
                        String trId = json.optJSONObject("header").optString(TR_ID, "");
                        if ("PINGPONG".equals(trId)) {
                            log.debug("PINGPONG 메시지 무시");
                            return WebSocket.Listener.super.onText(webSocket, data, last);
                        }

                        log.debug("[WebSocket 수신 JSON]: {}", message);
                        handleIncomingMessage(message);

                    } catch (Exception e) {
                        log.error("WebSocket JSON 메시지 처리 중 예외 발생", e);
                    }

                } else {
                    // 평문 메시지 처리
                    log.debug("[WebSocket 수신 평문]: {}", message);
                    handlePlainMessage(message);
                }

                return WebSocket.Listener.super.onText(webSocket, data, last);
            }

            @Override
            public void onError(WebSocket webSocket, Throwable error) {
                log.error("WebSocket 오류 발생", error);
                WebSocket.Listener.super.onError(webSocket, error);
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

    private void retryWithBackoff() {
        if (isConnected || webSocket != null) {
            log.info("재연결 시도 중단 (이미 연결됨)");
            return;
        }

        reconnectAttempts++;
        long delay = Math.min((1L << reconnectAttempts), 300);
        log.warn("재연결 시도 예정 ({}회차, {}ms 후)", reconnectAttempts, delay);

        scheduler.schedule(() -> {
            log.info("재연결 시도 중...");
            connect();
        }, delay, TimeUnit.SECONDS);
    }

    private void sendApprovalAndSubscribe(WebSocket webSocket, String approvalKey, String trId, String ticker) {
        String message = buildSubscribeMessage(approvalKey, trId, ticker);
        webSocket.sendText(message, true);
        log.info("인증 + 구독 요청 전송: {} / {}", trId, ticker);
    }

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

    private void handleIncomingMessage(String data) {
        try {
            JSONObject json = new JSONObject(data);
            JSONObject header = json.optJSONObject("header");
            JSONObject body = json.optJSONObject("body");

            if (header == null) {
                log.warn("WebSocket 수신 메시지에 header가 없습니다: {}", data);
                return;
            }

            String trKey = header.optString(TR_KEY, "");
            String encrypt = header.optString(ENCRYPT, "N");


            if (body == null) {
                log.info("body가 없는 WebSocket 메시지: {}", data);
                return;
            }

            if (SUCCESS_MSG_CD.equals(body.optString(MSG_CD)) && body.has(OUTPUT)) {
                handleKeyExchange(body, trKey);
            } else if (ENCRYPTED_FLAG_Y.equals(encrypt) && body.has(OUTPUT)) {
                handleEncryptedMessage(body, trKey);
            } else {
                log.info("평문 데이터 수신: {}", data);
            }
        } catch (Exception e) {
            log.error("실시간 메시지 처리 중 예외 발생", e);
        }
    }

    private void handleKeyExchange(JSONObject body, String trKey) {
        JSONObject output = body.getJSONObject(OUTPUT);
        String iv = output.optString("iv", null);
        String key = output.optString("key", null);

        if (iv != null && key != null) {
            encryptionIvMap.put(trKey, iv);
            encryptionKeyMap.put(trKey, key);
            log.info("구독 성공 → key/iv 저장됨 ({})", trKey);
        }
    }

    private void handleEncryptedMessage(JSONObject body, String trKey) {
        JSONObject output = body.getJSONObject(OUTPUT);
        String cipherText = output.optString(CIPHER_TEXT);
        String iv = encryptionIvMap.get(trKey);
        String key = encryptionKeyMap.get(trKey);

        if (iv != null && key != null && cipherText != null) {
            try {
                String decrypted = decryptAes256(cipherText, key, iv);
                log.info("복호화된 실시간 데이터 [{}]: {}", trKey, decrypted);

                handlePlainMessage(decrypted);

            } catch (Exception e) {
                log.error("복호화 실패 (trKey: {})", trKey, e);
            }
        } else {
            if (iv == null) log.warn("IV 누락됨 (trKey: {})", trKey);
            if (key == null) log.warn("KEY 누락됨 (trKey: {})", trKey);
            if (cipherText == null) log.warn("cipher_text 누락됨 (trKey: {})", trKey);
        }
    }

    private void handlePlainMessage(String message) {
        try {
            String[] parts = message.split("\\|");
            if (parts.length < 4) {
                log.warn("잘못된 평문 메시지: {}", message);
                return;
            }

            String trId = parts[1];
            String body = parts[3];

            log.debug("WebSocket 평문 메시지 필드 수: {}", body.split("\\^").length);

            List<StockPriceDto> dtos = parser.parsePlainText(trId, body);
            if (!dtos.isEmpty()) {
                dtos.forEach(dto -> log.info("실시간 평문 종목 시세 객체 생성: {}", dto));
            } else {
                log.debug("DTO 파싱 결과 없음: {}", message);
            }

        } catch (Exception e) {
            log.error("평문 메시지 처리 중 예외 발생", e);
        }
    }

    private boolean isMarketTime() {
        LocalDateTime now = LocalDateTime.now();
        DayOfWeek day = now.getDayOfWeek();
        if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY) return false;

        LocalTime time = now.toLocalTime();
        return !time.isBefore(LocalTime.of(9, 0)) && !time.isAfter(LocalTime.of(15, 30));
    }

    public static String decryptAes256(String base64CipherText, String key, String iv) throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "AES");
        IvParameterSpec ivParam = new IvParameterSpec(iv.getBytes(StandardCharsets.UTF_8));
        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParam);

        byte[] decodedBytes = Base64.getDecoder().decode(base64CipherText);
        byte[] decryptedBytes = cipher.doFinal(decodedBytes);
        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }
}

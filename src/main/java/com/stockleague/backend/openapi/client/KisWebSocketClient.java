package com.stockleague.backend.openapi.client;

import com.stockleague.backend.infra.redis.OpenApiTokenRedisService;
import com.stockleague.backend.openapi.parser.KisWebSocketResponseParser;
import com.stockleague.backend.stock.dto.response.stock.StockPriceDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
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

    private static final String WS_URL = "ws://ops.koreainvestment.com:21000";
    private static final List<String> TICKERS = List.of("005930", "000660");

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

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        log.info("[WebSocket] 서버 완전 초기화 후 WebSocket 연결 시도");
        connect();
    }

    public void connect() {
        log.info("[DEBUG] KisWebSocketClient 실행 시작됨");

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
                .exceptionally(ex -> {
                    log.error("WebSocket 연결 예외 발생", ex);
                    return null;
                });
    }

    private WebSocket.Listener createWebSocketListener(String approvalKey) {
        return new WebSocket.Listener() {

            @Override
            public void onOpen(WebSocket webSocket) {
                log.info("WebSocket 연결 성공");
                TICKERS.forEach(ticker -> {
                    sendApprovalAndSubscribe(webSocket, approvalKey, "H0STCNT0", ticker);
                    sendApprovalAndSubscribe(webSocket, approvalKey, "H0STASP0", ticker);
                });
                WebSocket.Listener.super.onOpen(webSocket);
            }

            @Override
            public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
                handleIncomingMessage(data.toString());
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
        };
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

            String trId = header.optString(TR_ID, "");
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

                StockPriceDto stockPriceDto = parser.parse(decrypted);
                if (stockPriceDto != null) {
                    log.info("실시간 종목 시세 객체 생성: {}", stockPriceDto);

                }
            } catch (Exception e) {
                log.error("복호화 실패 (trKey: {})", trKey, e);
            }
        } else {
            if (iv == null) log.warn("IV 누락됨 (trKey: {})", trKey);
            if (key == null) log.warn("KEY 누락됨 (trKey: {})", trKey);
            if (cipherText == null) log.warn("cipher_text 누락됨 (trKey: {})", trKey);
        }
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

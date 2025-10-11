package com.stockleague.backend.admin.controller;

import com.stockleague.backend.admin.dto.request.WsHealthPIngDto;
import com.stockleague.backend.admin.dto.response.HealthSimpleResponseDto;
import com.stockleague.backend.admin.dto.response.WsHealthPongDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.security.Principal;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/health")
@Tag(name = "Health", description = "연결 헬스 체크")
public class ClientHealthController {

    @PersistenceContext
    private final EntityManager em;

    private final SimpMessagingTemplate template;

    @GetMapping("/api")
    @Operation(
            summary = "API 서버 핑",
            description = "API 서버가 기동 중인지 간단히 확인합니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "정상",
            content = @Content(schema = @Schema(implementation = HealthSimpleResponseDto.class),
                    examples = @ExampleObject(
                            name = "ApiHealthOK",
                            summary = "API 서버 정상",
                            value = """
                                    {
                                      "success": true,
                                      "ok": true
                                    }
                                    """
                    )
            )
    )
    public ResponseEntity<HealthSimpleResponseDto> api() {
        return ResponseEntity.ok(new HealthSimpleResponseDto(true, true));
    }

    @GetMapping("/db")
    @Operation(
            summary = "DB 핑",
            description = "DB 연결 가능 여부를 확인합니다. 실패 시 500을 반환합니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "정상",
            content = @Content(schema = @Schema(implementation = HealthSimpleResponseDto.class),
                    examples = @ExampleObject(
                            name = "DbHealthOK",
                            summary = "DB 정상",
                            value = """
                                    {
                                      "ok": true,
                                      "ts": 1724567890123
                                    }
                                    """
                    )
            )
    )
    public ResponseEntity<?> db() {
        try {
            em.createNativeQuery("SELECT 1").getSingleResult();
            return ResponseEntity.ok(new HealthSimpleResponseDto(true, true));
        } catch (DataAccessException e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "ok", false,
                    "message", "DB ping failed"
            ));
        }
    }

    @MessageMapping("/health.ping")
    public void ping(WsHealthPIngDto ping, Principal principal) {
        String user = (principal != null ? principal.getName() : null);
        if (user == null) {
            return;
        }

        template.convertAndSendToUser(
                principal.getName(),
                "/queue/health",
                new WsHealthPongDto(true, true, ping.id())
        );
    }
}

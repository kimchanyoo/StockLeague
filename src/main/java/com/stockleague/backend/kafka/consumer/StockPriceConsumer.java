package com.stockleague.backend.kafka.consumer;

import com.stockleague.backend.stock.dto.response.stock.StockPriceDto;
import com.stockleague.backend.infra.redis.StockPriceRedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockPriceConsumer {

    private final StockPriceRedisService redisService;

    @KafkaListener(
            topics = "stock-price",
            groupId = "stock-price-group",
            containerFactory = "stockPriceListenerContainerFactory"
    )
    public void consume(StockPriceDto dto) {
        log.debug("Kafka 시세 수신: {} - {}", dto.ticker(), dto.datetime());
        redisService.save(dto);
    }
}

package com.stockleague.backend.kafka.producer;

import com.stockleague.backend.stock.dto.response.stock.StockPriceDto;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StockPriceProducer {

    private final KafkaTemplate<String, StockPriceDto> stockPriceKafkaTemplate;
    private static final String TOPIC = "stock-price";

    public void send(StockPriceDto dto) {
        stockPriceKafkaTemplate.send(TOPIC, dto);
    }
}

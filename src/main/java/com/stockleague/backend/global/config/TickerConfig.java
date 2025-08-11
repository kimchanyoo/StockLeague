package com.stockleague.backend.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
public class TickerConfig {

    @Bean
    public List<String> tickers() throws Exception {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(getClass().getClassLoader().getResourceAsStream("stocks.csv")))) {

            return reader.lines()
                    .skip(1) // 헤더 건너뜀
                    .map(line -> line.split(",")[1]) // stock_ticker
                    .map(String::trim)
                    .filter(t -> !t.isEmpty())
                    .collect(Collectors.toList());
        }
    }
}
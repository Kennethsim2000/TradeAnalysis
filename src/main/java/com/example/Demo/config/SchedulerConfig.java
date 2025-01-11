package com.example.Demo.config;

import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

@Configuration
@EnableScheduling
public class SchedulerConfig {

    final String timeSeries = "Time Series (60min)";
    final String metaData = "Meta Data";
    final String latest = "3. Last Refreshed";

    @Scheduled(fixedDelay = 10000) // duration between the end of the last execution and the start of the next execution is fixed
    public void scheduleFixedDelayTask() {
        String type = "TIME_SERIES_INTRADAY";
        String symbol = "META";
        String apiKey = "90J7NC83BNRTOTP6";
        String interval = "60min";
        String url = "https://www.alphavantage.co";
        WebClient client = WebClient.builder()
                .baseUrl(url)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
        Map<String, Object> res = client.get().uri(builder->builder
                .path("/query")
                .queryParam("function", type)
                .queryParam("symbol", symbol)
                .queryParam("interval", interval)
                .queryParam("apikey", apiKey)
                .build())
                .retrieve()
                .bodyToMono(Map.class)
                .onErrorResume(e -> Mono.empty())
                .block();
        Map<String, Object> tradeOrders = oneDayTrade(res);
        System.out.println(tradeOrders);
        System.out.println(
                "Fixed delay task - " + System.currentTimeMillis() / 1000);
    }

    public Map<String, Object> oneDayTrade(Map<String, Object> res) {
        Map<String, String> meta = (Map<String, String>) res.get(metaData);
        String latestDate = meta.get(latest);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime latestDateTime = LocalDateTime.parse(latestDate, formatter);
        Map<String, Object> tradeOrdersToday = new HashMap<>();
        Map<String, Object> tradeData = (Map<String, Object>) res.get(timeSeries);
        Set<String> dates = tradeData.keySet();
        for(String date: dates) {
            LocalDateTime dateTime = LocalDateTime.parse(date,formatter);
            if (dateTime.toLocalDate().equals(latestDateTime.toLocalDate())) {
                tradeOrdersToday.put(date, tradeData.get(date));
            }
        }
        return tradeOrdersToday;

    }
}

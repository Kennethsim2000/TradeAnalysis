package com.example.Demo.config;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.reactive.function.client.WebClient;

import com.example.Demo.model.OrderData;
import com.example.Demo.model.TradeOrder;
import com.example.Demo.service.TradeService;
import com.fasterxml.jackson.databind.ObjectMapper;

import reactor.core.publisher.Mono;

@Configuration
@EnableScheduling
public class SchedulerConfig {

    final String timeSeries = "Time Series (5min)";
    final String metaData = "Meta Data";
    final String latest = "3. Last Refreshed";

    @Autowired
    TradeService tradeService;

    @Scheduled(fixedDelay = 1000000) // duration between the end of the last execution and the start of the next execution is fixed
    public void scheduleFixedDelayTask() {
        String type = "TIME_SERIES_INTRADAY";
        String symbol = "IBM";
        String apiKey = "demo";
        String interval = "5min";
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
        List<TradeOrder> orders = collectOrders(tradeOrders, "META");
        for(TradeOrder order: orders) {
            tradeService.createOrder(order);
        }
        System.out.println(orders);
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

    public List<TradeOrder> collectOrders(Map<String, Object> res, String symbol) {
        Set<String> dateSet = res.keySet();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        ObjectMapper objectMapper = new ObjectMapper();
        List<TradeOrder> lst = new ArrayList<>();
        for(String dateStr: dateSet) {
            LocalDateTime dateTime = LocalDateTime.parse(dateStr, formatter);
            Object tradeDetails = res.get(dateStr);
            OrderData order = new OrderData();
            try {
                order = objectMapper.convertValue(tradeDetails, OrderData.class);
                TradeOrder tradeOrder = new TradeOrder();
                BeanUtils.copyProperties(order, tradeOrder);
                tradeOrder.setDate(dateTime);
                tradeOrder.setSymbol(symbol);
                lst.add(tradeOrder);
            } catch(Exception e) {
                System.out.println(e);
            }
        }
        return lst;

    }
}

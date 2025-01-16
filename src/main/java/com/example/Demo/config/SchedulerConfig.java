package com.example.Demo.config;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders;
import reactor.core.publisher.Mono;

@Configuration
@EnableScheduling
public class SchedulerConfig {

    final String timeSeries = "Time Series (5min)";
    final String metaData = "Meta Data";
    final String latest = "3. Last Refreshed";

    @Autowired
    TradeService tradeService;

    @Value(value="${spring.api.apiKey:demo}")
    private String apiKey;

    @Scheduled(fixedDelay = 1000000) // duration between the end of the last execution and the start of the next execution is fixed
    public void scheduleFixedDelayTask() {
        String type = "TIME_SERIES_INTRADAY";
        String symbol = "META";
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
        Map<String, Object> tradeOrders = ObtainTradeOrders(res, symbol);
        List<TradeOrder> orders = collectOrders(tradeOrders, symbol);
        for(TradeOrder order: orders) {
            tradeService.createOrder(order);
        }
        System.out.println(orders);
        System.out.println(
                "Fixed delay task - " + System.currentTimeMillis() / 1000);
    }

    public Map<String, Object> ObtainTradeOrders(Map<String, Object> res, String symbol) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        Map<String, Object> tradeOrders = new HashMap<>();
        Map<String, Object> tradeData = (Map<String, Object>) res.get(timeSeries);
        Set<String> dates = tradeData.keySet();
        for(String date: dates) {
            LocalDateTime dateTime = LocalDateTime.parse(date,formatter);
            LocalDateTime startOfDay = dateTime.with(LocalTime.MIN);
            LocalDateTime endOfDay = dateTime.with(LocalTime.MAX);
            List<TradeOrder> orderExist = tradeService.findByDateRangeAndSymbol(startOfDay, endOfDay, symbol);
            if (orderExist.size() == 0) {
                tradeOrders.put(date, tradeData.get(date));
            }
        }
        return tradeOrders;
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

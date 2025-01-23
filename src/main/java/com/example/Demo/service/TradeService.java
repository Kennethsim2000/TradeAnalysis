package com.example.Demo.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import com.example.Demo.model.TradeOrder;

import co.elastic.clients.elasticsearch._types.aggregations.Aggregate;
import co.elastic.clients.elasticsearch._types.aggregations.StatsAggregate;

public interface TradeService {
    public TradeOrder createOrder(TradeOrder order);
    public List<TradeOrder> findBySymbol(String symbol);
    public List<TradeOrder> findByDateRangeAndSymbol(LocalDateTime start, LocalDateTime end, String symbol);
    public List<TradeOrder> findBySymbolAndVolumeLessThan(int volume, String symbol);
    public StatsAggregate computeAggregation(LocalDateTime start, LocalDateTime end, String symbol);
    public List<TradeOrder>  getSignificantPriceDifferences(Integer threshold, String symbol);
    public Aggregate getMostSignificantPriceDifferencesPerDay(String symbol);
    public Map<String, Double> getVolumeTradedPerDay(String symbol);
}

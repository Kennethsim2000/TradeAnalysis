package com.example.Demo.service;

import java.time.LocalDateTime;
import java.util.List;

import com.example.Demo.model.TradeOrder;

public interface TradeService {
    public TradeOrder createOrder(TradeOrder order);
    public Iterable<TradeOrder> listOrders();
    public List<TradeOrder> findBySymbol(String symbol);
    public List<TradeOrder> findByDateRangeAndSymbol(LocalDateTime start, LocalDateTime end, String symbol);
    public List<TradeOrder> findBySymbolAndVolumeLessThan(int volume, String symbol);
}

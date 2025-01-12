package com.example.Demo.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.stereotype.Service;

import com.example.Demo.model.TradeOrder;
import com.example.Demo.repository.TradeRepository;

import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders;

@Service
public class TradeServiceImpl implements TradeService {
    @Autowired
    private TradeRepository tradeRepository;

    private ElasticsearchOperations elasticsearchOperations;


    @Override
    public TradeOrder createOrder(TradeOrder order) {
        return tradeRepository.save(order);
    }

    @Override
    public Iterable<TradeOrder> listOrders() {
        return tradeRepository.findAll();
    }

    @Override
    public List<TradeOrder> findBySymbol(String symbol) {
        return tradeRepository.findBySymbol(symbol);
    }

    @Override
    public List<TradeOrder> findByDateAndSymbol(String symbol, LocalDateTime date) {
        return tradeRepository.findByDateAndSymbol(symbol, date);
    }
}

package com.example.Demo.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import com.example.Demo.model.TradeOrder;

@Repository
public interface TradeRepository extends ElasticsearchRepository<TradeOrder, String> {
    List<TradeOrder> findBySymbol(String symbol);
    List<TradeOrder> findByDateAndSymbol(String symbol, LocalDateTime date);
}

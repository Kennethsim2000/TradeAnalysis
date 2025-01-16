package com.example.Demo.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.BaseQuery;
import org.springframework.stereotype.Service;

import com.example.Demo.model.TradeOrder;
import com.example.Demo.repository.TradeRepository;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.RangeQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.TermQuery;
import co.elastic.clients.json.JsonData;

@Service
public class TradeServiceImpl implements TradeService {
    @Autowired
    private TradeRepository tradeRepository;

    @Autowired
    private ElasticsearchOperations elasticSearchTemplate;

    @Autowired
    private ElasticsearchClient elasticsearchClient;

    /**
     * Creates a trade order.
     *
     * @param order order to be created
     * @return order that was created
     */
    @Override
    public TradeOrder createOrder(TradeOrder order) {
        return tradeRepository.save(order);
    }

    @Override
    public Iterable<TradeOrder> listOrders() {
        return tradeRepository.findAll();
    }

    /**
     * Retrieves trade orders that match the given stock symbol
     *
     * @param symbol symbol of trade order to filter by
     * @return list of trade orders filtered by symbol
     */
    @Override
    public List<TradeOrder> findBySymbol(String symbol) {
        return tradeRepository.findBySymbol(symbol);
    }

    /**
     * Retrieves trade orders that match the given stock symbol and fall within the specified date range.
     *
     * @param start  the start of the date range (inclusive).
     * @param end    the end of the date range (inclusive).
     * @param symbol the stock symbol to filter by.
     * @return a list of trade orders matching the symbol and date range.
     */
    @Override
    public List<TradeOrder> findByDateRangeAndSymbol(LocalDateTime start, LocalDateTime end, String symbol) {
        List<TradeOrder> orders = tradeRepository.findByDateBetweenAndSymbol(start, end, symbol);
        return orders;
    }

    /**
     * Finds trade orders that match the given stock symbol and have a transaction volume less than the specified value.
     *
     * @param volume the maximum transaction volume (exclusive).
     * @param symbol the stock symbol to filter by.
     * @return a list of trade orders matching the criteria.
     */
    @Override
    public List<TradeOrder> findBySymbolAndVolumeLessThan(int volume, String symbol) {
        Query queryByVolume = RangeQuery.of(r -> r
                        .field("volume")
                        .lte(JsonData.of(volume)))._toQuery();
        Query queryBySymbol = TermQuery.of(t -> t
                .field("symbol")
                .value(symbol))._toQuery();
        List<Query> queries = new ArrayList<>();
        queries.add(queryByVolume);
        queries.add(queryBySymbol);
        Query boolQuery = BoolQuery.of(b->b.must(queries))._toQuery();
        BaseQuery query = NativeQuery.builder()
                .withQuery(boolQuery)
                .build();
        SearchHits<TradeOrder> searchHits = elasticSearchTemplate.search(query, TradeOrder.class);
        List<SearchHit<TradeOrder>> hits = searchHits.getSearchHits();
        List<TradeOrder> res = new ArrayList<>();
        for(SearchHit<TradeOrder> hit: hits) {
            TradeOrder order = hit.getContent();
            res.add(order);
        }
        return res;
    }

}



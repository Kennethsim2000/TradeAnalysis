package com.example.Demo.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchAggregation;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchAggregations;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.BaseQuery;
import org.springframework.stereotype.Service;

import com.example.Demo.model.TradeOrder;
import com.example.Demo.repository.TradeRepository;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.Script;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregation;
import co.elastic.clients.elasticsearch._types.aggregations.StatsAggregate;
import co.elastic.clients.elasticsearch._types.aggregations.StatsAggregation;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.QueryBase;
import co.elastic.clients.elasticsearch._types.query_dsl.RangeQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.ScriptQuery;
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

    /**
     *
     * @param start The start date and time of the range for filtering, inclusive.
     * @param end The end date and time of the range for filtering, inclusive.
     * @param symbol The stock symbol to filter documents by (e.g. META).
     * @return object containing statistical metrics (e.g., min, max, avg, sum, count)
     * for the "volume" field based on the filtered documents.
     */
    @Override
    public StatsAggregate computeAggregation(LocalDateTime start, LocalDateTime end, String symbol) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
        String startDate = start.format(dateTimeFormatter);
        String endDate = end.format(dateTimeFormatter);
        Query queryByDate = RangeQuery.of(r -> r
                .field("date")
                .gte(JsonData.of(startDate)).lte(JsonData.of(endDate)))
                ._toQuery();
        Query queryBySymbol = TermQuery.of(t -> t
                .field("symbol")
                .value(symbol))._toQuery();
        List<Query> queries = new ArrayList<>();
        queries.add(queryByDate);
        queries.add(queryBySymbol);
        Query boolQuery = BoolQuery.of(b->b.must(queries))._toQuery();
        StatsAggregation aggregate = StatsAggregation.of(b-> b.field("volume"));
        Aggregation aggregation = aggregate._toAggregation();
        BaseQuery query = NativeQuery.builder()
                .withQuery(boolQuery)
                .withAggregation("statistics", aggregation)
                .build();
        SearchHits<TradeOrder> searchHits = elasticSearchTemplate.search(query, TradeOrder.class);
        ElasticsearchAggregations aggregations = (ElasticsearchAggregations) searchHits.getAggregations();
        Map<String, ElasticsearchAggregation> map = aggregations.aggregationsAsMap();
        StatsAggregate aggregationRes = map.get("statistics").aggregation().getAggregate().stats();
        return aggregationRes;
    }

    /**
     *
     * @param threshold The threshold value to determine if the difference between "high" and "low"
     * is significant. Trade orders with a difference greater than this value are included in the result.
     * @param symbol The stock symbol to filter documents by (e.g. META).
     * @return A list of {@link TradeOrder} objects that meet the specified conditions.
     */
    @Override
    public List<TradeOrder>  getSignificantPriceDifferences(Integer threshold, String symbol) {
        Map<String, JsonData> params = new HashMap<>();
        params.put("threshold", JsonData.of(threshold));
        String source = "doc['high'].value - doc['low'].value > params.threshold";
        Script scriptObj = Script.of(b->b
                .inline(c->c
                        .source(source)
                        .params(params)
                ));
        Query scriptQuery = ScriptQuery
                .of(b->b.script(scriptObj))
                ._toQuery();
        Query queryBySymbol = TermQuery.of(t -> t
                .field("symbol")
                .value(symbol))._toQuery();
        List<Query> queries = new ArrayList<>();
        queries.add(scriptQuery);
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



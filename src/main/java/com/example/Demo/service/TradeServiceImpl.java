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
import co.elastic.clients.elasticsearch._types.aggregations.CalendarInterval;
import co.elastic.clients.elasticsearch._types.aggregations.DateHistogramAggregate;
import co.elastic.clients.elasticsearch._types.aggregations.DateHistogramAggregation;
import co.elastic.clients.elasticsearch._types.aggregations.DateHistogramBucket;
import co.elastic.clients.elasticsearch._types.aggregations.ScriptedMetricAggregate;
import co.elastic.clients.elasticsearch._types.aggregations.ScriptedMetricAggregation;
import co.elastic.clients.elasticsearch._types.aggregations.StatsAggregate;
import co.elastic.clients.elasticsearch._types.aggregations.StatsAggregation;
import co.elastic.clients.elasticsearch._types.aggregations.SumAggregation;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
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


    /**
     *
     * @param symbol The stock symbol to filter documents by (e.g. META).
     * @return A map where the keys are dates (as strings) and the values are the data
     *         for the day with the most significant price difference. The data includes:
     *         - high: The highest price at that timeslot.
     *         - low: The lowest price at that timeslot.
     *         - date: The datetime for the given bucket.
     *         - volume: The trade volume on that timeslot.
     *         If no data is available for a specific date, the value will be null.
     */
    @Override
    public  Map<String, Object>  getMostSignificantPriceDifferencesPerDay(String symbol) {
        Query queryBySymbol = TermQuery.of(t -> t
                .field("symbol")
                .value(symbol))._toQuery();

        DateHistogramAggregation dateHistogramAggregate = DateHistogramAggregation.of(b-> b
                .field("date")
                .calendarInterval(CalendarInterval.Day)
                );

        String initString = "state.maxDiff = null; state.maxDoc = null";
        String mapString = "def diff = doc['high'].value - doc['low'].value; if (state.maxDiff == null || diff > state.maxDiff) { state.maxDiff = diff; state.maxDoc = new HashMap(); state.maxDoc.high = doc['high'].value; state.maxDoc.low = doc['low'].value; state.maxDoc.date = doc['date'].value; state.maxDoc.volume = doc['volume'].value; }";
        String combineString = "return state";
        String reduceString = "def maxState = null; for (s in states) { if (maxState == null || s.maxDiff > maxState.maxDiff) { maxState = s; } } return maxState";

        Aggregation maxDiffAggregation = generateScriptedMetricAggregation(initString, mapString, combineString, reduceString);
        Aggregation finalAggregation = Aggregation.of(b-> b
                .dateHistogram(dateHistogramAggregate)
                .aggregations("max_diff_aggregation", maxDiffAggregation));
        BaseQuery query = NativeQuery.builder()
                .withQuery(queryBySymbol)
                .withAggregation("high_low_aggregation", finalAggregation)
                .build();

        SearchHits<TradeOrder> searchHits = elasticSearchTemplate.search(query, TradeOrder.class);
        ElasticsearchAggregations aggregations = (ElasticsearchAggregations) searchHits.getAggregations();
        Map<String, ElasticsearchAggregation> map = aggregations.aggregationsAsMap();
        DateHistogramAggregate aggregationRes =  map.get("high_low_aggregation").aggregation().getAggregate().dateHistogram();
        List<DateHistogramBucket> lst = aggregationRes.buckets().array();
        Map<String, Object> res = new HashMap<>();
        for(DateHistogramBucket bucket: lst) {
            String key = bucket.keyAsString();
            ScriptedMetricAggregate scriptedMetric = bucket.aggregations().get("max_diff_aggregation").scriptedMetric();
            if(scriptedMetric != null && scriptedMetric.value() != null) {
                Map<String, Object> data = scriptedMetric.value().to(Map.class);
                res.put(key, data);
            } else {
                res.put(key, null);
            }
        }
        return res;
    }

    /**
     * Retrieves the total volume traded per day for a given trade symbol.
     * @param symbol The trade symbol to filter (e.g., "META", "AAPL").
     * @return A map where the key is the date (as a String in ISO 8601 format),
     *         and the value is the total traded volume (as a Double) for that date.
     */
    @Override
    public Map<String, Double> getVolumeTradedPerDay(String symbol) {
        DateHistogramAggregation dateHistogramAggregate = DateHistogramAggregation.of(b-> b
                .field("date")
                .calendarInterval(CalendarInterval.Day)
        );
        SumAggregation sumAggregation = SumAggregation.of(b->b.field("volume"));
        Aggregation sumAggregate = Aggregation.of(b->b.sum(sumAggregation));
        Aggregation finalAggregation = Aggregation.of(b-> b
                .dateHistogram(dateHistogramAggregate)
                .aggregations("sum_aggregation", sumAggregate));
        Query queryBySymbol = TermQuery.of(t -> t
                .field("symbol")
                .value(symbol))._toQuery();
        BaseQuery query = NativeQuery.builder()
                .withQuery(queryBySymbol)
                .withAggregation("total_volume_aggregation", finalAggregation)
                .build();
        SearchHits<TradeOrder> searchHits = elasticSearchTemplate.search(query, TradeOrder.class);
        ElasticsearchAggregations aggregations = (ElasticsearchAggregations) searchHits.getAggregations();
        Map<String, ElasticsearchAggregation> map = aggregations.aggregationsAsMap();
        DateHistogramAggregate aggregationRes =  map.get("total_volume_aggregation").aggregation().getAggregate().dateHistogram();
        List<DateHistogramBucket> lst = aggregationRes.buckets().array();
        Map<String, Double> res = new HashMap<>();
        for(DateHistogramBucket bucket: lst) {
            String key = bucket.keyAsString();
            Double sum = bucket.aggregations().get("sum_aggregation").sum().value();
            res.put(key, sum == null ? 0.0 : sum);
        }
        return res;
    }

    public Aggregation generateScriptedMetricAggregation(String initString, String mapString, String combineString, String reduceString) {
        Script initScript = Script.of(b-> b
                .inline(c-> c.source(initString)));
        Script mapScript = Script.of(b-> b.inline(c->c.source(mapString)));
        Script combineScript = Script.of(b-> b.inline(c->c.source(combineString)));
        Script reduceScript = Script.of(b->b.inline(c->c.source(reduceString)));

        ScriptedMetricAggregation scriptedMetricAggregation = ScriptedMetricAggregation.of(b-> b
                .initScript(initScript)
                .mapScript(mapScript)
                .combineScript(combineScript)
                .reduceScript(reduceScript));
        Aggregation maxDiffAggregation = scriptedMetricAggregation._toAggregation();
        return maxDiffAggregation;
    }

}



/*
* {
  "size": 0,
  "aggs": {
    "trades_by_date": {
      "date_histogram": {
        "field": "date",
        "calendar_interval": "day"
      },
      "aggs": {
        "high_low_diff": {
          "scripted_metric": {
            "init_script": "state.maxDiff = null; state.maxDoc = null;",
            "map_script": "def diff = doc['high'].value - doc['low'].value; if (state.maxDiff == null || diff > state.maxDiff) { state.maxDiff = diff; state.maxDoc = new HashMap(); state.maxDoc.high = doc['high'].value; state.maxDoc.low = doc['low'].value; state.maxDoc.date = doc['date'].value; state.maxDoc.volume = doc['volume'].value; }",
            "combine_script": "return state;",
            "reduce_script": "def maxState = null; for (s in states) { if (maxState == null || s.maxDiff > maxState.maxDiff) { maxState = s; } } return maxState;"
          }
        }
      }
    }
  }
}
 */

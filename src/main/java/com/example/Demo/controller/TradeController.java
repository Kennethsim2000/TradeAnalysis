package com.example.Demo.controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.Demo.config.CommonResult;
import com.example.Demo.model.TradeOrder;
import com.example.Demo.service.TradeService;
import com.example.Demo.vo.StatsVo;

import co.elastic.clients.elasticsearch._types.aggregations.StatsAggregate;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/trade")
@Slf4j
public class TradeController {
    @Autowired
    private TradeService tradeService;

    @GetMapping
    public CommonResult<List<TradeOrder>> getTradeOrderBySymbol(@RequestParam String symbol) {
        List<TradeOrder> orders = tradeService.findBySymbol(symbol);
        return CommonResult.success(orders, "Trade orders successfully retrieved");
    }

    @GetMapping
    @RequestMapping("/range")
    public CommonResult<List<TradeOrder>> getTradeOrderByRange(@RequestParam String start, @RequestParam String end, @RequestParam String symbol) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        LocalDateTime startDateTime = LocalDateTime.parse(start, formatter);
        LocalDateTime endDateTime = LocalDateTime.parse(end, formatter);
        List<TradeOrder> orders = tradeService.findByDateRangeAndSymbol(startDateTime, endDateTime, symbol);
        return CommonResult.success(orders, "Trade orders successfully retrieved");
    }

    @GetMapping
    @RequestMapping("/volume/less")
    public CommonResult<List<TradeOrder>> getTradeOrderWithVolumeLessThan(@RequestParam String start, @RequestParam String end, @RequestParam String symbol, @RequestParam Integer volume) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        LocalDateTime startDateTime = LocalDateTime.parse(start, formatter);
        LocalDateTime endDateTime = LocalDateTime.parse(end, formatter);
        List<TradeOrder> orders = tradeService.findBySymbolAndVolumeLessThan(volume, symbol);
        return CommonResult.success(orders, "Trade orders successfully retrieved");
    }

    //TODO Retrieve the trade order with the highest volume for a given symbol within a date range
    @GetMapping
    @RequestMapping("/volume/highest")
    public CommonResult<TradeOrder> getHighestVolumeTradeOrder(@RequestParam String start, @RequestParam String end, @RequestParam String symbol) {
        TradeOrder order = new TradeOrder();
        return CommonResult.success(order, "Trade orders successfully retrieved");
    }

    //TODO Find the time period with the largest difference between high and low prices for a given symbol within a date range
    @GetMapping
    @RequestMapping("/volatile")
    public CommonResult<TradeOrder> getMostVolatilePeriod(@RequestParam String start, @RequestParam String end, @RequestParam String symbol) {
        TradeOrder order = new TradeOrder();
        return CommonResult.success(order, "Trade orders successfully retrieved");
    }

    //TODO Find trades with significant price movements
    @GetMapping
    @RequestMapping("/significant")
    public CommonResult<List<TradeOrder>> getTradesWithSignificantPriceMovements(@RequestParam Integer threshold, @RequestParam String symbol) {
        List<TradeOrder> orders = tradeService.getSignificantPriceDifferences(threshold, symbol);
        return CommonResult.success(orders, "Trade orders with significant difference between high and low successfully retrieved");
    }

    @GetMapping
    @RequestMapping("/aggregate")
    public CommonResult<StatsVo> aggregation(@RequestParam String start, @RequestParam String end, @RequestParam String symbol) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        LocalDateTime startDateTime = LocalDateTime.parse(start, formatter);
        LocalDateTime endDateTime = LocalDateTime.parse(end, formatter);
        StatsAggregate aggregate = tradeService.computeAggregation(startDateTime, endDateTime, symbol);
        StatsVo vo = new StatsVo();
        vo.setCount(aggregate.count());
        vo.setAvg(aggregate.avg());
        vo.setMax(aggregate.max());
        vo.setMin(aggregate.min());
        vo.setSum(aggregate.sum());
        return CommonResult.success(vo, "Aggregation retrieved");
    }
}

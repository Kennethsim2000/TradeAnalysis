package com.example.Demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.Demo.config.CommonResult;
import com.example.Demo.model.TradeOrder;
import com.example.Demo.service.TradeService;

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
}

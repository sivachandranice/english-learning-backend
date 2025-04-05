package com.personal.stockanalysis.controller;

import com.personal.stockanalysis.service.StockAnalysisService;
import com.personal.stockanalysis.service.StockService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stocks")
public class StockController {

    private final StockService stockService;
    private final StockAnalysisService stockAnalysisService;

    public StockController(StockService stockService, StockAnalysisService stockAnalysisService) {
        this.stockService = stockService;
        this.stockAnalysisService = stockAnalysisService;
    }

    @GetMapping("/{symbol}")
    public ResponseEntity<Object> getStockAnalysis(@PathVariable String symbol) {
        return ResponseEntity.ok(stockService.analyzeStock(symbol));
    }

    @GetMapping("/analysis/{symbol}")
    public ResponseEntity<Object> getStockAnalysisDetail(@PathVariable String symbol) {
        return ResponseEntity.ok(stockAnalysisService.analyzeStock(symbol));
    }
}


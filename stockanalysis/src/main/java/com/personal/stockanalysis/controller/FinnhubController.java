package com.personal.stockanalysis.controller;

import com.personal.stockanalysis.service.FinnhubService;
import org.json.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/finnhub")
public class FinnhubController {

    private final FinnhubService finnhubService;

    public FinnhubController(FinnhubService finnhubService) {
        this.finnhubService = finnhubService;
    }

    @GetMapping("/{symbol}")
    public ResponseEntity<Object> analyzeStock(@PathVariable String symbol) {
        try {
            JSONObject stockData = finnhubService.fetchStockData(symbol);
            // Perform analysis (call analysis methods similar to IEXCloudService)
            return ResponseEntity.ok(stockData); // Placeholder
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "An error occurred: " + e.getMessage()));
        }
    }
}


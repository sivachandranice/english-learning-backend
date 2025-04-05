package com.personal.stockanalysis.controller;

import com.personal.stockanalysis.service.IEXCloudService;
import org.json.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/iex-cloud")
public class IEXCloudController {

    private final IEXCloudService iexCloudService;

    public IEXCloudController(IEXCloudService iexCloudService) {
        this.iexCloudService = iexCloudService;
    }

    @GetMapping("/{symbol}")
    public ResponseEntity<Object> analyzeStock(@PathVariable String symbol) {
        try {
            JSONObject stockData = iexCloudService.fetchStockData(symbol);
            Map<String, Object> analysisResults = iexCloudService.performAnalysis(stockData);

            return ResponseEntity.ok(analysisResults);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "An error occurred: " + e.getMessage()));
        }
    }
}


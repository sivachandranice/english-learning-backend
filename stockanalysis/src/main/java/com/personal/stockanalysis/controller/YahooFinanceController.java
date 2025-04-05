package com.personal.stockanalysis.controller;

import com.personal.stockanalysis.service.YahooFinanceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/yahoo-finance")
public class YahooFinanceController {

    private final YahooFinanceService yahooFinanceService;

    public YahooFinanceController(YahooFinanceService yahooFinanceService) {
        this.yahooFinanceService = yahooFinanceService;
    }

    /**
     * Endpoint to analyze stock data using Yahoo Finance service.
     * @param symbol The stock symbol (e.g., AAPL for Apple Inc.)
     * @return Analysis results as a JSON response
     */
    @GetMapping("/{symbol}")
    public ResponseEntity<Object> analyzeStock(@PathVariable String symbol) {
        try {
            // Call YahooFinanceService to perform the analysis
            Map<String, Object> analysisResults = yahooFinanceService.analyzeStock(symbol);

            // Return the analysis results
            return ResponseEntity.ok(analysisResults);
        } catch (Exception e) {
            // Handle any errors during the process
            return ResponseEntity.status(500).body(Map.of("error", "An error occurred: " + e.getMessage()));
        }
    }
}

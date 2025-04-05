package com.personal.stockanalysis.controller;

import com.personal.stockanalysis.service.StockAnalysisService;
import com.personal.stockanalysis.service.StockService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

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

    /**
     * API Endpoint to analyze stock data based on a configurable time series type
     *
     * @param symbol         Stock symbol (e.g., RELIANCE.BSE for Reliance)
     * @param timeSeriesType Time series type (e.g., "daily", "weekly", "monthly")
     * @return Analysis results as a JSON response
     */

    @GetMapping("/analysis/{symbol}")
    public ResponseEntity<Object> getStockAnalysisDetail(@PathVariable String symbol,
                                                         @RequestParam(defaultValue = "daily") String timeSeriesType) {
        try {
            // Validate the time series type
            if (!timeSeriesType.equalsIgnoreCase("daily") &&
                    !timeSeriesType.equalsIgnoreCase("weekly") &&
                    !timeSeriesType.equalsIgnoreCase("monthly")) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Invalid time series type. Allowed values are 'daily', 'weekly', or 'monthly'."));
            }

            // Invoke the service layer for analysis
            Map<String, Object> analysisResults = stockAnalysisService.analyzeStock(symbol, timeSeriesType);

            return ResponseEntity.ok(analysisResults);
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("error", "An error occurred while processing the request: " + e.getMessage()));
        }
    }
}


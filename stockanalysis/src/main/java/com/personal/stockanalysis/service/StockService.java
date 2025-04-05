package com.personal.stockanalysis.service;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

@Service
public class StockService {

    @Value("${stock.api.key}")
    private String apiKey;

    @Value("${stock.api.base.url}")
    private String baseUrl;

    public Map<String, Object> analyzeStock(String symbol) {
        try {
            // Fetch stock data
            String jsonResponse = fetchStockData(symbol);

            // Parse and analyze data
            JSONObject jsonObject = new JSONObject(jsonResponse);
            ArrayList<Double> prices = extractClosingPrices(jsonObject);

            // Perform Fibonacci analysis
            return performFibonacciAnalysis(prices);
        } catch (Exception e) {
            return Map.of("error", "Failed to fetch or analyze data: " + e.getMessage());
        }
    }

    private String fetchStockData(String symbol) throws Exception {
        String apiUrl = baseUrl + symbol + "&apikey=" + apiKey;
        URL url = new URL(apiUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            return reader.lines().reduce("", (acc, line) -> acc + line);
        }
    }

    private ArrayList<Double> extractClosingPrices(JSONObject jsonObject) {
        ArrayList<Double> closingPrices = new ArrayList<>();
        JSONObject timeSeries = jsonObject.getJSONObject("Time Series (Daily)");
        for (String date : timeSeries.keySet()) {
            double closingPrice = timeSeries.getJSONObject(date).getDouble("4. close");
            closingPrices.add(closingPrice);
        }
        return closingPrices;
    }

    private Map<String, Object> performFibonacciAnalysis(ArrayList<Double> prices) {
        double maxPrice = Collections.max(prices);
        double minPrice = Collections.min(prices);
        double diff = maxPrice - minPrice;

        // Calculate Fibonacci levels
        Map<String, Double> levels = Map.of(
                "23.6%", maxPrice - (diff * 0.236),
                "38.2%", maxPrice - (diff * 0.382),
                "50.0%", maxPrice - (diff * 0.500),
                "61.8%", maxPrice - (diff * 0.618)
        );

        // Return results
        return Map.of(
                "maxPrice", maxPrice,
                "minPrice", minPrice,
                "fibonacciLevels", levels
        );
    }
}

package com.personal.stockanalysis.service;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

@Service
public class IEXCloudService {

    @Value("${iexcloud.api.base.url}")
    private String iexCloudBaseUrl;

    @Value("${iexcloud.api.key}")
    private String apiKey;

    public JSONObject fetchStockData(String symbol) {
        try {
            // Construct the IEX Cloud API URL
            String apiUrl = iexCloudBaseUrl + "/stock/" + symbol + "/chart/1d?token=" + apiKey;
            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            return new JSONObject(response.toString());
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch stock data: " + e.getMessage());
        }
    }

    public Map<String, Object> performAnalysis(JSONObject stockData) {
        try {
            // Extract prices and volume from IEX Cloud response
            JSONArray dataPoints = stockData.getJSONArray("data");
            List<Double> closingPrices = new ArrayList<>();
            List<Long> volumes = new ArrayList<>();

            for (int i = 0; i < dataPoints.length(); i++) {
                JSONObject point = dataPoints.getJSONObject(i);
                closingPrices.add(point.getDouble("close"));
                volumes.add(point.getLong("volume"));
            }

            // Perform analysis
            return analyzeTrends(closingPrices, volumes);
        } catch (Exception e) {
            throw new RuntimeException("Failed to perform analysis: " + e.getMessage());
        }
    }

    private Map<String, Object> analyzeTrends(List<Double> prices, List<Long> volumes) {
        Map<String, Object> analysisResults = new HashMap<>();

        double shortTermSMA = calculateSMA(prices, 5);
        double longTermSMA = calculateSMA(prices, 10);
        analysisResults.put("Short-Term SMA", shortTermSMA);
        analysisResults.put("Long-Term SMA", longTermSMA);
        analysisResults.put("SMA Signal", shortTermSMA > longTermSMA ? "Buy Signal: Short-Term SMA is above Long-Term SMA."
                : "Sell Signal: Short-Term SMA is below Long-Term SMA.");

        double rsi = calculateRSI(prices);
        analysisResults.put("RSI", rsi);
        analysisResults.put("RSI Signal", rsi < 30 ? "Buy Signal: RSI indicates oversold conditions."
                : rsi > 70 ? "Sell Signal: RSI indicates overbought conditions." : "Neutral: RSI is balanced.");

        double maxPrice = Collections.max(prices);
        double minPrice = Collections.min(prices);
        double diff = maxPrice - minPrice;
        analysisResults.put("Fibonacci Levels", Map.of(
                "23.6%", maxPrice - (diff * 0.236),
                "38.2%", maxPrice - (diff * 0.382),
                "50.0%", maxPrice - (diff * 0.500),
                "61.8%", maxPrice - (diff * 0.618),
                "78.6%", maxPrice - (diff * 0.786)
        ));
        analysisResults.put("Max Price", maxPrice);
        analysisResults.put("Min Price", minPrice);
        analysisResults.put("Volume Analysis", calculateVolumeAnalysis(volumes));

        return analysisResults;
    }

    private double calculateSMA(List<Double> prices, int period) {
        if (prices.size() < period) return 0;
        return prices.subList(prices.size() - period, prices.size()).stream().mapToDouble(Double::doubleValue).average().orElse(0);
    }

    private double calculateRSI(List<Double> prices) {
        double gain = 0, loss = 0;
        for (int i = 1; i < prices.size(); i++) {
            double change = prices.get(i) - prices.get(i - 1);
            if (change > 0) gain += change;
            else loss -= change;
        }
        double avgGain = gain / prices.size();
        double avgLoss = loss / prices.size();
        double rs = avgGain / avgLoss;
        return 100 - (100 / (1 + rs));
    }

    private String calculateVolumeAnalysis(List<Long> volumes) {
        long averageVolume = volumes.stream().mapToLong(Long::longValue).sum() / volumes.size();
        long latestVolume = volumes.get(volumes.size() - 1);
        return latestVolume > averageVolume ? "High volume strengthens the buy signal."
                : "Low volume weakens the sell signal.";
    }
}


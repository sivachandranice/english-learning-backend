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
public class YahooFinanceService {

    @Value("${yahoofinance.base.url}")
    private String yahooFinanceBaseUrl;

    public Map<String, Object> analyzeStock(String symbol) {
        try {
            // Fetch data from Yahoo Finance API
            JSONObject stockData = fetchStockData(symbol);
            List<Double> closingPrices = extractClosingPrices(stockData);
            List<Long> volumes = extractVolumes(stockData);
            String latestDate = fetchLatestDate(stockData);
            double latestPrice = fetchLatestPrice(stockData, closingPrices.size() - 1);
            long latestVolume = volumes.get(volumes.size() - 1);

            // Perform analysis
            return performAnalysis(closingPrices, volumes, latestPrice, latestVolume, latestDate);

        } catch (Exception e) {
            return Map.of("error", "Failed to fetch or analyze data: " + e.getMessage());
        }
    }

    private JSONObject fetchStockData(String symbol) {
        try {
            String apiUrl = yahooFinanceBaseUrl + "/" + symbol + "?interval=1d&range=100d";
            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");
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

    private List<Double> extractClosingPrices(JSONObject jsonResponse) {
        try {
            List<Double> closingPrices = new ArrayList<>();
            JSONArray closes = jsonResponse
                    .getJSONObject("chart")
                    .getJSONArray("result")
                    .getJSONObject(0)
                    .getJSONObject("indicators")
                    .getJSONArray("quote")
                    .getJSONObject(0)
                    .getJSONArray("close");

            for (int i = 0; i < closes.length(); i++) {
                closingPrices.add(closes.getDouble(i));
            }

            return closingPrices;
        } catch (Exception e) {
            throw new RuntimeException("Failed to extract closing prices: " + e.getMessage());
        }
    }

    private List<Long> extractVolumes(JSONObject jsonResponse) {
        try {
            List<Long> volumes = new ArrayList<>();
            JSONArray volume = jsonResponse
                    .getJSONObject("chart")
                    .getJSONArray("result")
                    .getJSONObject(0)
                    .getJSONObject("indicators")
                    .getJSONArray("quote")
                    .getJSONObject(0)
                    .getJSONArray("volume");

            for (int i = 0; i < volume.length(); i++) {
                volumes.add(volume.getLong(i));
            }

            return volumes;
        } catch (Exception e) {
            throw new RuntimeException("Failed to extract volumes: " + e.getMessage());
        }
    }

    private String fetchLatestDate(JSONObject jsonResponse) {
        try {
            JSONArray timestamps = jsonResponse
                    .getJSONObject("chart")
                    .getJSONArray("result")
                    .getJSONObject(0)
                    .getJSONArray("timestamp");

            long latestTimestamp = timestamps.getLong(timestamps.length() - 1);
            return new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date(latestTimestamp * 1000));
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch the latest date: " + e.getMessage());
        }
    }

    private double fetchLatestPrice(JSONObject jsonResponse, int index) {
        try {
            JSONArray closes = jsonResponse
                    .getJSONObject("chart")
                    .getJSONArray("result")
                    .getJSONObject(0)
                    .getJSONObject("indicators")
                    .getJSONArray("quote")
                    .getJSONObject(0)
                    .getJSONArray("close");

            return closes.getDouble(index);
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch the latest price: " + e.getMessage());
        }
    }

    private Map<String, Object> performAnalysis(List<Double> prices, List<Long> volumes, double latestPrice, long latestVolume, String latestDate) {
        Map<String, Object> analysisResults = new HashMap<>();

        // Calculate Moving Averages
        double shortTermSMA = calculateSMA(prices, 5);
        double longTermSMA = calculateSMA(prices, 10);
        analysisResults.put("Short-Term SMA", shortTermSMA);
        analysisResults.put("Long-Term SMA", longTermSMA);
        analysisResults.put("SMA Signal", shortTermSMA > longTermSMA ? "Buy Signal: Short-Term SMA is above Long-Term SMA."
                : "Sell Signal: Short-Term SMA is below Long-Term SMA.");

        // Calculate RSI
        double rsi = calculateRSI(prices);
        analysisResults.put("RSI", rsi);
        analysisResults.put("RSI Signal", rsi < 30 ? "Buy Signal: RSI indicates oversold conditions."
                : rsi > 70 ? "Sell Signal: RSI indicates overbought conditions." : "Neutral: RSI is balanced.");

        // Calculate Fibonacci Levels
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

        // Candlestick Pattern
        double previousPrice = prices.get(prices.size() - 2);
        analysisResults.put("Candlestick Pattern", latestPrice > previousPrice * 1.02 ? "Hammer: Indicates potential reversal upward."
                : latestPrice < previousPrice * 0.98 ? "Shooting Star: Indicates potential reversal downward."
                : "Doji: Indicates market indecision.");

        // Optimal Buy/Sell Prices
        analysisResults.put("Optimal Buy Price", minPrice * 1.05);
        analysisResults.put("Optimal Sell Price", maxPrice * 0.95);

        // Current Price
        analysisResults.put("Current Price", latestPrice + " (" + latestDate + ")");

        // Volume Analysis
        long averageVolume = calculateAverageVolume(volumes);
        analysisResults.put("Volume Analysis", latestVolume > averageVolume ? "High volume near support levels strengthens the buy signal."
                : "Low volume near resistance levels weakens the sell signal.");

        // Trend Information: Uptrend or Downtrend
        String trend = identifyTrend(shortTermSMA, longTermSMA, rsi, latestPrice, previousPrice, latestVolume, averageVolume);
        analysisResults.put("Trend Information", trend);

        // Momentum Calculation
        double momentum = calculateMomentum(prices);
        analysisResults.put("Momentum", momentum > 0 ? "Positive Momentum: Price is accelerating upward."
                : momentum < 0 ? "Negative Momentum: Price is decelerating downward." : "Neutral Momentum: Price is stable.");

        // Decision Based on Current Price
        analysisResults.put("Today Decision", latestPrice < minPrice * 1.05 ? "Advisable to buy."
                : latestPrice > maxPrice * 0.95 ? "Not advisable to buy." : "Neutral: Current price is within acceptable range.");

        return analysisResults;
    }

    private double calculateMomentum(List<Double> prices) {
        // Rate of change over the last 5 days
        if (prices.size() < 5) return 0; // Handle case where there are fewer than 5 prices
        return prices.get(prices.size() - 1) - prices.get(prices.size() - 5);
    }

    private String identifyTrend(double shortTermSMA, double longTermSMA, double rsi, double latestPrice, double previousPrice, long latestVolume, long averageVolume) {
        if (shortTermSMA > longTermSMA && rsi < 50 && latestVolume > averageVolume) {
            return "Uptrend: Price is increasing steadily with strong volume.";
        } else if (shortTermSMA < longTermSMA && rsi > 50 && latestVolume < averageVolume) {
            return "Downtrend: Price is decreasing with weakening demand.";
        } else {
            return "Neutral: The trend is unclear; monitor further developments.";
        }
    }

    private double calculateSMA(List<Double> prices, int period) {
        if (prices.size() < period) return 0;
        double sum = 0;
        for (int i = prices.size() - period; i < prices.size(); i++) {
            sum += prices.get(i);
        }
        return sum / period;
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

    private long calculateAverageVolume(List<Long> volumes) {
        return volumes.stream().mapToLong(Long::longValue).sum() / volumes.size();
    }

}


package com.personal.stockanalysis.service;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

@Service
public class StockAnalysisService {

    @Value("${stock.api.key}")
    private String apiKey;

    @Value("${stock.api.base.url}")
    private String baseUrl;

    public Map<String, Object> analyzeStock(String symbol) {
        try {
            // Fetch stock data
            String jsonResponse = fetchStockData(symbol);

            // Parse and process weekly prices
            JSONObject jsonObject = new JSONObject(jsonResponse);
            List<Double> weeklyPrices = aggregateWeeklyData(jsonObject);

            // Fetch today's price dynamically
            double todaysPrice = fetchTodaysPrice(jsonObject);

            // Analyze trends and calculate decisions
            Map<String, Object> analysisResults = analyzeTrends(weeklyPrices, todaysPrice);

            return analysisResults;
        } catch (Exception e) {
            return Map.of("error", "Failed to fetch or analyze data: " + e.getMessage());
        }
    }

    private String fetchStockData(String stockSymbol) throws Exception {
        String apiUrl = baseUrl + stockSymbol + "&apikey=" + apiKey;
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
        return response.toString();
    }

    private double fetchTodaysPrice(JSONObject jsonObject) {
        // Extract the latest closing price from the JSON response
        JSONObject timeSeries = jsonObject.getJSONObject("Time Series (Daily)");
        String latestDate = timeSeries.keys().next(); // Get the most recent date
        return timeSeries.getJSONObject(latestDate).getDouble("4. close");
    }

    private List<Double> aggregateWeeklyData(JSONObject jsonObject) {
        List<Double> weeklyPrices = new ArrayList<>();
        JSONObject timeSeries = jsonObject.getJSONObject("Time Series (Daily)");
        int count = 0;
        double sum = 0;

        for (String date : timeSeries.keySet()) {
            double closingPrice = timeSeries.getJSONObject(date).getDouble("4. close");
            sum += closingPrice;
            count++;
            if (count == 5) { // Aggregate 5 days into a week
                weeklyPrices.add(sum / 5);
                sum = 0;
                count = 0;
            }
        }
        return weeklyPrices;
    }

    private Map<String, Object> analyzeTrends(List<Double> weeklyPrices, double todaysPrice) {
        Map<String, Object> analysisResults = new HashMap<>();

        // Calculate Moving Averages
        double shortTermSMA = calculateSMA(weeklyPrices, 5); // 5-week SMA
        double longTermSMA = calculateSMA(weeklyPrices, 10); // 10-week SMA
        analysisResults.put("Short-Term SMA", shortTermSMA);
        analysisResults.put("Long-Term SMA", longTermSMA);

        // Generate SMA Buy/Sell Signals
        if (shortTermSMA > longTermSMA) {
            analysisResults.put("SMA Signal", "Buy Signal: Short-Term SMA is above Long-Term SMA.");
        } else if (shortTermSMA < longTermSMA) {
            analysisResults.put("SMA Signal", "Sell Signal: Short-Term SMA is below Long-Term SMA.");
        }

        // Calculate RSI
        double rsi = calculateRSI(weeklyPrices);
        analysisResults.put("RSI", rsi);
        if (rsi < 30) {
            analysisResults.put("RSI Signal", "Buy Signal: RSI indicates oversold conditions.");
        } else if (rsi > 70) {
            analysisResults.put("RSI Signal", "Sell Signal: RSI indicates overbought conditions.");
        }

        // Calculate Fibonacci Levels
        double maxPrice = Collections.max(weeklyPrices);
        double minPrice = Collections.min(weeklyPrices);
        double diff = maxPrice - minPrice;
        double level23 = maxPrice - (diff * 0.236);
        double level38 = maxPrice - (diff * 0.382);
        double level50 = maxPrice - (diff * 0.500);
        double level61 = maxPrice - (diff * 0.618);

        Map<String, Double> fibonacciLevels = Map.of(
                "23.6%", level23,
                "38.2%", level38,
                "50.0%", level50,
                "61.8%", level61
        );
        analysisResults.put("Fibonacci Levels", fibonacciLevels);
        analysisResults.put("Max Price", maxPrice);
        analysisResults.put("Min Price", minPrice);

        // Suggest Optimal Buy and Sell Prices
        double optimalBuyPrice = level38; // A conservative level for potential support
        double optimalSellPrice = level61; // A resistance level for profit-taking
        analysisResults.put("Optimal Buy Price", optimalBuyPrice);
        analysisResults.put("Optimal Sell Price", optimalSellPrice);

        // Today's Price Analysis
        analysisResults.put("Current Price", todaysPrice);
        if (todaysPrice < level38) {
            analysisResults.put("Today Decision", "Advisable to buy: Today's price is below the 38.2% Fibonacci level.");
        } else if (todaysPrice > level61) {
            analysisResults.put("Today Decision", "Not advisable to buy: Today's price is above the 61.8% Fibonacci level.");
        } else {
            analysisResults.put("Today Decision", "Neutral: Today's price is within the Fibonacci range.");
        }

        return analysisResults;
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
}

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

    public Map<String, Object> analyzeStock(String symbol, String timeSeriesType) {
        try {
            // Fetch stock data based on the specified time series type
            String jsonResponse = fetchStockData(symbol, timeSeriesType);

            // Parse and process data based on the selected time series
            JSONObject jsonObject = new JSONObject(jsonResponse);
            List<Double> aggregatedPrices = aggregateData(jsonObject, timeSeriesType);
            List<Long> aggregatedVolumes = aggregateVolumeData(jsonObject, timeSeriesType);

            // Identify the latest price and volume dynamically
            String latestDate = fetchLatestDate(jsonObject, timeSeriesType);
            double latestPrice = fetchLatestPrice(jsonObject, latestDate, timeSeriesType);
            long latestVolume = fetchLatestVolume(jsonObject, latestDate, timeSeriesType);

            // Perform analysis and generate results
            Map<String, Object> analysisResults = performAnalysis(aggregatedPrices, aggregatedVolumes, latestPrice, latestVolume, latestDate);

            return analysisResults;
        } catch (Exception e) {
            return Map.of("error", "Failed to fetch or analyze data: " + e.getMessage());
        }
    }

    private String fetchStockData(String stockSymbol, String timeSeriesType) throws Exception {
        String function = "TIME_SERIES_" + timeSeriesType.toUpperCase(); // e.g., TIME_SERIES_DAILY
        String apiUrl = baseUrl + "function=" + function + "&symbol=" + stockSymbol + "&apikey=" + apiKey;
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

    private List<Double> aggregateData(JSONObject jsonObject, String timeSeriesType) {
        List<Double> aggregatedPrices = new ArrayList<>();
        String seriesKey = getSeriesKey(timeSeriesType);

        JSONObject timeSeries = jsonObject.getJSONObject(seriesKey);
        for (String date : timeSeries.keySet()) {
            double closingPrice = timeSeries.getJSONObject(date).getDouble("4. close");
            aggregatedPrices.add(closingPrice);
        }
        return aggregatedPrices;
    }

    private List<Long> aggregateVolumeData(JSONObject jsonObject, String timeSeriesType) {
        List<Long> aggregatedVolumes = new ArrayList<>();
        String seriesKey = getSeriesKey(timeSeriesType);

        JSONObject timeSeries = jsonObject.getJSONObject(seriesKey);
        for (String date : timeSeries.keySet()) {
            long volume = timeSeries.getJSONObject(date).getLong("5. volume");
            aggregatedVolumes.add(volume);
        }
        return aggregatedVolumes;
    }

    private String getSeriesKey(String timeSeriesType) {
        return timeSeriesType.equalsIgnoreCase("daily") ? "Time Series (Daily)" :
                timeSeriesType.equalsIgnoreCase("weekly") ? "Weekly Time Series" :
                        "Monthly Time Series";
    }

    private String fetchLatestDate(JSONObject jsonObject, String timeSeriesType) {
        String seriesKey = getSeriesKey(timeSeriesType);
        JSONObject timeSeries = jsonObject.getJSONObject(seriesKey);
        return Collections.max(timeSeries.keySet());
    }

    private double fetchLatestPrice(JSONObject jsonObject, String latestDate, String timeSeriesType) {
        String seriesKey = getSeriesKey(timeSeriesType);
        JSONObject timeSeries = jsonObject.getJSONObject(seriesKey);
        return timeSeries.getJSONObject(latestDate).getDouble("4. close");
    }

    private long fetchLatestVolume(JSONObject jsonObject, String latestDate, String timeSeriesType) {
        String seriesKey = getSeriesKey(timeSeriesType);
        JSONObject timeSeries = jsonObject.getJSONObject(seriesKey);
        return timeSeries.getJSONObject(latestDate).getLong("5. volume");
    }

    private Map<String, Object> performAnalysis(List<Double> prices, List<Long> volumes, double latestPrice, long latestVolume, String latestDate) {
        Map<String, Object> analysisResults = new HashMap<>();

        // Calculate Moving Averages
        double shortTermSMA = calculateSMA(prices, 5); // Short-term SMA (5 periods)
        double longTermSMA = calculateSMA(prices, 10); // Long-term SMA (10 periods)
        analysisResults.put("Short-Term SMA", shortTermSMA);
        analysisResults.put("Long-Term SMA", longTermSMA);

        // Generate SMA Signals
        analysisResults.put("SMA Signal", shortTermSMA > longTermSMA ? "Buy Signal: Short-Term SMA is above Long-Term SMA." :
                "Sell Signal: Short-Term SMA is below Long-Term SMA.");

        // Calculate RSI
        double rsi = calculateRSI(prices);
        analysisResults.put("RSI", rsi);
        analysisResults.put("RSI Signal", rsi < 30 ? "Buy Signal: RSI indicates oversold conditions." :
                rsi > 70 ? "Sell Signal: RSI indicates overbought conditions." : "Neutral: RSI is balanced.");

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

        // Candlestick Pattern Analysis
        double latestPriceInList = prices.get(prices.size() - 1);
        double previousPrice = prices.get(prices.size() - 2);
        analysisResults.put("Candlestick Pattern", latestPriceInList > previousPrice * 1.02 ? "Hammer: Indicates potential reversal upward." :
                latestPriceInList < previousPrice * 0.98 ? "Shooting Star: Indicates potential reversal downward." :
                        "Doji: Indicates market indecision.");

        // Suggest Optimal Buy/Sell Prices
        analysisResults.put("Optimal Buy Price", minPrice * 1.05); // Example: 5% above lowest price
        analysisResults.put("Optimal Sell Price", maxPrice * 0.95); // Example: 5% below highest price

        // Current Price and Volume
        analysisResults.put("Current Price", latestPrice + " (" + latestDate + ")");
        analysisResults.put("Volume", latestVolume);

        // Decision Based on Current Price
        analysisResults.put("Today Decision", latestPrice < minPrice * 1.05 ? "Advisable to buy." :
                latestPrice > maxPrice * 0.95 ? "Not advisable to buy." : "Neutral.");

        // Volume Analysis
        long averageVolume = calculateAverageVolume(volumes);
        analysisResults.put("Volume Analysis", latestVolume > averageVolume ?
                "High volume near support levels strengthens the buy signal." :
                "Low volume near resistance levels weakens the sell signal.");

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

    private long calculateAverageVolume(List<Long> volumes) {
        return volumes.stream().mapToLong(Long::longValue).sum() / volumes.size();
    }
}

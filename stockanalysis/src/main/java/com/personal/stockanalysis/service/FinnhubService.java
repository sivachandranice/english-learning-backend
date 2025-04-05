package com.personal.stockanalysis.service;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

@Service
public class FinnhubService {

    @Value("${finnhub.api.base.url}")
    private String finnhubBaseUrl;

    @Value("${finnhub.api.key}")
    private String apiKey;

    public JSONObject fetchStockData(String symbol) {
        try {
            String apiUrl = finnhubBaseUrl + "/stock/candle?symbol=" + symbol + "&resolution=D&from=START_TIMESTAMP&to=END_TIMESTAMP&token=" + apiKey;
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

    // Add analysis methods similar to IEXCloudService for Finnhub response
}


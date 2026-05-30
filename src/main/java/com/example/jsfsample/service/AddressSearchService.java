package com.example.jsfsample.service;

import com.example.jsfsample.model.AddressCandidate;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Named
@RequestScoped
public class AddressSearchService {

    private static final Logger LOG = Logger.getLogger(AddressSearchService.class.getName());
    private static final String ZIPCLOUD_URL = "https://zipcloud.ibsnet.co.jp/api/search?zipcode=";

    public List<AddressCandidate> search(String postalCode) {
        if (postalCode == null || postalCode.isBlank()) {
            return Collections.emptyList();
        }
        String normalized = postalCode.replace("-", "").trim();
        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(5))
                    .build();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(ZIPCLOUD_URL + normalized))
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                LOG.warning("zipcloud API status: " + response.statusCode());
                return Collections.emptyList();
            }
            return parse(response.body());

        } catch (Exception e) {
            LOG.log(Level.WARNING, "住所検索に失敗しました", e);
            return Collections.emptyList();
        }
    }

    private List<AddressCandidate> parse(String json) {
        try (JsonReader reader = Json.createReader(new StringReader(json))) {
            JsonObject root = reader.readObject();
            if (root.isNull("results")) {
                return Collections.emptyList();
            }
            JsonArray results = root.getJsonArray("results");
            List<AddressCandidate> list = new ArrayList<>(results.size());
            for (JsonObject item : results.getValuesAs(JsonObject.class)) {
                list.add(new AddressCandidate(
                        item.getString("address1", ""),
                        item.getString("address2", ""),
                        item.getString("address3", "")
                ));
            }
            return list;
        } catch (Exception e) {
            LOG.log(Level.WARNING, "住所JSONのパースに失敗しました", e);
            return Collections.emptyList();
        }
    }
}

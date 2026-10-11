package com.example.AITestMoodul.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Otsib GIPHY-st reaktsiooni-GIF-i. GIF on boonus: kui midagi läheb valesti, tagastatakse
 * Optional.empty() ja nali salvestatakse ilma GIF-ita.
 * API võti on ainult backendis, et see brauserisse ei jõuaks.
 */
@Slf4j
@Service
public class GiphyService {

    private static final int SEARCH_LIMIT = 10;
    private static final int MAX_QUERY_LENGTH = 50;

    private final RestClient restClient;
    private final String apiKey;
    private final String rating;

    public GiphyService(@Value("${giphy.api-key:}") String apiKey,
                        @Value("${giphy.rating:pg}") String rating,
                        @Value("${giphy.base-url:https://api.giphy.com/v1/gifs}") String baseUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
        this.apiKey = apiKey;
        this.rating = rating;
    }

    public Optional<String> findGifUrl(String query) {
        if (apiKey.isBlank() || query == null || query.isBlank()) {
            return Optional.empty();
        }
        String q = query.strip();
        if (q.length() > MAX_QUERY_LENGTH) {
            q = q.substring(0, MAX_QUERY_LENGTH);
        }

        try {
            final String searchQuery = q;
            SearchResponse response = restClient.get()
                    .uri(uri -> uri.path("/search")
                            .queryParam("api_key", apiKey)
                            .queryParam("q", searchQuery)
                            .queryParam("limit", SEARCH_LIMIT)
                            .queryParam("rating", rating)
                            .queryParam("lang", "en")
                            .build())
                    .retrieve()
                    .body(SearchResponse.class);

            List<String> urls = response == null || response.data() == null ? List.of() : response.data().stream()
                    .map(Gif::images)
                    .filter(images -> images != null && images.fixedHeight() != null)
                    .map(images -> images.fixedHeight().url())
                    .filter(url -> url != null && url.startsWith("https://"))
                    .toList();

            if (urls.isEmpty()) {
                log.info("[Giphy] no results for query='{}'", searchQuery);
                return Optional.empty();
            }
            // Juhuslik valik, et sama päring ei annaks alati sama GIF-i
            return Optional.of(urls.get(ThreadLocalRandom.current().nextInt(urls.size())));
        } catch (Exception e) {
            log.warn("[Giphy] search failed for query='{}': {}", q, e.getMessage());
            return Optional.empty();
        }
    }

    // GIPHY vastusest loetakse ainult vajalikud väljad, ülejäänud ignoreeritakse
    record SearchResponse(List<Gif> data) {}

    record Gif(Images images) {}

    record Images(@JsonProperty("fixed_height") Rendition fixedHeight) {}

    record Rendition(String url) {}
}

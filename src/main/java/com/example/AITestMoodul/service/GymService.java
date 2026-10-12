package com.example.AITestMoodul.service;

import com.example.AITestMoodul.dto.GymResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Otsib SerpApi (Google Maps) kaudu jõusaale Eesti linnadest ja annab neist juhusliku.
 * Nimekiri laetakse ainult üks kord ja hoitakse mälus, sest SerpApi tasuta pakett lubab vähe päringuid.
 * Nagu GIF, on ka jõusaal boonus: vea korral tagastatakse Optional.empty().
 */
@Slf4j
@Service
public class GymService {

    private static final Duration RETRY_AFTER_FAILURE = Duration.ofMinutes(10);

    private final RestClient restClient;
    private final String apiKey;
    private final List<String> cities;

    private List<GymResponse> gyms = List.of();
    private Instant lastFailedLoad;

    public GymService(@Value("${serpapi.api-key:}") String apiKey,
                      @Value("${serpapi.gym-cities:Tallinn,Tartu,P\u00e4rnu}") List<String> cities,
                      @Value("${serpapi.base-url:https://serpapi.com}") String baseUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
        this.apiKey = apiKey;
        this.cities = cities;
    }

    public Optional<GymResponse> findRandomGym() {
        List<GymResponse> loaded = loadGyms();
        if (loaded.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(loaded.get(ThreadLocalRandom.current().nextInt(loaded.size())));
    }

    private synchronized List<GymResponse> loadGyms() {
        if (!gyms.isEmpty() || apiKey.isBlank()) {
            return gyms;
        }
        // Ebaõnnestunud laadimise järel ei proovita kohe uuesti, et päringulimiiti mitte raisata
        if (lastFailedLoad != null && lastFailedLoad.plus(RETRY_AFTER_FAILURE).isAfter(Instant.now())) {
            return gyms;
        }

        List<GymResponse> found = new ArrayList<>();
        for (String city : cities) {
            found.addAll(searchGyms(city.strip()));
        }

        if (found.isEmpty()) {
            lastFailedLoad = Instant.now();
        } else {
            gyms = List.copyOf(found);
            log.info("[SerpApi] cached {} gyms from {}", gyms.size(), cities);
        }
        return gyms;
    }

    private List<GymResponse> searchGyms(String city) {
        try {
            SearchResponse response = restClient.get()
                    .uri(uri -> uri.path("/search.json")
                            .queryParam("engine", "google_maps")
                            .queryParam("type", "search")
                            .queryParam("q", "gym in " + city + ", Estonia")
                            .queryParam("hl", "en")
                            .queryParam("gl", "ee")
                            .queryParam("api_key", apiKey)
                            .build())
                    .retrieve()
                    .body(SearchResponse.class);

            if (response == null || response.localResults() == null) {
                log.info("[SerpApi] no gyms found in {}", city);
                return List.of();
            }
            return response.localResults().stream()
                    .filter(place -> place.title() != null && !place.title().isBlank())
                    .map(place -> new GymResponse(place.title(), place.address(), mapsUrl(place)))
                    .toList();
        } catch (Exception e) {
            log.warn("[SerpApi] gym search failed for {}: {}", city, e.getMessage());
            return List.of();
        }
    }

    private static String mapsUrl(Place place) {
        String query = place.address() == null ? place.title() : place.title() + ", " + place.address();
        UriComponentsBuilder url = UriComponentsBuilder.fromUriString("https://www.google.com/maps/search/")
                .queryParam("api", 1)
                .queryParam("query", query);
        if (place.placeId() != null) {
            url.queryParam("query_place_id", place.placeId());
        }
        return url.encode().toUriString();
    }

    // SerpApi vastusest loetakse ainult vajalikud väljad, ülejäänud ignoreeritakse
    record SearchResponse(@JsonProperty("local_results") List<Place> localResults) {}

    record Place(String title, String address, @JsonProperty("place_id") String placeId) {}
}

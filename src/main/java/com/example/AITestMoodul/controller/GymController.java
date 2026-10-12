package com.example.AITestMoodul.controller;

import com.example.AITestMoodul.dto.GymResponse;
import com.example.AITestMoodul.service.GymService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class GymController {

    private final GymService gymService;

    // 204 No Content, kui jõusaali ei leitud (nt SerpApi võti puudub)
    @GetMapping("/gyms/random")
    public ResponseEntity<GymResponse> randomGym() {
        return gymService.findRandomGym()
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }
}

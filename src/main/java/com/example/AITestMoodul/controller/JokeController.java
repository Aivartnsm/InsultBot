package com.example.AITestMoodul.controller;

import com.example.AITestMoodul.dto.ChatRequest;
import com.example.AITestMoodul.dto.JokeResponse;
import com.example.AITestMoodul.service.JokeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class JokeController {

    private final JokeService jokeService;

    @PostMapping("/chat")
    public JokeResponse chat(@RequestBody ChatRequest request) {
        return jokeService.tellJoke(request.message());
    }

    @GetMapping("/jokes")
    public List<JokeResponse> jokes() {
        return jokeService.findNewestJokes();
    }
}

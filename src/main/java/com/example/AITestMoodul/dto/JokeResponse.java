package com.example.AITestMoodul.dto;

import com.example.AITestMoodul.persistence.Joke;
import com.example.AITestMoodul.persistence.JokeType;

import java.time.LocalDateTime;

public record JokeResponse(Integer id, String userInput, JokeType type, String joke, String gifUrl,
                           LocalDateTime createdAt) {

    public static JokeResponse from(Joke joke) {
        return new JokeResponse(joke.getId(), joke.getUserInput(), joke.getType(), joke.getJoke(),
                joke.getGifUrl(), joke.getCreatedAt());
    }
}

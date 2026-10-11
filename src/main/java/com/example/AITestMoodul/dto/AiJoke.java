package com.example.AITestMoodul.dto;

import com.example.AITestMoodul.persistence.JokeType;

/**
 * Only the fields the AI is allowed to produce. id and createdAt come from the database.
 * gifQuery is a short GIPHY search term; the backend turns it into a GIF url.
 */
public record AiJoke(JokeType type, String joke, String gifQuery) {
}

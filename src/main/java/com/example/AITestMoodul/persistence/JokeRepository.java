package com.example.AITestMoodul.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface JokeRepository extends JpaRepository<Joke, Integer> {
    @Query("select j from Joke j order by j.createdAt DESC, j.id DESC")
    List<Joke> findNewestJokesFirst();

}

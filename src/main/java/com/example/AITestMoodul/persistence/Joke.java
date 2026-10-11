package com.example.AITestMoodul.persistence;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Generated;

import java.time.LocalDateTime;

@Entity
@Table(name = "jokes", schema = "insultbot")
@Getter
@Setter
@NoArgsConstructor
public class Joke {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String userInput;

    @Enumerated(EnumType.STRING)
    private JokeType type;

    private String joke;

    // NULL, kui GIF-i pole (SUPPORT vastus või GIPHY ei leidnud midagi)
    private String gifUrl;

    // DB täidab DEFAULT now() abil; @Generated loeb väärtuse pärast INSERT-i tagasi
    @Generated
    @Column(insertable = false, updatable = false)
    private LocalDateTime createdAt;
}

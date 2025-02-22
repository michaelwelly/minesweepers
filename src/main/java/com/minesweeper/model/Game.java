package com.minesweeper.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Game {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Изменено с String на Long

    private int width;
    private int height;
    private int minesCount;

    @Enumerated(EnumType.STRING)
    private GameState state = GameState.IN_PROGRESS;

    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<Cell> cells;

    public Game(int width, int height, int minesCount) {
        this.width = width;
        this.height = height;
        this.minesCount = minesCount;
    }
}
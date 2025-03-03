package com.minesweeper.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Game {
    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private int width;
    private int height;
    private int minesCount;

    @Enumerated(EnumType.STRING)
    private GameState state = GameState.IN_PROGRESS;

    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<Cell> cells;

    public Game(User user, int width, int height, int minesCount) {
        this.user = user;
        this.width = width;
        this.height = height;
        this.minesCount = minesCount;
        this.state = GameState.IN_PROGRESS;
    }
}
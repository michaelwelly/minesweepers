package com.minesweeper.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Cell {
    @Id
    private String id = UUID.randomUUID().toString();

    @ManyToOne
    @JoinColumn(name = "game_id")
    private Game game;

    private int row;
    private int col;
    private boolean hasMine;
    private boolean revealed;
    private int surroundingMines;

    public Cell(Game game, int row, int col) {
        this.game = game;
        this.row = row;
        this.col = col;
        this.hasMine = false;
        this.revealed = false;
    }
}
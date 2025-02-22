package com.minesweeper.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TurnRequest {
    @JsonProperty("gameId")
    private String gameId;
    private int row;
    private int col;
}
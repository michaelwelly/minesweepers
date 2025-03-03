package com.minesweeper.repository;

import com.minesweeper.model.Cell;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface CellRepository extends JpaRepository<Cell, Long> {
    List<Cell> findByGameId(UUID gameId);
}
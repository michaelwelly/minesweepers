package com.minesweeper.service;

import com.minesweeper.model.Game;

public interface GameService {
    /**
     * Создает новую игру с заданными параметрами.
     * @param width ширина игрового поля
     * @param height высота игрового поля
     * @param minesCount количество мин
     * @return созданная игра
     */
    Game createNewGame(int width, int height, int minesCount);

    /**
     * Получает игру по ее идентификатору.
     * @param gameId идентификатор игры
     * @return объект Game
     */
    Game getGame(Long gameId);

    /**
     * Обрабатывает ход игрока.
     * @param gameId идентификатор игры
     * @param row строка хода
     * @param col колонка хода
     * @return обновленный объект Game
     */
    Game makeMove(Long gameId, int row, int col);

    /**
     * Ищет игру по идентификатору.
     * @param id идентификатор игры
     * @return объект Game или null, если игра не найдена
     */
    Game findGameById(Long id);
}
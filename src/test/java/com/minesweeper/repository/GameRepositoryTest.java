package com.minesweeper.repository;

import com.minesweeper.model.Game;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Testcontainers
class GameRepositoryTest {

    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @BeforeAll
    static void startContainer() {
        postgres.start();
    }

    @AfterAll
    static void stopContainer() {
        postgres.stop();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private GameRepository gameRepository;

    @Test
    void testSaveGame() {
        Game game = new Game();
        game = gameRepository.save(game);

        assertNotNull(game.getId(), "ID должен быть сгенерирован после сохранения");
    }

    @Test
    void testFindGameById() {
        Game game = new Game();
        game = gameRepository.save(game);

        Game foundGame = gameRepository.findById(game.getId()).orElse(null);

        assertNotNull(foundGame, "Игра должна быть найдена");
        assertEquals(game.getId(), foundGame.getId(), "ID игры должен совпадать");
    }
}
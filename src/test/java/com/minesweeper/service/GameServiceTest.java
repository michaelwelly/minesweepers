package com.minesweeper.service;

import com.minesweeper.model.Cell;
import com.minesweeper.model.Game;
import com.minesweeper.model.GameState;
import com.minesweeper.repository.CellRepository;
import com.minesweeper.repository.GameRepository;
import com.minesweeper.service.impl.GameServiceImpl;
import org.junit.jupiter.api.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GameServiceTest {

    @Mock
    private GameRepository gameRepository;

    @Mock
    private CellRepository cellRepository;

    @InjectMocks
    private GameServiceImpl gameService;

    private AutoCloseable mocks;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        mocks.close();
    }

    @Test
    void testMakeMove_RevealEmptyCell() {
        // Данные теста
        Long gameId = 1L;
        int row = 2;
        int col = 3;

        Game game = new Game();
        game.setId(gameId);
        game.setState(GameState.IN_PROGRESS);

        Cell cell = new Cell(game, row, col);
        cell.setHasMine(false);
        cell.setRevealed(false);
        cell.setSurroundingMines(0);

        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));
        when(cellRepository.findByGameId(gameId)).thenReturn(List.of(cell));
        when(cellRepository.save(any(Cell.class))).thenReturn(cell);
        when(gameRepository.save(any(Game.class))).thenReturn(game);

        // Действие
        Game updatedGame = gameService.makeMove(gameId, row, col);

        // Проверки
        assertNotNull(updatedGame, "Игра не должна быть null");
        assertTrue(cell.isRevealed(), "Ячейка должна быть открыта");
        assertEquals(GameState.IN_PROGRESS, updatedGame.getState(), "Состояние игры должно остаться IN_PROGRESS");
        verify(cellRepository, times(1)).save(cell);
        verify(gameRepository, times(1)).save(game);
    }

    @Test
    void testMakeMove_HitMine() {
        // Данные теста
        Long gameId = 1L;
        int row = 2;
        int col = 3;

        Game game = new Game();
        game.setId(gameId);
        game.setState(GameState.IN_PROGRESS);

        Cell cell = new Cell(game, row, col);
        cell.setHasMine(true); // Ячейка содержит мину
        cell.setRevealed(false);

        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));
        when(cellRepository.findByGameId(gameId)).thenReturn(List.of(cell));
        when(cellRepository.save(any(Cell.class))).thenReturn(cell);
        when(gameRepository.save(any(Game.class))).thenReturn(game);

        // Действие
        Game updatedGame = gameService.makeMove(gameId, row, col);

        // Проверки
        assertNotNull(updatedGame, "Игра не должна быть null");
        assertTrue(cell.isRevealed(), "Ячейка должна быть открыта");
        assertEquals(GameState.LOST, updatedGame.getState(), "Игра должна быть проиграна");
        verify(cellRepository, times(1)).save(cell);
        verify(gameRepository, times(1)).save(game);
    }

    @Test
    void testMakeMove_WinGame() {
        // Данные теста
        Long gameId = 1L;
        int row = 2;
        int col = 3;

        Game game = new Game();
        game.setId(gameId);
        game.setState(GameState.IN_PROGRESS);
        game.setMinesCount(1); // Одна мина в игре

        // Создаем ячейки: одна с миной, остальные безопасные
        Cell mineCell = new Cell(game, 0, 0);
        mineCell.setHasMine(true);
        mineCell.setRevealed(false);

        Cell lastSafeCell = new Cell(game, row, col);
        lastSafeCell.setHasMine(false);
        lastSafeCell.setRevealed(false);

        List<Cell> cells = List.of(mineCell, lastSafeCell);

        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));
        when(cellRepository.findByGameId(gameId)).thenReturn(cells);
        when(cellRepository.save(any(Cell.class))).thenReturn(lastSafeCell);
        when(gameRepository.save(any(Game.class))).thenReturn(game);

        // Действие
        Game updatedGame = gameService.makeMove(gameId, row, col);

        // Проверки
        assertNotNull(updatedGame, "Игра не должна быть null");
        assertTrue(lastSafeCell.isRevealed(), "Ячейка должна быть открыта");
        assertEquals(GameState.WON, updatedGame.getState(), "Игра должна быть выиграна");
        verify(cellRepository, times(1)).save(lastSafeCell);
        verify(gameRepository, times(1)).save(game);
    }

    @Test
    void testMakeMove_AlreadyRevealedCell() {
        // Данные теста
        Long gameId = 1L;
        int row = 2;
        int col = 3;

        Game game = new Game();
        game.setId(gameId);
        game.setState(GameState.IN_PROGRESS);

        // Ячейка уже открыта
        Cell cell = new Cell(game, row, col);
        cell.setHasMine(false);
        cell.setRevealed(true); // Уже открыта

        List<Cell> cells = List.of(cell);

        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));
        when(cellRepository.findByGameId(gameId)).thenReturn(cells);

        // Действие и проверка исключения
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> gameService.makeMove(gameId, row, col));

        assertEquals("Эта ячейка уже открыта.", exception.getMessage(), "Должно выбрасываться исключение");
        verify(cellRepository, never()).save(any(Cell.class)); // Данные не должны сохраняться
        verify(gameRepository, never()).save(any(Game.class));
    }

    @Test
    void testMakeMove_GameNotFound() {
        // Данные теста
        Long gameId = 99L; // Несуществующий идентификатор игры
        int row = 1;
        int col = 1;

        when(gameRepository.findById(gameId)).thenReturn(Optional.empty());

        // Действие и проверка исключения
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> gameService.makeMove(gameId, row, col));

        assertEquals("Игра не найдена или уже завершена.", exception.getMessage(), "Должно выбрасываться исключение");
        verify(cellRepository, never()).findByGameId(any()); // Репозиторий ячеек не должен вызываться
        verify(cellRepository, never()).save(any(Cell.class));
        verify(gameRepository, never()).save(any(Game.class));
    }

    @Test
    void testMakeMove_OpenSafeCell() {
        // Данные теста
        Long gameId = 1L;
        int row = 1;
        int col = 1;

        Game game = new Game();
        game.setId(gameId);
        game.setState(GameState.IN_PROGRESS);

        Cell cell = new Cell(game, row, col);
        cell.setHasMine(false); // Без мины
        cell.setRevealed(false);
        cell.setSurroundingMines(2); // Есть рядом 2 мины

        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));
        when(cellRepository.findByGameId(gameId)).thenReturn(List.of(cell));
        when(cellRepository.save(any(Cell.class))).thenReturn(cell);
        when(gameRepository.save(any(Game.class))).thenReturn(game);

        // Действие
        Game updatedGame = gameService.makeMove(gameId, row, col);

        // Проверки
        assertNotNull(updatedGame, "Игра должна существовать");
        assertTrue(cell.isRevealed(), "Ячейка должна быть открыта");
        assertEquals(GameState.IN_PROGRESS, updatedGame.getState(), "Состояние игры не должно измениться");

        verify(cellRepository, times(1)).save(cell); // Ячейка должна сохраниться
        verify(gameRepository, times(1)).save(game); // Игра должна сохраниться
    }

    @Test
    void testMakeMove_OpenEmptyCell_RevealsAdjacent() {
        // Данные теста
        Long gameId = 1L;
        int row = 2, col = 2; // Центральная пустая ячейка

        Game game = new Game();
        game.setId(gameId);
        game.setState(GameState.IN_PROGRESS);

        // Ячейки на поле (рядом нет мин)
        Cell emptyCell = new Cell(game, row, col);
        emptyCell.setHasMine(false);
        emptyCell.setRevealed(false);
        emptyCell.setSurroundingMines(0);

        Cell adjacentCell1 = new Cell(game, row - 1, col); // Сверху
        Cell adjacentCell2 = new Cell(game, row + 1, col); // Снизу
        Cell adjacentCell3 = new Cell(game, row, col - 1); // Слева
        Cell adjacentCell4 = new Cell(game, row, col + 1); // Справа

        List<Cell> allCells = List.of(emptyCell, adjacentCell1, adjacentCell2, adjacentCell3, adjacentCell4);

        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));
        when(cellRepository.findByGameId(gameId)).thenReturn(allCells);
        when(cellRepository.save(any(Cell.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(gameRepository.save(any(Game.class))).thenReturn(game);

        // Действие
        Game updatedGame = gameService.makeMove(gameId, row, col);

        // Проверки
        assertNotNull(updatedGame, "Игра должна существовать");
        assertTrue(emptyCell.isRevealed(), "Выбранная пустая ячейка должна быть открыта");
        assertTrue(adjacentCell1.isRevealed(), "Соседняя ячейка сверху должна быть открыта");
        assertTrue(adjacentCell2.isRevealed(), "Соседняя ячейка снизу должна быть открыта");
        assertTrue(adjacentCell3.isRevealed(), "Соседняя ячейка слева должна быть открыта");
        assertTrue(adjacentCell4.isRevealed(), "Соседняя ячейка справа должна быть открыта");

        verify(cellRepository, times(5)).save(any(Cell.class)); // Все 5 ячеек должны сохраниться
        verify(gameRepository, times(1)).save(game); // Игра должна сохраниться
    }

    @Test
    void testCreateNewGame() {
        Game game = new Game(5, 5, 5);
        when(gameRepository.save(any(Game.class))).thenReturn(game);

        Game createdGame = gameService.createNewGame(5, 5, 5);

        assertNotNull(createdGame, "Созданная игра не должна быть null");
        verify(gameRepository, times(2)).save(any(Game.class)); // 1 раз для создания, 1 раз после установки мин
    }

    @Test
    void testFindGameById_Found() {
        Game game = new Game(5, 5, 5);
        game.setId(1L);

        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));

        Game foundGame = gameService.findGameById(1L);

        assertNotNull(foundGame, "Игра должна быть найдена");
        assertEquals(1L, foundGame.getId(), "ID игры должен совпадать");
    }

    @Test
    void testFindGameById_NotFound() {
        when(gameRepository.findById(2L)).thenReturn(Optional.empty());

        Game foundGame = gameService.findGameById(2L);

        assertNull(foundGame, "Если игра не найдена, метод должен вернуть null");
    }

    @Test
    void testMakeMove_RevealsCell() {
        Game game = new Game(5, 5, 5);
        game.setId(1L);

        Cell cell = new Cell(game, 2, 2);
        cell.setHasMine(false);
        cell.setRevealed(false);

        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));
        when(cellRepository.findByGameId(1L)).thenReturn(List.of(cell));
        when(cellRepository.save(any(Cell.class))).thenReturn(cell);

        Game updatedGame = gameService.makeMove(1L, 2, 2);

        assertTrue(cell.isRevealed(), "Ячейка должна быть открыта");
        assertEquals(GameState.IN_PROGRESS, updatedGame.getState(), "Игра должна оставаться в процессе");
    }

    @Test
    void testMakeMove_HitsMine() {
        Game game = new Game(5, 5, 5);
        game.setId(1L);

        Cell cell = new Cell(game, 3, 3);
        cell.setHasMine(true);
        cell.setRevealed(false);

        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));
        when(cellRepository.findByGameId(1L)).thenReturn(List.of(cell));
        when(cellRepository.save(any(Cell.class))).thenReturn(cell);

        Game updatedGame = gameService.makeMove(1L, 3, 3);

        assertTrue(cell.isRevealed(), "Ячейка должна быть открыта");
        assertEquals(GameState.LOST, updatedGame.getState(), "Игра должна быть проиграна");
    }

    @Test
    void testMakeMove_InvalidCoordinates() {
        Game game = new Game(5, 5, 5);
        game.setId(1L);

        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));
        when(cellRepository.findByGameId(1L)).thenReturn(List.of());

        Exception exception = assertThrows(IllegalStateException.class, () ->
                gameService.makeMove(1L, 6, 6)
        );

        assertEquals("Ячейка не найдена.", exception.getMessage());
    }

    @Test
    void testCheckWin_AllSafeCellsRevealed() {
        Game game = new Game(3, 3, 1);
        game.setId(1L);

        Cell mineCell = new Cell(game, 0, 0);
        mineCell.setHasMine(true);
        mineCell.setRevealed(false);

        Cell safeCell = new Cell(game, 0, 1);
        safeCell.setHasMine(false);
        safeCell.setRevealed(true);

        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));
        when(cellRepository.findByGameId(1L)).thenReturn(List.of(mineCell, safeCell));

        Game updatedGame = gameService.makeMove(1L, 0, 1);

        assertEquals(GameState.WON, updatedGame.getState(), "Игра должна быть выиграна");
    }
}
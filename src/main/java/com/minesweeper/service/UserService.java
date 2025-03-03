package com.minesweeper.service;

import com.minesweeper.model.User;

import java.util.Optional;
import java.util.UUID;

public interface UserService {
    /**
     * Получает пользователя по ID
     * @param userId идентификатор пользователя
     * @return Optional с пользователем, если найден
     */
    Optional<User> getUserById(UUID userId);

    /**
     * Создает нового пользователя
     * @param username имя пользователя
     * @return созданный пользователь
     */
    User createUser(String username);

    /**
     * Обновляет данные пользователя
     * @param user пользователь с обновленными данными
     * @return обновленный пользователь
     */
    User updateUser(User user);
}
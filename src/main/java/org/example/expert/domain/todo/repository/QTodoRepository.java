package org.example.expert.domain.todo.repository;

import org.example.expert.domain.todo.dto.response.TodoQueryDslResponse;
import org.example.expert.domain.todo.entity.Todo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Optional;



public interface QTodoRepository {
    Optional<Todo> findByIdWithUserQueryDsl(Long todoId);

    Page<TodoQueryDslResponse> searchTodos(
            String title, String nickname,
            LocalDateTime start, LocalDateTime end, Pageable pageable);
}

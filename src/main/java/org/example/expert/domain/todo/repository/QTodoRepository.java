package org.example.expert.domain.todo.repository;

import org.example.expert.domain.todo.entity.Todo;

import java.util.Optional;



public interface QTodoRepository {
    Optional<Todo> findByIdWithUserQueryDsl(Long todoId);
}

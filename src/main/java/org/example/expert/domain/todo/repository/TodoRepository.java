package org.example.expert.domain.todo.repository;

import org.example.expert.domain.todo.entity.Todo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface TodoRepository extends JpaRepository<Todo, Long> , QTodoRepository {

    // fetch join 제거 (Pageable 문제)
    @EntityGraph(attributePaths = {"user"})
    @Query("SELECT t FROM Todo t ORDER BY t.modifiedAt DESC")
    Page<Todo> findAllByOrderByModifiedAtDesc(Pageable pageable);


    @Query("SELECT t FROM Todo t " +
            "LEFT JOIN t.user " +
            "WHERE t.id = :todoId")
    Optional<Todo> findByIdWithUser(@Param("todoId") Long todoId);




    //할 일 검색 시 weather 조건으로도 검색가능
    @EntityGraph(attributePaths = {"user"})
    @Query("SELECT t FROM Todo t WHERE "
            + "(:weather IS NULL OR t.weather = :weather) AND "
            + "(:startModifiedAt IS NULL OR t.modifiedAt >= :startModifiedAt) AND "
            + "(:endModifiedAt IS NULL OR t.modifiedAt <= :endModifiedAt)")
    Page<Todo> findTodosByWeather(
            @Param("weather") String weather,
            @Param("startModifiedAt") LocalDateTime startModifiedAt,
            @Param("endModifiedAt") LocalDateTime endModifiedAt,
            Pageable pageable
    );

}

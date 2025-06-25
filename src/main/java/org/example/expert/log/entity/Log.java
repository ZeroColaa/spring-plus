package org.example.expert.log.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.expert.domain.common.entity.Timestamped;


@Entity
@Table(name = "log")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Log extends Timestamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String message;

    private Long todoId;

    private Long userId;

    public Log(String message, Long todoId, Long userId) {
        this.message = message;
        this.todoId = todoId;
        this.userId = userId;
    }
}

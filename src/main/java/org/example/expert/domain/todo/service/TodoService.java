package org.example.expert.domain.todo.service;

import lombok.RequiredArgsConstructor;
import org.example.expert.client.WeatherClient;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.todo.dto.request.TodoSaveRequest;
import org.example.expert.domain.todo.dto.response.TodoQueryDslResponse;
import org.example.expert.domain.todo.dto.response.TodoResponse;
import org.example.expert.domain.todo.dto.response.TodoSaveResponse;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.todo.repository.QTodoRepository;
import org.example.expert.domain.todo.repository.TodoRepository;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class TodoService {

    private final TodoRepository todoRepository;
    private final QTodoRepository qtodoRepository;
    private final WeatherClient weatherClient;
    private final UserRepository userRepository;

    public TodoSaveResponse saveTodo(Long userId, TodoSaveRequest todoSaveRequest) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new InvalidRequestException("사용자를 찾을 수 없습니다."));

        String weather = weatherClient.getTodayWeather();

        Todo newTodo = new Todo(
                todoSaveRequest.getTitle(),
                todoSaveRequest.getContents(),
                weather,
                user
        );
        Todo savedTodo = todoRepository.save(newTodo);

        return new TodoSaveResponse(
                savedTodo.getId(),
                savedTodo.getTitle(),
                savedTodo.getContents(),
                weather,
                new UserResponse(user.getId(), user.getEmail())
        );
    }



    //할 일 검색 시 weather 조건으로도 검색가능
    @Transactional(readOnly = true)
    public Page<TodoResponse> getTodosByWeather(
            String weather,
            LocalDateTime startModifiedAt,
            LocalDateTime endModifiedAt,
            int page,
            int size
    ) {
        Pageable pageable = PageRequest.of(page - 1, size);

        Page<Todo> todos = todoRepository.findTodosByWeather(weather, startModifiedAt, endModifiedAt, pageable);


        return todos.map(todo -> {
            User user = todo.getUser();
            return new TodoResponse(
                    todo.getId(),
                    todo.getTitle(),
                    todo.getContents(),
                    todo.getWeather(),
                    new UserResponse(user.getId(), user.getEmail()),
                    todo.getCreatedAt(),
                    todo.getModifiedAt()
            );
        });
    }

    @Transactional(readOnly = true)
    public Page<TodoResponse> getTodos(int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);

        Page<Todo> todos = todoRepository.findAllByOrderByModifiedAtDesc(pageable);

        return todos.map(todo -> new TodoResponse(
                todo.getId(),
                todo.getTitle(),
                todo.getContents(),
                todo.getWeather(),
                new UserResponse(todo.getUser().getId(), todo.getUser().getEmail()),
                todo.getCreatedAt(),
                todo.getModifiedAt()
        ));
    }

    @Transactional(readOnly = true)
    public TodoResponse getTodo(long todoId) {

        Todo todo = qtodoRepository.findByIdWithUserQueryDsl(todoId)
                .orElseThrow(() -> new InvalidRequestException("Todo not found"));

        User user = todo.getUser();

        return new TodoResponse(
                todo.getId(),
                todo.getTitle(),
                todo.getContents(),
                todo.getWeather(),
                new UserResponse(user.getId(), user.getEmail()),
                todo.getCreatedAt(),
                todo.getModifiedAt()
        );
    }

    public Page<TodoQueryDslResponse> searchTodos(
            String title, String nickname,
            LocalDateTime start, LocalDateTime end,
            int page, int size) {

        Pageable pageable = PageRequest.of(page - 1, size);
        return  qtodoRepository.searchTodos(title, nickname, start, end, pageable);
    }

}

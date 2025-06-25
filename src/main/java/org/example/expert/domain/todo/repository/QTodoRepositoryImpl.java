package org.example.expert.domain.todo.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.RequiredArgsConstructor;
import org.example.expert.domain.comment.entity.QComment;
import org.example.expert.domain.manager.entity.QManager;
import org.example.expert.domain.todo.dto.response.QTodoQueryDslResponse;
import org.example.expert.domain.todo.dto.response.TodoQueryDslResponse;
import org.example.expert.domain.todo.entity.QTodo;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.user.entity.QUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class QTodoRepositoryImpl implements QTodoRepository{

    private final JPAQueryFactory queryFactory;


    @Override
    public Optional<Todo> findByIdWithUserQueryDsl(Long todoId) {

        Todo result = queryFactory
                .selectFrom(QTodo.todo)
                .innerJoin(QTodo.todo.user, QUser.user).fetchJoin()
                .where(QTodo.todo.id.eq(todoId))
                .fetchOne();

        return Optional.ofNullable(result);


    }

    @Override
    public Page<TodoQueryDslResponse> searchTodos(
            String title, String nickname,
            LocalDateTime start, LocalDateTime end, Pageable pageable) {

        QTodo todo = QTodo.todo;
        QManager manager = QManager.manager;
        QComment comment = QComment.comment;
        QUser user = QUser.user;


        List<TodoQueryDslResponse> content = queryFactory
                .select(new QTodoQueryDslResponse(
                        todo.title,
                        manager.id.countDistinct(),
                        comment.id.countDistinct()))
                .from(todo)
                .innerJoin(todo.managers, manager)
                .leftJoin(todo.comments, comment)
                .innerJoin(todo.user, user)
                .where(
                        titleContains(title),
                        nicknameContains(nickname),
                        createdAfter(start),
                        createdBefore(end))
                .groupBy(todo.id)
                .orderBy(todo.createdAt.desc())
                .offset(pageable.getOffset())
                .limit (pageable.getPageSize())
                .fetch();


        //countQuery
        Long total = queryFactory
                .select(todo.countDistinct())
                .from(todo)
                .innerJoin(todo.managers, manager)
                .innerJoin(todo.user, user)
                .where(
                        titleContains(title),
                        nicknameContains(nickname),
                        createdAfter(start),
                        createdBefore(end))
                        .fetchOne();


        return new PageImpl<>(content, pageable, total == null ? 0 : total);

    }


    //동적 조건 메서드
    private BooleanExpression titleContains(String t){
        return (t == null || t.isBlank()) ? null : QTodo.todo.title.containsIgnoreCase(t);
    }

    private BooleanExpression nicknameContains(String n) {
        return (n == null || n.isBlank()) ? null
                : QTodo.todo.user.nickname.containsIgnoreCase(n);
    }
    private BooleanExpression createdAfter(LocalDateTime from) {
        return from == null ? null : QTodo.todo.createdAt.goe(from);
    }
    private BooleanExpression createdBefore(LocalDateTime to) {
        return to == null ? null : QTodo.todo.createdAt.loe(to);
    }

}

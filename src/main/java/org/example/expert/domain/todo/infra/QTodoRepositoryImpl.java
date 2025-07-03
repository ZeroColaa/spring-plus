package org.example.expert.domain.todo.infra;

import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.RequiredArgsConstructor;
import org.example.expert.domain.todo.dto.response.QTodoQueryDslResponse;
import org.example.expert.domain.todo.dto.response.TodoQueryDslResponse;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.todo.repository.QTodoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.example.expert.domain.comment.entity.QComment.comment;
import static org.example.expert.domain.manager.entity.QManager.manager;
import static org.example.expert.domain.todo.entity.QTodo.todo;
import static org.example.expert.domain.user.entity.QUser.user;

import static com.querydsl.jpa.JPAExpressions.select;


@Repository
@RequiredArgsConstructor
public class QTodoRepositoryImpl implements QTodoRepository {

    private final JPAQueryFactory queryFactory;


    @Override
    public Optional<Todo> findByIdWithUserQueryDsl(Long todoId) {

        return Optional.ofNullable(
                queryFactory
                        .selectFrom(todo)
                        .innerJoin(todo.user, user).fetchJoin()
                        .where(todo.id.eq(todoId))
                        .fetchOne()
        );

    }

    @Override
    public Page<TodoQueryDslResponse> searchTodos(
            String title, String nickname,
            LocalDateTime start, LocalDateTime end, Pageable pageable) {

        List<TodoQueryDslResponse> content = queryFactory
                .select(new QTodoQueryDslResponse(
                        todo.title,
                        manager.id.countDistinct(),
                        select(comment.id.count())
                                .from(comment)
                                .where(comment.todo.eq(todo))
                ))
                .from(todo)
                .leftJoin(todo.managers, manager) //담당자
                .leftJoin(manager.user, user) //담당자의 user (닉네임)
                .where(
                        titleContains(title),
                        managerNicknameContains(nickname),
                        createdAfter(start),
                        createdBefore(end)
                )
                .groupBy(todo.id, todo.title)
                .orderBy(todo.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();


        //countQuery: 닉네임 검색이 없으면 JOIN 생략 가능
        JPAQuery<Long> countBase = queryFactory
                .select(todo.id.countDistinct())
                .from(todo);


                if (StringUtils.hasText(nickname)) {
                    countBase.leftJoin(todo.managers, manager)
                            .leftJoin(manager.user, user);
                }

        Long total = countBase
                .where(
                        titleContains(title),
                        managerNicknameContains(nickname),
                        createdAfter(start),
                        createdBefore(end)
                )
                .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0 : total);

    }


    //동적 조건 메서드
    private BooleanExpression titleContains(String t) {
        return (t == null || t.isBlank()) ? null : todo.title.containsIgnoreCase(t);
    }

    private BooleanExpression managerNicknameContains(String n) {
        return StringUtils.hasText(n) ? user.nickname.containsIgnoreCase(n) : null;
    }

    private BooleanExpression createdAfter(LocalDateTime from) {
        return from == null ? null : todo.createdAt.goe(from);
    }

    private BooleanExpression createdBefore(LocalDateTime to) {
        return to == null ? null : todo.createdAt.loe(to);
    }

}

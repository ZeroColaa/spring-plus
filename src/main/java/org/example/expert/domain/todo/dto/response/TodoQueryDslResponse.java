package org.example.expert.domain.todo.dto.response;


import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;

@Getter
public class TodoQueryDslResponse {
    private String title;
    private Long managerCount;
    private Long commentCount;


    @QueryProjection
    public TodoQueryDslResponse(String title,
                                Long managerCount,
                                Long commentCount) {
        this.title        = title;
        this.managerCount = managerCount;
        this.commentCount = commentCount;
    }
}

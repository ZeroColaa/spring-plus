package org.example.expert.config;


import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class QueryDslConfig {

    private final EntityManager entityManager;

    @Bean
    public JPAQueryFactory jpaQueryFactory() {
        return new JPAQueryFactory(entityManager);
    }

    //queryfactory를 사용할 때마다 new로 사용하며 메모리 낭비 하지 않기 위해서 bean에 등록
    //jpa를 이용하기 때문에 jpa를 쓸 때 필요한 entityManager가 필요하다
}

package org.example.expert.domain.user;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestConstructor;
import org.springframework.util.StopWatch;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@SpringBootTest
@ActiveProfiles("test")
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
public class UserSearchTest {
    private final DataSource dataSource;

    public UserSearchTest(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private void measure(String tableName, String targetNickname) throws Exception {
        String query = "SELECT SQL_NO_CACHE * FROM " + tableName + " WHERE nickname = ?";
        StopWatch sw = new StopWatch();
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {
            ps.setString(1, targetNickname);
            sw.start();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {

                    // skip 조회 결과만으로 측정
                }
            }
            sw.stop();
        }
        System.out.println(tableName + " 조회 시간: " + sw.getTotalTimeNanos() + " ns");
    }

    @Test
    void nicknameSearchPerformanceTest() throws Exception {
        String target = "nick_a303b76b14a3dad9";

        System.out.println(" users_noidx: 인덱스 없음");
        measure("users_noidx", target);

        System.out.println(" users_idx: 인덱스 있음");
        measure("users_idx", target);

        System.out.println(" users_idx (cached): 인덱스 + 캐시");
        measure("users_idx", target); // 같은 쿼리 두 번째 실행 (InnoDB 캐시 hit)
    }
}

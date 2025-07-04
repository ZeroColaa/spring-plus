package org.example.expert.domain.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestConstructor;
import org.springframework.util.StopWatch;

import javax.sql.DataSource;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.Statement;




@SpringBootTest
@ActiveProfiles("test")
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@RequiredArgsConstructor
@Slf4j
class BulkInsertTest {

    private final DataSource ds;

    @Test
    void bulkInsertWithMultiValues() throws Exception {
        final int TOTAL = 1_000_000;
        final int BATCH = 1_000;

        SecureRandom rnd = new SecureRandom();
        try (Connection con = ds.getConnection();
             Statement st = con.createStatement()) {

            con.setAutoCommit(false);                       // 트랜잭션 한 번
            StopWatch sw = new StopWatch(); sw.start();

            StringBuilder sql = new StringBuilder(
                    "INSERT INTO users (email,password,nickname,user_role,created_at,modified_at) VALUES ");

            int cnt = 0;
            for (int i = 1; i <= TOTAL; i++) {
                String nick = "nick_" + Long.toHexString(rnd.nextLong()).replace("-", "");
                sql.append("('user").append(i).append("@ex.com','{noop}p")
                        .append(i).append("','").append(nick).append("','USER',NOW(),NOW()),");
                if (++cnt == BATCH || i == TOTAL) {
                    st.execute(sql.substring(0, sql.length() - 1)); // 맨 뒤 콤마 제거
                    sql.setLength("INSERT INTO users ... VALUES ".length());
                    cnt = 0;
                }
            }
            con.commit();
            sw.stop();
            log.info("{} rows inserted via multi-VALUES in {} s", TOTAL, sw.getTotalTimeSeconds());
        }
    }

}

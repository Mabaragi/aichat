package com.example.aichat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;
import java.sql.Connection;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class AichatApplicationTests {

    @Autowired
    private Environment environment;

    @Autowired
    private DataSource dataSource;

    @Test
    void contextLoadsWithTestDatabaseProfile() throws Exception {
        assertThat(environment.getActiveProfiles()).contains("test");

        try (Connection connection = dataSource.getConnection()) {
            assertThat(connection.getMetaData().getURL())
                    .contains("target/aichat-test.db");
        }
    }

}

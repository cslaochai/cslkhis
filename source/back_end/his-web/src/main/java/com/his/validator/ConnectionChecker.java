package com.his.validator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;


/**
 * 数据库连接、redis连接验证
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ConnectionChecker implements CommandLineRunner {

    private final DataSource dataSource;
    private final RedisConnectionFactory redisConnectionFactory;

    @Override
    public void run(String... args) throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            log.info("MySQL 连接成功");
        }

        redisConnectionFactory.getConnection().ping();
        log.info("Redis 连接成功");
    }
}

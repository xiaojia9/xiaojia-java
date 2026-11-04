package com.tlias;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Tlias 智能学习辅助系统 - 启动类
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class TliasApplication {

    public static void main(String[] args) {
        SpringApplication.run(TliasApplication.class, args);
        System.out.println("""
                ====================================================
                 Tlias backend started successfully.
                 API base url : http://localhost:8080
                 ====================================================
                """);
    }
}

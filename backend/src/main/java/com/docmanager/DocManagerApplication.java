package com.docmanager;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.docmanager.mapper")
public class DocManagerApplication {

    public static void main(String[] args) {
        SpringApplication.run(DocManagerApplication.class, args);
    }
}

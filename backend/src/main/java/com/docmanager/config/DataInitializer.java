package com.docmanager.config;

import com.docmanager.entity.User;
import com.docmanager.mapper.UserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        User admin = userMapper.findByUsername("admin");
        if (admin != null && !passwordEncoder.matches("123456", admin.getPassword())) {
            admin.setPassword(passwordEncoder.encode("123456"));
            userMapper.updateById(admin);
            logger.info("管理员密码已重置");
        }

        User testUser = userMapper.findByUsername("zhangsan");
        if (testUser != null && !passwordEncoder.matches("123456", testUser.getPassword())) {
            testUser.setPassword(passwordEncoder.encode("123456"));
            userMapper.updateById(testUser);
            logger.info("测试用户密码已重置");
        }

        User testUser2 = userMapper.findByUsername("lisi");
        if (testUser2 != null && !passwordEncoder.matches("123456", testUser2.getPassword())) {
            testUser2.setPassword(passwordEncoder.encode("123456"));
            userMapper.updateById(testUser2);
            logger.info("测试用户密码已重置");
        }

        logger.info("数据初始化检查完成");
    }
}

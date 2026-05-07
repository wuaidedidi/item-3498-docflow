package com.docmanager.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.docmanager.dto.LoginRequest;
import com.docmanager.dto.PasswordUpdateRequest;
import com.docmanager.dto.RegisterRequest;
import com.docmanager.dto.UserUpdateRequest;
import com.docmanager.entity.User;
import com.docmanager.exception.BusinessException;
import com.docmanager.mapper.UserMapper;
import com.docmanager.security.JwtUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtils;

    public Map<String, Object> login(LoginRequest request) {
        User user = userMapper.findByUsername(request.getUsername());
        if (user == null) {
            throw new BusinessException("用户名或密码错误");
        }
        if (user.getStatus() != 1) {
            throw new BusinessException("该账号已被禁用，请联系管理员");
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }

        String token = jwtUtils.generateToken(user.getId(), user.getUsername(), user.getRole());
        logger.info("用户登录成功: {}", user.getUsername());

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("user", sanitizeUser(user));
        return result;
    }

    public User register(RegisterRequest request) {
        User existing = userMapper.findByUsername(request.getUsername());
        if (existing != null) {
            throw new BusinessException("用户名已存在");
        }

        if (StringUtils.hasText(request.getEmail())) {
            LambdaQueryWrapper<User> emailQuery = new LambdaQueryWrapper<>();
            emailQuery.eq(User::getEmail, request.getEmail()).eq(User::getDeleted, 0);
            if (userMapper.selectCount(emailQuery) > 0) {
                throw new BusinessException("该邮箱已被注册");
            }
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(request.getNickname());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setRole("user");
        user.setStatus(1);
        user.setDeleted(0);
        userMapper.insert(user);

        logger.info("新用户注册成功: {}", user.getUsername());
        return sanitizeUser(user);
    }

    public User getUserById(Long id) {
        User user = userMapper.selectById(id);
        if (user == null || user.getDeleted() == 1) {
            throw new BusinessException("用户不存在");
        }
        return sanitizeUser(user);
    }

    public IPage<User> getUserList(int page, int size, String keyword, String role, Integer status) {
        Page<User> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(User::getUsername, keyword).or().like(User::getNickname, keyword));
        }
        if (StringUtils.hasText(role)) {
            wrapper.eq(User::getRole, role);
        }
        if (status != null) {
            wrapper.eq(User::getStatus, status);
        }
        wrapper.orderByDesc(User::getCreateTime);
        IPage<User> result = userMapper.selectPage(pageParam, wrapper);
        result.getRecords().forEach(u -> u.setPassword(null));
        return result;
    }

    public User updateUser(Long id, UserUpdateRequest request, User currentUser) {
        User user = userMapper.selectById(id);
        if (user == null || user.getDeleted() == 1) {
            throw new BusinessException("用户不存在");
        }

        if (request.getRole() != null && !request.getRole().equals(user.getRole())) {
            if (currentUser.getId().equals(id)) {
                throw new BusinessException("不能修改自己的角色");
            }
            user.setRole(request.getRole());
        }

        if (request.getStatus() != null && !request.getStatus().equals(user.getStatus())) {
            if (currentUser.getId().equals(id)) {
                throw new BusinessException("不能修改自己的状态");
            }
            user.setStatus(request.getStatus());
        }

        if (StringUtils.hasText(request.getNickname())) {
            user.setNickname(request.getNickname());
        }

        if (request.getEmail() != null) {
            if (StringUtils.hasText(request.getEmail())) {
                LambdaQueryWrapper<User> emailQuery = new LambdaQueryWrapper<>();
                emailQuery.eq(User::getEmail, request.getEmail()).ne(User::getId, id).eq(User::getDeleted, 0);
                if (userMapper.selectCount(emailQuery) > 0) {
                    throw new BusinessException("该邮箱已被使用");
                }
            }
            user.setEmail(request.getEmail());
        }

        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }

        if (request.getAvatar() != null) {
            user.setAvatar(request.getAvatar());
        }

        userMapper.updateById(user);
        logger.info("用户信息更新成功: {}", user.getUsername());
        return sanitizeUser(user);
    }

    public void updatePassword(Long userId, PasswordUpdateRequest request) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new BusinessException("原密码错误");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userMapper.updateById(user);
        logger.info("用户密码修改成功: {}", user.getUsername());
    }

    public void deleteUser(Long id, User currentUser) {
        if (currentUser.getId().equals(id)) {
            throw new BusinessException("不能删除自己的账号");
        }
        User user = userMapper.selectById(id);
        if (user == null || user.getDeleted() == 1) {
            throw new BusinessException("用户不存在");
        }
        if ("admin".equals(user.getRole())) {
            throw new BusinessException("不能删除管理员账号");
        }
        userMapper.deleteById(id);
        logger.info("用户删除成功: {}", user.getUsername());
    }

    public java.util.List<User> getAllUsers() {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getStatus, 1);
        wrapper.orderByAsc(User::getUsername);
        java.util.List<User> users = userMapper.selectList(wrapper);
        users.forEach(u -> u.setPassword(null));
        return users;
    }

    private User sanitizeUser(User user) {
        user.setPassword(null);
        return user;
    }
}

package com.docmanager.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.docmanager.common.Result;
import com.docmanager.dto.PasswordUpdateRequest;
import com.docmanager.dto.UserUpdateRequest;
import com.docmanager.entity.User;
import com.docmanager.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/me")
    public Result<User> getCurrentUser(@AuthenticationPrincipal User user) {
        return Result.success(userService.getUserById(user.getId()));
    }

    @PutMapping("/me")
    public Result<User> updateCurrentUser(@AuthenticationPrincipal User currentUser,
                                           @RequestBody UserUpdateRequest request) {
        User updated = userService.updateUser(currentUser.getId(), request, currentUser);
        return Result.success("个人信息更新成功", updated);
    }

    @PutMapping("/me/password")
    public Result<Void> updatePassword(@AuthenticationPrincipal User currentUser,
                                        @Valid @RequestBody PasswordUpdateRequest request) {
        userService.updatePassword(currentUser.getId(), request);
        return Result.success("密码修改成功", null);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<IPage<User>> getUserList(@RequestParam(defaultValue = "1") int page,
                                            @RequestParam(defaultValue = "10") int size,
                                            @RequestParam(required = false) String keyword,
                                            @RequestParam(required = false) String role,
                                            @RequestParam(required = false) Integer status) {
        return Result.success(userService.getUserList(page, size, keyword, role, status));
    }

    @GetMapping("/all")
    public Result<List<User>> getAllUsers() {
        return Result.success(userService.getAllUsers());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<User> getUserById(@PathVariable Long id) {
        return Result.success(userService.getUserById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<User> updateUser(@PathVariable Long id,
                                    @RequestBody UserUpdateRequest request,
                                    @AuthenticationPrincipal User currentUser) {
        User updated = userService.updateUser(id, request, currentUser);
        return Result.success("用户信息更新成功", updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> deleteUser(@PathVariable Long id, @AuthenticationPrincipal User currentUser) {
        userService.deleteUser(id, currentUser);
        return Result.success("用户删除成功", null);
    }
}

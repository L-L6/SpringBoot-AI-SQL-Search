package com.example.springboot.controller;

import com.example.springboot.pojo.ResponseMessage;
import com.example.springboot.pojo.User;
import com.example.springboot.pojo.dto.UserDto;
import com.example.springboot.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/web")  // 添加这行，定义控制器的基础路径
public class Webcontroller {
    @Autowired
    IUserService userService;

    @GetMapping("/all")
    public String getAllUsers() {
        List<User> users = userService.getAllUsers();

        if (users.isEmpty()) {
            return "数据库中暂无用户数据！\n请使用 POST 方法访问 /user 添加用户。";
        }

        StringBuilder result = new StringBuilder();
        result.append("=== 用户列表 ===\n\n");
        result.append("共找到 ").append(users.size()).append(" 个用户：\n\n");

        for (User user : users) {
            result.append("用户ID：").append(user.getUserId()).append("\n");
            result.append("姓名：").append(user.getUserName()).append("\n");
            result.append("邮箱：").append(user.getEmail()).append("\n");
            result.append("密码：").append(user.getPassword()).append("\n");
            result.append("----------------------------\n");
        }

        return result.toString();
    }

    /**
     * 查询所有用户（JSON格式）
     * 访问：http://localhost:8088/user/list
     */
    @GetMapping("/list")
    public ResponseMessage<List<User>> getAllUsersJson() {
        List<User> users = userService.getAllUsers();
        return ResponseMessage.success(users);
    }


}

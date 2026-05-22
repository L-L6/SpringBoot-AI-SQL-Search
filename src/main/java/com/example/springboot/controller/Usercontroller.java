package com.example.springboot.controller;

import com.example.springboot.pojo.ResponseMessage;
import com.example.springboot.pojo.User;
import com.example.springboot.pojo.dto.UserDto;
import com.example.springboot.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController //接口方法返回对象 转换成json文本
@RequestMapping("/user")
public class Usercontroller {

    @Autowired
    IUserService userService;

    //REST
    //增加
    @PostMapping   //URL: localhost:8088/user     method:post
    public ResponseMessage<User> add(@Validated @RequestBody UserDto user){
        User userNew = userService.add(user);
        return ResponseMessage.success(userNew);
    }

    //查询
    @GetMapping("/{userId}") //URL: localhost:8088/user/1     method:get
    public ResponseMessage<User> get(@PathVariable Integer userId){
        User userNew = userService.getUser(userId);
        return ResponseMessage.success(userNew);
    }

    //修改
    @PutMapping
    public ResponseMessage<User> edit(@Validated @RequestBody UserDto user){
        User userNew = userService.edit(user);
        return ResponseMessage.success(userNew);
    }

    //删除
    @DeleteMapping("/{userId}")
    public ResponseMessage<User> delete(@PathVariable Integer userId){
        userService.delete(userId);
        return ResponseMessage.success();
    }

}

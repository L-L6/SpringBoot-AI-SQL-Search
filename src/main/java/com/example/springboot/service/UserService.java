package com.example.springboot.service;


import com.example.springboot.pojo.User;
import com.example.springboot.pojo.dto.UserDto;
import com.example.springboot.repository.UserRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service          //配置成spring的bean
public class UserService implements IUserService {

    @Autowired
    UserRepository userRepository;

    @Override
    public User add(UserDto user) {

        // 模拟处理时间
        simulateProcessTime(50);

        User userPojo = new User();

        BeanUtils.copyProperties(user,userPojo);

        return userRepository.save(userPojo);
        //调用数据访问类
    }

    @Override
    public User getUser(Integer userId) {
        // 模拟数据库查询时间
        simulateProcessTime(20);

        return userRepository.findById(userId).orElseThrow(() -> {
            throw new IllegalArgumentException("用户不存在，参数异常");
        });

    }

    @Override
    public User edit(UserDto user) {

        User userPojo = new User();

        BeanUtils.copyProperties(user,userPojo);

        return userRepository.save(userPojo);
    }

    @Override
    public void delete(Integer userId) {
        userRepository.deleteById(userId);
    }

    //
    @Override
    public List<User> getAllUsers() {
        // 模拟复杂查询时间
        simulateProcessTime(100);

        Iterable<User> usersIterable = userRepository.findAll();
        List<User> users = new ArrayList<>();
        usersIterable.forEach(users::add);
        return users;
    }

    /**
     * 模拟处理时间的方法
     */
    private void simulateProcessTime(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

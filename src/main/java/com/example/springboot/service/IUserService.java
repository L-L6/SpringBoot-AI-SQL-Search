package com.example.springboot.service;

import com.example.springboot.pojo.User;
import com.example.springboot.pojo.dto.UserDto;

import java.util.List;

public interface IUserService {
    /**
     * 插入用户
     *
     * @param user 参数
     * @return
     */
    User add(UserDto user);

    /**
     * 查询用户
     * @param userId 用户ID
     * @return
     */
    User getUser(Integer userId);

    /**
     * 修改用户
     * @param user 修改用户对象
     * @return
     */
    User edit(UserDto user);

    /**
     * 删除
     *
     * @param userId
     */
    void delete(Integer userId);

    // 在IUserService接口中添加这个方法
    List<User> getAllUsers();


}

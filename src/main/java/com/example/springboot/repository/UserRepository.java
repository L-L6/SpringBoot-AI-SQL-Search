package com.example.springboot.repository;

import com.example.springboot.pojo.User;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.QueryByExampleExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository       //把当前的类注册为spring的bean
public interface UserRepository extends CrudRepository<User, Integer> {
}


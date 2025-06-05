package com.as.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.as.entity.User;
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findByUserName(String userName);

    List<User> findAllByOrderByBestRecordDesc();
}
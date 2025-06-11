package com.as.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.as.entity.User;
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findByUserName(String userName);

    List<User> findAllByOrderByBestRecordDesc();

    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.bestRecord = :record WHERE u.id = :userId")
    void updateBestRecord(@Param("userId") int userId, @Param("record") int record);
}
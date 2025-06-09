package com.as.repository;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.as.entity.Monster;

public interface MonsterRepository extends JpaRepository<Monster, Long> {
    // findAll(), findById() などSELECT系のメソッドは自動生成される

    List<Monster> findByPhase(int phase);
}
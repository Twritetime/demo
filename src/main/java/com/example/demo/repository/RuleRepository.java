package com.example.demo.repository;

import com.example.demo.model.entity.Rule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RuleRepository extends JpaRepository<Rule, Integer> {

    //自定义查询方法，查找所有激活的规则 ---------------------
    List<Rule> findAllByIsActiveTrue();
}

package com.example.demo.controller;

import com.example.demo.service.DroolsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rules")
public class RuleController {

    @Autowired
    private DroolsService droolsService;

    /**
     * 触发 Drools 规则的动态重新加载.
     * 调用此端点将使 DroolsService 从数据库中获取最新的规则并重新编译.
     *
     * @return a ResponseEntity indicating the outcome of the operation.
     */
    @PostMapping("/reload")
    public ResponseEntity<String> reloadRules() {
        try {
            droolsService.loadRules();
            return ResponseEntity.ok("Drools rules reloaded successfully.");
        } catch (Exception e) {
            // In a real application, you'd want more sophisticated error handling
            return ResponseEntity.status(500).body("Failed to reload Drools rules: " + e.getMessage());
        }
    }
}
package com.example.demo.model.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 用于封装奖励计算请求的数据传输对象 (DTO).
 */
@Getter
@Setter
public class UserRequest {
    private Long userId;
    private double benefit;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public double getBenefit() {
        return benefit;
    }

    public void setBenefit(double benefit) {
        this.benefit = benefit;
    }
}

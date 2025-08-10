package com.example.demo.controller;

import com.example.demo.model.dto.UserRequest;
import com.example.demo.model.entity.User;
import com.example.demo.service.CoinService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/coin")
public class CoinController {

    @Autowired
    private CoinService coinService;

    /**
     *
     * @param userRequest 包含 userId 和 benefit 的请求体
     * @return 返回一个包含所有被更新过的用户列表
     */
    @PostMapping("/caculate")
    public ResponseEntity<List<User>> caculate(@RequestBody UserRequest userRequest) {
        List<User> userList = coinService.caculateCoins(
                userRequest.getUserId(),
                userRequest.getBenefit()
        );
        return ResponseEntity.ok(userList);
    }
}

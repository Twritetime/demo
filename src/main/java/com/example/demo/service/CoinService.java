package com.example.demo.service;

import com.example.demo.model.entity.User;
import com.example.demo.repository.UserRepository;
import org.kie.api.runtime.KieSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CoinService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DroolsService droolsService;
    /**
     * 计算硬币的方法
     *
     * @param originUserId 触发规则源头用户id
     * @param benefit 效益
     */
    @Transactional
    public List<User> caculateCoins(Long originUserId, double benefit) {
        // 1.加载所有参与分配的用户成为当前JPA事务管理的实体
        List<User> users = userRepository.findAll();
        // -------------------------------------------------------------------
        Map<Long, User> usersMap = users.stream().collect(Collectors.toMap(User::getId, user -> user));
        // 2.设置源头用户的效益
        User originUser = usersMap.get(originUserId);
        if (originUser == null) {
            throw new IllegalArgumentException("Origin user " + originUserId + " not found");
        }
        originUser.setBenefit(benefit);
        // 3.获取KieSession执行规则
        KieSession kieSession = droolsService.getKieSession();
        try {
            // 将所有受JPA管理的事实插入会话
            for (User user : users) {
                kieSession.insert(user);
            }
            kieSession.fireAllRules();
        } finally {
            kieSession.dispose();
        }
        // 4.将父级链上的用户返回
        // 方法是事务性的，所有被规则修改过的事实的状态的变化将在事务提交时自动同步到数据库，无需手动调用save()
        List<User> userList = new ArrayList<>();
        User currentUser = originUser;
        while (currentUser != null) {
            userList.add(currentUser);
            currentUser = (currentUser.getParentId() != null) ? usersMap.get(currentUser.getParentId()) :null;
        }
        return userList;
    }
}

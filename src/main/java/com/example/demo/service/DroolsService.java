package com.example.demo.service;

import com.example.demo.model.entity.Rule;
import com.example.demo.repository.RuleRepository;
import jakarta.annotation.PostConstruct;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.Message;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
/**
 * Drools 核心服务类.
 * 负责在应用启动时从数据库加载规则，并提供动态更新规则和获取 KieSession 的能力.
 */
@Service
public class DroolsService {

    private static final Logger logger = LoggerFactory.getLogger(DroolsService.class);

    @Autowired
    private RuleRepository ruleRepository;

    private KieContainer kieContainer;

    /**
     * Spring Bean 初始化后执行的方法.
     * 用于在应用启动时完成第一次规则加载.
     */
    @PostConstruct
    public void init() {
        loadRules();
        logger.info("--- 成功加载Drools规则 ---");
    }

    public void loadRules() {
        // 1.获取KieService，它是所有KieAPI的入口
        KieServices kieServices = KieServices.Factory.get();
        // 2.创建一个KieFileSystem，用于存放DRL规则内容
        KieFileSystem kieFileSystem = kieServices.newKieFileSystem();
        // 3.从数据库加载所有激活的规则
        List<Rule> rules = ruleRepository.findAllByIsActiveTrue();
        for (Rule rule : rules) {
            // 4.将DRL规则内容写入虚拟文件系统
            String drl = rule.getContent();
            kieFileSystem.write("src/main/resources/rules/" + rule.getRuleKey() + ".drl", drl);
            logger.info("加载规则：{} (版本：{})", rule.getRuleKey(), rule.getVersion());
        }
        // 5.使用KieBuilder 构建（编译）KieModule
        KieBuilder kieBuilder = kieServices.newKieBuilder(kieFileSystem).buildAll();
        // 6.检查编译结果，如有错误则抛出异常
        if (kieBuilder.getResults().hasMessages(Message.Level.ERROR)) {
            throw new RuntimeException("Build Error:" + kieBuilder.getResults().toString());
        }
        // 7.获取一个新的KieContainer
        this.kieContainer = kieServices.newKieContainer(kieServices.getRepository().getDefaultReleaseId());
    }

    /**
     * KieContainer 是一个包含了所有规则定义的容器.
     * 它是线程安全的，可以被认为是规则库的单例实例.
     * 应用中所有对规则的调用都应该通过这个容器来获取 KieSession.
     */
    /**
     * 从 KieContainer 中获取一个新的 KieSession.
     * KieSession 是与规则引擎交互的会话，它不是线程安全的，
     * 因此每次请求都应该创建一个新的 KieSession.
     *
     * @return KieSession
     */
    public KieSession getKieSession() {
        return this.kieContainer.newKieSession();
    }
}

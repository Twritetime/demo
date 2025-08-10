package com.example.demo.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "rules")
@Getter
@Setter
public class Rule {

    /**
     * 规则的唯一标识符，主键.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 规则的业务标识符，用于在代码或日志中引用规则，具有唯一性约束.
     * 例如 "user_discount_rule".
     */
    @Column(name = "rule_key", unique = true, nullable = false)
    private String ruleKey;

    /**
     * 规则的核心内容，通常是一段完整的 DRL (Drools Rule Language) 脚本.
     * 使用 @Lob 和 columnDefinition="LONGTEXT" 来存储较长的文本.
     */
    @Lob
    @Column(name = "content", nullable = false, columnDefinition = "LONGTEXT")
    private String content;

    /**
     * 规则的版本号，用于追踪规则的变更历史.
     */
    @Column(name = "version")
    private String version;

    /**
     * 对规则功能的简短描述.
     */
    @Column(name = "description")
    private String description;

    /**
     * 标志位，指示该规则当前是否启用。
     * 只有当此值为true时，规则才会被加载到KieContainer中。
     */
    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRuleKey() {
        return ruleKey;
    }

    public void setRuleKey(String ruleKey) {
        this.ruleKey = ruleKey;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getActive() {
        return isActive;
    }

    public void setActive(Boolean active) {
        isActive = active;
    }
}
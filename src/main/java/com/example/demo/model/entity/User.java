package com.example.demo.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
public class User {
    /**
     * 唯一标识符，主键.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 参与者的名称.
     */
    @Column(nullable = false)
    private String name;

    /**
     * 参与者的级别，用于级差计算.
     */
    @Column(nullable = false)
    private int level;

    /**
     * 直接上级的 ID，用于构建层级关系.
     * 如果为 null，表示是顶级参与者.
     */
    private Long parentId;

    /**
     * 本次业务操作产生的效益，是计算金币奖励的基础.
     * 这个字段是临时的，通常在业务计算时传入，不一定需要持久化.
     * 我们使用 @Transient 注解来标记它，使其不被 JPA 映射到数据库.
     */
    @Transient
    private double benefit;

    /**
     * 获得的金币奖励.
     */
    private double goldCoin = 0.0;

    /**
     * 获得的银币奖励.
     */
    private int silverCoin = 0;

    /**
     * 获得的铜币奖励.
     */
    private int copperCoin = 0;

    /**
     * 直接上级对象.
     * 非持久化字段，在规则计算前由业务逻辑填充.
     */
    @Transient
    private User parent;

    /**
     * 直接下级列表.
     * 非持久化字段，在规则计算前由业务逻辑填充.
     */
    @Transient
    private List<User> children;

    /**
     * 所有下级（直接和间接）中的最高级别.
     * 非持久化字段，在 Drools 规则计算过程中动态填充.
     */
    @Transient
    private int subordinatesMaxLevel = 0;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    @JsonIgnore
    public double getBenefit() {
        return benefit;
    }

    public void setBenefit(double benefit) {
        this.benefit = benefit;
    }

    public double getGoldCoin() {
        return goldCoin;
    }

    public void setGoldCoin(double goldCoin) {
        this.goldCoin = goldCoin;
    }

    public int getSilverCoin() {
        return silverCoin;
    }

    public void setSilverCoin(int silverCoin) {
        this.silverCoin = silverCoin;
    }

    public int getCopperCoin() {
        return copperCoin;
    }

    public void setCopperCoin(int copperCoin) {
        this.copperCoin = copperCoin;
    }

    @JsonIgnore
    public User getParent() {
        return parent;
    }

    public void setParent(User parent) {
        this.parent = parent;
    }

    @JsonIgnore
    public List<User> getChildren() {
        return children;
    }

    public void setChildren(List<User> children) {
        this.children = children;
    }

    @JsonIgnore
    public int getSubordinatesMaxLevel() {
        return subordinatesMaxLevel;
    }

    public void setSubordinatesMaxLevel(int subordinatesMaxLevel) {
        this.subordinatesMaxLevel = subordinatesMaxLevel;
    }
}

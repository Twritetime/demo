-- 删除旧表（如果存在）
DROP TABLE IF EXISTS rules;
DROP TABLE IF EXISTS users;

-- 创建 rules 表
CREATE TABLE rules (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    rule_key VARCHAR(255) NOT NULL UNIQUE COMMENT '规则的唯一业务标识',
    content LONGTEXT NOT NULL COMMENT 'DRL规则内容的文本',
    version VARCHAR(255) DEFAULT '1.0' COMMENT '规则版本',
    description VARCHAR(255) COMMENT '规则描述',
    is_active BOOLEAN NOT NULL DEFAULT TRUE COMMENT '规则是否启用',
    created_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 创建 users 表
CREATE TABLE users (
   id BIGINT AUTO_INCREMENT PRIMARY KEY,
   name VARCHAR(255) NOT NULL COMMENT '姓名或昵称',
   level INT NOT NULL COMMENT '级别，用于级差计算',
   parent_id BIGINT COMMENT '父级的ID',
   gold_coin DOUBLE DEFAULT 0.0,
   silver_coin INT DEFAULT 0,
   copper_coin INT DEFAULT 0
);
-- 插入参与者测试数据，构建一个层级关系
-- 顶级
INSERT INTO users (id, name, level, parent_id) VALUES (1, 'GrandParent (L4)', 4, NULL);
-- 父1
INSERT INTO users (id, name, level, parent_id) VALUES (2, 'Parent1 (L3)', 3, 1);
-- 父2
INSERT INTO users (id, name, level, parent_id) VALUES (3, 'Parent2 (L2)', 2, 1);
-- 自己
INSERT INTO users (id, name, level, parent_id) VALUES (4, 'Me (L2)', 2, 2);
-- 其他人
INSERT INTO users (id, name, level, parent_id) VALUES (5, 'Other (L1)', 1, 3);

-- 插入规则数据
INSERT INTO rules (rule_key, content, description) VALUES
('gold_coin_rule', 'package rules;
import com.example.demo.model.entity.User;

rule "Assign Gold Coins to Parent"
    dialect "mvel"
    when
        $child : User(benefit > 0, parentId != null)
        $parent : User(id == $child.parentId)
    then
        double coin = $child.getBenefit() * 0.15;
        $parent.setGoldCoin($parent.getGoldCoin() + coin);
        modify($parent){}
end', '金币规则：当下级产生业绩，上级获得其业绩的 15% 作为金币提成。');

INSERT INTO rules (rule_key, content, description) VALUES
('silver_coin_rule', 'package rules;
import com.example.demo.model.entity.User;

rule "Assign Silver Coins to Self"
    dialect "mvel"
    when
        $user : User(benefit > 0)
    then
        $user.setSilverCoin($user.getSilverCoin() + 20);
        modify($user){setBenefit(0)}
end', '银币规则：任何用户只要产生了业绩，就能立即获得 20 个银币的固定奖励。');

INSERT INTO rules (rule_key, content, description) VALUES
('copper_coin_rule', 'package rules;
import com.example.demo.model.entity.User;

rule "Initialize Subordinates Max Level"
    salience 10
    dialect "mvel"
    when
        $user : User(subordinatesMaxLevel == 0)
    then
        modify($user){setSubordinatesMaxLevel($user.getLevel())}
end

rule "Propagate Max Level to Parent"
    salience 5
    dialect "mvel"
    when
        $child : User(parentId != null)
        $parent : User(id == $child.parentId)
        eval($child.getSubordinatesMaxLevel() > $parent.getSubordinatesMaxLevel())
    then
        modify($parent) { setSubordinatesMaxLevel($child.getSubordinatesMaxLevel()) };
end

rule "Calculate and Assign Copper Coins"
    salience 1
    dialect "mvel"
    when
        exists User(benefit > 0)
        $user : User()
    then
        int maxDirectChildLevel = 0;
        for (Object obj : kcontext.getKieRuntime().getObjects()) {
            if (obj instanceof User) {
                User child = (User) obj;
                if ($user.getId().equals(child.getParentId()) && child.getLevel() > maxDirectChildLevel) {
                    maxDirectChildLevel = child.getLevel();
                }
            }
        }
        int levelGap = $user.getLevel() - maxDirectChildLevel;
        if (levelGap > 0) {
            int coin = levelGap * 10;
            $user.setCopperCoin($user.getCopperCoin() + coin);
            modify($user) {};
        }
end', '铜币规则：根据用户自身的级别与其直属下级团队的最高级别之间的差值来计算奖励。');
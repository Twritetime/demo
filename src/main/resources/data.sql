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

// 规则：给上级分配金币
rule "给上级分配金币"
    dialect "mvel"
    when
        // 当一个下级产生了收益，并且他有上级时
        $child : User(benefit > 0, parentId != null)
        // 找到这个下级的上级
        $parent : User(id == $child.parentId)
    then
        // 计算金币奖励（收益的15%）
        double coin = $child.getBenefit() * 0.15;
        // 将计算出的金币添加到上级的账户
        $parent.setGoldCoin($parent.getGoldCoin() + coin);
        // 更新上级对象的状态
        modify($parent){}
end', '金币规则：当下级产生业绩，上级获得其业绩的 15% 作为金币提成。');

INSERT INTO rules (rule_key, content, description) VALUES
('silver_coin_rule', 'package rules;
import com.example.demo.model.entity.User;

// 规则：给自己分配银币
rule "给自己分配银币"
    dialect "mvel"
    when
        // 当任何用户产生了收益时
        $user : User(benefit > 0)
    then
        // 该用户获得固定的20个银币
        $user.setSilverCoin($user.getSilverCoin() + 20);
        // 将该用户的收益清零，防止重复计算
        modify($user){setBenefit(0)}
end', '银币规则：任何用户只要产生了业绩，就能立即获得 20 个银币的固定奖励。');

INSERT INTO rules (rule_key, content, description) VALUES
('copper_coin_rule', 'package rules;
import com.example.demo.model.entity.User;

// 此规则将下级（直接和间接）的最高级别向上传播。
// 它会同时考虑子节点自身的级别及其所有下级的最高级别。
rule "向上传播下级的最高级别"
    salience 5
    dialect "mvel"
    when
        // 当存在一个有上级的子用户时
        $child : User(parentId != null)
        // 找到该子用户的上级
        $parent : User(id == $child.parentId)
        // 计算潜在的新的最高级别（取子节点自身级别和其下级最高级别中的较大值）
        $newMax : Number(intValue > $parent.getSubordinatesMaxLevel()) from
            Math.max($child.getLevel(), $child.getSubordinatesMaxLevel())
    then
        // 如果计算出的新最高级别更高，则更新父级的“下级最高级别”
        modify($parent) { setSubordinatesMaxLevel($newMax.intValue()) };
end

// 此规则为用户计算并分配铜币。
// 它在所有级别信息在层级中传播完毕后触发。
rule "计算并分配铜币"
    salience 1
    dialect "mvel"
    when
        // 用户的级别必须高于其所有下级的最高级别，才有资格获得铜币。
        // 用户必须有下级（即 subordinatesMaxLevel > 0）。
        // 同时检查 benefit > 0 作为通用防护条件。
        $user : User(benefit > 0, level > subordinatesMaxLevel, subordinatesMaxLevel > 0)
    then
        // 级别差决定了铜币的数量。
        int levelGap = $user.getLevel() - $user.getSubordinatesMaxLevel();
        int coin = levelGap * 10;
        // 更新用户的铜币数量。
        modify($user) { setCopperCoin($user.getCopperCoin() + coin) };
end', '铜币规则：根据用户自身的级别与其所有下级（直接和间接）的最高级别之间的差值来计算奖励。');
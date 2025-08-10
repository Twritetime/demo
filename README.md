# Spring Boot & Drools 动态规则引擎示例

本项目是一个基于 Java Spring Boot 和 Drools 规则引擎构建的动态佣金/积分计算系统。它演示了如何将复杂的业务规则从代码中分离，存储在数据库中，并通过 API 调用实现动态加载，从而在不重启服务的情况下实时更新业务逻辑。

## 技术栈

- **后端框架**: Spring Boot
- **规则引擎**: Drools
- **数据持久化**: Spring Data JPA / Hibernate
- **数据库**: MySQL
- **开发语言**: Java 17

## 核心功能

### 1. 动态化的业务规则

所有业务规则（例如，奖励计算的比例、条件等）都以 DRL (Drools Rule Language) 脚本的形式存储在数据库的 `rules` 表中。应用在启动时会自动加载所有激活的规则。

这种设计使得业务人员可以在不修改任何 Java 代码的情况下，通过修改数据库中的规则来调整业务逻辑。

### 2. 多维度奖励计算

系统内置了三套独立的奖励计算规则：

- **金币规则 (`gold_coin_rule.drl`)**: 当一个用户产生效益时，其**直接上级**将获得该效益的 **15%** 作为金币奖励。
- **银币规则 (`silver_coin_rule.drl`)**: 产生效益的**用户自己**将获得 **20个** 银币的固定奖励。
- **铜币规则 (`copper_coin_rule.drl`)**: 根据用户与其**直属下级团队**之间的**级别差**来计算铜币奖励，级别差越大，奖励越多。

### 3. 动态规则热重载

通过新增加的 API 端点，可以强制应用重新从数据库加载所有规则，使修改后的规则即时生效。

## API 端点

### 1. 触发奖励计算

- **URL**: `/api/coin/caculate`
- **方法**: `POST`
- **请求体**:
  ```json
  {
    "userId": 1,
    "benefit": 100.0
  }
  ```
- **功能**: 为指定 `userId` 的用户设置效益 `benefit`，并触发规则引擎计算所有相关的奖励。
- **响应**: 返回一个 JSON 数组，其中包含从触发计算的初始用户开始，沿着其 `parentId` 向上追溯的整条用户链。

  **响应示例**:
  假设用户层级为 `User1 -> User2 -> User3` (User3是User2的上级，User2是User1的上级)。当触发 `userId: 1` 的计算时，返回结果会是 `[User1, User2, User3]` 的信息列表，其中包含了他们更新后的金币、银币、铜币数量。
  ```json
  [
    {
      "id": 1,
      "name": "User1",
      "level": 1,
      "parentId": 2,
      "goldCoin": 0,
      "silverCoin": 20,
      "copperCoin": 0
    },
    {
      "id": 2,
      "name": "User2",
      "level": 2,
      "parentId": 3,
      "goldCoin": 15.0,
      "silverCoin": 0,
      "copperCoin": 10
    },
    {
      "id": 3,
      "name": "User3",
      "level": 3,
      "parentId": null,
      "goldCoin": 0,
      "silverCoin": 0,
      "copperCoin": 10
    }
  ]
  ```

### 2. 动态重载规则

- **URL**: `/api/rules/reload`
- **方法**: `POST`
- **功能**: 清空当前内存中的所有规则，并从数据库中重新加载所有被标记为 `isActive=true` 的规则。这使得对业务规则的修改可以立即生效，无需重启服务。
- **响应**:
  - **成功**: `200 OK` - "Drools rules reloaded successfully."
  - **失败**: `500 Internal Server Error` - 包含错误信息。

## 如何运行

1.  **配置数据库**:
    - 在您的 MySQL 实例中创建一个数据库。
    - 修改 `src/main/resources/application.properties` 文件，更新 `spring.datasource.url`, `spring.datasource.username`, 和 `spring.datasource.password` 以匹配您的数据库配置。

2.  **初始化数据**:
    - 系统启动时，Spring Boot 会自动执行位于 `src/main/resources/data.sql` 的脚本。
    - 该脚本会完成以下操作：
      - 创建 `users` 和 `rules` 表。
      - 插入一套包含5个用户的层级测试数据。
      - 将全部三套业务规则（金币、银币、铜币）插入 `rules` 表。
    - 这确保了项目在第一次运行时就拥有了完整的、可供测试的数据和业务逻辑。

3.  **启动应用**:
    - 在项目根目录下运行 Maven 命令来构建和启动应用：
      ```bash
      mvn spring-boot:run
      ```
    - 或者直接在您的 IDE 中运行 `DemoApplication.java` 的 `main` 方法。

应用成功启动后，即可通过上述 API 端点与系统进行交互。
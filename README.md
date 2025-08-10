# Spring Boot & Drools 动态奖励分配系统

这是一个涉及三级货币（金币、银币、铜币）和层级关系的动态奖励分配系统。该系统主要围绕“自己”、“上级”以及“上级的上级”（即父级链）进行奖励分配，形成一个多层次、可动态配置的激励机制。

## 技术栈

- **后端框架**: Spring Boot
- **规则引擎**: Drools
- **数据持久化**: Spring Data JPA / Hibernate
- **数据库**: MySQL
- **开发语言**: Java 17

## 核心分配规则

系统通过三种不同的分配方式（比例、定值、级差）和三种不同的货币（金币、银币、铜币），对个人、直接上级以及整个上级链条进行激励。

### 1. 对上级的分配（金币 - 比例分配）

- **规则**: 直接上级（父1）可以获得下级产生效益的 **5%到30%** 作为金币奖励（当前配置为15%）。
- **目的**: 鼓励用户发展直接下级。

### 2. 对自己的分配（银币 - 定值分配）

- **规则**: 产生效益的自己可以获得 **10到40个** 银币的固定奖励（当前配置为20个）。
- **目的**: 为用户的每次贡献提供即时、固定的正反馈。

### 3. 对父级链的分配（铜币 - 级差分配）

- **规则**: 系统会根据一个树状的层级关系，对自己以及自己的所有上级（父1、父2、父3等）进行铜币奖励的分配。该分配方式基于“级差”计算。
- **级差计算**: `当前层级的级别 - 其所有直属下级中的最高级别`。
- **铜币计算**: `级差 * 10`。
- **目的**: 奖励那些有效管理和提升团队整体级别的领导者。级别差越大，说明其团队发展越健康，获得的奖励也越多。

### 4. 动态规则热重载

所有上述规则（如金币的分配比例、银币的固定值、铜币的计算系数）都存储在数据库中。通过调用 `/api/rules/reload` 接口，可以实现业务规则的实时更新，无需重启服务。

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
  使用 `data.sql` 初始化数据后，用户层级关系如下：
  ```mermaid
  graph TD
      U1["User 1<br>GrandParent (L4)"]
      U2["User 2<br>Parent1 (L3)"]
      U3["User 3<br>Parent2 (L2)"]
      U4["User 4<br>Me (L2)"]
      U5["User 5<br>Other (L1)"]
  
      U1 --> U2
      U1 --> U3
      U2 --> U4
      U3 --> U5
  ```
  当您发送请求 `{"userId": 4, "benefit": 100.0}` 时，API会返回从触发者 `User 4` 到其顶级上级 `User 1` 的用户链信息，其中包含了经过所有规则计算后的奖励。
  ```json
  [
    {
      "id": 4,
      "name": "Me (L2)",
      "level": 2,
      "parentId": 2,
      "goldCoin": 0.0,
      "silverCoin": 20,
      "copperCoin": 20
    },
    {
      "id": 2,
      "name": "Parent1 (L3)",
      "level": 3,
      "parentId": 1,
      "goldCoin": 15.0,
      "silverCoin": 0,
      "copperCoin": 10
    },
    {
      "id": 1,
      "name": "GrandParent (L4)",
      "level": 4,
      "parentId": null,
      "goldCoin": 0.0,
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
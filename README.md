# Computer AI Chat

基于 Spring Boot 3、Vue 3 和 LangChain4j 的 AI 知识库对话系统，包含后台管理、知识库文档管理、RAG 问答和流式对话能力。

## 技术栈

- 后端：Java 17、Spring Boot 3.5、MyBatis、MySQL、Druid
- 前端：Vue 3、Vite、Element Plus、Pinia
- AI：LangChain4j、DashScope、通义千问
- 知识库：文档加载、切片、向量嵌入、检索增强生成

## 项目结构

```text
hua-springboot3/   Spring Boot 后端
hua-user-vue3/     Vue 3 前端
```

## 启动后端

1. 创建 MySQL 数据库，并导入你自己的兼容数据库结构。
2. 应用项目内提供的迁移脚本：

```text
hua-springboot3/sql/sys_role_kb_manager.sql
hua-springboot3/sql/sys_menu_user_role_layout.sql
hua-springboot3/sql/sys_menu_remove_role_only.sql
hua-springboot3/sql/kb_document_menu.sql
```

3. 复制配置模板：

```bash
cd hua-springboot3/src/main/resources
cp application.example.yml application.yml
cp application-druid.example.yml application-druid.yml
```

4. 设置数据库、Token 和 DashScope 环境变量：

```bash
DB_URL=jdbc:mysql://localhost:3306/computer
DB_USERNAME=root
DB_PASSWORD=your-password
TOKEN_SECRET=replace-with-a-long-random-secret
DASHSCOPE_API_KEY=your-dashscope-api-key
```

5. 启动：

```bash
cd hua-springboot3
mvn spring-boot:run
```

## 启动前端

```bash
cd hua-user-vue3
cp .env.example .env.development
npm ci
npm run dev
```

生产构建：

```bash
npm run build:prod
```

## 配置说明

真实 `application.yml`、`application-druid.yml` 和前端环境文件不会提交到 Git。
仓库只保留脱敏示例。请勿把数据库密码、JWT Secret 或模型 API Key 写入源码。

## 数据库说明

完整数据库导出可能包含用户和环境相关数据，因此未纳入公开仓库。请从自己的环境导出基础结构，并执行 `sql/` 下的增量迁移脚本。

## 参考项目

前端基础结构参考 RuoYi Vue 3。原始参考项目不包含在此仓库中。

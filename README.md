# 斗地主 Web

联网对战 + 人机对战的斗地主，AI 决策由规则引擎驱动，大模型负责决策微调与人格化表达。

## 状态

> 🚧 **开发中（Work in Progress）**——项目尚未完成，接口与数据结构仍可能变动，请勿用于生产环境。

- **M0 完成**：环境与骨架（后端可启动、库表已建、接口文档可用、Redis 容器化）。
- **M1 进行中**：规则引擎、对局流程、房间匹配、WebSocket 通信与 LLM 接入已有初版实现，仍在迭代。

规划详见 [`docs/立项书.md`](docs/立项书.md) 与 [`docs/开发路线图.md`](docs/开发路线图.md)。

## 技术栈

- **前端**：Vue 3 + TypeScript + Vite + Pinia + Tailwind CSS 4 + motion-v
- **通信**：原生 WebSocket（非 socket.io，原因见立项书 ADR-02）
- **后端**：Spring Boot 4.1 + JDK 21 + MyBatis-Plus
- **接口文档**：springdoc-openapi 3.0.3 + Swagger UI（不用 Knife4j，原因见 ADR-06）
- **存储**：MySQL 8.0（本机服务）+ Redis 7（Docker 容器，原因见 ADR-07）
- **AI**：规则引擎（主）+ OpenAI 兼容协议大模型（辅）
- **部署**：Docker Compose + Nginx

## 目录

```
docs/        立项书、规则文档、接口契约
frontend/    Vue 3 前端
backend/     Spring Boot 后端
deploy/      Docker 与 Nginx 配置
```

## 本地启动

### 0. 配置口令（首次必做）

仓库内不含任何明文密码或 API Key。二选一：

```bash
# 方式 A：复制模板并填入自己的值（推荐，此后 mvn spring-boot:run 直接生效）
cd backend/src/main/resources
cp application-local.example.yml application-local.yml   # 再编辑填入数据库密码、DeepSeek Key

# 方式 B：用环境变量注入
export DB_PASSWORD=你的MySQL密码
export LLM_API_KEY=你的DeepSeek密钥
```

> `application-local.yml` 已被 `.gitignore` 排除，不会被提交。

### 1. 启动服务

```bash
# 1. Redis（Docker 容器，常驻，只需执行一次）
cd deploy && docker compose up -d redis

# 2. 后端（JDK 21）
cd backend
JAVA_HOME=D:/JavaDk/ms-21.0.11 mvn spring-boot:run

# 3. 自检，期望 {"mysql":"up","redis":"up"}
curl http://localhost:8080/api/ping/deps
```

- 接口文档：<http://localhost:8080/swagger-ui.html>
- 前端开发：`cd frontend && npm install && npm run dev`
- 数据库：本机 MySQL 服务 `MySQL80`，库 `doudizhu`（DDL 见 `backend/src/main/resources/db/schema.sql`）
- 端口被占用时用 `--server.port=18080` 覆盖，不要杀其它项目进程

## 开发约定

1. 规则引擎只在后端实现一份（Java），前端不复刻。
2. 所有 WebSocket 下行消息必须带 `seq`；重连只拉全量快照。
3. LLM 调用必须可降级，任何异常都回落引擎结果。
4. 服务端权威，永不下发他人手牌。

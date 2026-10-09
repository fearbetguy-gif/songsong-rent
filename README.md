# 松松租房管理平台

一个面向租客、房东和管理员的前后端分离租房平台，覆盖房源浏览、地图找房、收藏、预约看房、在线咨询、租约与账单管理等业务。

## 技术栈

- 后端：Java 17、Spring Boot 3、Spring MVC、MyBatis-Plus、MySQL、Redis、JWT、BCrypt、MinIO、Knife4j
- 前端：Vue 3、Vite、Vue Router、Pinia、高德地图 JavaScript API

## 目录结构

```text
backend/     Spring Boot 后端
frontend/    Vue 3 前端
sql/         数据库初始化脚本
logo-icon/   项目图标资源
```

## 本地运行

1. 执行 `sql/init.sql` 初始化 MySQL 数据库。
2. 配置后端运行所需的环境变量。
3. 在 `backend` 目录执行 `mvn spring-boot:run`。
4. 在 `frontend` 目录执行 `npm install` 和 `npm run dev`。

后端默认端口为 `8080`，前端开发地址由 Vite 启动日志给出。

## 环境变量

```text
DB_URL
DB_USERNAME
DB_PASSWORD
REDIS_HOST
REDIS_PORT
REDIS_DATABASE
MINIO_ENDPOINT
MINIO_ACCESS_KEY
MINIO_SECRET_KEY
MINIO_BUCKET_NAME
JWT_SECRET
```

其中数据库地址、Redis 和 MinIO 地址提供了本地默认值；密码、MinIO 凭证及 JWT 密钥需要在本机环境中配置，请勿提交真实密钥。

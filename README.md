# MyBot

[Mirai Console](https://github.com/mamoe/mirai-console) 插件, 使用 Kotlin + Gradle.kts.

# 如何编译

`gradle build2Jar`

# 如何使用

- 编译
    1. 克隆项目
    2. 运行 `gradle build2Jar` (因为使用了ksp，从未编译过的情况下会有报错，直接编译即可)
    3. 成功打包后插件即自动复制到 [./plugins/KTBot-1.0.0.mirai2.jar](./plugins/KTBot-1.0.0.mirai2.jar)

- 运行
    1. 将 jar 文件放入服务器上对应文件夹下
    2. 将根目录下方的 [db.db](./db.db) 文件移动到服务器上 `./data` 目录下
    3. 启动 mirai

# 如何调试/本地启动

- 首次调试/更新版本
    1. 下载 [`mcl-installer`](https://github.com/iTXTech/mcl-installer/releases) 至项目目录下
    2. 运行 `mcl-installer`，下载 `jdk17` 或 `jre17` 并全部 `yes`

- 开始调试
    1. 运行/调试 `run mcl`

**注：** 运行 `mcl-installer` 之后需要回滚 `README.md` 和 `LICENSE` 文件

# 日志

2022/2/10 删除历史提交，防止账号密码泄露，公开仓库

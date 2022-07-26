# mirai-console-plugin-template

[Mirai Console](https://github.com/mamoe/mirai-console) 插件模板, 使用 Kotlin + Gradle.

[如何使用](https://github.com/project-mirai/how-to-use-plugin-template)

# 如何使用

- 编译
    1. 克隆项目
    2. 运行 `gradle build2Jar` (因为使用了ksp，从未编译过的情况下会有报错，正常编译即可)
    3. 成功打包后插件即自动复制到 [./plugins/KTBot-1.0.0.mirai2.jar](./plugins/KTBot-1.0.0.mirai2.jar)

- 运行
    1. 将 jar 文件放入服务器上对应文件夹下
    2. 将根目录下方的 [db.db](./db.db) 文件移动到服务器上 `./data` 目录下
    3. 启动 mirai

# 日志

2022/2/10 删除历史提交，防止账号密码泄露，公开仓库

# MyBot

[Mirai Console](https://github.com/mamoe/mirai-console) 插件, 使用 Kotlin + Gradle.kts.

# 当前功能

### 聊天相关

1. 骰子：。d
    - 主功能
    - 特殊模式
    - 常用模式简写
        - 设置/删除简写
    - 加骰
    - 全1模式
2. SBI特化骰子：。s
    - 主功能
    - 加骰
    - 结果处理
3. 消息转发（群聊）：。cstart
   - 转发消息（私聊）：。c
4. 来点色图（关闭）
5. 看看p站（关闭）
6. 管理员控制群内事件开关（群管理）
7. 开发者信息
8. ping
9. 系统信息
10. 情话
11. help
12. jeff笑话
13. 能不能好好说话
14. 夸我
15. 60秒读懂世界
16. 给开发者的话：给管理员发送
17. 历史上的今天
18. （玩梗用自动回复）
19. 谁@我
20. qq活跃（暂时无用）
21. 经验增加
22. （复读）

### 其他事件

1. Bot 被邀请加群： 自动通过（取消）
2. Bot 加入群事件： 仅日志
3. Bot 离开群事件： 仅日志
4. Bot 被禁言事件： 禁用群内功能
5. Bot 下线事件： 仅日志
6. Bot 上线事件： 仅日志
7. Bot 重新登录事件： 仅日志
8. Bot 被取消禁言事件： 启用群内功能
9. 成员加入群事件： 发送入群消息
10. 成员离开群事件： 发送离群消息
11. 添加好友事件： 给管理员发送
12. 其他客户端上线事件： 给管理员发送
13. 其他客户端下线事件： 给管理员发送

# 如何编译

`gradle build2Jar` (因为使用了ksp，从未编译过的情况下会有报错，直接编译即可)

# 如何使用

1. 编译
	1. 克隆项目
	2. 编译项目
	3. 成功打包后插件自动复制到 [./plugins/](./plugins/KTBot-1.0.0.mirai2.jar) 文件夹下
2. 运行
	1. 将 jar 文件放入服务器上对应文件夹下
	2. 将根目录下方的 [./db.db](./db.db) 文件移动到服务器上 `./data` 目录下
	3. 启动 mirai

# 如何本地启动/调试

1. 首次启动/调试/更新版本
	1. 下载 [`mcl-installer`](https://github.com/iTXTech/mcl-installer/releases) 至项目目录下
	2. 运行 `mcl-installer`，下载 `jdk17` 或 `jre17` 并全部 `yes`
	3. 运行 `./mcl --disable-module addon` `./mcl --remove-package org.itxtech:mcl-addon`
2. 开始运行/调试
	1. 运行/调试 `run mcl`

**注：** 运行 `mcl-installer` 之后会覆盖 `README.md` 和 `LICENSE` 文件

# GitHub Action

* `TagRelease.yml` 为增加 tag 时触发，tag格式为 `v*`，触发后自动发布新版本
* `build2Jar.yml` 为手动触发，触发后在事件内上传 `Artifacts`

# 其他情况

~~踩坑记录~~

* 启动时如果一直卡注不动，并且服务器在国外，那么可以在 [PluginDependencies.yml](./config/Console/PluginDependencies.yml)
  加上 `https://repo.maven.apache.org/maven2`
* kotlin 继承 java 类时有可能出现明明有对应方法但是编译时提示未找到继承的目标，大概是 kotlin 可空类型的
  bug，手动写个接口，把报错的方法再写一遍，就像 [XmlHandler.kt](./src/main/kotlin/my/ktbot/utils/xml/XmlHandler.kt) 一样

# 日志

- 2022/08/02 增加了 GitHub Action ~~，妈妈再也不用担心我不会打包了~~
- 2022/02/10 删除历史提交，防止账号密码泄露，公开仓库

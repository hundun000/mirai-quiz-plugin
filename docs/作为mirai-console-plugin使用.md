### 环境准备

- java 11
- [mirai-console](https://github.com/mamoe/mirai/blob/dev/docs/UserManual.md)
- 确保可以 [在聊天环境执行指令](https://github.com/project-mirai/chat-command)  
  
### 下载本项目制品

- hundun.quizgame-XXX.mirai.jar

插件本体，放入mirai-console的plugins。

- config.zip
- data.zip

解压后合并至mirai-console的同名文件夹。[data和config详细说明](./data和config说明.md)

### 启动和登录

启动mirai-console，在mirai-console里登录。

确保已[授予群员执行该插件指令的权限](https://github.com/mamoe/mirai-console/blob/master/docs/BuiltInCommands.md#mirai-console---builtin-commands)  

> /perm grant m* hundun.quizgame:command.quiz
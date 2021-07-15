### 环境准备

- java 11
- [mirai-console](https://github.com/mamoe/mirai/blob/dev/docs/UserManual.md)
- 确保可以 [在聊天环境执行指令](https://github.com/project-mirai/chat-command)  
  
### 下载本项目制品

解压后得到：
```
+-- hundun.quizgame-XXX.mirai.jar
+-- data
|   +-- hundun.quizgame
|   |   +-- quiz
|   |   |   +-- question_packages
|   |   |   |   +-- questions_small
|   |   |   |   |   +-- 动画
|   |   |   |   |   |   +-- 紫罗兰永恒花园.txt
|   |   |   |   |   +-- 声优偶像.txt
|   |   |   +-- pictures
|   |   |   |   +-- 000001.jpg
```

data合并至mirai-console的同名文件夹。hundun.quizgame-XXX.mirai.jar是插件本体，放入plugins。

### 配置

题目库：每一个`question_packages`的子文件夹都是一个题目库。样例目录`questions_small`是一个题目库。

题目文件：题目库文件夹，或其子文件夹，可放任意个`题目文件`，一同构成这个题目库。分多个文件是为实验性功能做准备，目前放同一个文件即可。样例目录包含两个题目文件：`紫罗兰永恒花园.txt`、`声优偶像.txt`。

题目：每个`题目文件`可包含任意个题目，按如下固定格式编写。

样例 紫罗兰永恒花园.txt：
```
2    // 本文件内的题目数
     // 固定空一行
《紫罗兰永恒花园》中薇尔莉特的名字是来自？  // 题干
紫罗兰    // A选项
勿忘我    // B选项
向日葵    // C选项
玫瑰      // D选项
A         // 正确答案
无资源    // 本题无图片，固定写“无资源”
          // 固定空一行
《紫罗兰永恒花园》中，写话剧的作家是因为谁而陷入消沉
他的女儿
薇尔莉特
霍金斯中佐
公主
A
000001.jpg  // 本题图片名。放于pictures文件夹。
```

### 启动和登录

启动mirai-console，在mirai-console里登录。

确保已[授予群员执行该插件指令的权限](https://github.com/mamoe/mirai-console/blob/master/docs/BuiltInCommands.md#mirai-console---builtin-commands)  

> /perm grant m* hundun.quizgame:command.quiz
# mirai-quiz-plugin

## 简介

答题比赛插件。

现有功能：

- 自由编写题库。题目可包括文本题干、图片。
- 多种赛制可选。
- 可分多个队伍参与同一局比赛，比拼得分。

开发中功能：

- 题目可包括音频。
- 每个题目拥有若干标签。每个队伍可以对标签进行ban、pick。
- 每个队伍选择一个英雄角色。英雄角色可以使用不同技能。
- 技能可分为：  
  瞬时技能：延长倒计时、排除错误选项、跳过本题……  
  持续增益：连续答对时获得额外得分……  


使用方式：[作为mirai-console-plugin使用](docs/作为mirai-console-plugin使用.md)


## 声明

### 一切开发旨在学习，请勿用于非法用途

- 本项目是完全免费且开放源代码的软件，仅供学习和娱乐用途使用
- 鉴于项目的特殊性，开发团队可能在任何时间**停止更新**或**删除项目**。

## 功能详细介绍

[玩法详细说明](docs/一站到底详细说明.md)

[指令](docs/指令.md)

## 关于答题核心逻辑

答题核心逻辑来自项目[quizgame](https://github.com/hundun000/quizgame)的子项目，其jar可通过maven获得。

> implementation 'com.github.hundun000.quizgame:quizgame-core:76099d5bee'

`quizgame-core`还在被另一个mirai插件项目[ZacaFleetBot](https://github.com/hundun000/ZacaFleetBot)使用。开发者在未来或许可以基于`quizgame-core`开发自己的插件（然而`quizgame-core`目前没有文档，也不稳定）。

## 开发说明

[TODO](docs/开发说明.md)


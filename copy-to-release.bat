@echo off

set fromFolder="./config/hundun.quizgame"

set toFolder="./release/config/hundun.quizgame"
if not exist %toFolder% mkdir %toFolder%
xcopy %fromFolder% %toFolder% /s /y

set fromFolder="./data/hundun.quizgame"

set toFolder="./release/data/hundun.quizgame"
if not exist %toFolder% mkdir %toFolder%
xcopy %fromFolder% %toFolder% /s /y

@echo copy done!
pause
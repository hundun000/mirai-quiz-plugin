@echo off

rem set fromFolder="./config"

rem set toFolder="/release/config"
rem if not exist %toFolder% mkdir %toFolder%
rem xcopy %fromFolder% %toFolder% /s /y

set fromFolder="./data"

set toFolder="./release/data"
if not exist %toFolder% mkdir %toFolder%
xcopy %fromFolder% %toFolder% /s /y

@echo copy done!
pause
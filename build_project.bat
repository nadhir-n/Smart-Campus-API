@echo off
set "JAVA_HOME=C:\Program Files\Java\jdk-21.0.10"
set "PATH=%JAVA_HOME%\bin;%PATH%"
cd /d "C:\BLACK_BOARD\client_server\Course_Work\smart-campus-api"
"C:\Program Files\NetBeans-24\netbeans\java\maven\bin\mvn.cmd" clean package -q
if exist "target\smart-campus-api-1.0-SNAPSHOT.jar" (
    echo Build successful!
) else (
    echo Build might have issues, checking classes...
    dir target\classes\com\smartcampus\Main.class
)
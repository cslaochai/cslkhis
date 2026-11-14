@echo off
rem ============================================================
rem 故障演练沙箱启动脚本（Windows）
rem -Xmx384m：故意给小堆，OOM 场景几秒内就能复现
rem NativeMemoryTracking：堆外泄漏场景要用 jcmd VM.native_memory 对照
rem HeapDumpOnOutOfMemoryError：真 OOME 时自动落 dump，用 MAT 打开
rem ============================================================
setlocal
cd /d "%~dp0"
set "BASE=%~dp0"
set "PORT=18090"
set "JAVA_BIN=java"
if exist "%JAVA_HOME%\bin\java.exe" set "JAVA_BIN=%JAVA_HOME%\bin\java.exe"
if not exist "%BASE%run\logs" mkdir "%BASE%run\logs"
if not exist "%BASE%run\dumps" mkdir "%BASE%run\dumps"

"%JAVA_BIN%" -Xmx384m ^
  -XX:+HeapDumpOnOutOfMemoryError ^
  -XX:HeapDumpPath=%BASE%run\dumps ^
  -XX:NativeMemoryTracking=summary ^
  -Xlog:gc*:file=%BASE%run\logs\gc.log:time,uptime,level,tags ^
  -jar "%BASE%target\his-web-fault-1.0.0.jar" --server.port=%PORT%

endlocal

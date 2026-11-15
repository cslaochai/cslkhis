# 后端镜像：只装运行时，jar 由本机 `mvn -o -DskipTests clean install` 产出后放进构建上下文
# （目标机无公网、也没有 Maven 本地仓库，容器内构建依赖拉不动）
FROM eclipse-temurin:21-jre

WORKDIR /app
ENV TZ=Asia/Shanghai
ENV JAVA_OPTS=""

COPY his-backend.jar /app/app.jar
EXPOSE 8080
# 指标端口（与业务口分开，见 application.yml 的 management.server.port）
EXPOSE 18080

# 先建目录再起 JVM：HeapDumpPath / GC 日志目录必须已存在，否则 OOM 那一刻 dump 静默失败
ENTRYPOINT ["sh", "-c", "mkdir -p /app/logs /app/cslk-data/dumps && exec java $JAVA_OPTS -jar /app/app.jar"]

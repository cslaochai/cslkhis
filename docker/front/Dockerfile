# 前端镜像：静态产物由本机 `npm run build` 产出（目标机无公网，容器内 npm install 拉不到包）
FROM nginx:1.27-alpine

ENV TZ=Asia/Shanghai
RUN rm -f /etc/nginx/conf.d/default.conf
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY dist/ /usr/share/nginx/html/

EXPOSE 80

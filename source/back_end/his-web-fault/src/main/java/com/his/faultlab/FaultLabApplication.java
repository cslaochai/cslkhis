package com.his.faultlab;

import com.his.faultlab.config.FaultLabProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * 故障演练沙箱入口。
 * <p>
 * 与 his-web 完全独立：不依赖 MySQL / Redis / RocketMQ，只监听 127.0.0.1:8090。
 * 目的是把「线上才会出现的故障」变成一条 curl 就能复现的确定性事件，供 jcmd / arthas / MAT 演练。
 */
@SpringBootApplication
@EnableConfigurationProperties(FaultLabProperties.class)
public class FaultLabApplication {

    public static void main(String[] args) {
        SpringApplication.run(FaultLabApplication.class, args);
    }
}

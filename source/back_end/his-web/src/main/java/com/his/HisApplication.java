package com.his;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 启动类。
 *
 * <p>{@code @EnableScheduling} 是为日终结转（{@code DayEndSettleTrigger}）开的 ——
 * 全工程目前只有这一个定时任务。开它的同时保留了「手工补跑 + 进页面懒触发」两条路，
 * 因为定时任务只在夜里跑，出问题时白天才暴露：光靠 @Scheduled 会让「哪天没跑」无声无息。
 */
@SpringBootApplication
@MapperScan("com.his.**.mapper")
@EnableScheduling
public class HisApplication {

    public static void main(String[] args) {
        SpringApplication.run(HisApplication.class, args);
    }
}

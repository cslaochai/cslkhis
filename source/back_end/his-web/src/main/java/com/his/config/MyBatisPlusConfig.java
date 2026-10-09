package com.his.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.his.system.entity.CurrentUser;
import com.his.system.utils.UserUtils;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus配置
 */
@Configuration
public class MyBatisPlusConfig {

    /**
     * 分页插件
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }

    /**
     * 自动填充处理器
     */
    @Bean
    public MetaObjectHandler metaObjectHandler() {
        return new MetaObjectHandler() {
            @Override
            public void insertFill(MetaObject metaObject) {
                LocalDateTime now = LocalDateTime.now();
                this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, now);
                this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, now);
                this.strictInsertFill(metaObject, "delFlag", Integer.class, 0);
                CurrentUser u = UserUtils.getCurrentUser();
                if (u != null) {
                    String name = u.getRealName();
                    Long uid = u.getEmployeeId();
                    this.strictInsertFill(metaObject, "createBy", String.class, name);
                    this.strictInsertFill(metaObject, "updateBy", String.class, name);
                    this.strictInsertFill(metaObject, "createById", Long.class, uid);
                    this.strictInsertFill(metaObject, "updateById", Long.class, uid);
                }
            }

            @Override
            public void updateFill(MetaObject metaObject) {
                this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
                CurrentUser u = UserUtils.getCurrentUser();
                if (u != null) {
                    this.strictUpdateFill(metaObject, "updateBy", String.class, u.getRealName());
                    this.strictUpdateFill(metaObject, "updateById", Long.class, u.getEmployeeId());
                }
            }
        };
    }
}

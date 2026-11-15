package com.his.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.his.common.enums.UserTypeEnum;
import com.his.common.entity.CurrentUser;
import com.his.common.utils.UserUtils;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus 配置：分页插件 + 审计字段自动填充。
 *
 * <p>这里的两项是全仓数据访问的公共底座，所有业务 Mapper 都隐含依赖：
 * <ul>
 *   <li>分页插件：全仓分页只走它（Mapper 首参 IPage、SQL 不写 LIMIT，§7）。缺了它接口会
 *       静默返回全表，前端分页条变成假的；</li>
 *   <li>自动填充：新建/修改时补 createTime/updateTime/delFlag/操作人，见 {@link #metaObjectHandler()}。</li>
 * </ul>
 */
@Configuration
public class MyBatisPlusConfig {

    /**
     * 分页插件：MyBatis-Plus 用它自动套 COUNT 与 LIMIT。
     * 方言写死 MYSQL，换库必须同步改这里，否则分页语法对不上（报错在运行时、不在启动期）。
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }

    /**
     * 审计字段自动填充：createTime/updateTime/delFlag + 操作人四列
     * （createBy/createById/updateBy/updateById）。
     *
     * <p>用 strictXxxFill 而不是 setFieldValByName：前者只填「实体有这个字段且当前为 null」，
     * 实体没声明这些列就静默跳过，不会因映射不到字段抛异常。
     *
     * <p>操作人取自 {@link UserUtils#getCurrentUser()}（由 JwtAuthenticationFilter 在线程内写入），
     * 拿不到就直接 return、什么都不填。所以定时任务 / 异步线程 / MQ 消费这些没有登录态的入口，
     * 必须由调用方显式把 operator 传进业务方法，不允许在这里塞 "system" 之类默认值（§12）。
     *
     * <p>已登录时按身份分流：内网员工（INNER）取 employeeName + employeeId，患者端（PATIENT）
     * 取 realName + userId；其他身份类型目前不填，见下方 todo。
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
                CurrentUser currentUser = UserUtils.getCurrentUser();
                if (currentUser == null) {
                    return;
                }
                if (currentUser.getUserType() == UserTypeEnum.INNER.getCode()) {
                    this.strictInsertFill(metaObject, "createBy", String.class, currentUser.getEmployeeName());
                    this.strictInsertFill(metaObject, "updateBy", String.class, currentUser.getEmployeeName());
                    this.strictInsertFill(metaObject, "createById", Long.class, currentUser.getEmployeeId());
                    this.strictInsertFill(metaObject, "updateById", Long.class, currentUser.getEmployeeId());
                } else if (currentUser.getUserType() == UserTypeEnum.PATIENT.getCode()) {
                    this.strictInsertFill(metaObject, "createBy", String.class, currentUser.getRealName());
                    this.strictInsertFill(metaObject, "updateBy", String.class, currentUser.getRealName());
                    this.strictInsertFill(metaObject, "createById", Long.class, currentUser.getUserId());
                    this.strictInsertFill(metaObject, "updateById", Long.class, currentUser.getUserId());
                } else {
                    //todo:待扩展
                }
            }

            @Override
            public void updateFill(MetaObject metaObject) {
                this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
                CurrentUser currentUser = UserUtils.getCurrentUser();
                if (currentUser == null) {
                    return;
                }
                if (currentUser.getUserType() == UserTypeEnum.INNER.getCode()) {
                    this.strictUpdateFill(metaObject, "updateBy", String.class, currentUser.getEmployeeName());
                    this.strictUpdateFill(metaObject, "updateById", Long.class, currentUser.getEmployeeId());
                } else if (currentUser.getUserType() == UserTypeEnum.PATIENT.getCode()) {
                    this.strictUpdateFill(metaObject, "updateBy", String.class, currentUser.getRealName());
                    this.strictUpdateFill(metaObject, "updateById", Long.class, currentUser.getUserId());
                } else {
                    //todo:待扩展
                }
            }
        };
    }
}

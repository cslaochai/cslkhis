package com.his.common.support;

import com.his.common.enums.UserTypeEnum;
import com.his.common.entity.CurrentUser;
import lombok.NoArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

/**
 * 无登录态入口（渠道异步回调、系统触发的写账）的操作人归属。
 *
 * <p>为什么必须有：收款流水的收银员、审计字段都取自当前登录人，而渠道回调这一侧根本没有登录态 ——
 * 直接走下去就是 NPE（表现为「渠道已经收到钱、本地入账失败」），
 * 兜成 "system"/"未知操作人" 则是把「谁收的钱」永久藏进库里。
 * 唯一可归因的事实是<b>这张支付单是谁开出来的</b>，出码人就在支付单的创建人字段上，回调把它绑回来即可。
 *
 * <p>身份必须按出码人的<b>账号类型</b>绑，不能一律当成员工：收款流水的收银员列取的是员工ID，
 * 而日结把「员工ID>0」当作柜面收款、「=0」当作患者端自助/系统代收分桶统计。
 * 把患者账号的ID当员工ID绑进去，这笔钱就会被算进某个不存在（更坏是撞上同号真人）的收银员班结里。
 *
 * <p>用法约束：bind 之后必须在 finally 里 {@link #clear}，
 * 且只能绑<b>本单自己的出码人</b>（由我方库里读出，不是通知报文里的字段），
 * 否则等于给外部报文开了一条伪造身份写账的路。
 */
@NoArgsConstructor
public final class SystemOperatorContext {

    /**
     * 以院内出码人（收银员）身份执行后续写账。只给身份与姓名，不给角色与权限码
     * （回调链路上没有控制器方法鉴权，绑权限反而是多余的提权面）。
     */
    public static void bindEmployee(Long employeeId, String employeeName) {
        CurrentUser operator = new CurrentUser();
        operator.setUserType(UserTypeEnum.INNER.getCode());
        operator.setEmployeeId(employeeId);
        operator.setEmployeeName(employeeName);
        operator.setRealName(employeeName);
        bind(operator);
    }

    /**
     * 以患者身份执行后续写账：不是员工，所以收款流水自然落进「系统代收」桶。
     */
    public static void bindPatient(Long userId, Long patientId, String realName) {
        CurrentUser operator = new CurrentUser();
        operator.setUserType(UserTypeEnum.PATIENT.getCode());
        operator.setUserId(userId);
        operator.setPatientId(patientId);
        operator.setRealName(realName);
        bind(operator);
    }

    private static void bind(CurrentUser operator) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(operator, null, List.of()));
    }

    public static void clear() {
        SecurityContextHolder.clearContext();
    }
}

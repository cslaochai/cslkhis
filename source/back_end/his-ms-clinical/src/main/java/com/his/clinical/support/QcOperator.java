package com.his.clinical.support;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 质控操作人快照：一次质控「由谁发起」的身份。
 *
 * <p>为什么要在调用链上显式传而不是沿途现取：结诊收尾与定时补扫都跑在没有登录态的线程里，
 * 现取只能拿到 null；而质控单是要进考核与整改流程的证据，质控人必须回溯到发起时点的那个人。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QcOperator {

    /**
     * 操作人员工ID（系统发起时为 0）
     */
    private Long employeeId;

    /**
     * 操作人姓名（系统发起时为任务标识，非真人姓名）
     */
    private String name;
}

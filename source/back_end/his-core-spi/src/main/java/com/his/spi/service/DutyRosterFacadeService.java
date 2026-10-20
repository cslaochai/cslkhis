package com.his.spi.service;

import com.his.spi.vo.DutyOfficerBriefVO;

/**
 * 值班表端口：把「现在该谁值班」这个事实交给需要落执行人的业务域。
 *
 * <p>值班排班、代班、审核的编排仍归排班域自己；本端口只承诺按当前登录岗位取当班责任人。
 */
public interface DutyRosterFacadeService {

    /**
     * 取当前时刻的当班责任人。
     *
     * <p>用于急诊分诊记录落当班护士、床位中心与转科单落当班负责人——
     * 单据上没有「谁负责」就取不到归属人，交接责任断链。
     *
     * @return 当班责任人；当日该岗位没排人时返回的 {@code employeeId} 为 null
     */
    DutyOfficerBriefVO current();
}

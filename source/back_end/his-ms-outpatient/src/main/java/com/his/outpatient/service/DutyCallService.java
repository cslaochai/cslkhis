package com.his.outpatient.service;

import com.his.common.base.PageResult;
import com.his.outpatient.dto.DutyCallArriveDTO;
import com.his.outpatient.dto.DutyCallCancelDTO;
import com.his.outpatient.dto.DutyCallPageQueryDTO;
import com.his.outpatient.dto.DutyCallUpsertDTO;
import com.his.outpatient.vo.DutyCallTargetVO;
import com.his.outpatient.vo.DutyCallVO;

import java.time.LocalDate;
import java.util.List;

/**
 * 临床值班听班呼叫服务（sql/297）。
 *
 * <p>链路：发起呼叫 → 到岗应答 → 超时升级。呼叫的是「这一班的听班医生」，
 * 目标人由排班解析，不由前端指定 —— 值班制度要的是叫这一班的人，不是叫某个熟人。
 */
public interface DutyCallService {

    /**
     * 发起听班呼叫：解析当班该层级的听班值班人 → 落呼叫单 → 站内信通知被叫人。
     *
     * @return 新建的呼叫单
     */
    DutyCallVO call(DutyCallUpsertDTO dto);

    /**
     * 到岗应答：被叫人确认已到岗，闭环呼叫单并关掉站内信待办。
     */
    DutyCallVO arrive(DutyCallArriveDTO dto);

    /**
     * 取消呼叫：发起方主动撤销（病人好转 / 换人处理了）。
     */
    DutyCallVO cancel(DutyCallCancelDTO dto);

    /**
     * 呼叫单分页：按日期、科室、层级、被叫人、状态筛。
     */
    PageResult<DutyCallVO> listPage(DutyCallPageQueryDTO q);

    /**
     * 呼叫单详情（含呼叫流水）。
     */
    DutyCallVO detail(Long id);

    /**
     * 可呼叫的听班目标下拉：指定科室 + 日期 + 班次下各层级的当班听班人。
     * 呼叫方在发起前就能看到「这一班有没有二线」，避免叫了才知道没人。
     */
    List<DutyCallTargetVO> listTargets(Long deptId, LocalDate dutyDate, Integer shiftType);

    /**
     * 超时升级扫描：到岗时限内没人应答的呼叫单 → 升级到上一层级并落 escalate 标记。
     *
     * <p>「是否升级过」落库（escalate_status），定时任务每轮只处理未升级过的 ——
     * 重复升级会把同一条呼叫轰炸好几轮。
     *
     * @return 本轮升级的条数
     */
    int escalateOverdue();

    /**
     * 手工补跑超时升级（与定时任务同一入口，幂等）。
     */
    int escalateOverdueManual();
}

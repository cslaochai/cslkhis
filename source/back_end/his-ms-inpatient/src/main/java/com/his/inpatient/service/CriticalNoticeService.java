package com.his.inpatient.service;

import com.his.common.base.PageResult;
import com.his.inpatient.dto.CriticalNoticeDTO;
import com.his.inpatient.vo.CriticalNoticeVO;

import java.util.List;

/**
 * 病危/病重通知与告知书签收回执（sql/161，菜单 319）。
 */
public interface CriticalNoticeService {

    /**
     * 通知台账分页：按患者关键字、通知类别、状态、科室与开单日期区间筛。
     * 用于「病危重通知」页查询卡。
     */
    PageResult<CriticalNoticeVO.Row> listPage(CriticalNoticeDTO.PageQuery query);

    /**
     * 详情＝签收/打印数据源（敏感列已脱敏，含签名证据摘要）
     */
    CriticalNoticeVO.Detail getDetailById(Long id);

    /**
     * 开单底稿：按住院重查患者快照
     */
    CriticalNoticeVO.Base base(Long admissionId);

    /**
     * 在院患者候选（医生站横幅数据源）
     */
    List<CriticalNoticeVO.Inpatient> inpatients(String keyword, Integer limit);

    /**
     * 医师候选清单：在职且具备医生角色的员工。
     * 用于通知填写弹框选告知医师与见证医师。
     */
    List<CriticalNoticeVO.DoctorOption> doctorOptions();

    /**
     * 通知各状态计数汇总。
     * 用于「病危重通知」页顶部统计卡。
     */
    CriticalNoticeVO.Stats stats();

    /**
     * 填写/修改草稿（已签发/已签收禁改，签名锁定另有闸）
     */
    Long upsert(CriticalNoticeDTO.Upsert dto);

    /**
     * 签发：校验在院 → 医师电子签名（业务类型=9）→ 状态 1→2
     */
    void issue(CriticalNoticeDTO.Issue dto);

    /**
     * 签收：家属手写签名+法定关系落板，状态 2→3
     */
    void acknowledge(CriticalNoticeDTO.Acknowledge dto);

    /**
     * 作废（必填原因；已签收的告知事实不许作废）
     */
    void voidNotice(CriticalNoticeDTO.VoidNotice dto);

    /**
     * 回执打印计数（只有已签收可打印）
     */
    void print(CriticalNoticeDTO.Print dto);
}

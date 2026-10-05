package com.his.patient.service;

import com.his.common.base.PageResult;
import com.his.patient.dto.CriticalNoticeDTO;
import com.his.patient.vo.CriticalNoticeVO;

import java.util.List;

/**
 * 病危/病重通知与告知书签收回执（sql/161，菜单 319）。
 */
public interface CriticalNoticeService {

    PageResult<CriticalNoticeVO.Row> listPage(CriticalNoticeDTO.QueryPage query);

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

    List<CriticalNoticeVO.DoctorOption> doctorOptions();

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

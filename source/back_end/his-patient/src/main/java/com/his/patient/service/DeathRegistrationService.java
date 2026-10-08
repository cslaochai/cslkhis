package com.his.patient.service;

import com.his.common.base.PageResult;
import com.his.patient.dto.DeathRegistrationDTO;
import com.his.patient.vo.DeathRegisterVO;

import java.util.List;

/**
 * 住院死亡登记簿：死亡类型判定、报公安留痕、遗体去向、家属领取联次、纠纷登记。
 */
public interface DeathRegistrationService {

    PageResult<DeathRegisterVO.Row> listPage(DeathRegistrationDTO.QueryPage query);

    /**
     * 详情＝编辑回显（办理人电话明文）
     */
    DeathRegisterVO.Detail getDetailById(Long id);

    /**
     * 登记底稿：按住院带出死者/死亡时间/科室床位与有效证明摘要（服务端算，前端不自己拼）
     */
    DeathRegisterVO.Base base(Long admissionId);

    /**
     * 可登记候选：已办死亡离院的住院（死亡登记的前提是死亡事实已确认）
     */
    List<DeathRegisterVO.Base> admissionCandidates(String keyword, Integer limit);

    /**
     * 填写/修改（草稿可改；已登记后只能作废重登）
     */
    Long upsert(DeathRegistrationDTO.Upsert dto);

    /**
     * 确认登记（1→2）：非疾病死亡/死因不明必须已报公安，否则拒绝
     */
    void confirm(DeathRegistrationDTO.Confirm dto);

    /**
     * 作废（1/2→3）：作废后可对同一次住院重登
     */
    void voidRegister(DeathRegistrationDTO.VoidRegister dto);
}

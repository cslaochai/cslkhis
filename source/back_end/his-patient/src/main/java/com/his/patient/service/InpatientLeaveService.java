package com.his.patient.service;

import com.his.common.base.PageResult;
import com.his.patient.dto.InpatientLeaveDTO;
import com.his.patient.vo.InpatientLeaveVO;

import java.util.List;

/**
 * 住院患者请假/离院登记（sql/162，菜单 320）。
 *
 * <p>状态机：1-待审批 →(approve allow=1，电子签名) 2-已批准 →(leave：患方承诺三要素)
 * 3-已离院 →(back) 4-已返回；1 →(allow=0) 5-已拒绝；1/2 →(cancel) 6-已取消。
 * 超期未归是查询时算的展示态，不落状态列。
 */
public interface InpatientLeaveService {

    PageResult<InpatientLeaveVO.Row> listPage(InpatientLeaveDTO.QueryPage query);

    InpatientLeaveVO.Detail getDetailById(Long id);

    InpatientLeaveVO.Base base(Long admissionId);

    List<InpatientLeaveVO.Inpatient> inpatients(String keyword, Integer limit);

    InpatientLeaveVO.Stats stats();

    /**
     * 填写/修改申请单（仅待审批可改；一般项目服务端重查快照）
     */
    Long upsert(InpatientLeaveDTO.Upsert dto);

    /**
     * 审批：allow=true 批准（当前登录医师电子签名锁定）；allow=false 拒绝（必填理由）
     */
    void approve(InpatientLeaveDTO.Approve dto);

    /**
     * 登记离院 = 患方签署承诺书三要素（姓名/关系/手写签名）+ 实际离院时间
     */
    void confirmLeave(InpatientLeaveDTO.Confirm dto);

    /**
     * 返回销假（登记实际返回时间）
     */
    void confirmBack(InpatientLeaveDTO.Back dto);

    /**
     * 取消（仅待审批/已批准；已离院的单不许取消 —— 人已经出去了，事实不能蒸发）
     */
    void cancel(InpatientLeaveDTO.Cancel dto);

    /**
     * 超期处置记录（仅已离院且超期的单可记；联系不上必须升级上报）
     */
    void recordContact(InpatientLeaveDTO.Contact dto);

    /**
     * 承诺书打印计数（已离院/已返回可打印）
     */
    void print(InpatientLeaveDTO.Print dto);
}

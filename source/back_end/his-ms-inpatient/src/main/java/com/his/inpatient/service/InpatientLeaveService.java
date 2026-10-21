package com.his.inpatient.service;

import com.his.common.base.PageResult;
import com.his.inpatient.dto.InpatientLeaveDTO;
import com.his.inpatient.vo.InpatientLeaveVO;

import java.util.List;

/**
 * 住院患者请假/离院登记（sql/162，菜单 320）。
 */
public interface InpatientLeaveService {

    /**
     * 请假台账分页：按患者、请假类型与状态、科室、申请日期区间筛；只看超期未归时超期由服务端现算，库里不落超期状态。
     * 用于「患者请假离院」页查询卡。
     */
    PageResult<InpatientLeaveVO.Row> listPage(InpatientLeaveDTO.PageQuery query);

    /**
     * 单张请假单详情：含承诺书签署证据摘要与当前可用动作标记，联系电话已脱敏。
     * 用于「患者请假离院」页详情弹框，以及编辑回显、离院登记与打印前的取数。
     */
    InpatientLeaveVO.Detail getDetailById(Long id);

    /**
     * 开单底稿：按住院记录带出患者快照与在途请假单张数，快照由服务端重查不采信前端传入。
     * 用于新建请假申请时填充表单抬头。
     */
    InpatientLeaveVO.Base base(Long admissionId);

    /**
     * 在院患者候选及其在途请假状态，支持关键字过滤与条数上限。
     * 用于「患者请假离院」页顶部的在院患者横幅。
     */
    List<InpatientLeaveVO.Inpatient> inpatients(String keyword, Integer limit);

    /**
     * 请假业务计数：待审批、已批准待离院、在院外、超期未归、今日返回。
     * 用于「患者请假离院」页顶部统计卡。
     */
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

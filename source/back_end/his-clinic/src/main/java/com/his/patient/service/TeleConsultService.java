package com.his.patient.service;

import com.his.common.base.PageResult;
import com.his.patient.dto.*;
import com.his.patient.vo.OnlineConsultVO;
import com.his.patient.vo.TeleConsultStatVO;
import com.his.patient.vo.TeleConsultVO;

/**
 * 互联网医院 / 远程会诊服务。
 */
public interface TeleConsultService {

    // 远程会诊

    PageResult<TeleConsultVO> teleListPage(TeleConsultPageQueryDTO dto);

    /**
     * 远程会诊单详情：含申请、安排与会诊意见全量字段。
     * 用于会诊详情弹框展示，也作申请/修改表单的回显数据源。
     */
    TeleConsultVO teleGetDetailById(Long id);

    /**
     * 申请 / 修改（仅待安排可改）
     */
    TeleConsultVO teleUpsert(TeleConsultUpsertDTO dto);

    /**
     * 安排（待安排→已安排；planTime 必填）
     */
    TeleConsultVO teleArrange(TeleArrangeDTO dto);

    /**
     * 完成（已安排→已完成；会诊意见必填）
     */
    TeleConsultVO teleComplete(TeleActionDTO dto);

    /**
     * 取消（非终态→已取消；原因必填）
     */
    TeleConsultVO teleCancel(TeleActionDTO dto);

    /**
     * 删除（软删；仅待安排）
     */
    boolean teleDeleteById(Long id);

    // 线上问诊

    PageResult<OnlineConsultVO> onlineListPage(OnlinePageQueryDTO dto);

    /**
     * 线上问诊单详情：含患者诉求、接诊人与回复内容。
     * 用于问诊详情弹框展示。
     */
    OnlineConsultVO onlineGetDetailById(Long id);

    /**
     * 发起问诊（待接诊）
     */
    OnlineConsultVO onlineApply(OnlineApplyDTO dto);

    /**
     * 接诊（待接诊→接诊中；接诊人=当前登录人）
     */
    OnlineConsultVO onlineAccept(Long id);

    /**
     * 回复（接诊中→已完成；回复必填，回复即结束）
     */
    OnlineConsultVO onlineReply(OnlineReplyDTO dto);

    /**
     * 退诊（待接诊/接诊中→已退诊；原因必填）
     */
    OnlineConsultVO onlineReject(TeleActionDTO dto);

    /**
     * 删除（软删；仅待接诊）
     */
    boolean onlineDeleteById(Long id);

    /**
     * 统计（两条线的状态分布）
     */
    TeleConsultStatVO stat();
}

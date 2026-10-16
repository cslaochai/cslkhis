package com.his.appoint.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.his.appoint.dto.*;
import com.his.api.dto.*;
import com.his.appoint.entity.BizAppointInfo;
import com.his.appoint.vo.AppointStatusCountVO;
import com.his.api.vo.BizAppointInfoListVO;
import com.his.api.vo.RevisitFeePreviewVO;
import com.his.api.vo.RevisitRecordSelectVO;
import com.his.common.base.PageResult;

import java.util.List;

/**
 * 挂号服务接口
 */
public interface BizAppointService extends IService<BizAppointInfo> {

    /**
     * 分页查询挂号记录
     */
    PageResult<BizAppointInfoListVO> listPage(AppointQueryDTO queryDTO);

    /**
     * 挂号状态统计（六个状态各自的条数）—— 「六格状态卡」的唯一取数出口。
     */
    AppointStatusCountVO statusCount(AppointQueryDTO queryDTO);

    /**
     * 预约看板：一次取整段区间（日视图 1 天 / 周视图 7 天）的**全部**挂号，不分页。
     */
    List<BizAppointInfoListVO> boardList(AppointBoardQueryDTO queryDTO);

    /**
     * 患者挂号
     */
    BizAppointInfo addAppoint(AppointUpsertDTO upsertDTO);

    /**
     * 复诊费用预估：提交前告知「这张复诊号收不收费、按哪条策略」。
     */
    RevisitFeePreviewVO revisitFeePreview(RevisitFeePreviewDTO previewDTO);

    /**
     * 复诊「原病历」候选列表：按患者取最近若干次真正就诊过的病历。
     */
    java.util.List<RevisitRecordSelectVO> revisitRecordSelectList(Long patientId, int limit);

    /**
     * 退号
     */
    boolean cancelRegist(Long registId, String reason);

    /**
     * 修改挂号（仅窗口挂号且候诊中可改）
     */
    boolean updateRegist(BizAppointInfo registInfo);

    /**
     * 通用状态更新 —— <b>只允许改「还在流转中」的挂号，且不允许改成「已退号」</b>。
     *
     */
    boolean updateStatus(Long registId, Integer targetStatus);

    /**
     * 挂号/改号合一：新增回挂号记录，修改回空（前端只在新建时需要拿到号）。
     */
    BizAppointInfoListVO appointUpsert(AppointUpsertDTO upsertDTO);

    /**
     * 医生站建复诊号：只放来源 1-当日回诊、2-医嘱复诊预约，且只允许新建。
     */
    BizAppointInfoListVO revisitUpsert(AppointUpsertDTO upsertDTO);

    /**
     * 院内工作站的复诊费用预估：员工放行、患者 token 只能碰自己绑定的就诊人。
     */
    RevisitFeePreviewVO revisitFeePreviewScoped(RevisitFeePreviewDTO previewDTO);

    /**
     * 院内工作站的复诊原病历候选：归属口径同 {@link #revisitFeePreviewScoped}。
     */
    List<RevisitRecordSelectVO> revisitRecordSelectListScoped(Long patientId);

    /**
     * 退号，原因缺省为「患者主动退号」。
     */
    void cancelRegist(AppointCancelDTO appointCancelDTO);

    /**
     * 按主键取挂号详情。
     */
    BizAppointInfoListVO getDetail(AppointQueryDTO appointQueryDTO);
}

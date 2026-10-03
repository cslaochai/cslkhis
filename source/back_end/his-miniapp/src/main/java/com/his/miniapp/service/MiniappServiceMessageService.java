package com.his.miniapp.service;

import com.his.common.base.PageResult;
import com.his.miniapp.dto.ServiceMessageUpsertDTO;
import com.his.miniapp.dto.ServiceTicketActionDTO;
import com.his.miniapp.dto.ServiceTicketAppendDTO;
import com.his.miniapp.vo.ServiceMessageListVO;
import com.his.miniapp.vo.ServiceTicketDetailVO;

/**
 * 患者端工单（原「留言」，sql/221 升级为可受理工单）。
 */
public interface MiniappServiceMessageService {

    /** 提交工单，返回工单号 */
    String submit(ServiceMessageUpsertDTO dto);

    /** 我的工单（分页，含处理状态与结果） */
    PageResult<ServiceMessageListVO> myPage(Integer pageNum, Integer pageSize);

    /**
     * 工单详情（含流转时间轴）。
     * <p>归属校验在服务端：只能看自己的单，查不到与无权返回同一句（不区分"不存在或无权"）。
     */
    ServiceTicketDetailVO myDetail(Long id);

    /** 患者补充留言 */
    void append(ServiceTicketAppendDTO dto);

    /** 患者动作：撤单 / 确认解决 / 重开 */
    void patientAction(ServiceTicketActionDTO dto);
}

package com.his.miniapp.service;

import com.his.common.base.PageResult;
import com.his.miniapp.dto.TicketHandleDTO;
import com.his.miniapp.dto.TicketSearchDTO;
import com.his.miniapp.vo.ServiceMessageListVO;
import com.his.miniapp.vo.ServiceTicketDetailVO;
import com.his.miniapp.vo.TicketStatsVO;

/**
 * 院内工单受理（客服工作台）。
 *
 * <p>患者端提交的是「留言」，这里把它当工单处理：受理 → 回复 → 办结 → 患者确认。
 * 每一步都写 {@code biz_service_ticket_log}，患者端能看见进展。
 *
 * <p><b>操作人一律服务端取登录人</b>：受理人是谁不能由前端说了算。
 */
public interface MiniappServiceTicketAdminService {

    /** 工单列表（含全部状态） */
    PageResult<ServiceMessageListVO> adminPage(TicketSearchDTO dto);

    /** 工作台统计 */
    TicketStatsVO stats();

    /** 工单详情（含全量流转记录，含内部备注） */
    ServiceTicketDetailVO detail(Long id);

    /** 受理 / 回复 / 办结 / 关闭 / 内部备注 */
    void handle(TicketHandleDTO dto);
}

package com.his.miniapp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.common.base.PageResult;
import com.his.common.exception.BusinessException;
import com.his.common.util.DateFormats;
import com.his.common.util.TextUtil;
import com.his.miniapp.dto.TicketHandleDTO;
import com.his.miniapp.dto.TicketSearchDTO;
import com.his.miniapp.entity.BizServiceMessage;
import com.his.miniapp.entity.BizServiceTicketLog;
import com.his.miniapp.mapper.MiniappServiceMessageMapper;
import com.his.miniapp.mapper.MiniappServiceTicketLogMapper;
import com.his.miniapp.service.MiniappServiceTicketAdminService;
import com.his.miniapp.support.ServiceTicketStatus;
import com.his.miniapp.vo.ServiceMessageListVO;
import com.his.miniapp.vo.ServiceTicketDetailVO;
import com.his.miniapp.vo.ServiceTicketLogVO;
import com.his.miniapp.vo.TicketStatsVO;
import com.his.system.entity.CurrentUser;
import com.his.system.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 院内工单受理实现。
 *
 * <p><b>状态机在这里收口</b>：所有状态变更只走 {@link #handle}，
 * 每个动作都校验前置状态。散在各个 Controller 里改状态，迟早出现
 * 「已关闭的工单还能回复」这种说不清的账。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MiniappServiceTicketAdminServiceImpl extends ServiceImpl<MiniappServiceMessageMapper, BizServiceMessage> implements MiniappServiceTicketAdminService {

    /**
     * 超过这个时长仍未受理算超时
     */
    private static final int OVERDUE_HOURS = 24;

    private static final int LOG_CONTENT_MAX = 1000;

    private final MiniappServiceMessageMapper miniappServiceMessageMapper;
    private final MiniappServiceTicketLogMapper miniappServiceTicketLogMapper;

    private static String currentUsername() {
        CurrentUser user = UserUtils.getCurrentUser();
        return user == null ? null : user.getUsername();
    }

    @Override
    public PageResult<ServiceMessageListVO> adminPage(TicketSearchDTO dto) {
        TicketSearchDTO query = dto == null ? new TicketSearchDTO() : dto;
        int pageNum = query.getPageNum();
        int pageSize = query.getPageSize();

        String keyword = StringUtils.hasText(query.getKeyword()) ? query.getKeyword().trim() : null;
        String mine = Boolean.TRUE.equals(query.getOnlyMine()) ? currentUsername() : null;

        LambdaQueryWrapper<BizServiceMessage> w = new LambdaQueryWrapper<>();
        w.and(StringUtils.hasText(keyword), q -> q.like(BizServiceMessage::getMessageNo, keyword)
                        .or().like(BizServiceMessage::getContent, keyword)
                        .or().like(BizServiceMessage::getPatientName, keyword)
                        .or().like(BizServiceMessage::getContactPhone, keyword))
                .eq(query.getStatus() != null, BizServiceMessage::getStatus, query.getStatus())
                .eq(StringUtils.hasText(query.getCategoryCode()), BizServiceMessage::getCategoryCode, query.getCategoryCode())
                .eq(mine != null, BizServiceMessage::getAcceptBy, mine)
                // 待受理优先：客服进来是找新单，不是翻历史
                .orderByAsc(BizServiceMessage::getStatus)
                .orderByDesc(BizServiceMessage::getCreateTime)
                // 分页补唯一二级键
                .orderByDesc(BizServiceMessage::getId);

        long total = miniappServiceMessageMapper.selectCount(w);
        List<BizServiceMessage> rows = miniappServiceMessageMapper.selectList(
                w.last("LIMIT " + (pageNum - 1) * pageSize + ", " + pageSize));
        List<ServiceMessageListVO> records = new ArrayList<>();
        for (BizServiceMessage row : rows) {
            ServiceMessageListVO vo = new ServiceMessageListVO();
            vo.setId(row.getId());
            vo.setMessageNo(row.getMessageNo());
            vo.setCategoryCode(row.getCategoryCode());
            vo.setContent(row.getContent());
            vo.setStatus(row.getStatus());
            vo.setStatusText(ServiceTicketStatus.statusText(row.getStatus()));
            vo.setAcceptByName(row.getAcceptByName());
            vo.setReplyCount(row.getReplyCount());
            vo.setHandleResult(row.getHandleResult());
            vo.setCreateTime(row.getCreateTime() == null ? "" : row.getCreateTime().format(DateFormats.DATETIME));
            records.add(vo);
        }
        long pages = (total + pageSize - 1) / pageSize;
        return PageResult.of(total, pageNum, pageSize, pages, records);
    }

    @Override
    public TicketStatsVO stats() {
        TicketStatsVO vo = new TicketStatsVO();
        vo.setWaitAccept(countBy(null, ServiceTicketStatus.WAIT_ACCEPT));
        vo.setHandling(countBy(null, ServiceTicketStatus.HANDLING));
        vo.setFinished(countBy(null, ServiceTicketStatus.FINISHED));
        vo.setClosed(countBy(null, ServiceTicketStatus.CLOSED));

        LocalDateTime deadline = LocalDateTime.now().minusHours(OVERDUE_HOURS);
        Long overdue = miniappServiceMessageMapper.selectCount(new LambdaQueryWrapper<BizServiceMessage>()
                .eq(BizServiceMessage::getStatus, ServiceTicketStatus.WAIT_ACCEPT)
                .lt(BizServiceMessage::getCreateTime, deadline));
        vo.setOverdueWaitAccept(overdue == null ? 0 : overdue.intValue());
        return vo;
    }

    @Override
    public ServiceTicketDetailVO detail(Long id) {
        BizServiceMessage ticket = requireTicket(id);
        ServiceTicketDetailVO vo = new ServiceTicketDetailVO();
        vo.setId(String.valueOf(ticket.getId()));
        vo.setMessageNo(ticket.getMessageNo());
        vo.setPatientName(ticket.getPatientName());
        vo.setContactPhone(ticket.getContactPhone());
        vo.setCategoryCode(ticket.getCategoryCode());
        vo.setContent(ticket.getContent());
        vo.setStatus(ticket.getStatus());
        vo.setStatusText(ServiceTicketStatus.statusText(ticket.getStatus()));
        vo.setPriority(ticket.getPriority());
        vo.setAcceptByName(ticket.getAcceptByName());
        vo.setCloseReason(ticket.getCloseReason());
        vo.setHandleResult(ticket.getHandleResult());
        vo.setCreateTime(ticket.getCreateTime() == null ? "" : ticket.getCreateTime().format(DateFormats.DATETIME));
        // 院内看全量（含内部备注），患者端才过滤
        vo.setLogs(allLogs(ticket.getId()));
        return vo;
    }

    // 私有

    @Override
    public void handle(TicketHandleDTO dto) {
        BizServiceMessage ticket = requireTicket(dto.getId());
        String action = dto.getAction() == null ? "" : dto.getAction().trim();
        CurrentUser user = UserUtils.getCurrentUser();
        String operator = user == null ? null : user.getUsername();
        String operatorName = user == null ? null : user.getRealName();
        String content = TextUtil.cut(dto.getContent(), LOG_CONTENT_MAX);

        switch (action) {
            case "accept" -> {
                if (ticket.getStatus() != ServiceTicketStatus.WAIT_ACCEPT) {
                    throw new BusinessException("只有待受理的工单能受理，当前状态："
                            + ServiceTicketStatus.statusText(ticket.getStatus()));
                }
                ticket.setStatus(ServiceTicketStatus.HANDLING);
                ticket.setAcceptBy(operator);
                ticket.setAcceptByName(operatorName);
                ticket.setAcceptTime(LocalDateTime.now());
                ticket.setHandleBy(operator);
                miniappServiceMessageMapper.updateById(ticket);
                writeLog(ticket, ServiceTicketStatus.ACT_ACCEPT, operatorName + " 受理了这张工单", 1, operator, operatorName);
            }
            case "reply" -> {
                if (!StringUtils.hasText(content)) {
                    throw new BusinessException("回复内容不能为空");
                }
                if (ServiceTicketStatus.isClosed(ticket.getStatus())) {
                    throw new BusinessException("工单已关闭，不能回复");
                }
                // 回复未受理的工单 = 顺手受理，否则患者看到"有人回了但没人接"
                if (ticket.getStatus() == ServiceTicketStatus.WAIT_ACCEPT) {
                    ticket.setStatus(ServiceTicketStatus.HANDLING);
                    ticket.setAcceptBy(operator);
                    ticket.setAcceptByName(operatorName);
                    ticket.setAcceptTime(LocalDateTime.now());
                }
                ticket.setReplyCount((ticket.getReplyCount() == null ? 0 : ticket.getReplyCount()) + 1);
                ticket.setLastReplyTime(LocalDateTime.now());
                ticket.setHandleBy(operator);
                miniappServiceMessageMapper.updateById(ticket);
                int visible = dto.getVisibleToPatient() == null ? 1 : dto.getVisibleToPatient();
                writeLog(ticket, ServiceTicketStatus.ACT_REPLY, content, visible, operator, operatorName);
            }
            case "finish" -> {
                if (!StringUtils.hasText(content)) {
                    throw new BusinessException("办结必须写处理结果，否则患者不知道你做了什么");
                }
                if (ticket.getStatus() != ServiceTicketStatus.HANDLING) {
                    throw new BusinessException("只有处理中的工单能办结，当前状态："
                            + ServiceTicketStatus.statusText(ticket.getStatus()));
                }
                ticket.setStatus(ServiceTicketStatus.FINISHED);
                ticket.setHandleResult(content);
                ticket.setHandleBy(operator);
                ticket.setHandleTime(LocalDateTime.now());
                miniappServiceMessageMapper.updateById(ticket);
                writeLog(ticket, ServiceTicketStatus.ACT_FINISH, content, 1, operator, operatorName);
            }
            case "close" -> {
                if (!StringUtils.hasText(content)) {
                    throw new BusinessException("关闭必须写原因");
                }
                if (ServiceTicketStatus.isClosed(ticket.getStatus())) {
                    throw new BusinessException("工单已经是关闭状态");
                }
                ticket.setStatus(ServiceTicketStatus.CLOSED);
                ticket.setCloseBy(operator);
                ticket.setCloseTime(LocalDateTime.now());
                ticket.setCloseReason(content);
                miniappServiceMessageMapper.updateById(ticket);
                writeLog(ticket, ServiceTicketStatus.ACT_CLOSE, content, 1, operator, operatorName);
            }
            case "note" -> {
                if (!StringUtils.hasText(content)) {
                    throw new BusinessException("备注内容不能为空");
                }
                int visible = dto.getVisibleToPatient() == null ? 0 : dto.getVisibleToPatient();
                writeLog(ticket, ServiceTicketStatus.ACT_REPLY, content, visible, operator, operatorName);
            }
            default -> throw new BusinessException("不支持的动作：" + action);
        }
        log.info("[院内工单] 单号={} 动作={} 操作人={}", ticket.getMessageNo(), action, operator);
    }

    private BizServiceMessage requireTicket(Long id) {
        if (id == null) {
            throw new BusinessException("工单ID不合法");
        }
        BizServiceMessage ticket = miniappServiceMessageMapper.selectById(id);
        if (ticket == null) {
            throw new BusinessException("工单不存在");
        }
        return ticket;
    }

    private int countBy(String acceptBy, Integer status) {
        LambdaQueryWrapper<BizServiceMessage> w = new LambdaQueryWrapper<BizServiceMessage>()
                .eq(BizServiceMessage::getStatus, status);
        if (StringUtils.hasText(acceptBy)) {
            w.eq(BizServiceMessage::getAcceptBy, acceptBy);
        }
        Long n = miniappServiceMessageMapper.selectCount(w);
        return n == null ? 0 : n.intValue();
    }

    private List<ServiceTicketLogVO> allLogs(Long messageId) {
        List<BizServiceTicketLog> rows = miniappServiceTicketLogMapper.selectList(
                new LambdaQueryWrapper<BizServiceTicketLog>()
                        .eq(BizServiceTicketLog::getMessageId, messageId)
                        .orderByAsc(BizServiceTicketLog::getCreateTime)
                        .orderByAsc(BizServiceTicketLog::getId));
        List<ServiceTicketLogVO> vos = new ArrayList<>();
        for (BizServiceTicketLog row : rows) {
            ServiceTicketLogVO vo = new ServiceTicketLogVO();
            vo.setAction(row.getAction());
            vo.setActionText(ServiceTicketStatus.actionText(row.getAction()));
            vo.setContent(row.getContent());
            vo.setOperatorType(row.getOperatorType());
            vo.setOperatorName(row.getOperatorName());
            vo.setVisibleToPatient(row.getVisibleToPatient());
            vo.setCreateTime(row.getCreateTime() == null ? "" : row.getCreateTime().format(DateFormats.DATETIME));
            vos.add(vo);
        }
        return vos;
    }

    private void writeLog(BizServiceMessage ticket, int action, String content,
                          int visibleToPatient, String operator, String operatorName) {
        BizServiceTicketLog logEntity = new BizServiceTicketLog();
        logEntity.setMessageId(ticket.getId());
        logEntity.setMessageNo(ticket.getMessageNo());
        logEntity.setAction(action);
        logEntity.setContent(content);
        logEntity.setVisibleToPatient(visibleToPatient);
        logEntity.setOperatorType(2);
        logEntity.setOperator(operator);
        logEntity.setOperatorName(operatorName);
        logEntity.setCreateBy(operator);
        miniappServiceTicketLogMapper.insert(logEntity);
    }

}

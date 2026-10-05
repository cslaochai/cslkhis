package com.his.miniapp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.his.common.base.PageResult;
import com.his.common.exception.BusinessException;
import com.his.miniapp.dto.ServiceMessageUpsertDTO;
import com.his.miniapp.dto.ServiceTicketActionDTO;
import com.his.miniapp.dto.ServiceTicketAppendDTO;
import com.his.miniapp.entity.BizServiceMessage;
import com.his.miniapp.entity.BizServiceTicketLog;
import com.his.miniapp.mapper.MiniappServiceMessageMapper;
import com.his.miniapp.mapper.MiniappServiceTicketLogMapper;
import com.his.miniapp.service.MiniappDirectoryService;
import com.his.miniapp.service.MiniappServiceMessageService;
import com.his.miniapp.support.ServiceTicketStatus;
import com.his.miniapp.vo.PatientDetailVO;
import com.his.miniapp.vo.ServiceMessageListVO;
import com.his.miniapp.vo.ServiceTicketDetailVO;
import com.his.miniapp.vo.ServiceTicketLogVO;
import com.his.security.entity.CurrentUser;
import com.his.security.UserUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 患者端工单实现（sql/221 把留言升级为可受理工单）。
 *
 * <p><b>归属一律服务端定</b>：patientId 取登录态，前端传什么都不认。
 * 工单内容会进医院的工作队列，能被别人看到 —— 这条口子一旦放开，
 * 就是"用别人的身份给医院发消息"。
 *
 * <p><b>每一步流转都写 log</b>：患者端时间轴与客服端证据链是同一份数据。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MiniappServiceMessageServiceImpl implements MiniappServiceMessageService {

    private static final String NO_PREFIX = "MSG";

    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("yyyyMMdd");

    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /** 与 biz_service_message.content 列宽一致，写库前先截 */
    private static final int CONTENT_MAX = 1000;

    /** log.content 列宽 1000 */
    private static final int LOG_CONTENT_MAX = 1000;

    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int MAX_PAGE_SIZE = 50;

    private final MiniappServiceMessageMapper messageMapper;
    private final MiniappServiceTicketLogMapper ticketLogMapper;
    private final MiniappDirectoryService directoryService;

    @Override
    public String submit(ServiceMessageUpsertDTO dto) {
        CurrentUser user = UserUtils.getCurrentUser();
        if (user == null || user.getPatientId() == null) {
            throw new BusinessException("当前账号未绑定就诊人，无法提交留言");
        }
        Long patientId = user.getPatientId();
        BizServiceMessage entity = new BizServiceMessage();
        entity.setMessageNo(nextMessageNo());
        entity.setUserId(user.getUserId());
        entity.setPatientId(patientId);
        entity.setContent(cut(dto.getContent(), CONTENT_MAX));
        entity.setCategoryCode(dto.getCategoryCode());
        entity.setStatus(ServiceTicketStatus.WAIT_ACCEPT);
        entity.setPriority(0);
        entity.setReplyCount(0);
        // 姓名与电话落快照：处理工单时患者可能已经换号，快照才是当时的联系方式
        fillContact(entity, patientId, dto.getContactPhone());
        // MetaObjectHandler 不填 createBy：工单要能追溯到"谁提的"
        entity.setCreateBy(user.getUsername());
        messageMapper.insert(entity);

        // 提单本身就是时间轴第一格：患者能看到"我什么时候提的"
        writeLog(entity, ServiceTicketStatus.ACT_SUBMIT, entity.getContent(), 1,
                user.getUsername(), entity.getPatientName());
        log.info("[患者工单] 提交 单号={} 患者={} 分类={}", entity.getMessageNo(), patientId, dto.getCategoryCode());
        return entity.getMessageNo();
    }

    @Override
    public PageResult<ServiceMessageListVO> myPage(Integer pageNum, Integer pageSize) {
        CurrentUser user = UserUtils.getCurrentUser();
        if (user == null || user.getUserId() == null) {
            throw new BusinessException("未获取到登录用户");
        }
        int size = pageSize == null || pageSize < 1 ? DEFAULT_PAGE_SIZE : Math.min(pageSize, MAX_PAGE_SIZE);
        int current = pageNum == null || pageNum < 1 ? 1 : pageNum;
        LambdaQueryWrapper<BizServiceMessage> wrapper = new LambdaQueryWrapper<BizServiceMessage>()
                .eq(BizServiceMessage::getUserId, user.getUserId())
                .orderByDesc(BizServiceMessage::getCreateTime)
                .orderByDesc(BizServiceMessage::getId);
        long total = messageMapper.selectCount(wrapper);
        List<BizServiceMessage> rows = messageMapper.selectList(wrapper.last("LIMIT " + (current - 1) * size + ", " + size));
        List<ServiceMessageListVO> records = new ArrayList<>();
        for (BizServiceMessage row : rows) {
            records.add(toVO(row));
        }
        return PageResult.of(total, current, size, (total + size - 1) / size, records);
    }

    @Override
    public ServiceTicketDetailVO myDetail(Long id) {
        BizServiceMessage ticket = requireOwnTicket(id);
        ServiceTicketDetailVO vo = toDetailVO(ticket);
        // 内部备注（visible_to_patient=0）不给患者：客服写「这人上次也投诉过收费」不该被看见
        vo.setLogs(patientVisibleLogs(ticket.getId()));
        vo.setActions(patientActions(ticket.getStatus()));
        return vo;
    }

    @Override
    public void append(ServiceTicketAppendDTO dto) {
        BizServiceMessage ticket = requireOwnTicket(parseId(dto.getId()));
        if (!ServiceTicketStatus.canAppend(ticket.getStatus())) {
            throw new BusinessException("工单已关闭，无法补充；如需继续咨询请重新提交");
        }
        String content = cut(dto.getContent(), LOG_CONTENT_MAX);
        boolean wasFinished = ticket.getStatus() == ServiceTicketStatus.FINISHED;
        if (wasFinished) {
            // 办结后还在补充 = 患者认为没解决，自动重开，不让患者对着"已办结"说话
            ticket.setStatus(ServiceTicketStatus.HANDLING);
        }
        messageMapper.updateById(ticket);
        writeLog(ticket, ServiceTicketStatus.ACT_APPEND, content, 1,
                ticket.getCreateBy(), ticket.getPatientName());
        if (wasFinished) {
            writeLog(ticket, ServiceTicketStatus.ACT_REOPEN, "患者补充了留言，工单重新进入处理", 1,
                    ticket.getCreateBy(), ticket.getPatientName());
        }
    }

    @Override
    public void patientAction(ServiceTicketActionDTO dto) {
        BizServiceMessage ticket = requireOwnTicket(parseId(dto.getId()));
        String action = dto.getAction() == null ? "" : dto.getAction().trim();
        CurrentUser user = UserUtils.getCurrentUser();
        String operator = user == null ? null : user.getUsername();
        String reason = cut(dto.getReason(), 200);

        switch (action) {
            case "cancel" -> {
                if (!ServiceTicketStatus.canCancel(ticket.getStatus())) {
                    throw new BusinessException("工单已办结或已关闭，不能撤单");
                }
                ticket.setStatus(ServiceTicketStatus.CLOSED);
                ticket.setCloseBy(operator);
                ticket.setCloseTime(LocalDateTime.now());
                ticket.setCloseReason(StringUtils.hasText(reason) ? reason : "患者撤单");
                messageMapper.updateById(ticket);
                writeLog(ticket, ServiceTicketStatus.ACT_CANCEL,
                        StringUtils.hasText(reason) ? reason : "患者撤单", 1, operator, ticket.getPatientName());
            }
            case "confirm" -> {
                if (!ServiceTicketStatus.canConfirm(ticket.getStatus())) {
                    throw new BusinessException("只有已办结的工单能确认解决");
                }
                ticket.setStatus(ServiceTicketStatus.CLOSED);
                ticket.setCloseBy(operator);
                ticket.setCloseTime(LocalDateTime.now());
                ticket.setCloseReason("患者确认解决");
                messageMapper.updateById(ticket);
                writeLog(ticket, ServiceTicketStatus.ACT_CLOSE, "患者确认问题已解决", 1, operator, ticket.getPatientName());
            }
            case "reopen" -> {
                if (!ServiceTicketStatus.canConfirm(ticket.getStatus())) {
                    throw new BusinessException("只有已办结的工单能重开");
                }
                ticket.setStatus(ServiceTicketStatus.HANDLING);
                ticket.setCloseBy(null);
                ticket.setCloseTime(null);
                ticket.setCloseReason(null);
                messageMapper.updateById(ticket);
                writeLog(ticket, ServiceTicketStatus.ACT_REOPEN,
                        StringUtils.hasText(reason) ? reason : "患者认为问题未解决，工单重开", 1, operator, ticket.getPatientName());
            }
            default -> throw new BusinessException("不支持的动作：" + action);
        }
        log.info("[患者工单] {} 单号={} 动作={}", ticket.getPatientName(), ticket.getMessageNo(), action);
    }

    /**
     * 取工单并校验归属。
     *
     * <p><b>查不到与无权返回同一句</b>：不告诉对方"这张单存在但不是你的"，
     * 那是免费的信息探测。
     */
    private BizServiceMessage requireOwnTicket(Long id) {
        if (id == null) {
            throw new BusinessException("工单ID不合法");
        }
        CurrentUser user = UserUtils.getCurrentUser();
        if (user == null || user.getUserId() == null) {
            throw new BusinessException("未获取到登录用户");
        }
        BizServiceMessage ticket = messageMapper.selectById(id);
        if (ticket == null || ticket.getUserId() == null || !ticket.getUserId().equals(user.getUserId())) {
            throw new BusinessException("工单不存在或无权查看");
        }
        return ticket;
    }

    /**
     * 单号 = MSG + 日期 + 当天最大序号 + 1。
     * <p><b>绝不用「当天 count + 1」</b>：删掉一条之后 count 回退，下一个单号直接撞唯一键。
     */
    private String nextMessageNo() {
        String prefix = NO_PREFIX + LocalDate.now().format(DAY);
        String max = messageMapper.maxMessageNo(prefix);
        int seq = 1;
        if (StringUtils.hasText(max) && max.length() > prefix.length()) {
            try {
                seq = Integer.parseInt(max.substring(prefix.length())) + 1;
            } catch (NumberFormatException ex) {
                log.warn("[患者工单] 单号序号解析失败，回落 1：{}", max);
                seq = 1;
            }
        }
        return prefix + String.format("%04d", seq);
    }

    private void fillContact(BizServiceMessage entity, Long patientId, String inputPhone) {
        String phone = inputPhone;
        String name = null;
        try {
            PatientDetailVO profile = directoryService.patientProfile(patientId);
            if (profile != null) {
                name = profile.getPatientName();
                if (!StringUtils.hasText(phone)) {
                    phone = profile.getPhone();
                }
            }
        } catch (Exception ex) {
            // 档案查不到不能阻断提单：患者要留的是言，姓名电话只是给处理人看的辅助信息
            log.warn("[患者工单] 就诊人档案查询失败，只记录内容 patientId={}", patientId);
        }
        entity.setPatientName(name);
        entity.setContactPhone(phone);
    }

    private void writeLog(BizServiceMessage ticket, int action, String content,
                          int visibleToPatient, String operator, String operatorName) {
        BizServiceTicketLog logEntity = new BizServiceTicketLog();
        logEntity.setMessageId(ticket.getId());
        logEntity.setMessageNo(ticket.getMessageNo());
        logEntity.setAction(action);
        logEntity.setContent(content);
        logEntity.setVisibleToPatient(visibleToPatient);
        logEntity.setOperatorType(1);
        logEntity.setOperator(operator);
        logEntity.setOperatorName(operatorName);
        logEntity.setCreateBy(operator);
        ticketLogMapper.insert(logEntity);
    }

    private List<ServiceTicketLogVO> patientVisibleLogs(Long messageId) {
        List<BizServiceTicketLog> rows = ticketLogMapper.selectList(
                new LambdaQueryWrapper<BizServiceTicketLog>()
                        .eq(BizServiceTicketLog::getMessageId, messageId)
                        .eq(BizServiceTicketLog::getVisibleToPatient, 1)
                        .orderByAsc(BizServiceTicketLog::getCreateTime)
                        .orderByAsc(BizServiceTicketLog::getId));
        List<ServiceTicketLogVO> vos = new ArrayList<>();
        for (BizServiceTicketLog row : rows) {
            ServiceTicketLogVO vo = new ServiceTicketLogVO();
            vo.setAction(row.getAction());
            vo.setActionText(ServiceTicketStatus.actionText(row.getAction()));
            vo.setContent(row.getContent());
            vo.setOperatorType(row.getOperatorType());
            vo.setOperatorName(row.getOperatorType() != null && row.getOperatorType() == 1
                    ? "我" : row.getOperatorName());
            vo.setVisibleToPatient(row.getVisibleToPatient());
            vo.setCreateTime(row.getCreateTime() == null ? "" : row.getCreateTime().format(TIME));
            vos.add(vo);
        }
        return vos;
    }

    private static List<String> patientActions(Integer status) {
        List<String> actions = new ArrayList<>();
        if (ServiceTicketStatus.canAppend(status)) {
            actions.add("append");
        }
        if (ServiceTicketStatus.canCancel(status)) {
            actions.add("cancel");
        }
        if (ServiceTicketStatus.canConfirm(status)) {
            actions.add("confirm");
            actions.add("reopen");
        }
        return actions;
    }

    private static Long parseId(String value) {
        if (!StringUtils.hasText(value) || !value.matches("\\d{1,20}")) {
            return null;
        }
        return Long.parseLong(value);
    }

    private static String cut(String text, int max) {
        if (!StringUtils.hasText(text)) {
            return text;
        }
        String value = text.trim();
        return value.length() <= max ? value : value.substring(0, max);
    }

    private static ServiceMessageListVO toVO(BizServiceMessage entity) {
        ServiceMessageListVO vo = new ServiceMessageListVO();
        vo.setId(entity.getId());
        vo.setMessageNo(entity.getMessageNo());
        vo.setCategoryCode(entity.getCategoryCode());
        vo.setContent(entity.getContent());
        vo.setStatus(entity.getStatus());
        vo.setStatusText(ServiceTicketStatus.statusText(entity.getStatus()));
        vo.setAcceptByName(entity.getAcceptByName());
        vo.setReplyCount(entity.getReplyCount());
        vo.setHandleResult(entity.getHandleResult());
        vo.setActions(patientActions(entity.getStatus()));
        vo.setCreateTime(entity.getCreateTime() == null ? "" : entity.getCreateTime().format(TIME));
        return vo;
    }

    private static ServiceTicketDetailVO toDetailVO(BizServiceMessage entity) {
        ServiceTicketDetailVO vo = new ServiceTicketDetailVO();
        vo.setId(String.valueOf(entity.getId()));
        vo.setMessageNo(entity.getMessageNo());
        vo.setPatientName(entity.getPatientName());
        vo.setContactPhone(entity.getContactPhone());
        vo.setCategoryCode(entity.getCategoryCode());
        vo.setContent(entity.getContent());
        vo.setStatus(entity.getStatus());
        vo.setStatusText(ServiceTicketStatus.statusText(entity.getStatus()));
        vo.setPriority(entity.getPriority());
        vo.setAcceptByName(entity.getAcceptByName());
        vo.setCloseReason(entity.getCloseReason());
        vo.setHandleResult(entity.getHandleResult());
        vo.setCreateTime(entity.getCreateTime() == null ? "" : entity.getCreateTime().format(TIME));
        return vo;
    }
}

package com.his.iot.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.his.common.entity.CurrentUser;
import com.his.common.enums.IotEventCodeEnum;
import com.his.common.enums.IotEventHandleStatusEnum;
import com.his.common.enums.IotEventLevelEnum;
import com.his.common.exception.BusinessException;
import com.his.common.utils.UserUtils;
import com.his.iot.dto.IotEventHandleDTO;
import com.his.iot.dto.IotEventPageQueryDTO;
import com.his.iot.entity.IotDeviceBind;
import com.his.iot.entity.IotEvent;
import com.his.iot.mapper.IotEventMapper;
import com.his.iot.service.IotDeviceBindService;
import com.his.iot.service.IotEventService;
import com.his.iot.service.RobotDispatchService;
import com.his.iot.support.IotAlarmPushService;
import com.his.iot.vo.IotEventVO;
import com.his.spi.service.DeptScopeService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 设备事件落库与处理。
 * <p>
 * 幂等靠表上的 (device_no, seq, ts) 唯一键：设备 QoS1 重发、HIS 重连补发都会命中同一键，
 * 这里吃 {@link DuplicateKeyException} 当成功，不写业务判断（并发下判重必漏）。
 */
@Service
@RequiredArgsConstructor
public class IotEventServiceImpl extends ServiceImpl<IotEventMapper, IotEvent> implements IotEventService {

    private static final Logger log = LoggerFactory.getLogger(IotEventServiceImpl.class);
    /** detail 列宽 varchar(500)，长文本先截断，别让一条告警把整批写库拖失败 */
    private static final int DETAIL_MAX = 500;
    private static final int REMARK_MAX = 500;
    /** 推给前端的展示时间：SSE 帧走 JSON，LocalDateTime 序列化成 ISO 反而要在页面再格式化 */
    private static final DateTimeFormatter TS_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final IotAlarmPushService alarmPushService;
    private final IotDeviceBindService deviceBindService;
    private final DeptScopeService deptScopeService;
    private final RobotDispatchService robotDispatchService;
    private final ObjectMapper json = new ObjectMapper();

    @Override
    public void saveDeviceEvent(String deviceNo, String deviceType, String eventCode, Integer level, String detail,
                                String dataJson, Long seq, LocalDateTime ts) {
        if (deviceNo == null || eventCode == null || eventCode.isBlank()) {
            return;
        }
        // 设备挂在患者身上的才有归属科室：告警推送按科室广播，靠这条在用绑定把 device_no 折成 dept_id
        IotDeviceBind bind = deviceBindService.findActiveBind(deviceNo);
        IotEvent event = new IotEvent();
        event.setDeviceNo(deviceNo);
        event.setDeviceType(deviceType == null ? "" : deviceType);
        event.setDeptId(bind == null ? null : bind.getDeptId());
        event.setEventCode(eventCode);
        event.setLevel(level == null ? IotEventLevelEnum.INFO.getCode() : level);
        event.setDetail(truncate(detail == null ? IotEventCodeEnum.getLabel(eventCode, eventCode) : detail, DETAIL_MAX));
        event.setDataJson(dataJson);
        event.setSeq(seq == null ? 0L : seq);
        event.setTs(ts == null ? LocalDateTime.now() : ts);
        event.setHandledFlag(IotEventHandleStatusEnum.PENDING.getCode());
        event.setCreateTime(LocalDateTime.now());
        try {
            save(event);
        } catch (DuplicateKeyException e) {
            // QoS1 重发：同一个 (device_no, seq, ts) 已经落过库，也不再重复推送一帧，否则大屏会响两遍
            log.debug("设备事件重复报文已忽略：{} {} {}", deviceNo, eventCode, event.getTs());
            return;
        }
        pushAlarm(event, bind);
        syncDispatchOrder(event, dataJson);
    }

    /**
     * 机器人执行事件回写配送任务单：TASK_START 推到配送中，TASK_COMPLETE 回填签收人与行程回执。
     * 事件报文解析失败不影响事件落库本身，只记日志。
     */
    private void syncDispatchOrder(IotEvent event, String dataJson) {
        if (!"robot".equals(event.getDeviceType()) || dataJson == null || dataJson.isBlank()) {
            return;
        }
        try {
            JsonNode data = json.readTree(dataJson);
            String orderNo = data.path("orderNo").asText(null);
            robotDispatchService.syncFromEvent(event.getEventCode(), orderNo, data,
                    event.getDeviceNo(), event.getTs());
        } catch (Exception e) {
            log.warn("配送任务单状态回写失败 deviceNo={} eventCode={}：{}",
                    event.getDeviceNo(), event.getEventCode(), e.getMessage());
        }
    }

    /**
     * 告警推送到所属科室的监护大屏。
     * <p>推送失败不能影响落库（MQTT 订阅线程还在跑），只记日志。
     */
    private void pushAlarm(IotEvent event, IotDeviceBind bind) {
        if (bind == null || bind.getDeptId() == null) {
            // 未绑定设备的告警没有归属科室：宁可页面看不到，也不能猜一个病区推过去（那会把别人的告警推到护士站）
            return;
        }
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("type", "iot-alarm");
            payload.put("eventId", event.getId());
            payload.put("deviceNo", event.getDeviceNo());
            payload.put("deviceType", event.getDeviceType());
            payload.put("eventCode", event.getEventCode());
            payload.put("eventCodeText", IotEventCodeEnum.getLabel(event.getEventCode(), event.getEventCode()));
            payload.put("level", event.getLevel());
            payload.put("levelText", IotEventLevelEnum.getLabel(event.getLevel()));
            payload.put("detail", event.getDetail());
            payload.put("wardId", bind.getWardId());
            payload.put("bedNo", bind.getBedNo());
            payload.put("ts", event.getTs() == null ? null : TS_FORMAT.format(event.getTs()));
            alarmPushService.pushAlarm(bind.getDeptId(), payload);
        } catch (Exception e) {
            log.warn("设备告警推送失败 deviceNo={} eventCode={}", event.getDeviceNo(), event.getEventCode(), e);
        }
    }

    @Override
    public SseEmitter subscribeAlarm(Long deptId) {
        CurrentUser user = UserUtils.getCurrentUser();
        if (user == null) {
            throw new BusinessException(401, "未获取到登录用户，不能订阅设备告警");
        }
        // 数据权限执行点：传了科室就校验（越权直接抛），没传落到当前岗位科室
        Long target = deptScopeService.resolveDeptId(deptId);
        if (target == null) {
            target = user.getDeptId();
        }
        if (target == null) {
            throw new BusinessException(400, "未指定科室，且当前岗位没有科室归属");
        }
        // 子树聚合：订阅父科室时挂到自身 + 后代科室；受限角色再与授权集合交一次，不挂未授权后代
        Set<Long> scope = new HashSet<>(deptScopeService.descendantDeptIds(target));
        if (deptScopeService.isScoped()) {
            Set<Long> allowed = deptScopeService.allowedDeptIds();
            if (allowed != null) {
                scope.retainAll(allowed);
            }
        }
        if (scope.isEmpty()) {
            throw new BusinessException(400, "当前科室子树内无可订阅的科室");
        }
        return alarmPushService.register(scope);
    }

    @Override
    public IPage<IotEventVO> listPage(IotEventPageQueryDTO query) {
        Page<IotEvent> page = new Page<>(query.getPageNum(), query.getPageSize());
        // 选中父科室时按子树聚合：先过授权门禁，再展开「自身 + 后代」，受限角色与授权集合交一次；
        // 不传科室时受限角色直接落授权集合，不能退化成不加科室条件=全院
        Set<Long> deptIds;
        if (query.getDeptId() != null) {
            Long target = deptScopeService.resolveDeptId(query.getDeptId());
            if (target == null) {
                throw new BusinessException(400, "未指定科室，且当前岗位没有科室归属");
            }
            deptIds = new HashSet<>(deptScopeService.descendantDeptIds(target));
            if (deptScopeService.isScoped()) {
                Set<Long> allowed = deptScopeService.allowedDeptIds();
                if (allowed != null) {
                    deptIds.retainAll(allowed);
                }
            }
        } else if (deptScopeService.isScoped()) {
            deptIds = new HashSet<>(deptScopeService.allowedDeptIds());
        } else {
            deptIds = null;
        }
        boolean hasScope = deptIds != null && !deptIds.isEmpty();
        page(page, Wrappers.<IotEvent>lambdaQuery()
                .eq(query.getDeviceType() != null && !query.getDeviceType().isBlank(),
                        IotEvent::getDeviceType, query.getDeviceType())
                .eq(query.getDeviceNo() != null && !query.getDeviceNo().isBlank(),
                        IotEvent::getDeviceNo, query.getDeviceNo())
                .in(hasScope, IotEvent::getDeptId, deptIds)
                // 传了科室但授权子树为空：强制空结果，不能退化成不加科室条件=全院
                .eq(!hasScope && query.getDeptId() != null, IotEvent::getDeptId, -1L)
                .eq(query.getHandledFlag() != null, IotEvent::getHandledFlag, query.getHandledFlag())
                .ge(query.getMinLevel() != null, IotEvent::getLevel, query.getMinLevel())
                .ge(query.getStartTime() != null, IotEvent::getTs, query.getStartTime())
                .le(query.getEndTime() != null, IotEvent::getTs, query.getEndTime())
                .orderByDesc(IotEvent::getTs));

        List<IotEventVO> records = page.getRecords().stream().map(this::toVO).toList();
        Page<IotEventVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        voPage.setRecords(records);
        return voPage;
    }

    @Override
    public void handle(IotEventHandleDTO dto) {
        CurrentUser operator = UserUtils.getCurrentUser();
        if (operator == null) {
            throw new BusinessException(401, "未获取到登录用户，不能处理告警");
        }
        IotEvent event = getById(dto.getId());
        if (event == null) {
            throw new BusinessException(400, "事件不存在或已处理，不能重复处理");
        }
        // 数据权限门禁：非授权科室的告警不能在本岗位闭环
        deptScopeService.assertDeptAccessible(event.getDeptId());
        // 条件更新：两个护士同时点「已处理」时，第二次受影响行数为 0。
        // 原来先 select 再判状态再 update，并发下两边都能改，状态还能被改写一遍
        boolean updated = update(Wrappers.<IotEvent>lambdaUpdate()
                .eq(IotEvent::getId, dto.getId())
                .eq(IotEvent::getHandledFlag, IotEventHandleStatusEnum.PENDING.getCode())
                .set(IotEvent::getHandledFlag, dto.getHandledFlag())
                .set(IotEvent::getHandleById, operator.getEmployeeId())
                .set(IotEvent::getHandleBy, operator.getRealName())
                .set(IotEvent::getHandleTime, LocalDateTime.now())
                .set(IotEvent::getHandleRemark, truncate(dto.getRemark(), REMARK_MAX)));
        if (!updated) {
            throw new BusinessException(400, "事件不存在或已处理，不能重复处理");
        }
    }

    private IotEventVO toVO(IotEvent e) {
        IotEventVO vo = new IotEventVO();
        vo.setId(e.getId());
        vo.setDeviceNo(e.getDeviceNo());
        vo.setDeviceType(e.getDeviceType());
        vo.setEventCode(e.getEventCode());
        vo.setEventCodeText(IotEventCodeEnum.getLabel(e.getEventCode(), e.getEventCode()));
        vo.setLevel(e.getLevel());
        vo.setLevelText(IotEventLevelEnum.getLabel(e.getLevel()));
        vo.setDetail(e.getDetail());
        vo.setDataJson(e.getDataJson());
        vo.setTs(e.getTs());
        vo.setHandledFlag(e.getHandledFlag());
        vo.setHandledFlagText(IotEventHandleStatusEnum.getLabel(e.getHandledFlag()));
        vo.setHandleBy(e.getHandleBy());
        vo.setHandleTime(e.getHandleTime());
        vo.setHandleRemark(e.getHandleRemark());
        return vo;
    }

    private static String truncate(String s, int max) {
        if (s == null) {
            return null;
        }
        return s.length() <= max ? s : s.substring(0, max);
    }
}

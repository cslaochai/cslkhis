package com.his.iot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.his.common.entity.CurrentUser;
import com.his.common.enums.RobotDispatchPriorityEnum;
import com.his.common.enums.RobotDispatchSourceEnum;
import com.his.common.enums.RobotDispatchStatusEnum;
import com.his.common.enums.RobotGoodsTypeEnum;
import com.his.common.enums.RobotStationTypeEnum;
import com.his.common.exception.BusinessException;
import com.his.common.util.TextUtil;
import com.his.common.utils.UserUtils;
import com.his.iot.dto.IotLatestQueryDTO;
import com.his.iot.dto.RobotDispatchAssignDTO;
import com.his.iot.dto.RobotDispatchCancelDTO;
import com.his.iot.dto.RobotDispatchCreateDTO;
import com.his.iot.dto.RobotDispatchPageQueryDTO;
import com.his.iot.entity.IotDevice;
import com.his.iot.entity.IotRobotDispatchOrder;
import com.his.iot.entity.RobotStation;
import com.his.iot.mapper.IotDeviceMapper;
import com.his.iot.mapper.IotRobotDispatchOrderMapper;
import com.his.iot.service.IotDeviceService;
import com.his.iot.service.IotTelemetryService;
import com.his.iot.service.RobotDispatchService;
import com.his.iot.service.RobotStationService;
import com.his.iot.support.IotMqttPublisher;
import com.his.iot.support.RobotDispatchConst;
import com.his.iot.vo.IotDeviceVO;
import com.his.iot.vo.IotLatestVO;
import com.his.iot.vo.IotMetricVO;
import com.his.iot.vo.RobotDispatchOrderVO;
import com.his.iot.vo.RobotFleetVO;
import com.his.spi.service.RedisSequenceService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 物流机器人调度实现。
 * <p>
 * 车队全院共享：任务单由各站点/设备科发起，调度按空闲状态与电量选车，指令经 MQTT 下行到机器人；
 * 执行过程事件（TASK_START / TASK_COMPLETE 等）回写单据状态与行程回执。
 */
@Service
@RequiredArgsConstructor
public class RobotDispatchServiceImpl extends ServiceImpl<IotRobotDispatchOrderMapper, IotRobotDispatchOrder>
        implements RobotDispatchService {

    private static final Logger log = LoggerFactory.getLogger(RobotDispatchServiceImpl.class);
    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("yyyyMMdd");

    /** 可接新单的遥测状态：只有原地空闲的车派单；配送/装车/乘梯/签收/返程/充电/暂停/告警都不派。 */
    private static final Set<String> DISPATCHABLE_STATES = Set.of("IDLE");
    /** 派单最低电量(%)：低于此值车要回桩补能，不接新单。 */
    private static final double MIN_DISPATCH_BATTERY = 30d;
    /** 楼层文本解析：B1 → -1，2F → 2，12F → 12。存量单据没有站点ID时兜底用。 */
    private static final Pattern FLOOR_PATTERN = Pattern.compile("^(B?)(\\d+)");

    private final IotDeviceMapper iotDeviceMapper;
    private final IotDeviceService iotDeviceService;
    private final IotTelemetryService iotTelemetryService;
    private final IotMqttPublisher publisher;
    private final RedisSequenceService redisSequenceService;
    private final RobotStationService stationService;
    private final ObjectMapper json = new ObjectMapper();

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RobotDispatchOrderVO create(RobotDispatchCreateDTO dto) {
        CurrentUser user = requireUser();

        // 站点主数据是唯一事实：名称、楼栋楼层、来源都由站点带出，下单人不再手填
        RobotStation apply = stationService.requireEnabled(dto.getApplyStationId());
        RobotStation from = stationService.requireStation(dto.getFromStationId(), true);
        RobotStation to = stationService.requireStation(dto.getToStationId(), false);
        if (Objects.equals(from.getId(), to.getId())) {
            throw new BusinessException(400, "取货点与收货点不能是同一站点");
        }

        // 重量 = 品类单箱基准重量 × 箱数（品类口径见 RobotGoodsTypeEnum，不接受手填总重）
        Double unitKg = RobotGoodsTypeEnum.unitWeightOf(dto.getGoodsType());
        if (unitKg == null) {
            throw new BusinessException(400, "物品品类不合法：" + dto.getGoodsType());
        }
        int boxes = dto.getBoxCount();
        int maxBox = (int) Math.floor(RobotDispatchConst.NOMINAL_PAYLOAD_KG / unitKg);
        if (boxes > maxBox) {
            throw new BusinessException(400, dto.getGoodsType() + "单趟最多 " + maxBox
                    + " 箱（单车额定 " + (int) RobotDispatchConst.NOMINAL_PAYLOAD_KG + "kg）");
        }

        IotRobotDispatchOrder order = new IotRobotDispatchOrder();
        order.setOrderNo(nextOrderNo());
        order.setSourceType(RobotStationTypeEnum.sourceTypeOf(apply.getStationType()));
        order.setSourceDeptName(apply.getStationName());
        order.setApplyStationId(apply.getId());
        order.setRequesterId(user.getEmployeeId());
        order.setRequesterName(TextUtil.hasText(user.getRealName()) ? user.getRealName() : user.getUsername());
        order.setGoodsType(dto.getGoodsType());
        order.setGoodsName(TextUtil.cut(dto.getGoodsName(), 200));
        order.setFromStationId(from.getId());
        order.setFromStation(from.getStationName());
        order.setToStationId(to.getId());
        order.setToStation(to.getStationName());
        order.setToFloor(to.getFloorNo());
        order.setPriority(dto.getPriority());
        order.setBoxCount(boxes);
        order.setLoadKg(round1(unitKg * boxes));
        order.setStatus(RobotDispatchStatusEnum.PENDING_DISPATCH.getCode());
        order.setRemark(TextUtil.cut(dto.getRemark(), 500));
        baseMapper.insert(order);

        if (Boolean.TRUE.equals(dto.getAutoAssign())) {
            try {
                RobotDispatchAssignDTO assign = new RobotDispatchAssignDTO();
                assign.setId(order.getId());
                assign.setRobotNo(dto.getRobotNo());
                assign(assign);
            } catch (BusinessException e) {
                // 无空闲车 / 下行通道不可用：单据保留在待调度队列，不丢单，稍后可在列表里手工派车
                log.warn("任务单 {} 自动派车未成功，留在待调度队列：{}", order.getOrderNo(), e.getMessage());
            }
        }
        return toVo(baseMapper.selectById(order.getId()), robotNameMap());
    }

    @Override
    public IPage<RobotDispatchOrderVO> listPage(RobotDispatchPageQueryDTO query) {
        String kw = TextUtil.trim(query.getKeyword());
        LambdaQueryWrapper<IotRobotDispatchOrder> w = new LambdaQueryWrapper<IotRobotDispatchOrder>()
                .eq(query.getStatus() != null, IotRobotDispatchOrder::getStatus, query.getStatus())
                .eq(query.getPriority() != null, IotRobotDispatchOrder::getPriority, query.getPriority())
                .eq(TextUtil.hasText(query.getSourceType()), IotRobotDispatchOrder::getSourceType, query.getSourceType())
                .eq(TextUtil.hasText(query.getRobotNo()), IotRobotDispatchOrder::getRobotNo, query.getRobotNo())
                .and(TextUtil.hasText(kw), x -> x
                        .like(IotRobotDispatchOrder::getOrderNo, kw)
                        .or().like(IotRobotDispatchOrder::getGoodsName, kw)
                        .or().like(IotRobotDispatchOrder::getFromStation, kw)
                        .or().like(IotRobotDispatchOrder::getToStation, kw)
                        .or().like(IotRobotDispatchOrder::getRequesterName, kw))
                .orderByDesc(IotRobotDispatchOrder::getPriority)
                .orderByDesc(IotRobotDispatchOrder::getCreateTime)
                .orderByDesc(IotRobotDispatchOrder::getId);
        Page<IotRobotDispatchOrder> page = baseMapper.selectPage(
                new Page<>(query.getPageNum(), query.getPageSize()), w);
        Map<String, String> robotNames = robotNameMap();
        Page<RobotDispatchOrderVO> result = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        result.setRecords(page.getRecords().stream().map(o -> toVo(o, robotNames)).toList());
        return result;
    }

    @Override
    public RobotDispatchOrderVO getDetail(Long id) {
        return toVo(requireOrder(id), robotNameMap());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assign(RobotDispatchAssignDTO dto) {
        CurrentUser user = requireUser();
        doAssign(dto.getId(), dto.getRobotNo(), user.getRealName(), user.getEmployeeId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignAs(Long orderId, String robotNo, String operatorName, Long operatorId) {
        doAssign(orderId, robotNo, operatorName, operatorId);
    }

    /**
     * 派车主体：人工派车与队列补派共用一条路径（选车 → 锁车 → 复核在途 → 下发 → 改单）。
     * <p>
     * operator 由调用方显式给出：手工派车取当前登录人，补派没有登录态，由任务传 {@code system:robotDispatch}。
     * 不在这里回落"未知操作人"——取不到就是传错了，宁可报错。
     */
    private void doAssign(Long orderId, String requestedRobotNo, String operatorName, Long operatorId) {
        IotRobotDispatchOrder order = requireOrder(orderId);
        if (!Objects.equals(RobotDispatchStatusEnum.PENDING_DISPATCH.getCode(), order.getStatus())) {
            throw new BusinessException(400, "当前状态不可派车：" + RobotDispatchStatusEnum.getLabelOrUnknown(order.getStatus()));
        }

        String robotNo = TextUtil.hasText(requestedRobotNo) ? requestedRobotNo : pickFreeRobot(order);
        // 锁车行 + 复核：选车到锁车之间可能被另一张单抢先派走，锁住之后再确认一次才算数
        lockRobot(robotNo);
        assertRobotIdle(robotNo);

        ObjectNode payload = buildPayload(order);
        boolean sent = publisher.publishCommand("robot", robotNo, "ASSIGN_TASK", order.getOrderNo(), payload);
        if (!sent) {
            throw new BusinessException(503, "指令下发通道不可用，派车未生效；请确认 MQTT broker 后重新派车");
        }

        order.setRobotNo(robotNo);
        order.setStatus(RobotDispatchStatusEnum.DISPATCHED.getCode());
        order.setDispatchBy(operatorName);
        order.setDispatchById(operatorId);
        order.setDispatchTime(LocalDateTime.now());
        baseMapper.updateById(order);
    }

    @Override
    public List<Long> pendingOrderIds(int limit) {
        int cap = Math.max(1, Math.min(limit, 50));
        return baseMapper.selectList(new LambdaQueryWrapper<IotRobotDispatchOrder>()
                        .eq(IotRobotDispatchOrder::getStatus, RobotDispatchStatusEnum.PENDING_DISPATCH.getCode())
                        .orderByDesc(IotRobotDispatchOrder::getPriority)
                        .orderByAsc(IotRobotDispatchOrder::getCreateTime)
                        .orderByAsc(IotRobotDispatchOrder::getId)
                        .last("LIMIT " + cap))
                .stream().map(IotRobotDispatchOrder::getId).toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancel(RobotDispatchCancelDTO dto) {
        IotRobotDispatchOrder order = requireOrder(dto.getId());
        if (!List.<Integer>of(RobotDispatchStatusEnum.PENDING_DISPATCH.getCode(),
                RobotDispatchStatusEnum.DISPATCHED.getCode()).contains(order.getStatus())) {
            throw new BusinessException(400, "当前状态不可取消：" + RobotDispatchStatusEnum.getLabelOrUnknown(order.getStatus()));
        }
        if (Objects.equals(RobotDispatchStatusEnum.DISPATCHED.getCode(), order.getStatus())
                && TextUtil.hasText(order.getRobotNo())) {
            publisher.publishCommand("robot", order.getRobotNo(), "CANCEL_TASK", order.getOrderNo(), null);
        }
        order.setStatus(RobotDispatchStatusEnum.CANCELLED.getCode());
        order.setCancelReason(TextUtil.cut(dto.getCancelReason(), 500));
        baseMapper.updateById(order);
    }

    @Override
    public List<RobotFleetVO> fleet() {
        // 车队全院共享：走不收口的清单，受限岗位也要看到整队
        List<IotDeviceVO> roster = iotDeviceService.listSharedFleet("robot");

        IotLatestQueryDTO latestQuery = new IotLatestQueryDTO();
        latestQuery.setDeviceType("robot");
        Map<String, IotLatestVO> latestByNo = iotTelemetryService.latestList(latestQuery).stream()
                .collect(Collectors.toMap(IotLatestVO::getDeviceNo, v -> v, (a, b) -> a));

        // 活跃任务（已派单/配送中）挂到对应车上
        List<IotRobotDispatchOrder> activeOrders = baseMapper.selectList(
                new LambdaQueryWrapper<IotRobotDispatchOrder>()
                        .in(IotRobotDispatchOrder::getStatus,
                                List.of(RobotDispatchStatusEnum.DISPATCHED.getCode(),
                                        RobotDispatchStatusEnum.DELIVERING.getCode())));
        Map<String, IotRobotDispatchOrder> activeByRobot = activeOrders.stream()
                .collect(Collectors.toMap(IotRobotDispatchOrder::getRobotNo, o -> o, (a, b) -> a));

        List<RobotFleetVO> fleet = new ArrayList<>();
        for (IotDeviceVO device : roster) {
            RobotFleetVO vo = new RobotFleetVO();
            vo.setRobotNo(device.getDeviceNo());
            vo.setDeviceName(device.getDeviceName());
            vo.setLocation(device.getLocation());
            IotLatestVO latest = latestByNo.get(device.getDeviceNo());
            if (latest != null) {
                vo.setTs(latest.getTs());
                vo.setOnline(latest.getTs() != null);
                vo.setBattery(metricNumber(latest, "battery"));
                vo.setLoadKg(metricNumber(latest, "loadKg"));
                Double boxCount = metricNumber(latest, "boxCount");
                vo.setBoxCount(boxCount == null ? null : boxCount.intValue());
                vo.setLocationText(metricText(latest, "location"));
                String state = metricText(latest, "state");
                vo.setState(state);
                vo.setStateLabel(stateLabel(state));
            } else {
                vo.setOnline(false);
                vo.setState("OFFLINE");
                vo.setStateLabel("离线");
            }
            IotRobotDispatchOrder active = activeByRobot.get(device.getDeviceNo());
            if (active != null) {
                vo.setCurrentOrderNo(active.getOrderNo());
                vo.setCurrentGoods(active.getGoodsName());
                vo.setCurrentToStation(active.getToStation());
                vo.setCurrentPriority(active.getPriority());
            }
            fleet.add(vo);
        }
        fleet.sort(Comparator.comparing(RobotFleetVO::getRobotNo));
        return fleet;
    }

    @Override
    public void syncFromEvent(String eventCode, String orderNo, JsonNode data, String deviceNo, LocalDateTime ts) {
        if (!TextUtil.hasText(orderNo)) {
            return;
        }
        IotRobotDispatchOrder order = baseMapper.selectOne(
                new LambdaQueryWrapper<IotRobotDispatchOrder>()
                        .eq(IotRobotDispatchOrder::getOrderNo, orderNo)
                        .last("LIMIT 1"));
        if (order == null) {
            return;
        }
        switch (eventCode) {
            case "TASK_START" -> {
                if (Objects.equals(RobotDispatchStatusEnum.PENDING_DISPATCH.getCode(), order.getStatus())
                        || Objects.equals(RobotDispatchStatusEnum.DISPATCHED.getCode(), order.getStatus())) {
                    order.setStatus(RobotDispatchStatusEnum.DELIVERING.getCode());
                    if (!TextUtil.hasText(order.getRobotNo()) && TextUtil.hasText(deviceNo)) {
                        order.setRobotNo(deviceNo);
                    }
                    baseMapper.updateById(order);
                }
            }
            case "TASK_COMPLETE" -> {
                order.setStatus(RobotDispatchStatusEnum.COMPLETED.getCode());
                order.setCompleteTime(ts);
                if (data != null) {
                    order.setSigner(text(data, "signer"));
                    order.setTotalSec(intOf(data, "totalSec"));
                    order.setElevatorSec(intOf(data, "elevatorSec"));
                    order.setQueueSec(intOf(data, "queueSec"));
                    order.setRideSec(intOf(data, "rideSec"));
                    Double meters = doubleOf(data, "odometerM");
                    order.setOdometerM(meters);
                }
                baseMapper.updateById(order);
            }
            default -> {
                // ELEVATOR_WAIT / CARGO_LOADED 等过程事件不改单据状态，只在事件中心留痕
            }
        }
    }

    // ================= 内部 =================

    /**
     * 自动派车选车：先过硬约束（无活跃单、状态可接单、无告警、电量达标、载重装得下），
     * 再按「取货楼层就近 + 电量余量 + 载重余量」加权打分，取最高分；同分按编号兜底。
     * <p>
     * 对标主流 AGV 车队调度：在途车不重复派单，低电/告警车退出本轮，就近空车优先空驶到取货点，
     * 装不下的大单不硬塞。所有候选都不满足时单据留在待调度队列，不丢单。
     */
    private String pickFreeRobot(IotRobotDispatchOrder order) {
        Set<String> busyRobots = baseMapper.selectList(
                        new LambdaQueryWrapper<IotRobotDispatchOrder>()
                                .in(IotRobotDispatchOrder::getStatus,
                                        List.of(RobotDispatchStatusEnum.DISPATCHED.getCode(),
                                                RobotDispatchStatusEnum.DELIVERING.getCode()))
                                .isNotNull(IotRobotDispatchOrder::getRobotNo))
                .stream().map(IotRobotDispatchOrder::getRobotNo).collect(Collectors.toSet());

        List<IotDeviceVO> roster = iotDeviceService.listSharedFleet("robot");

        IotLatestQueryDTO latestQuery = new IotLatestQueryDTO();
        latestQuery.setDeviceType("robot");
        Map<String, IotLatestVO> latestByNo = iotTelemetryService.latestList(latestQuery).stream()
                .collect(Collectors.toMap(IotLatestVO::getDeviceNo, v -> v, (a, b) -> a));

        int fromFloor = fromFloorOf(order);
        double needKg = order.getLoadKg() == null ? 0d : order.getLoadKg();

        List<Candidate> candidates = new ArrayList<>();
        Map<String, Integer> rejectReasons = new HashMap<>();

        for (IotDeviceVO robot : roster) {
            String no = robot.getDeviceNo();
            if (busyRobots.contains(no)) {
                rejectReasons.merge("执行中", 1, Integer::sum);
                continue;
            }
            IotLatestVO latest = latestByNo.get(no);
            String state = latest == null ? null : metricText(latest, "state");
            Double battery = latest == null ? null : metricNumber(latest, "battery");
            Double onboardKg = latest == null ? null : metricNumber(latest, "loadKg");
            String location = latest == null ? null : metricText(latest, "location");
            Integer alarm = latest == null ? null : latest.getAlarmLevel();

            if (state != null && !DISPATCHABLE_STATES.contains(state)) {
                rejectReasons.merge(rejectStateReason(state), 1, Integer::sum);
                continue;
            }
            if (alarm != null && alarm >= 2) {
                rejectReasons.merge("告警", 1, Integer::sum);
                continue;
            }
            if (battery != null && battery < MIN_DISPATCH_BATTERY) {
                rejectReasons.merge("低电", 1, Integer::sum);
                continue;
            }
            double freeKg = RobotDispatchConst.NOMINAL_PAYLOAD_KG - (onboardKg == null ? 0d : onboardKg);
            if (freeKg < needKg) {
                rejectReasons.merge("超载", 1, Integer::sum);
                continue;
            }
            candidates.add(new Candidate(robot, dispatchScore(fromFloor, floorOf(location), battery, freeKg, needKg)));
        }

        if (candidates.isEmpty()) {
            throw new BusinessException(400, "当前没有可派机器人：" + summarize(rejectReasons)
                    + "；任务单留在待调度队列，稍后再派车");
        }

        candidates.sort(Comparator.comparingDouble(Candidate::score).reversed()
                .thenComparing(c -> c.robot().getDeviceNo()));
        return candidates.get(0).robot().getDeviceNo();
    }

    /** 候选车：台账 + 调度评分。 */
    private record Candidate(IotDeviceVO robot, double score) {
    }

    /**
     * 调度评分（满分 100）：就近 40 + 电量 40 + 载重余量 20。
     * 楼层或电量取不到时给中性分，不因缺一次遥测就把车淘汰；载重余量按本单重量折算。
     */
    private double dispatchScore(int fromFloor, int robotFloor, Double battery, double freeKg, double needKg) {
        double score;
        if (fromFloor != Integer.MIN_VALUE && robotFloor != Integer.MIN_VALUE) {
            int diff = Math.abs(fromFloor - robotFloor);
            score = diff == 0 ? 40 : diff == 1 ? 28 : diff <= 3 ? 16 : 8;
        } else {
            score = 20;
        }
        score += battery == null ? 20 : battery / 100d * 40d;
        double capacityRatio = needKg <= 0 ? 1d : Math.max(0d, Math.min(1d, freeKg / needKg));
        score += capacityRatio * 20d;
        return score;
    }

    /**
     * 取货楼层：有站点ID就直接读站点 floor_no（站点是唯一事实），
     * 存量单据没有站点ID时才退回从文本抠楼层。
     */
    private int fromFloorOf(IotRobotDispatchOrder order) {
        if (order.getFromStationId() != null) {
            RobotStation station = stationService.findById(order.getFromStationId());
            if (station != null && station.getFloorNo() != null) {
                return station.getFloorNo();
            }
        }
        return floorOf(order.getFromStation());
    }

    /** 从站点/位置文本解析楼层：B1 → -1，2F → 2，12F → 12；解析不出返回 MIN_VALUE。 */
    private static int floorOf(String station) {
        if (!TextUtil.hasText(station)) {
            return Integer.MIN_VALUE;
        }
        Matcher m = FLOOR_PATTERN.matcher(station.trim());
        if (!m.find()) {
            return Integer.MIN_VALUE;
        }
        int n = Integer.parseInt(m.group(2));
        return "B".equals(m.group(1)) ? -n : n;
    }

    private static String rejectStateReason(String state) {
        return switch (state) {
            case "CHARGING" -> "充电中";
            case "PAUSED" -> "已暂停";
            case "RETURNING" -> "返程中";
            case "ALARM" -> "告警";
            case "LOADING", "DELIVERING", "WAITING_ELEVATOR", "SIGNING" -> "执行中";
            default -> "忙碌";
        };
    }

    private static String summarize(Map<String, Integer> reasons) {
        if (reasons.isEmpty()) {
            return "车队暂不可用";
        }
        return reasons.entrySet().stream()
                .map(e -> e.getValue() + " 台" + e.getKey())
                .collect(Collectors.joining("、"));
    }

    /**
     * 锁住车号对应的机器人台账行（FOR UPDATE），同时校验存在与启用。
     * <p>
     * 必须在本方法所属事务内调用：行锁到事务提交才释放，用来把"同车并发派单"串行化。
     */
    private IotDevice lockRobot(String robotNo) {
        if (!TextUtil.hasText(robotNo)) {
            throw new BusinessException(400, "未指定机器人");
        }
        IotDevice robot = iotDeviceMapper.selectRobotForUpdate(robotNo);
        if (robot == null || !Objects.equals(robot.getEnabled(), 1)) {
            throw new BusinessException(400, "机器人不存在或已停用：" + robotNo);
        }
        return robot;
    }

    /**
     * 锁车之后复核在途单：并发派车时前一张单可能刚提交（DISPATCHED/DELIVERING），
     * 此时本单必须放弃，换车或留在队列，不能把同一台车派两次。
     */
    private void assertRobotIdle(String robotNo) {
        long active = baseMapper.selectCount(new LambdaQueryWrapper<IotRobotDispatchOrder>()
                .eq(IotRobotDispatchOrder::getRobotNo, robotNo)
                .in(IotRobotDispatchOrder::getStatus,
                        List.of(RobotDispatchStatusEnum.DISPATCHED.getCode(),
                                RobotDispatchStatusEnum.DELIVERING.getCode())));
        if (active > 0) {
            throw new BusinessException(400, "机器人已被其他任务占用：" + robotNo + "；任务单留在待调度队列");
        }
    }

    private ObjectNode buildPayload(IotRobotDispatchOrder order) {
        ObjectNode p = json.createObjectNode();
        p.put("orderNo", order.getOrderNo());
        p.put("sourceType", order.getSourceType());
        p.put("sourceDeptName", order.getSourceDeptName());
        p.put("goodsType", order.getGoodsType());
        p.put("goodsName", order.getGoodsName());
        p.put("fromStation", order.getFromStation());
        p.put("toFloor", order.getToFloor());
        p.put("toStation", order.getToStation());
        p.put("priority", order.getPriority());
        p.put("boxCount", order.getBoxCount());
        p.put("loadKg", order.getLoadKg());
        // 真机认停靠点位码，不认站点名：站点维护里对过码就下发，车辆据此精确定位停靠
        RobotStation from = stationService.findById(order.getFromStationId());
        if (from != null && TextUtil.hasText(from.getDockCode())) {
            p.put("fromDockCode", from.getDockCode());
        }
        RobotStation to = stationService.findById(order.getToStationId());
        if (to != null && TextUtil.hasText(to.getDockCode())) {
            p.put("toDockCode", to.getDockCode());
        }
        if (TextUtil.hasText(order.getRequesterName())) {
            p.put("requesterName", order.getRequesterName());
        }
        return p;
    }

    private Map<String, String> robotNameMap() {
        return iotDeviceService.listSharedFleet("robot").stream()
                .collect(Collectors.toMap(IotDeviceVO::getDeviceNo, IotDeviceVO::getDeviceName, (a, b) -> a));
    }

    private RobotDispatchOrderVO toVo(IotRobotDispatchOrder order, Map<String, String> robotNames) {
        RobotDispatchOrderVO vo = new RobotDispatchOrderVO();
        vo.setId(order.getId());
        vo.setOrderNo(order.getOrderNo());
        vo.setSourceType(order.getSourceType());
        vo.setSourceTypeLabel(RobotDispatchSourceEnum.getLabel(order.getSourceType()));
        vo.setSourceDeptName(order.getSourceDeptName());
        vo.setApplyStationId(order.getApplyStationId());
        vo.setRequesterId(order.getRequesterId());
        vo.setRequesterName(order.getRequesterName());
        vo.setGoodsType(order.getGoodsType());
        vo.setGoodsName(order.getGoodsName());
        vo.setFromStation(order.getFromStation());
        vo.setFromStationId(order.getFromStationId());
        vo.setToFloor(order.getToFloor());
        vo.setToStation(order.getToStation());
        vo.setToStationId(order.getToStationId());
        vo.setPriority(order.getPriority());
        vo.setPriorityLabel(RobotDispatchPriorityEnum.getLabel(order.getPriority()));
        vo.setBoxCount(order.getBoxCount());
        vo.setLoadKg(order.getLoadKg());
        vo.setStatus(order.getStatus());
        vo.setStatusLabel(RobotDispatchStatusEnum.getLabelOrUnknown(order.getStatus()));
        vo.setRobotNo(order.getRobotNo());
        vo.setRobotName(order.getRobotNo() == null ? null : robotNames.get(order.getRobotNo()));
        vo.setSigner(order.getSigner());
        vo.setDispatchBy(order.getDispatchBy());
        vo.setDispatchTime(order.getDispatchTime());
        vo.setCompleteTime(order.getCompleteTime());
        vo.setTotalSec(order.getTotalSec());
        vo.setElevatorSec(order.getElevatorSec());
        vo.setQueueSec(order.getQueueSec());
        vo.setRideSec(order.getRideSec());
        vo.setOdometerM(order.getOdometerM());
        vo.setCancelReason(order.getCancelReason());
        vo.setRemark(order.getRemark());
        vo.setCreateTime(order.getCreateTime());
        return vo;
    }

    private IotRobotDispatchOrder requireOrder(Long id) {
        IotRobotDispatchOrder order = baseMapper.selectById(id);
        if (order == null) {
            throw new BusinessException(400, "任务单不存在：" + id);
        }
        return order;
    }

    private CurrentUser requireUser() {
        CurrentUser user = UserUtils.getCurrentUser();
        if (user == null) {
            throw new BusinessException(401, "未获取到登录用户");
        }
        return user;
    }

    private String nextOrderNo() {
        long seq = redisSequenceService.next("ROBOT_DISPATCH");
        return "RB" + LocalDate.now().format(DAY) + String.format("%05d", seq % 100000);
    }

    /** 载重留一位小数：3.6kg × 3 箱 = 10.8kg，不出现 10.799999 这种浮点尾巴。 */
    private static double round1(double v) {
        return Math.round(v * 10.0) / 10.0;
    }

    private static String stateLabel(String state) {
        if (state == null) {
            return "离线";
        }
        return switch (state) {
            case "IDLE" -> "空闲";
            case "DELIVERING", "LOADING", "SIGNING", "WAITING_ELEVATOR", "RETURNING" -> "配送中";
            case "CHARGING" -> "充电中";
            case "PAUSED" -> "已暂停";
            case "ALARM" -> "告警";
            default -> state;
        };
    }

    private static Double metricNumber(IotLatestVO latest, String key) {
        IotMetricVO m = latest.getMetrics() == null ? null
                : latest.getMetrics().stream().filter(x -> key.equals(x.getKey())).findFirst().orElse(null);
        return m == null ? null : m.getNumeric();
    }

    private static String metricText(IotLatestVO latest, String key) {
        IotMetricVO m = latest.getMetrics() == null ? null
                : latest.getMetrics().stream().filter(x -> key.equals(x.getKey())).findFirst().orElse(null);
        return m == null ? null : m.getValue();
    }

    private static String text(JsonNode node, String field) {
        JsonNode v = node == null ? null : node.path(field);
        return v == null || v.isMissingNode() || v.isNull() ? null : v.asText();
    }

    private static Integer intOf(JsonNode node, String field) {
        JsonNode v = node == null ? null : node.path(field);
        return v == null || v.isMissingNode() || v.isNull() ? null : v.asInt();
    }

    private static Double doubleOf(JsonNode node, String field) {
        JsonNode v = node == null ? null : node.path(field);
        return v == null || v.isMissingNode() || v.isNull() ? null : v.asDouble();
    }
}

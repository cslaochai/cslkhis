package com.his.medicaltech.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.his.common.base.PageResult;
import com.his.common.enums.DelFlagEnum;
import com.his.common.enums.YesOrNoEnum;
import com.his.common.exception.BusinessException;
import com.his.common.service.RedisSequenceService;
import com.his.common.util.DateFormats;
import com.his.medicaltech.dto.InfusionRoomDTO;
import com.his.medicaltech.entity.BizInfusionSeat;
import com.his.medicaltech.entity.BizOutpInfusion;
import com.his.medicaltech.entity.BizOutpInfusionRound;
import com.his.medicaltech.entity.BizSkinTest;
import com.his.medicaltech.enums.InfusionSeatStatusEnum;
import com.his.medicaltech.enums.InfusionStatusEnum;
import com.his.medicaltech.enums.SkinTestResultEnum;
import com.his.medicaltech.mapper.BizInfusionSeatMapper;
import com.his.medicaltech.mapper.BizOutpInfusionMapper;
import com.his.medicaltech.mapper.BizOutpInfusionRoundMapper;
import com.his.medicaltech.mapper.BizSkinTestMapper;
import com.his.medicaltech.service.InfusionRoomService;
import com.his.medicaltech.vo.InfusionRoomVO;
import com.his.system.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 门诊输液室服务（M10）。
 *
 * <p>流程：入座（建单+占座）→【需皮试：打皮试 → ≥15 分钟观察窗 → 判读】→ 开始（滴速）
 * → N 次巡视 → 结束（不良反应）。结束/取消释放座位。
 * 座位是全院物理资源，不收科室数据权限（与 M6 床位口径一致）。
 */
@Service
@RequiredArgsConstructor
public class InfusionRoomServiceImpl implements InfusionRoomService {

    /**
     * 皮试判读最小观察窗（分钟）
     */
    private static final long SKIN_TEST_OBSERVE_MINUTES = 15;

    private final BizInfusionSeatMapper seatMapper;
    private final BizOutpInfusionMapper infusionMapper;
    private final BizOutpInfusionRoundMapper roundMapper;
    private final BizSkinTestMapper skinTestMapper;
    private final RedisSequenceService redisSequenceService;

    // 座位

    /**
     * 座位图（含占用输液单摘要）
     */
    public List<InfusionRoomVO.Seat> seats() {
        List<BizInfusionSeat> seats = seatMapper.selectList(new LambdaQueryWrapper<BizInfusionSeat>()
                .orderByAsc(BizInfusionSeat::getArea).orderByAsc(BizInfusionSeat::getSeatNo));
        List<Long> seatIds = seats.stream().map(BizInfusionSeat::getId).toList();
        Map<Long, BizOutpInfusion> occupying = seatIds.isEmpty() ? Map.of()
                : infusionMapper.selectList(new LambdaQueryWrapper<BizOutpInfusion>()
                        .in(BizOutpInfusion::getSeatId, seatIds)
                        .in(BizOutpInfusion::getStatus,
                                InfusionStatusEnum.PENDING_TEST.getCode(),
                                InfusionStatusEnum.WAITING.getCode(),
                                InfusionStatusEnum.INFUSING.getCode())
                        .eq(BizOutpInfusion::getDelFlag, DelFlagEnum.NORMAL.getCode()))
                .stream().collect(Collectors.toMap(BizOutpInfusion::getSeatId, i -> i, (a, b) -> a));
        return seats.stream().map(s -> {
            InfusionRoomVO.Seat vo = new InfusionRoomVO.Seat();
            vo.setId(s.getId());
            vo.setSeatNo(s.getSeatNo());
            vo.setArea(s.getArea());
            vo.setSeatStatus(s.getSeatStatus());
            vo.setRemark(s.getRemark());
            BizOutpInfusion inf = occupying.get(s.getId());
            if (inf != null) {
                vo.setInfusionId(inf.getId());
                vo.setPatientName(inf.getPatientName());
                vo.setInfusionStatus(inf.getStatus());
            }
            return vo;
        }).toList();
    }

    /**
     * 座位新增/修改（座位号唯一；占用中的座位不允许改状态）
     */
    @Transactional(rollbackFor = Exception.class)
    public InfusionRoomVO.Seat seatUpsert(InfusionRoomDTO.SeatUpsert dto) {
        if (dto.getSeatStatus() != null && dto.getSeatStatus() == InfusionSeatStatusEnum.OCCUPIED.getCode()) {
            throw new BusinessException("座位占用由入座流程驱动，不能直接置为占用");
        }
        BizInfusionSeat seat;
        if (dto.getId() != null) {
            seat = seatMapper.selectById(dto.getId());
            if (seat == null || seat.getDelFlag() == DelFlagEnum.DELETED.getCode()) {
                throw new BusinessException("座位不存在");
            }
            if (seat.getSeatStatus() == InfusionSeatStatusEnum.OCCUPIED.getCode()) {
                throw new BusinessException("座位占用中，请先结束/取消该输液单");
            }
            seat.setSeatStatus(dto.getSeatStatus());
        } else {
            seat = new BizInfusionSeat();
            seat.setSeatStatus(dto.getSeatStatus());
        }
        Long dupId = seatMapper.selectCount(new LambdaQueryWrapper<BizInfusionSeat>()
                .eq(BizInfusionSeat::getSeatNo, dto.getSeatNo())
                .ne(dto.getId() != null, BizInfusionSeat::getId, dto.getId())
                .eq(BizInfusionSeat::getDelFlag, DelFlagEnum.NORMAL.getCode()));
        if (dupId > 0) {
            throw new BusinessException("座位号已存在（含软删行占用的唯一键，请换号）");
        }
        seat.setSeatNo(dto.getSeatNo());
        seat.setArea(dto.getArea());
        seat.setRemark(dto.getRemark());
        seat.setUpdateBy(UserUtils.getCurrentUser().getRealName());
        if (dto.getId() != null) {
            seatMapper.updateById(seat);
        } else {
            seat.setCreateBy(UserUtils.getCurrentUser().getRealName());
            seatMapper.insert(seat);
        }
        InfusionRoomVO.Seat vo = new InfusionRoomVO.Seat();
        vo.setId(seat.getId());
        vo.setSeatNo(seat.getSeatNo());
        vo.setArea(seat.getArea());
        vo.setSeatStatus(seat.getSeatStatus());
        vo.setRemark(seat.getRemark());
        return vo;
    }

    // 入座

    /**
     * 入座：建输液单 + 占座。needSkinTest=1 → 待皮试；否则待输注。
     */
    @Transactional(rollbackFor = Exception.class)
    public InfusionRoomVO.Infusion admit(InfusionRoomDTO.Admit dto) {
        BizInfusionSeat seat = seatMapper.selectById(dto.getSeatId());
        if (seat == null || seat.getDelFlag() == DelFlagEnum.DELETED.getCode()) {
            throw new BusinessException("座位不存在");
        }
        if (seat.getSeatStatus() == InfusionSeatStatusEnum.DISABLED.getCode()) {
            throw new BusinessException("座位已停用");
        }
        if (seat.getSeatStatus() == InfusionSeatStatusEnum.OCCUPIED.getCode()) {
            throw new BusinessException("座位已占用（座位号 " + seat.getSeatNo() + "）");
        }
        BizOutpInfusion inf = new BizOutpInfusion();
        inf.setInfusionNo(nextNo("TZ"));
        inf.setTreatmentRecordId(dto.getTreatmentRecordId());
        inf.setPatientId(dto.getPatientId());
        fillPatientSnapshot(inf);
        inf.setDrugSummary(dto.getDrugSummary());
        inf.setSeatId(seat.getId());
        inf.setSeatNo(seat.getSeatNo());
        inf.setStatus(Boolean.TRUE.equals(needTest(dto.getNeedSkinTest()))
                ? InfusionStatusEnum.PENDING_TEST.getCode() : InfusionStatusEnum.WAITING.getCode());
        inf.setAdverseFlag(YesOrNoEnum.NO.getCode());
        inf.setNurseId(UserUtils.getCurrentUser().getEmployeeId());
        inf.setNurseName(UserUtils.getCurrentUser().getRealName());
        inf.setRemark(dto.getRemark());
        inf.setCreateBy(inf.getNurseName());
        infusionMapper.insert(inf);

        seat.setSeatStatus(InfusionSeatStatusEnum.OCCUPIED.getCode());
        seat.setUpdateBy(inf.getNurseName());
        seatMapper.updateById(seat);
        return toInfusionVO(inf);
    }

    private boolean needTest(Integer flag) {
        return flag != null && flag == YesOrNoEnum.YES.getCode();
    }

    /**
     * 患者快照（服务端重查，不信任前端）。
     */
    private void fillPatientSnapshot(BizOutpInfusion inf) {
        Map<String, Object> row = infusionMapper.selectPatientSnapshot(inf.getPatientId());
        if (row == null) {
            throw new BusinessException("患者不存在（ID " + inf.getPatientId() + "）");
        }
        inf.setPatientNo((String) row.get("patientNo"));
        inf.setPatientName((String) row.get("patientName"));
        Object gender = row.get("gender");
        inf.setGender(gender == null ? null : Integer.valueOf(String.valueOf(gender)));
        Object age = row.get("age");
        inf.setAge(age == null ? null : Integer.valueOf(String.valueOf(age)));
    }

    // 皮试

    /**
     * 打皮试：输液单必须处于待皮试；一张输液单一张皮试单。
     */
    @Transactional(rollbackFor = Exception.class)
    public InfusionRoomVO.Infusion skinTest(InfusionRoomDTO.SkinTestCreate dto) {
        BizOutpInfusion inf = mustInfusing(dto.getInfusionId());
        if (inf.getStatus() != InfusionStatusEnum.PENDING_TEST.getCode()) {
            throw new BusinessException("输液单当前状态不需要皮试（状态 " + inf.getStatus() + "）");
        }
        BizSkinTest st = new BizSkinTest();
        st.setTestNo(nextNo("PS"));
        st.setPatientId(inf.getPatientId());
        st.setPatientName(inf.getPatientName());
        st.setDrugName(dto.getDrugName());
        st.setTreatmentRecordId(inf.getTreatmentRecordId());
        st.setTestTime(LocalDateTime.now().truncatedTo(java.time.temporal.ChronoUnit.SECONDS));
        st.setResult(SkinTestResultEnum.PENDING.getCode());
        st.setNurseId(UserUtils.getCurrentUser().getEmployeeId());
        st.setNurseName(UserUtils.getCurrentUser().getRealName());
        st.setRemark(dto.getRemark());
        st.setCreateBy(st.getNurseName());
        skinTestMapper.insert(st);

        inf.setSkinTestId(st.getId());
        inf.setUpdateBy(st.getNurseName());
        infusionMapper.updateById(inf);
        return toInfusionVO(inf);
    }

    /**
     * 皮试判读：观察窗 <15 分钟拒绝；阳性 → 输液单取消 + 释放座位。
     */
    @Transactional(rollbackFor = Exception.class)
    public InfusionRoomVO.Infusion skinTestResult(InfusionRoomDTO.SkinTestResult dto) {
        BizSkinTest st = skinTestMapper.selectById(dto.getSkinTestId());
        if (st == null || st.getDelFlag() == DelFlagEnum.DELETED.getCode()) {
            throw new BusinessException("皮试记录不存在");
        }
        if (st.getResult() != null && st.getResult() != SkinTestResultEnum.PENDING.getCode()) {
            throw new BusinessException("皮试已判读（结果 " + st.getResult() + "），判读不可改");
        }
        long minutes = Duration.between(st.getTestTime(), LocalDateTime.now()).toMinutes();
        if (minutes < SKIN_TEST_OBSERVE_MINUTES) {
            throw new BusinessException("观察不足 " + SKIN_TEST_OBSERVE_MINUTES + " 分钟（已 " + minutes
                    + " 分钟），不能判读");
        }
        st.setResult(dto.getResult());
        st.setResultTime(LocalDateTime.now().truncatedTo(java.time.temporal.ChronoUnit.SECONDS));
        st.setUpdateBy(UserUtils.getCurrentUser().getRealName());
        skinTestMapper.updateById(st);

        // 皮试表不持有输液单外键（输液单持 skinTestId），反查进行中的输液单
        BizOutpInfusion inf = infusionMapper.selectOne(new LambdaQueryWrapper<BizOutpInfusion>()
                .eq(BizOutpInfusion::getSkinTestId, st.getId())
                .eq(BizOutpInfusion::getDelFlag, DelFlagEnum.NORMAL.getCode())
                .orderByDesc(BizOutpInfusion::getId)
                .last("LIMIT 1"));
        if (inf != null) {
            if (dto.getResult() == SkinTestResultEnum.POSITIVE.getCode()) {
                cancelInternal(inf, "皮试阳性（皮试单 " + st.getTestNo() + "）");
            } else {
                inf.setStatus(InfusionStatusEnum.WAITING.getCode());
                inf.setUpdateBy(UserUtils.getCurrentUser().getRealName());
                infusionMapper.updateById(inf);
            }
        }
        BizOutpInfusion latest = inf == null ? null : infusionMapper.selectById(inf.getId());
        return latest == null ? null : toInfusionVO(latest);
    }

    // 输注

    /**
     * 开始输注：待输注（或待皮试且皮试阴性）才可开始；写滴速 + 开始时间。
     */
    @Transactional(rollbackFor = Exception.class)
    public InfusionRoomVO.Infusion start(InfusionRoomDTO.Start dto) {
        BizOutpInfusion inf = mustInfusing(dto.getInfusionId());
        if (inf.getStatus() != InfusionStatusEnum.WAITING.getCode()) {
            throw new BusinessException("输液单不在待输注状态（状态 " + inf.getStatus() + "）");
        }
        if (inf.getSkinTestId() != null) {
            BizSkinTest st = skinTestMapper.selectById(inf.getSkinTestId());
            if (st == null || st.getResult() == null || st.getResult() != SkinTestResultEnum.NEGATIVE.getCode()) {
                throw new BusinessException("皮试未判读阴性，不能开始输注");
            }
        }
        inf.setStatus(InfusionStatusEnum.INFUSING.getCode());
        inf.setStartTime(LocalDateTime.now().truncatedTo(java.time.temporal.ChronoUnit.SECONDS));
        inf.setDripRate(dto.getDripRate());
        inf.setUpdateBy(UserUtils.getCurrentUser().getRealName());
        infusionMapper.updateById(inf);
        return toInfusionVO(inf);
    }

    /**
     * 巡视：只增不改（输液中才可巡视）。
     */
    @Transactional(rollbackFor = Exception.class)
    public InfusionRoomVO.Round round(InfusionRoomDTO.Round dto) {
        BizOutpInfusion inf = mustInfusing(dto.getInfusionId());
        if (inf.getStatus() != InfusionStatusEnum.INFUSING.getCode()) {
            throw new BusinessException("输液单不在输注中（状态 " + inf.getStatus() + "）");
        }
        BizOutpInfusionRound r = new BizOutpInfusionRound();
        r.setInfusionId(inf.getId());
        r.setRoundTime(LocalDateTime.now().truncatedTo(java.time.temporal.ChronoUnit.SECONDS));
        r.setDripRate(dto.getDripRate());
        r.setRemainingVolume(dto.getRemainingVolume());
        r.setNurseId(UserUtils.getCurrentUser().getEmployeeId());
        r.setNurseName(UserUtils.getCurrentUser().getRealName());
        r.setRemark(dto.getRemark());
        r.setCreateBy(r.getNurseName());
        roundMapper.insert(r);
        InfusionRoomVO.Round vo = new InfusionRoomVO.Round();
        vo.setId(r.getId());
        vo.setInfusionId(r.getInfusionId());
        vo.setRoundTime(r.getRoundTime());
        vo.setDripRate(r.getDripRate());
        vo.setRemainingVolume(r.getRemainingVolume());
        vo.setNurseName(r.getNurseName());
        vo.setRemark(r.getRemark());
        return vo;
    }

    /**
     * 结束输注：adverseFlag=1 时描述必填；释放座位。
     */
    @Transactional(rollbackFor = Exception.class)
    public InfusionRoomVO.Infusion finish(InfusionRoomDTO.Finish dto) {
        BizOutpInfusion inf = mustInfusing(dto.getInfusionId());
        if (inf.getStatus() != InfusionStatusEnum.INFUSING.getCode()) {
            throw new BusinessException("输液单不在输注中（状态 " + inf.getStatus() + "）");
        }
        // B类条件必填：仅 adverseFlag=1 时描述必填，DTO 注解一刀切会挡掉无不良反应的合法请求
        if (dto.getAdverseFlag() == YesOrNoEnum.YES.getCode() && (dto.getAdverseDesc() == null || dto.getAdverseDesc().isBlank())) {
            throw new BusinessException("有不良反应时描述必填");
        }
        inf.setStatus(InfusionStatusEnum.FINISHED.getCode());
        inf.setEndTime(LocalDateTime.now().truncatedTo(java.time.temporal.ChronoUnit.SECONDS));
        inf.setAdverseFlag(dto.getAdverseFlag());
        inf.setAdverseDesc(dto.getAdverseDesc());
        inf.setUpdateBy(UserUtils.getCurrentUser().getRealName());
        infusionMapper.updateById(inf);
        releaseSeat(inf);
        return toInfusionVO(inf);
    }

    /**
     * 取消（1/2/3 态均可）：必须给原因；释放座位。
     */
    @Transactional(rollbackFor = Exception.class)
    public InfusionRoomVO.Infusion cancel(InfusionRoomDTO.Cancel dto) {
        BizOutpInfusion inf = mustInfusing(dto.getInfusionId());
        if (inf.getStatus() == InfusionStatusEnum.FINISHED.getCode()
                || inf.getStatus() == InfusionStatusEnum.CANCELLED.getCode()) {
            throw new BusinessException("输液单已终态，不能取消");
        }
        cancelInternal(inf, dto.getCancelReason());
        return toInfusionVO(infusionMapper.selectById(inf.getId()));
    }

    private void cancelInternal(BizOutpInfusion inf, String reason) {
        inf.setStatus(InfusionStatusEnum.CANCELLED.getCode());
        inf.setCancelReason(cut(reason, 500));
        inf.setEndTime(LocalDateTime.now().truncatedTo(java.time.temporal.ChronoUnit.SECONDS));
        inf.setUpdateBy(UserUtils.getCurrentUser().getRealName());
        infusionMapper.updateById(inf);
        releaseSeat(inf);
    }

    /**
     * 释放座位（占用该座的输液单已不在进行态才释放）。
     */
    private void releaseSeat(BizOutpInfusion inf) {
        if (inf.getSeatId() == null) {
            return;
        }
        BizInfusionSeat seat = seatMapper.selectById(inf.getSeatId());
        if (seat == null) {
            return;
        }
        Long ongoing = infusionMapper.selectCount(new LambdaQueryWrapper<BizOutpInfusion>()
                .eq(BizOutpInfusion::getSeatId, seat.getId())
                .in(BizOutpInfusion::getStatus,
                        InfusionStatusEnum.PENDING_TEST.getCode(),
                        InfusionStatusEnum.WAITING.getCode(),
                        InfusionStatusEnum.INFUSING.getCode())
                .eq(BizOutpInfusion::getDelFlag, DelFlagEnum.NORMAL.getCode())
                .ne(BizOutpInfusion::getId, inf.getId()));
        if (ongoing == 0 && seat.getSeatStatus() == InfusionSeatStatusEnum.OCCUPIED.getCode()) {
            seat.setSeatStatus(InfusionSeatStatusEnum.FREE.getCode());
            seat.setUpdateBy(UserUtils.getCurrentUser().getRealName());
            seatMapper.updateById(seat);
        }
    }

    // 查询

    /**
     * 今日输液单分页（含皮试信息联查）。
     */
    public PageResult<InfusionRoomVO.Infusion> listPage(InfusionRoomDTO.InfusionQuery query) {
        LambdaQueryWrapper<BizOutpInfusion> wrapper = new LambdaQueryWrapper<>();
        wrapper.apply("DATE(create_time) = {0}", LocalDate.now())
                .eq(query.getStatus() != null, BizOutpInfusion::getStatus, query.getStatus())
                .and(query.getKeyword() != null && !query.getKeyword().isBlank(), w -> w
                        .like(BizOutpInfusion::getPatientName, query.getKeyword())
                        .or().like(BizOutpInfusion::getInfusionNo, query.getKeyword()))
                .orderByAsc(BizOutpInfusion::getStatus)
                .orderByDesc(BizOutpInfusion::getId);
        IPage<BizOutpInfusion> page = infusionMapper.selectPage(
                new Page<>(query.getPageNum(), query.getPageSize()), wrapper);
        List<InfusionRoomVO.Infusion> vos = page.getRecords().stream().map(this::toInfusionVO).toList();
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), vos);
    }

    /**
     * 巡视记录（时间升序）。
     */
    public List<InfusionRoomVO.Round> rounds(Long infusionId) {
        return roundMapper.selectList(new LambdaQueryWrapper<BizOutpInfusionRound>()
                        .eq(BizOutpInfusionRound::getInfusionId, infusionId)
                        .orderByAsc(BizOutpInfusionRound::getRoundTime))
                .stream().map(r -> {
                    InfusionRoomVO.Round vo = new InfusionRoomVO.Round();
                    vo.setId(r.getId());
                    vo.setInfusionId(r.getInfusionId());
                    vo.setRoundTime(r.getRoundTime());
                    vo.setDripRate(r.getDripRate());
                    vo.setRemainingVolume(r.getRemainingVolume());
                    vo.setNurseName(r.getNurseName());
                    vo.setRemark(r.getRemark());
                    return vo;
                }).toList();
    }

    /**
     * 今日看板：座位图 + 各状态计数。
     */
    public InfusionRoomVO.Board board() {
        InfusionRoomVO.Board board = new InfusionRoomVO.Board();
        board.setSeats(seats());
        List<BizOutpInfusion> today = infusionMapper.selectList(new LambdaQueryWrapper<BizOutpInfusion>()
                .apply("DATE(create_time) = {0}", LocalDate.now()));
        board.setPendingTest((int) today.stream().filter(i -> i.getStatus() == InfusionStatusEnum.PENDING_TEST.getCode()).count());
        board.setWaiting((int) today.stream().filter(i -> i.getStatus() == InfusionStatusEnum.WAITING.getCode()).count());
        board.setInfusing((int) today.stream().filter(i -> i.getStatus() == InfusionStatusEnum.INFUSING.getCode()).count());
        board.setFinished((int) today.stream().filter(i -> i.getStatus() == InfusionStatusEnum.FINISHED.getCode()).count());
        board.setCancelled((int) today.stream().filter(i -> i.getStatus() == InfusionStatusEnum.CANCELLED.getCode()).count());
        return board;
    }

    // 工具

    private BizOutpInfusion mustInfusing(Long id) {
        BizOutpInfusion inf = infusionMapper.selectById(id);
        if (inf == null || inf.getDelFlag() == DelFlagEnum.DELETED.getCode()) {
            throw new BusinessException("输液单不存在");
        }
        return inf;
    }

    private InfusionRoomVO.Infusion toInfusionVO(BizOutpInfusion inf) {
        InfusionRoomVO.Infusion vo = new InfusionRoomVO.Infusion();
        vo.setId(inf.getId());
        vo.setInfusionNo(inf.getInfusionNo());
        vo.setTreatmentRecordId(inf.getTreatmentRecordId());
        vo.setPatientId(inf.getPatientId());
        vo.setPatientNo(inf.getPatientNo());
        vo.setPatientName(inf.getPatientName());
        vo.setGender(inf.getGender());
        vo.setAge(inf.getAge());
        vo.setDrugSummary(inf.getDrugSummary());
        vo.setSeatId(inf.getSeatId());
        vo.setSeatNo(inf.getSeatNo());
        vo.setSkinTestId(inf.getSkinTestId());
        vo.setStatus(inf.getStatus());
        vo.setStartTime(inf.getStartTime());
        vo.setDripRate(inf.getDripRate());
        vo.setEndTime(inf.getEndTime());
        vo.setAdverseFlag(inf.getAdverseFlag());
        vo.setAdverseDesc(inf.getAdverseDesc());
        vo.setNurseName(inf.getNurseName());
        vo.setCancelReason(inf.getCancelReason());
        vo.setCreateTime(inf.getCreateTime());
        vo.setRemark(inf.getRemark());
        if (inf.getSkinTestId() != null) {
            BizSkinTest st = skinTestMapper.selectById(inf.getSkinTestId());
            if (st != null) {
                vo.setSkinTestResult(st.getResult());
                vo.setSkinTestDrug(st.getDrugName());
            }
        }
        return vo;
    }

    /**
     * 单号：前缀 + yyyyMMdd + 5 位 Redis 流水（与挂号单号同一套机制）。
     */
    private String nextNo(String prefix) {
        return prefix + LocalDate.now().format(DateFormats.COMPACT_DATE)
                + String.format("%05d", redisSequenceService.next("INFUSION_" + prefix));
    }

    private String cut(String s, int max) {
        if (s == null || s.length() <= max) {
            return s;
        }
        return s.substring(0, max);
    }
}

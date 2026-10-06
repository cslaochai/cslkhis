package com.his.system.service.impl;

import com.his.common.enums.DutyShiftTypeEnum;
import com.his.system.service.DutyLogService;
import com.his.system.service.DutyRosterService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.his.common.base.PageResult;
import com.his.common.exception.BusinessException;
import com.his.system.utils.UserUtils;
import com.his.system.dto.DutyLogHandoverDTO;
import com.his.system.dto.DutyLogQueryPageDTO;
import com.his.system.dto.DutyLogUpsertDTO;
import com.his.system.entity.BizDutyLog;
import com.his.system.entity.SysEmployee;
import com.his.system.enums.BizTypeEnum;
import com.his.system.enums.DutyLogStatusEnum;
import com.his.system.mapper.BizDutyLogMapper;
import com.his.system.mapper.SysEmployeeMapper;
import com.his.system.vo.DutyLogVO;
import com.his.system.vo.DutyOfficerVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import com.his.system.service.SysMessageService;
import com.his.system.service.DictCacheService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 值班日志（交班本）服务。
 *
 * <p>对外真正有价值的是 {@link #handover} 和 {@link #ack} 这一对：
 * 「本班没处理完的事 → 自动落到下一班总值班头上 → 接班人签收」。
 * 只记不交，交班本就只是一本没人看的备忘录；只交不签，等于交出去的东西没人认领。
 *
 * <p>三条口径：
 * <ol>
 *   <li><b>值班人 ≠ 记录人</b>：{@code employeeId} 留空取当前总值班，代记时显式传；
 *       {@code createBy} 永远是登录人。事后追责要能分清"他值班时出的事"和"他代记的"。</li>
 *   <li><b>status 2/3 不可直接写</b>：登记接口只接受 0/1，交班推到 2，签收推到 3。
 *       允许登记时直接写"已签收"，等于可以自己给自己签字交班。</li>
 *   <li><b>签收必须本人</b>：{@code handover_emp_id} 不是当前登录员工就拒 ——
 *       别人代签收，交班本上"已签收"三个字就是假的。</li>
 * </ol>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DutyLogServiceImpl implements DutyLogService {
    @Autowired
    private DictCacheService dictText;

    /** 列表默认窗口：近 7 天（交班本是流水账，翻三个月前没意义，当天和前一天必须带出来） */
    private static final int DEFAULT_BACK_DAYS = 7;

    private final BizDutyLogMapper logMapper;
    private final SysEmployeeMapper employeeMapper;
    private final DutyRosterService dutyRosterService;
    private final SysMessageService sysMessageService;

    // 查询

    public PageResult<DutyLogVO> listPage(DutyLogQueryPageDTO q) {
        LocalDate begin = q.getBeginDate() != null ? q.getBeginDate() : LocalDate.now().minusDays(DEFAULT_BACK_DAYS);
        LocalDate end = q.getEndDate() != null ? q.getEndDate() : LocalDate.now();
        LambdaQueryWrapper<BizDutyLog> w = new LambdaQueryWrapper<>();
        w.ge(BizDutyLog::getDutyDate, begin)
                .le(BizDutyLog::getDutyDate, end)
                .eq(q.getShiftType() != null, BizDutyLog::getShiftType, q.getShiftType())
                .eq(q.getLogType() != null, BizDutyLog::getLogType, q.getLogType())
                .eq(q.getStatus() != null, BizDutyLog::getStatus, q.getStatus())
                .eq(q.getEmployeeId() != null, BizDutyLog::getEmployeeId, q.getEmployeeId())
                .eq(q.getHandoverEmpId() != null, BizDutyLog::getHandoverEmpId, q.getHandoverEmpId())
                .orderByDesc(BizDutyLog::getDutyDate)
                .orderByDesc(BizDutyLog::getShiftType)
                // 二级键收口：同一天同班次内顺序必须稳定，否则分页会重复/漏行
                .orderByDesc(BizDutyLog::getId);
        IPage<BizDutyLog> page = logMapper.selectPage(new Page<>(q.getPageNum(), q.getPageSize()), w);
        List<DutyLogVO> list = page.getRecords().stream().map(this::toVO).toList();
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), list);
    }

    /** 当前登录人的待签收遗留事项（交班对象是自己、状态已交班、还没签收） */
    public List<DutyLogVO> pendingMine() {
        Long empId = UserUtils.getCurrentEmployeeId();
        if (empId == null) {
            return List.of();
        }
        List<BizDutyLog> rows = logMapper.selectList(new LambdaQueryWrapper<BizDutyLog>()
                .eq(BizDutyLog::getHandoverEmpId, empId)
                .eq(BizDutyLog::getStatus, DutyLogStatusEnum.HANDED.getCode())
                .orderByAsc(BizDutyLog::getDutyDate)
                .orderByAsc(BizDutyLog::getId));
        return rows.stream().map(this::toVO).toList();
    }

    // 登记 / 维护

    @Transactional(rollbackFor = Exception.class)
    public Long upsert(DutyLogUpsertDTO dto) {
        int status = dto.getStatus() == null ? DutyLogStatusEnum.PENDING.getCode() : dto.getStatus();
        if (status != DutyLogStatusEnum.PENDING.getCode() && status != DutyLogStatusEnum.DONE.getCode()) {
            // 交班本最容易被绕过的地方：登记时直接写"已签收"，等于自己给自己签字交接。
            throw new BusinessException("状态只能登记为待处理或已处理（已交班/已签收须走交班与签收动作）");
        }
        // B 类保留（条件必填）：只有标记为已处理时才要求填写，一刀切的 @NotBlank 会把待处理的登记挡成 400
        if (status == DutyLogStatusEnum.DONE.getCode() && !StringUtils.hasText(dto.getHandleResult())) {
            throw new BusinessException("标记为已处理时必须填写处理情况");
        }

        Long empId = dto.getEmployeeId();
        String empName;
        if (empId == null) {
            // 记在自己头上：取当前时刻的总值班。查不到就直接报错 ——
            // 兜一个"未知值班人"出来，交班本上就会出现一堆没有归属的记录。
            DutyOfficerVO cur = dutyRosterService.current();
            if (cur == null || !Objects.equals(1, cur.getFound()) || cur.getEmployeeId() == null) {
                throw new BusinessException("当前没有总值班（无法自动归属值班人），请先登记排班或显式选择值班人");
            }
            empId = cur.getEmployeeId();
            empName = cur.getEmployeeName();
        } else {
            SysEmployee emp = employeeMapper.selectById(empId);
            if (emp == null || (emp.getDelFlag() != null && emp.getDelFlag() == 1)) {
                throw new BusinessException("值班人不存在");
            }
            empName = emp.getEmpName();
        }

        BizDutyLog row = dto.getId() == null ? null : logMapper.selectById(dto.getId());
        boolean insert = row == null;
        if (insert) {
            row = new BizDutyLog();
        } else if (row.getStatus() != null && row.getStatus() >= DutyLogStatusEnum.HANDED.getCode()) {
            throw new BusinessException("已交班/已签收的记录不能再修改（交接完成后改内容 = 篡改交班本）");
        }
        row.setDutyDate(dto.getDutyDate());
        row.setShiftType(dto.getShiftType());
        row.setEmployeeId(empId);
        row.setEmployeeName(empName);
        row.setLogType(dto.getLogType());
        row.setHappenTime(dto.getHappenTime() != null ? dto.getHappenTime() : LocalDateTime.now());
        row.setTitle(dto.getTitle().trim());
        row.setContent(dto.getContent());
        row.setHandleResult(dto.getHandleResult());
        row.setStatus(status);
        row.setRemark(dto.getRemark());

        // ⚠ 记录人必须显式写：全局 MetaObjectHandler 只填 createTime/updateTime/delFlag，
        //   **不填 createBy**（见 his-web MyBatisPlusConfig）。值班日志的"谁写的"是要留痕的
        //   —— 代记/补记时值班人和记录人不是同一个人，全系统默认 null 等于这条痕没了。
        String operator = UserUtils.getCurrentEmployeeName();
        if (insert) {
            row.setCreateBy(operator);
            logMapper.insert(row);
        } else {
            row.setUpdateBy(operator);
            logMapper.updateById(row);
        }
        log.info("值班日志{} id={} date={} shift={} 值班人={} 类型={} 状态={}",
                insert ? "登记" : "修改", row.getId(), row.getDutyDate(), row.getShiftType(), empName,
                row.getLogType(), status);
        return row.getId();
    }

    /** 删除（软删：日志没有唯一键，且值班记录要留档给评审翻） */
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(Long id) {
        BizDutyLog row = logMapper.selectById(id);
        if (row == null) {
            throw new BusinessException("值班日志记录不存在");
        }
        if (row.getStatus() != null && row.getStatus() == DutyLogStatusEnum.ACKED.getCode()) {
            throw new BusinessException("已签收的记录不能删除（交接已完成，删除等于抹掉交接凭据）");
        }
        logMapper.deleteById(id);
    }

    // 交班 / 签收 —— 交班本的闭环

    /**
     * 交班：把本班没处理完的事交给下一班总值班。
     * 接班人默认取「下一班的总值班」（不传就用它），交完发站内信 ——
     * 只写库不发信，接班人要到第二天自己翻页面才知道有事压在他头上。
     */
    @Transactional(rollbackFor = Exception.class)
    public void handover(DutyLogHandoverDTO dto) {
        BizDutyLog row = logMapper.selectById(dto.getId());
        if (row == null) {
            throw new BusinessException("值班日志记录不存在");
        }
        if (row.getStatus() != null && row.getStatus() >= DutyLogStatusEnum.HANDED.getCode()) {
            throw new BusinessException("该记录已交班，不能重复交班");
        }

        Long nextEmpId = dto.getHandoverEmpId();
        String nextEmpName;
        if (nextEmpId != null) {
            SysEmployee emp = employeeMapper.selectById(nextEmpId);
            if (emp == null || (emp.getDelFlag() != null && emp.getDelFlag() == 1)) {
                throw new BusinessException("接班人不存在");
            }
            nextEmpName = emp.getEmpName();
        } else {
            DutyOfficerVO next = dutyRosterService.nextOfficer(row.getDutyDate(),
                    row.getShiftType() == null ? DutyShiftTypeEnum.DAY.getCode() : row.getShiftType());
            if (next == null || !Objects.equals(1, next.getFound()) || next.getEmployeeId() == null) {
                throw new BusinessException("下一班未排总值班，无法自动确定接班人，请显式选择接班人");
            }
            nextEmpId = next.getEmployeeId();
            nextEmpName = next.getEmployeeName();
        }

        LocalDateTime now = LocalDateTime.now();
        String handle = StringUtils.hasText(dto.getHandleResult()) ? dto.getHandleResult().trim() : row.getHandleResult();
        // ⚠ UpdateWrapper 显式 set：updateById 走 NOT_NULL 策略，把 ack_time 之类从有值改回 null 会被跳过
        logMapper.update(null, new LambdaUpdateWrapper<BizDutyLog>()
                .eq(BizDutyLog::getId, row.getId())
                .set(BizDutyLog::getStatus, DutyLogStatusEnum.HANDED.getCode())
                .set(BizDutyLog::getHandoverEmpId, nextEmpId)
                .set(BizDutyLog::getHandoverEmpName, nextEmpName)
                .set(BizDutyLog::getHandoverTime, now)
                .set(BizDutyLog::getHandleResult, handle));

        sysMessageService.sendSystemMessage(nextEmpId, nextEmpName,
                "值班交班：" + row.getTitle(),
                String.format("%s %s 的遗留事项交给你跟进。%s 请到「总值班排班 → 值班日志」签收确认。",
                        row.getDutyDate(), shiftText(row.getShiftType()),
                        StringUtils.hasText(row.getContent()) ? row.getContent() : ""),
                BizTypeEnum.DUTY_COORD.getType(), row.getId(), "warning", null, 0);
        log.info("值班交班 id={} 值班人={} 接班人={}", row.getId(), row.getEmployeeName(), nextEmpName);
    }

    /** 签收：接班人确认收到。必须是交班对象本人，代签收的"已签收"是假的 */
    @Transactional(rollbackFor = Exception.class)
    public void ack(Long id) {
        BizDutyLog row = logMapper.selectById(id);
        if (row == null) {
            throw new BusinessException("值班日志记录不存在");
        }
        if (!Objects.equals(DutyLogStatusEnum.HANDED.getCode(), row.getStatus())) {
            throw new BusinessException("只有已交班的记录才能签收（当前状态：" + statusText(row.getStatus()) + "）");
        }
        Long me = UserUtils.getCurrentEmployeeId();
        if (me == null || !me.equals(row.getHandoverEmpId())) {
            throw new BusinessException("只有接班人本人能签收（当前登录人不是交班对象）");
        }
        logMapper.update(null, new LambdaUpdateWrapper<BizDutyLog>()
                .eq(BizDutyLog::getId, row.getId())
                .set(BizDutyLog::getStatus, DutyLogStatusEnum.ACKED.getCode())
                .set(BizDutyLog::getAckTime, LocalDateTime.now()));
        log.info("值班交班签收 id={} 接班人={}", row.getId(), row.getHandoverEmpName());
    }

    // 内部

    private DutyLogVO toVO(BizDutyLog r) {
        Long me = UserUtils.getCurrentEmployeeId();
        DutyLogVO vo = new DutyLogVO();
        vo.setId(r.getId());
        vo.setDutyDate(r.getDutyDate());
        vo.setShiftType(r.getShiftType());
        vo.setShiftTypeText(shiftText(r.getShiftType()));
        vo.setRosterId(r.getRosterId());
        vo.setEmployeeId(r.getEmployeeId());
        vo.setEmployeeName(r.getEmployeeName());
        vo.setLogType(r.getLogType());
        vo.setLogTypeText(logTypeText(r.getLogType()));
        vo.setHappenTime(r.getHappenTime());
        vo.setTitle(r.getTitle());
        vo.setContent(r.getContent());
        vo.setHandleResult(r.getHandleResult());
        vo.setStatus(r.getStatus());
        vo.setStatusText(statusText(r.getStatus()));
        vo.setHandoverEmpId(r.getHandoverEmpId());
        vo.setHandoverEmpName(r.getHandoverEmpName());
        vo.setHandoverTime(r.getHandoverTime());
        vo.setAckTime(r.getAckTime());
        vo.setCreateBy(r.getCreateBy());
        vo.setCreateTime(r.getCreateTime());
        vo.setCanAck(me != null && me.equals(r.getHandoverEmpId()) && Objects.equals(DutyLogStatusEnum.HANDED.getCode(), r.getStatus()) ? 1 : 0);
        vo.setRemark(r.getRemark());
        return vo;
    }

    private String shiftText(Integer shift) {
        return shift != null && shift == DutyShiftTypeEnum.NIGHT.getCode() ? "夜班" : "白班";
    }

    private String logTypeText(Integer t) {
        if (t == null) {
            return "-";
        }
        return dictText.getDicDataLabel("biz_system_dutyLogTypeEnum", t);
    }

    private String statusText(Integer s) {
        if (s == null) {
            return "-";
        }
        return DutyLogStatusEnum.getText(s);
    }
}
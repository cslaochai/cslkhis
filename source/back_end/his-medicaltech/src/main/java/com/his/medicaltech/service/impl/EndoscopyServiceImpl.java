package com.his.medicaltech.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.common.base.PageResult;
import com.his.common.enums.TechAuthCategoryEnum;
import com.his.common.enums.TechOverrideSourceEnum;
import com.his.common.exception.BusinessException;
import com.his.medicaltech.dto.EndoscopyDTO;
import com.his.medicaltech.dto.PathologyDTO;
import com.his.medicaltech.entity.BizEndoscopyRecord;
import com.his.medicaltech.entity.BizPathologyOrder;
import com.his.medicaltech.enums.EndoscopyHpResultEnum;
import com.his.medicaltech.enums.EndoscopyTypeEnum;
import com.his.medicaltech.enums.InsRecordStatusEnum;
import com.his.medicaltech.mapper.BizEndoscopyRecordMapper;
import com.his.medicaltech.service.EndoscopyService;
import com.his.medicaltech.service.PathologyService;
import com.his.medicaltech.vo.EndoscopyVO;
import com.his.system.dto.TechAuthGateDTO;
import com.his.system.service.DictCacheService;
import com.his.system.service.EmployeeTechAuthService;
import com.his.system.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 内镜亚专业服务
 *
 * <p>状态机：1 已登记 → 2 已签到 → 3 检查中 → 4 已出报告 → 5 已审核 → 6 已发布；7 已取消。
 *
 * <p>两条硬规则：
 * <ol>
 *   <li>活检送病理走「同模块内 PathologyService.createFromEndoscopy」生成病理单并回填病理号，
 *       **同一内镜单重复送检直接拒绝**（否则一份活检出两份病理单，临床会拿到互相矛盾的诊断）；</li>
 *   <li>审核人不得是报告人本人。</li>
 * </ol>
 */
@Service
@RequiredArgsConstructor
public class EndoscopyServiceImpl extends ServiceImpl<BizEndoscopyRecordMapper, BizEndoscopyRecord> implements EndoscopyService {

    private static final String DICT_STATUS = "his_endous_status";
    private static final String DICT_ENDO_TYPE = "his_endoscopy_type";
    private static final String DICT_ANESTHESIA = "his_endoscopy_anesthesia";

    private final BizEndoscopyRecordMapper recordMapper;
    private final PathologyService pathologyService;
    private final DictCacheService dictText;
    private final EmployeeTechAuthService techAuthService;

    /**
     * ERCP=4 级；取活检=2 级；其余诊断性镜检=1 级
     */
    private static int requiredEndoLevel(BizEndoscopyRecord r) {
        if (EndoscopyTypeEnum.ERCP.is(r.getEndoType())) {
            return 4;
        }
        if (Integer.valueOf(1).equals(r.getBiopsyFlag())) {
            return 2;
        }
        return 1;
    }

    public PageResult<EndoscopyVO.ListVO> pageVO(EndoscopyDTO.Query q) {
        LambdaQueryWrapper<BizEndoscopyRecord> w = new LambdaQueryWrapper<>();
        w.eq(StringUtils.hasText(q.getRecordNo()), BizEndoscopyRecord::getRecordNo, q.getRecordNo())
                .eq(q.getPatientId() != null, BizEndoscopyRecord::getPatientId, q.getPatientId())
                .eq(q.getEndoType() != null, BizEndoscopyRecord::getEndoType, q.getEndoType())
                .eq(q.getStatus() != null, BizEndoscopyRecord::getStatus, q.getStatus())
                .like(StringUtils.hasText(q.getPatientName()), BizEndoscopyRecord::getPatientName, tr(q.getPatientName()))
                .ge(q.getStartDate() != null, BizEndoscopyRecord::getVisitDate, q.getStartDate())
                .le(q.getEndDate() != null, BizEndoscopyRecord::getVisitDate, q.getEndDate())
                .orderByDesc(BizEndoscopyRecord::getId);
        Page<BizEndoscopyRecord> page = recordMapper.selectPage(
                new Page<>(q.getPageNum(), q.getPageSize()), w);
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(),
                toListVo(page.getRecords()));
    }

    public EndoscopyVO.StatsVO stats() {
        EndoscopyVO.StatsVO vo = new EndoscopyVO.StatsVO();
        vo.setTotal(recordMapper.selectCount(new LambdaQueryWrapper<BizEndoscopyRecord>()
                .ne(BizEndoscopyRecord::getStatus, InsRecordStatusEnum.CANCELLED.getCode())));
        vo.setPending(count(InsRecordStatusEnum.REGISTERED.getCode()) + count(InsRecordStatusEnum.SIGNED_IN.getCode()));
        vo.setExamining(count(InsRecordStatusEnum.CHECKING.getCode()));
        vo.setPendingAudit(count(InsRecordStatusEnum.RESULTED.getCode()));
        vo.setPublished(count(InsRecordStatusEnum.PUBLISHED.getCode()));
        vo.setBiopsyCount(recordMapper.selectCount(new LambdaQueryWrapper<BizEndoscopyRecord>()
                .eq(BizEndoscopyRecord::getBiopsyFlag, 1)));
        return vo;
    }

    private long count(int status) {
        return recordMapper.selectCount(new LambdaQueryWrapper<BizEndoscopyRecord>().eq(BizEndoscopyRecord::getStatus, status));
    }

    public EndoscopyVO.DetailVO getDetail(Long recordId) {
        BizEndoscopyRecord r = require(recordId);
        EndoscopyVO.DetailVO vo = new EndoscopyVO.DetailVO();
        BeanUtils.copyProperties(r, vo);
        fillText(vo);
        return vo;
    }

    private List<EndoscopyVO.ListVO> toListVo(List<BizEndoscopyRecord> records) {
        List<EndoscopyVO.ListVO> out = new ArrayList<>();
        for (BizEndoscopyRecord r : records) {
            EndoscopyVO.ListVO v = new EndoscopyVO.ListVO();
            BeanUtils.copyProperties(r, v);
            v.setStatusText(dictText.getDicDataLabel(DICT_STATUS, r.getStatus()));
            v.setEndoTypeText(dictText.getDicDataLabel(DICT_ENDO_TYPE, r.getEndoType()));
            v.setAnesthesiaMethodText(dictText.getDicDataLabel(DICT_ANESTHESIA, r.getAnesthesiaMethod()));
            out.add(v);
        }
        return out;
    }

    // 写入

    private void fillText(EndoscopyVO.DetailVO vo) {
        vo.setStatusText(dictText.getDicDataLabel(DICT_STATUS, vo.getStatus()));
        vo.setEndoTypeText(dictText.getDicDataLabel(DICT_ENDO_TYPE, vo.getEndoType()));
        vo.setAnesthesiaMethodText(dictText.getDicDataLabel(DICT_ANESTHESIA, vo.getAnesthesiaMethod()));
        vo.setHpResultText(EndoscopyHpResultEnum.getText(vo.getHpResult()));
    }

    @Transactional(rollbackFor = Exception.class)
    public EndoscopyVO.DetailVO upsertRecord(EndoscopyDTO.RecordUpsert dto) {
        BizEndoscopyRecord r = dto.getId() == null ? create(dto) : update(dto);
        return getDetail(r.getId());
    }

    private BizEndoscopyRecord create(EndoscopyDTO.RecordUpsert dto) {
        BizEndoscopyRecord r = new BizEndoscopyRecord();
        BeanUtils.copyProperties(dto, r);
        r.setId(null);
        LocalDate visitDate = dto.getVisitDate() != null ? dto.getVisitDate() : LocalDate.now();
        r.setVisitDate(visitDate);
        if (r.getEndoType() == null) {
            r.setEndoType(EndoscopyTypeEnum.GASTRO.getCode());
        }
        if (r.getAnesthesiaMethod() == null) {
            r.setAnesthesiaMethod(1);
        }
        r.setBiopsyFlag(0);
        r.setBiopsyCount(0);
        r.setStatus(InsRecordStatusEnum.REGISTERED.getCode());
        r.setRecordNo(nextRecordNo(visitDate));
        recordMapper.insert(r);
        return r;
    }

    private BizEndoscopyRecord update(EndoscopyDTO.RecordUpsert dto) {
        BizEndoscopyRecord r = require(dto.getId());
        assertMutable(r);
        if (!InsRecordStatusEnum.REGISTERED.is(r.getStatus()) && !InsRecordStatusEnum.SIGNED_IN.is(r.getStatus())) {
            throw new BusinessException("已开始检查的记录不可修改登记信息（当前：" + dictText.getDicDataLabel(DICT_STATUS, r.getStatus()) + "）");
        }
        BeanUtils.copyProperties(dto, r);
        r.setId(dto.getId());
        recordMapper.updateById(r);
        return r;
    }

    @Transactional(rollbackFor = Exception.class)
    public void checkIn(Long recordId) {
        BizEndoscopyRecord r = require(recordId);
        assertMutable(r);
        if (!InsRecordStatusEnum.REGISTERED.is(r.getStatus())) {
            throw new BusinessException("仅「已登记」可签到（当前：" + dictText.getDicDataLabel(DICT_STATUS, r.getStatus()) + "）");
        }
        r.setStatus(InsRecordStatusEnum.SIGNED_IN.getCode());
        recordMapper.updateById(r);
    }

    /**
     * 执行检查：写医师/所见相关字段 → 检查中
     */
    @Transactional(rollbackFor = Exception.class)
    public void execute(EndoscopyDTO.Execute dto) {
        BizEndoscopyRecord r = require(dto.getRecordId());
        assertMutable(r);
        if (!InsRecordStatusEnum.SIGNED_IN.is(r.getStatus()) && !InsRecordStatusEnum.CHECKING.is(r.getStatus())) {
            throw new BusinessException("请先签到再执行检查（当前：" + dictText.getDicDataLabel(DICT_STATUS, r.getStatus()) + "）");
        }
        r.setEndoscopist(StringUtils.hasText(dto.getEndoscopist()) ? dto.getEndoscopist() : currentName());
        if (dto.getBowelPrepScore() != null) {
            if (dto.getBowelPrepScore() < 0 || dto.getBowelPrepScore() > 9) {
                throw new BusinessException("肠道准备 Boston 评分须在 0~9 之间");
            }
            if (!EndoscopyTypeEnum.COLON.is(r.getEndoType())) {
                throw new BusinessException("仅肠镜记录肠道准备评分");
            }
            r.setBowelPrepScore(dto.getBowelPrepScore());
        }
        if (dto.getHpResult() != null) {
            if (dto.getHpResult() < 0 || dto.getHpResult() > 2) {
                throw new BusinessException("幽门螺杆菌结果码值非法");
            }
            if (!EndoscopyTypeEnum.GASTRO.is(r.getEndoType())) {
                throw new BusinessException("仅胃镜记录幽门螺杆菌结果");
            }
            r.setHpResult(dto.getHpResult());
        }
        if (StringUtils.hasText(dto.getBodyPart())) {
            r.setBodyPart(dto.getBodyPart());
        }
        if (dto.getBiopsyFlag() != null) {
            r.setBiopsyFlag(dto.getBiopsyFlag());
            if (Integer.valueOf(1).equals(dto.getBiopsyFlag())) {
                // B类条件必填：仅勾选活检时部位必填，注解一刀切会挡掉未取活检的检查登记
                if (!StringUtils.hasText(dto.getBiopsyPart())) {
                    throw new BusinessException("勾选活检时活检部位必填");
                }
                r.setBiopsyPart(dto.getBiopsyPart());
                r.setBiopsyCount(dto.getBiopsyCount() == null || dto.getBiopsyCount() <= 0 ? 1 : dto.getBiopsyCount());
            }
        }
        // G21 技术授权闸门：谁拿镜子谁过闸。放在字段都定下来之后——要不要算「活检」级别，
        // 取决于本次是否真的取活检；放在登记时更不行，登记只排期、术者到执行才落定。
        gateTechAuth(r);
        if (InsRecordStatusEnum.SIGNED_IN.is(r.getStatus())) {
            r.setStatus(InsRecordStatusEnum.CHECKING.getCode());
            r.setExecuteTime(LocalDateTime.now().withNano(0));
        }
        recordMapper.updateById(r);
    }

    /**
     * 活检送病理：生成病理单并回填病理号
     */
    @Transactional(rollbackFor = Exception.class)
    public String sendBiopsy(EndoscopyDTO.BiopsySend dto) {
        BizEndoscopyRecord r = require(dto.getRecordId());
        assertMutable(r);
        if (StringUtils.hasText(r.getPathologyOrderNo())) {
            throw new BusinessException("该内镜检查已送检病理（病理号 " + r.getPathologyOrderNo() + "），不可重复送检");
        }
        int count = dto.getBiopsyCount() == null || dto.getBiopsyCount() <= 0 ? 1 : dto.getBiopsyCount();

        PathologyDTO.FromEndoscopy from = new PathologyDTO.FromEndoscopy();
        from.setPatientId(r.getPatientId());
        from.setPatientNo(r.getPatientNo());
        from.setPatientName(r.getPatientName());
        from.setGender(r.getGender());
        from.setAge(r.getAge());
        from.setVisitDate(r.getVisitDate() == null ? LocalDate.now() : r.getVisitDate());
        from.setApplyDeptName(r.getApplyDeptName());
        from.setApplyDoctorName(r.getApplyDoctorName());
        from.setClinicalDiagnosis(r.getClinicalDiagnosis());
        from.setBiopsyPart(dto.getBiopsyPart());
        from.setBiopsyCount(count);
        from.setSourceRecordNo(r.getRecordNo());

        BizPathologyOrder order = pathologyService.createFromEndoscopy(from);
        r.setBiopsyFlag(1);
        r.setBiopsyPart(dto.getBiopsyPart());
        r.setBiopsyCount(count);
        r.setPathologyOrderNo(order.getOrderNo());
        recordMapper.updateById(r);
        return order.getOrderNo();
    }

    /**
     * 出报告（检查中 → 已出报告）
     */
    @Transactional(rollbackFor = Exception.class)
    public void report(EndoscopyDTO.Report dto) {
        BizEndoscopyRecord r = require(dto.getRecordId());
        assertMutable(r);
        if (!InsRecordStatusEnum.CHECKING.is(r.getStatus())) {
            throw new BusinessException("仅「检查中」的记录可出具报告（当前：" + dictText.getDicDataLabel(DICT_STATUS, r.getStatus()) + "）");
        }
        r.setFindings(dto.getFindings());
        r.setDiagnosis(dto.getDiagnosis());
        r.setSuggestion(dto.getSuggestion());
        r.setStatus(InsRecordStatusEnum.RESULTED.getCode());
        r.setReportBy(currentName());
        r.setReportTime(LocalDateTime.now().withNano(0));
        recordMapper.updateById(r);
    }

    @Transactional(rollbackFor = Exception.class)
    public void audit(EndoscopyDTO.Audit dto) {
        BizEndoscopyRecord r = require(dto.getRecordId());
        assertMutable(r);
        if (!InsRecordStatusEnum.RESULTED.is(r.getStatus())) {
            throw new BusinessException("仅「已出报告」可审核（当前：" + dictText.getDicDataLabel(DICT_STATUS, r.getStatus()) + "）");
        }
        String who = currentName();
        if (who != null && who.equals(r.getReportBy())) {
            throw new BusinessException("审核人不得是报告医师本人（" + who + "）——内镜报告必须两级签署");
        }
        r.setStatus(InsRecordStatusEnum.REVIEWED.getCode());
        r.setAuditBy(who);
        r.setAuditTime(LocalDateTime.now().withNano(0));
        if (StringUtils.hasText(dto.getAuditOpinion())) {
            r.setRemark(clip((r.getRemark() == null ? "" : r.getRemark() + " | 审核意见：") + dto.getAuditOpinion()));
        }
        recordMapper.updateById(r);
    }

    @Transactional(rollbackFor = Exception.class)
    public void publish(Long recordId) {
        BizEndoscopyRecord r = require(recordId);
        if (InsRecordStatusEnum.PUBLISHED.is(r.getStatus())) {
            throw new BusinessException("该报告已发布");
        }
        if (!InsRecordStatusEnum.REVIEWED.is(r.getStatus())) {
            throw new BusinessException("发布前必须完成审核（当前：" + dictText.getDicDataLabel(DICT_STATUS, r.getStatus()) + "）");
        }
        r.setStatus(InsRecordStatusEnum.PUBLISHED.getCode());
        r.setPublishBy(currentName());
        r.setPublishTime(LocalDateTime.now().withNano(0));
        recordMapper.updateById(r);
    }

    // 内部

    @Transactional(rollbackFor = Exception.class)
    public void cancel(EndoscopyDTO.Cancel dto) {
        BizEndoscopyRecord r = require(dto.getRecordId());
        if (InsRecordStatusEnum.PUBLISHED.is(r.getStatus())) {
            throw new BusinessException("已发布的报告不能取消");
        }
        if (InsRecordStatusEnum.CANCELLED.is(r.getStatus())) {
            throw new BusinessException("该记录已取消");
        }
        r.setStatus(InsRecordStatusEnum.CANCELLED.getCode());
        r.setCancelReason(clip(dto.getCancelReason()));
        r.setCancelTime(LocalDateTime.now().withNano(0));
        recordMapper.updateById(r);
    }

    private BizEndoscopyRecord require(Long id) {
        // C类：入参是主键参数而非请求 DTO，Bean Validation 只在 HTTP DTO 绑定时生效，无法下沉
        if (id == null) {
            throw new BusinessException("检查记录ID不能为空");
        }
        BizEndoscopyRecord r = recordMapper.selectById(id);
        if (r == null) {
            throw new BusinessException("内镜检查记录不存在：" + id);
        }
        return r;
    }

    private void assertMutable(BizEndoscopyRecord r) {
        if (InsRecordStatusEnum.PUBLISHED.is(r.getStatus()) || InsRecordStatusEnum.CANCELLED.is(r.getStatus())) {
            throw new BusinessException("已" + (InsRecordStatusEnum.PUBLISHED.is(r.getStatus()) ? "发布" : "取消") + "的记录不可再操作");
        }
    }

    private String nextRecordNo(LocalDate date) {
        String day = date.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long base = recordMapper.selectCount(new LambdaQueryWrapper<BizEndoscopyRecord>()
                .ge(BizEndoscopyRecord::getCreateTime, date.atStartOfDay())
                .lt(BizEndoscopyRecord::getCreateTime, date.plusDays(1).atStartOfDay())) + 1;
        for (int i = 0; i < 20; i++) {
            String no = "NJ" + day + String.format("%03d", base + i);
            if (recordMapper.selectCount(new LambdaQueryWrapper<BizEndoscopyRecord>()
                    .eq(BizEndoscopyRecord::getRecordNo, no)) == 0) {
                return no;
            }
        }
        return "NJ" + day + System.currentTimeMillis() % 100000;
    }

    private String currentName() {
        String n = UserUtils.getCurrentEmployeeName();
        return n != null ? n : "未知操作人";
    }

    /**
     * G21 内镜技术授权闸门：术者须持有「内镜与介入」类授权，级别按操作类型要求。
     *
     * <p>级别映射取<b>院内自定的起步口径</b>（写在这里便于医务科评审后只改一处）：
     * ERCP 属四级内镜介入；取活检（含治疗性内镜）至少按二级要求；普通诊断性镜检按一级。
     * 真正的强制力来自「有没有授权 + 级别够不够」，映射松紧属政策而非缺陷。
     */
    private void gateTechAuth(BizEndoscopyRecord r) {
        String operator = r.getEndoscopist();
        TechAuthGateDTO gate = new TechAuthGateDTO();
        // 登录人就是术者时直接用工号，避免同名员工误判；否则按姓名回捞档案
        if (UserUtils.getCurrentEmployeeName() != null && UserUtils.getCurrentEmployeeName().equals(operator)) {
            gate.setEmployeeId(UserUtils.getCurrentEmployeeId());
        }
        gate.setEmployeeName(operator);
        gate.setAuthCategory(TechAuthCategoryEnum.ENDOSCOPY.getCode());
        gate.setRequiredLevel(requiredEndoLevel(r));
        gate.setOperateDate(r.getVisitDate());
        gate.setSourceType(TechOverrideSourceEnum.ENDOSCOPY.getCode());
        gate.setSourceId(r.getId());
        gate.setSourceNo(r.getRecordNo());
        try {
            techAuthService.gate(gate);
        } catch (BusinessException e) {
            throw new BusinessException("内镜术者「" + operator + "」" + e.getMessage());
        }
    }

    /**
     * null 安全 trim：查询条件的 value 参数是急切求值的，直接 x.trim() 会在 x 为 null 时 NPE
     */
    private String tr(String s) {
        return s == null ? null : s.trim();
    }

    private String clip(String s) {
        if (s == null) {
            return null;
        }
        return s.length() > 480 ? s.substring(0, 480) : s;
    }
}

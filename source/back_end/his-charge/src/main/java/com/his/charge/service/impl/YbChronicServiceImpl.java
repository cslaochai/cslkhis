package com.his.charge.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.charge.api.PatientGateway;
import com.his.charge.dto.*;
import com.his.charge.entity.BizYbChronicCatalog;
import com.his.charge.entity.BizYbChronicReg;
import com.his.charge.mapper.BizYbChronicCatalogMapper;
import com.his.charge.mapper.BizYbChronicRegMapper;
import com.his.charge.service.YbChronicService;
import com.his.charge.vo.ChronicCatalogVO;
import com.his.charge.vo.ChronicRegListVO;
import com.his.charge.vo.ChronicRegSummaryVO;
import com.his.charge.vo.PatientBriefVO;
import com.his.common.base.PageResult;
import com.his.common.exception.BusinessException;
import com.his.common.service.RedisSequenceService;
import com.his.common.util.SensitiveMaskUtil;
import com.his.common.util.TextUtil;
import com.his.system.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;

/**
 * 门诊慢特病服务实现。
 *
 * <p>这张表存在的意义就是「谁办的备案」：registerEmpName 新建时由服务端取当前登录人回填，
 * 改成外部经办机构人员时姓名不再是本院员工（registerEmpId 置空），必须写明备注。
 * 有效期过期是展示态（前端提示续备），库里只有 reg_status，避免多出「日期已过、状态还没刷」的漂移窗口。
 */
@Service
@RequiredArgsConstructor
public class YbChronicServiceImpl extends ServiceImpl<BizYbChronicRegMapper, BizYbChronicReg> implements YbChronicService {

    /**
     * 备案状态：1-有效 2-已注销 3-已驳回
     */
    private static final int REG_VALID = 1;
    private static final int REG_CANCELLED = 2;
    private static final int REG_REJECTED = 3;

    /**
     * 展示态多一档：4-已过期（有效但待遇终止日已过）
     */
    private static final int DISPLAY_EXPIRED = 4;

    private final BizYbChronicCatalogMapper bizYbChronicCatalogMapper;
    private final BizYbChronicRegMapper bizYbChronicRegMapper;
    private final PatientGateway patientGateway;
    private final RedisSequenceService redisSequenceService;

    @Override
    public PageResult<ChronicCatalogVO> catalogListPage(ChronicCatalogQueryPageDTO queryDTO) {
        IPage<BizYbChronicCatalog> page = bizYbChronicCatalogMapper.selectPage(
                new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize()), catalogWrapper(queryDTO));
        List<ChronicCatalogVO> voList = page.getRecords().stream().map(this::toCatalogVO).toList();
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), voList);
    }

    @Override
    public List<ChronicCatalogVO> selectCatalogList() {
        ChronicCatalogQueryPageDTO query = new ChronicCatalogQueryPageDTO();
        query.setStatus(1);
        return bizYbChronicCatalogMapper.selectList(catalogWrapper(query)).stream().map(this::toCatalogVO).toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ChronicCatalogVO catalogUpsert(ChronicCatalogUpsertDTO dto) {
        String code = dto.getDiseaseCode().trim();
        BizYbChronicCatalog dup = bizYbChronicCatalogMapper.selectOne(new LambdaQueryWrapper<BizYbChronicCatalog>()
                .eq(BizYbChronicCatalog::getDiseaseCode, code)
                .ne(dto.getId() != null, BizYbChronicCatalog::getId, dto.getId())
                .last("LIMIT 1"));
        if (dup != null) {
            throw new BusinessException("病种编码已存在：" + code);
        }
        BizYbChronicCatalog entity;
        boolean creating = dto.getId() == null;
        if (creating) {
            entity = new BizYbChronicCatalog();
            entity.setStatus(dto.getStatus() == null ? 1 : dto.getStatus());
        } else {
            entity = requireCatalog(dto.getId());
            if (dto.getStatus() != null) {
                entity.setStatus(dto.getStatus());
            }
        }
        entity.setDiseaseCode(code);
        entity.setDiseaseName(TextUtil.cut(dto.getDiseaseName(), 200));
        entity.setDiseaseType(dto.getDiseaseType());
        entity.setIcdCode(TextUtil.cut(dto.getIcdCode(), 32));
        entity.setDefaultValidMonths(dto.getDefaultValidMonths());
        entity.setRemark(TextUtil.cut(dto.getRemark(), 500));
        entity.setUpdateBy(UserUtils.getCurrentUser().getRealName());
        if (creating) {
            bizYbChronicCatalogMapper.insert(entity);
        } else {
            bizYbChronicCatalogMapper.updateById(entity);
        }
        return toCatalogVO(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changeCatalogStatus(Long id, Integer status) {
        // C/D 类保留：入参是 @RequestParam 拆开的 Long/Integer（无 DTO 承载），status 0/1 属码值合法性校验
        if (id == null || status == null || (status != 0 && status != 1)) {
            throw new BusinessException("参数不合法：id 与 status(0/1) 必填");
        }
        BizYbChronicCatalog entity = requireCatalog(id);
        entity.setStatus(status);
        entity.setUpdateBy(UserUtils.getCurrentUser().getRealName());
        bizYbChronicCatalogMapper.updateById(entity);
    }

    @Override
    public PageResult<ChronicRegListVO> regListPage(ChronicRegQueryPageDTO queryDTO) {
        LocalDate today = LocalDate.now();
        LambdaQueryWrapper<BizYbChronicReg> wrapper = new LambdaQueryWrapper<BizYbChronicReg>()
                .eq(queryDTO.getRegStatus() != null, BizYbChronicReg::getRegStatus, queryDTO.getRegStatus())
                .eq(queryDTO.getDiseaseType() != null, BizYbChronicReg::getDiseaseType, queryDTO.getDiseaseType())
                .eq(queryDTO.getCatalogId() != null, BizYbChronicReg::getCatalogId, queryDTO.getCatalogId())
                .eq(queryDTO.getPatientId() != null, BizYbChronicReg::getPatientId, queryDTO.getPatientId())
                .and(isText(queryDTO.getKeyword()), w -> w
                        .like(BizYbChronicReg::getRegNo, queryDTO.getKeyword())
                        .or().like(BizYbChronicReg::getPatientName, queryDTO.getKeyword())
                        .or().like(BizYbChronicReg::getPatientNo, queryDTO.getKeyword())
                        .or().like(BizYbChronicReg::getDiseaseName, queryDTO.getKeyword())
                        .or().like(BizYbChronicReg::getRegisterEmpName, queryDTO.getKeyword()))
                // 台账先看有没有在用，再看登记时间；诊断依据是长文本，列表不捞
                .select(BizYbChronicReg.class, field -> !"certifyBasis".equals(field.getProperty()))
                .orderByAsc(BizYbChronicReg::getRegStatus)
                .orderByDesc(BizYbChronicReg::getRegisterDate)
                .orderByDesc(BizYbChronicReg::getId);
        if (Boolean.TRUE.equals(queryDTO.getOnlyExpired())) {
            wrapper.eq(BizYbChronicReg::getRegStatus, REG_VALID)
                    .isNotNull(BizYbChronicReg::getValidEnd)
                    .lt(BizYbChronicReg::getValidEnd, today);
        }
        IPage<BizYbChronicReg> page = bizYbChronicRegMapper.selectPage(new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize()), wrapper);
        List<ChronicRegListVO> voList = page.getRecords().stream().map(entity -> toRegVO(entity, today)).toList();
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), voList);
    }

    @Override
    public ChronicRegListVO regGetById(Long id) {
        return toRegVO(requireReg(id), LocalDate.now());
    }

    @Override
    public List<ChronicRegListVO> regActiveOfPatient(Long patientId) {
        if (patientId == null) {
            return List.of();
        }
        LocalDate today = LocalDate.now();
        return bizYbChronicRegMapper.selectList(new LambdaQueryWrapper<BizYbChronicReg>()
                        .eq(BizYbChronicReg::getPatientId, patientId)
                        .eq(BizYbChronicReg::getRegStatus, REG_VALID)
                        .orderByDesc(BizYbChronicReg::getRegisterDate))
                .stream().map(entity -> toRegVO(entity, today)).toList();
    }

    @Override
    public ChronicRegSummaryVO regSummary() {
        ChronicRegSummaryVO vo = bizYbChronicRegMapper.summary();
        return vo == null ? new ChronicRegSummaryVO() : vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ChronicRegListVO regUpsert(ChronicRegUpsertDTO dto) {
        if (dto.getValidEnd() != null && dto.getValidEnd().isBefore(dto.getValidStart())) {
            throw new BusinessException("待遇终止日不能早于生效日");
        }
        BizYbChronicCatalog catalog = requireCatalog(dto.getCatalogId());
        if (!Integer.valueOf(1).equals(catalog.getStatus())) {
            throw new BusinessException("病种「" + catalog.getDiseaseName() + "」已停用，请先启用目录或换病种");
        }
        PatientBriefVO patient = patientGateway.findPatient(dto.getPatientId());
        if (patient == null) {
            throw new BusinessException("患者不存在或已删除");
        }
        LocalDate validEndKey = dto.getValidEnd() == null ? BizYbChronicReg.LONG_TERM_KEY : dto.getValidEnd();
        BizYbChronicReg entity;
        boolean creating = dto.getId() == null;
        if (creating) {
            assertNoActiveDuplicate(dto.getPatientId(), catalog.getDiseaseCode(), validEndKey, null);
            entity = new BizYbChronicReg();
            entity.setRegNo(redisSequenceService.generateChronicRegNo());
            entity.setRegStatus(REG_VALID);
        } else {
            entity = requireReg(dto.getId());
            if (!Integer.valueOf(REG_VALID).equals(entity.getRegStatus())) {
                throw new BusinessException("仅「有效」的备案可修改；已注销/已驳回是终态，请另起新单");
            }
            assertNoActiveDuplicate(dto.getPatientId(), catalog.getDiseaseCode(), validEndKey, entity.getId());
        }
        entity.setPatientId(patient.getId());
        entity.setPatientName(TextUtil.cut(patient.getPatientName(), 50));
        entity.setPatientNo(TextUtil.cut(patient.getPatientNo(), 32));
        entity.setMedicalInsuranceNo(TextUtil.cut(patient.getMedicalInsuranceNo(), 32));
        entity.setCatalogId(catalog.getId());
        entity.setDiseaseCode(catalog.getDiseaseCode());
        entity.setDiseaseName(catalog.getDiseaseName());
        entity.setDiseaseType(catalog.getDiseaseType());
        entity.setCertifyDeptId(dto.getCertifyDeptId());
        entity.setCertifyDeptName(TextUtil.cut(dto.getCertifyDeptName(), 100));
        entity.setCertifyDoctorName(TextUtil.cut(dto.getCertifyDoctorName(), 64));
        entity.setCertifyDate(dto.getCertifyDate());
        entity.setCertifyBasis(TextUtil.cut(dto.getCertifyBasis(), 500));
        entity.setRegisterDeptId(dto.getRegisterDeptId());
        entity.setRegisterDeptName(TextUtil.cut(dto.getRegisterDeptName(), 100));
        applyRegisterEmployee(entity, dto, creating);
        entity.setRegisterDate(dto.getRegisterDate());
        entity.setValidStart(dto.getValidStart());
        entity.setValidEnd(dto.getValidEnd());
        entity.setValidEndKey(validEndKey);
        entity.setRemark(TextUtil.cut(dto.getRemark(), 500));
        entity.setUpdateBy(UserUtils.getCurrentUser().getRealName());
        if (creating) {
            bizYbChronicRegMapper.insert(entity);
        } else {
            bizYbChronicRegMapper.updateById(entity);
        }
        return toRegVO(entity, LocalDate.now());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void regCancel(ChronicRegTerminalDTO dto) {
        BizYbChronicReg entity = requireReg(dto.getId());
        if (!Integer.valueOf(REG_VALID).equals(entity.getRegStatus())) {
            throw new BusinessException("仅「有效」的备案可注销，已注销/已驳回是终态");
        }
        String operator = UserUtils.getCurrentUser().getRealName();
        entity.setRegStatus(REG_CANCELLED);
        entity.setCancelReason(TextUtil.cut(dto.getReason(), 500));
        entity.setCancelBy(operator);
        entity.setCancelTime(LocalDateTime.now());
        entity.setUpdateBy(operator);
        bizYbChronicRegMapper.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void regReject(ChronicRegTerminalDTO dto) {
        BizYbChronicReg entity = requireReg(dto.getId());
        if (!Integer.valueOf(REG_VALID).equals(entity.getRegStatus())) {
            throw new BusinessException("仅「有效」的备案可驳回，已注销/已驳回是终态");
        }
        String operator = UserUtils.getCurrentUser().getRealName();
        entity.setRegStatus(REG_REJECTED);
        entity.setRejectReason(TextUtil.cut(dto.getReason(), 500));
        entity.setRejectBy(operator);
        entity.setRejectTime(LocalDateTime.now());
        entity.setUpdateBy(operator);
        bizYbChronicRegMapper.updateById(entity);
    }

    /**
     * 经办人归属：新建不传姓名就落当前登录人（谁点保存就是谁办的）；
     * 传了姓名视为外部经办机构人员，registerEmpId 置空，且必须写备注说明。
     */
    private void applyRegisterEmployee(BizYbChronicReg entity, ChronicRegUpsertDTO dto, boolean creating) {
        String inputName = dto.getRegisterEmpName() == null ? null : dto.getRegisterEmpName().trim();
        if (inputName == null || inputName.isEmpty()) {
            entity.setRegisterEmpName(TextUtil.cut(UserUtils.getCurrentUser().getRealName(), 64));
            entity.setRegisterEmpId(UserUtils.getCurrentUser().getEmployeeId());
            return;
        }
        if (creating && !Objects.equals(inputName, UserUtils.getCurrentUser().getRealName()) && !isText(dto.getRemark())) {
            throw new BusinessException("经办人不是当前登录人（外部机构代办）时，必须在备注写明原因，例如「XX市医保中心窗口张XX代办」");
        }
        entity.setRegisterEmpName(TextUtil.cut(inputName, 64));
        entity.setRegisterEmpId(Objects.equals(inputName, UserUtils.getCurrentUser().getRealName())
                ? UserUtils.getCurrentUser().getEmployeeId() : null);
    }

    /**
     * 同患者同病种同时只许一条有效备案（数据库 uk_chronic_active 兜底，这里预检成人话）。
     * 已过期（validEnd 早于今天）的行不再占位，允许续备。
     */
    private void assertNoActiveDuplicate(Long patientId, String diseaseCode, LocalDate validEndKey, Long excludeId) {
        LocalDate today = LocalDate.now();
        List<BizYbChronicReg> actives = bizYbChronicRegMapper.selectList(new LambdaQueryWrapper<BizYbChronicReg>()
                .eq(BizYbChronicReg::getPatientId, patientId)
                .eq(BizYbChronicReg::getDiseaseCode, diseaseCode)
                .eq(BizYbChronicReg::getRegStatus, REG_VALID)
                .ne(excludeId != null, BizYbChronicReg::getId, excludeId)
                .and(w -> w.isNull(BizYbChronicReg::getValidEnd).or().ge(BizYbChronicReg::getValidEnd, today)));
        if (!actives.isEmpty()) {
            BizYbChronicReg hit = actives.get(0);
            throw new BusinessException("该患者在此病种下已有有效备案（单号 " + hit.getRegNo()
                    + "，待遇至 " + (hit.getValidEnd() == null ? "长期" : hit.getValidEnd())
                    + "），不能重复备案；如需变更请先注销原单");
        }
        // 唯一键含 valid_end_key，同一终止日的两条有效单会直接撞库，先给出可读提示
        BizYbChronicReg sameKey = bizYbChronicRegMapper.selectOne(new LambdaQueryWrapper<BizYbChronicReg>()
                .eq(BizYbChronicReg::getPatientId, patientId)
                .eq(BizYbChronicReg::getDiseaseCode, diseaseCode)
                .eq(BizYbChronicReg::getRegStatus, REG_VALID)
                .eq(BizYbChronicReg::getValidEndKey, validEndKey)
                .ne(excludeId != null, BizYbChronicReg::getId, excludeId)
                .last("LIMIT 1"));
        if (sameKey != null) {
            throw new BusinessException("同一患者同一病种在相同待遇期限下已存在备案（单号 " + sameKey.getRegNo() + "）");
        }
    }

    private BizYbChronicCatalog requireCatalog(Long id) {
        BizYbChronicCatalog entity = id == null ? null : bizYbChronicCatalogMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException("慢特病病种目录不存在或已删除");
        }
        return entity;
    }

    private BizYbChronicReg requireReg(Long id) {
        BizYbChronicReg entity = id == null ? null : bizYbChronicRegMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException("慢特病备案不存在或已删除");
        }
        return entity;
    }

    private LambdaQueryWrapper<BizYbChronicCatalog> catalogWrapper(ChronicCatalogQueryPageDTO queryDTO) {
        return new LambdaQueryWrapper<BizYbChronicCatalog>()
                .eq(queryDTO.getDiseaseType() != null, BizYbChronicCatalog::getDiseaseType, queryDTO.getDiseaseType())
                .eq(queryDTO.getStatus() != null, BizYbChronicCatalog::getStatus, queryDTO.getStatus())
                .and(isText(queryDTO.getKeyword()), w -> w
                        .like(BizYbChronicCatalog::getDiseaseCode, queryDTO.getKeyword())
                        .or().like(BizYbChronicCatalog::getDiseaseName, queryDTO.getKeyword())
                        .or().like(BizYbChronicCatalog::getIcdCode, queryDTO.getKeyword()))
                .orderByAsc(BizYbChronicCatalog::getDiseaseType)
                .orderByAsc(BizYbChronicCatalog::getDiseaseCode);
    }

    private ChronicCatalogVO toCatalogVO(BizYbChronicCatalog entity) {
        ChronicCatalogVO vo = new ChronicCatalogVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    private ChronicRegListVO toRegVO(BizYbChronicReg entity, LocalDate today) {
        ChronicRegListVO vo = new ChronicRegListVO();
        BeanUtils.copyProperties(entity, vo);
        // 医保卡号是纯展示字段，出参一律遮码；编辑回显走 getById 之外的明文入口不存在，故这里直接遮
        vo.setMedicalInsuranceNoMasked(SensitiveMaskUtil.maskCardNo(entity.getMedicalInsuranceNo()));
        boolean expired = Integer.valueOf(REG_VALID).equals(entity.getRegStatus())
                && entity.getValidEnd() != null && entity.getValidEnd().isBefore(today);
        vo.setDisplayStatus(expired ? DISPLAY_EXPIRED : entity.getRegStatus());
        vo.setLongTerm(entity.getValidEnd() == null);
        vo.setRemainDays(entity.getValidEnd() == null ? null
                : (int) ChronoUnit.DAYS.between(today, entity.getValidEnd()));
        return vo;
    }

    private boolean isText(String text) {
        return text != null && !text.isBlank();
    }

}

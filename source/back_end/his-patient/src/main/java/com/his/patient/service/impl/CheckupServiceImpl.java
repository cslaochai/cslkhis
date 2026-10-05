package com.his.patient.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.his.common.exception.BusinessException;
import com.his.patient.dto.CheckupDTO;
import com.his.patient.entity.*;
import com.his.patient.enums.CheckupStatusEnum;
import com.his.patient.mapper.*;
import com.his.patient.service.CheckupService;
import com.his.patient.support.DictText;
import com.his.patient.vo.CheckupVO;
import com.his.security.UserUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * 体检服务（套餐 / 登记 / 结果 / 总检）。
 *
 * <p>状态机：1 已登记 → 2 检查中 → 3 已完成 → 4 已出报告，单向流转，4 为终态（禁改禁删）。
 * 登记时按套餐项目预生成结果空行；出报告要求全部明细已录 + 总检结论必填。
 */
@Service
@RequiredArgsConstructor
public class CheckupServiceImpl implements CheckupService {

    private final SysCheckupPackageMapper packageMapper;
    private final SysCheckupPackageItemMapper packageItemMapper;
    private final BizCheckupRecordMapper recordMapper;
    private final BizCheckupResultMapper resultMapper;
    private final BizPatientMapper patientMapper;
    private final DictText dictText;

    // 套餐

    private static String tr(String s) {
        return s == null ? "" : s.trim();
    }

    @Transactional(rollbackFor = Exception.class)
    public CheckupVO.PackageVO savePackage(CheckupDTO.PackageSave dto) {
        String name = dto.getPackageName().trim();
        Long dupId = packageMapper.selectIdByNameIncludeDeleted(name);
        if (dupId != null && !Objects.equals(dupId, dto.getId())) {
            throw new BusinessException("套餐名称已存在：" + name);
        }
        if (dto.getItems() == null || dto.getItems().isEmpty()) {
            throw new BusinessException("套餐至少要有一个项目");
        }
        SysCheckupPackage p;
        boolean update = dto.getId() != null;
        if (update) {
            p = packageMapper.selectById(dto.getId());
            if (p == null || p.getDelFlag() != null && p.getDelFlag() == 1) {
                throw new BusinessException("套餐不存在或已删除");
            }
        } else {
            p = new SysCheckupPackage();
            p.setStatus(dto.getStatus() == null ? 1 : dto.getStatus());
            p.setCreateBy(UserUtils.getCurrentUser().getUsername());
        }
        p.setPackageName(name);
        p.setGenderLimit(dto.getGenderLimit() == null ? 0 : dto.getGenderLimit());
        p.setPrice(dto.getPrice());
        p.setDescription(dto.getDescription());
        p.setStatus(dto.getStatus() == null ? p.getStatus() : dto.getStatus());
        p.setRemark(dto.getRemark());
        p.setUpdateBy(UserUtils.getCurrentUser().getUsername());
        if (update) {
            packageMapper.updateById(p);
        } else {
            packageMapper.insert(p);
        }
        // 明细整单替换（逻辑删旧行）
        List<SysCheckupPackageItem> old = packageItemMapper.selectList(
                new LambdaQueryWrapper<SysCheckupPackageItem>().eq(SysCheckupPackageItem::getPackageId, p.getId()));
        for (SysCheckupPackageItem o : old) {
            if (o.getDelFlag() == null || o.getDelFlag() == 0) {
                o.setDelFlag(1);
                o.setUpdateBy(UserUtils.getCurrentUser().getUsername());
                packageItemMapper.updateById(o);
            }
        }
        int sort = 0;
        for (CheckupDTO.PackageSave.Item item : dto.getItems()) {
            SysCheckupPackageItem it = new SysCheckupPackageItem();
            it.setPackageId(p.getId());
            it.setItemName(item.getItemName().trim());
            it.setItemType(item.getItemType() == null ? 1 : item.getItemType());
            it.setRefStandard(item.getRefStandard());
            it.setAmount(item.getAmount());
            it.setSortOrder(sort++);
            it.setDelFlag(0);
            it.setCreateBy(UserUtils.getCurrentUser().getUsername());
            packageItemMapper.insert(it);
        }
        return toPackageVo(p);
    }

    public IPage<CheckupVO.PackageVO> packagePage(CheckupDTO.PackageQuery dto) {
        LambdaQueryWrapper<SysCheckupPackage> qw = new LambdaQueryWrapper<SysCheckupPackage>()
                .like(StringUtils.hasText(dto.getKeyword()), SysCheckupPackage::getPackageName, tr(dto.getKeyword()))
                .eq(dto.getStatus() != null, SysCheckupPackage::getStatus, dto.getStatus())
                .orderByDesc(SysCheckupPackage::getId);
        IPage<SysCheckupPackage> page = packageMapper.selectPage(Page.of(dto.getPageNum(), dto.getPageSize()), qw);
        return page.convert(this::toPackageVo);
    }

    public CheckupVO.PackageVO getPackage(Long id) {
        SysCheckupPackage p = packageMapper.selectById(id);
        if (p == null || p.getDelFlag() != null && p.getDelFlag() == 1) {
            throw new BusinessException("套餐不存在或已删除");
        }
        return toPackageVo(p);
    }

    // 登记 / 结果 / 总检

    /**
     * 停用套餐：已停用不可再用于新登记
     */
    @Transactional(rollbackFor = Exception.class)
    public void disablePackage(Long id) {
        SysCheckupPackage p = packageMapper.selectById(id);
        if (p == null || p.getDelFlag() != null && p.getDelFlag() == 1) {
            throw new BusinessException("套餐不存在或已删除");
        }
        p.setStatus(0);
        p.setUpdateBy(UserUtils.getCurrentUser().getUsername());
        packageMapper.updateById(p);
    }

    @Transactional(rollbackFor = Exception.class)
    public CheckupVO.RecordVO createRecord(CheckupDTO.RecordCreate dto) {
        SysCheckupPackage p = packageMapper.selectById(dto.getPackageId());
        if (p == null || p.getDelFlag() != null && p.getDelFlag() == 1) {
            throw new BusinessException("套餐不存在或已删除");
        }
        if (p.getStatus() == null || p.getStatus() != 1) {
            throw new BusinessException("套餐已停用，不能用于新登记");
        }
        BizPatient patient = patientMapper.selectById(dto.getPatientId());
        if (patient == null) {
            throw new BusinessException("体检人不存在");
        }
        if (p.getGenderLimit() != null && p.getGenderLimit() > 0
                && patient.getGender() != null && !patient.getGender().equals(p.getGenderLimit())) {
            throw new BusinessException("套餐性别限制：该套餐仅适用于" + dictText.text("sys_gender", p.getGenderLimit()));
        }
        BizCheckupRecord r = new BizCheckupRecord();
        r.setRecordNo("CU" + DateTimeFormatter.ofPattern("yyyyMMddHHmmss").format(LocalDateTime.now())
                + ThreadLocalRandom.current().nextInt(100, 1000));
        r.setPatientId(patient.getId());
        r.setPatientName(patient.getPatientName());
        r.setGender(patient.getGender());
        r.setAge(patient.getAge());
        r.setPhone(patient.getPhone());
        r.setPersonType(dto.getPersonType() == null ? 1 : dto.getPersonType());
        r.setPackageId(p.getId());
        r.setPackageName(p.getPackageName());
        r.setTotalAmount(p.getPrice());
        r.setCheckupDate(dto.getCheckupDate());
        r.setRecordStatus(CheckupStatusEnum.REGISTERED.getCode());
        r.setCreateBy(UserUtils.getCurrentUser().getUsername());
        r.setRemark(dto.getRemark());
        recordMapper.insert(r);
        // 按套餐项目预生成结果空行
        List<SysCheckupPackageItem> items = packageItemMapper.selectList(
                new LambdaQueryWrapper<SysCheckupPackageItem>()
                        .eq(SysCheckupPackageItem::getPackageId, p.getId())
                        .eq(SysCheckupPackageItem::getDelFlag, 0)
                        .orderByAsc(SysCheckupPackageItem::getSortOrder)
                        .orderByAsc(SysCheckupPackageItem::getId));
        if (items.isEmpty()) {
            throw new BusinessException("套餐没有可用项目，请先维护套餐明细");
        }
        for (SysCheckupPackageItem item : items) {
            BizCheckupResult res = new BizCheckupResult();
            res.setRecordId(r.getId());
            res.setItemName(item.getItemName());
            res.setItemType(item.getItemType());
            res.setRefStandard(item.getRefStandard());
            res.setAbnormalFlag(0);
            res.setCreateBy(UserUtils.getCurrentUser().getUsername());
            resultMapper.insert(res);
        }
        return toRecordVo(r, false);
    }

    public IPage<CheckupVO.RecordVO> recordPage(CheckupDTO.RecordQuery dto) {
        LambdaQueryWrapper<BizCheckupRecord> qw = new LambdaQueryWrapper<BizCheckupRecord>()
                .and(StringUtils.hasText(dto.getKeyword()), w -> w
                        .like(BizCheckupRecord::getPatientName, tr(dto.getKeyword()))
                        .or().like(BizCheckupRecord::getRecordNo, tr(dto.getKeyword())))
                .eq(dto.getRecordStatus() != null, BizCheckupRecord::getRecordStatus, dto.getRecordStatus())
                .eq(dto.getPersonType() != null, BizCheckupRecord::getPersonType, dto.getPersonType())
                .eq(dto.getCheckupDate() != null, BizCheckupRecord::getCheckupDate, dto.getCheckupDate())
                .orderByDesc(BizCheckupRecord::getCheckupDate)
                .orderByDesc(BizCheckupRecord::getId);
        IPage<BizCheckupRecord> page = recordMapper.selectPage(Page.of(dto.getPageNum(), dto.getPageSize()), qw);
        return page.convert(r -> toRecordVo(r, false));
    }

    public CheckupVO.RecordVO getRecord(Long id) {
        BizCheckupRecord r = recordMapper.selectById(id);
        if (r == null || r.getDelFlag() != null && r.getDelFlag() == 1) {
            throw new BusinessException("体检登记不存在或已删除");
        }
        return toRecordVo(r, true);
    }

    /**
     * 单项结果录入：已出报告（4）禁改；录入时状态 1/2 → 2
     */
    @Transactional(rollbackFor = Exception.class)
    public CheckupVO.ResultVO saveResult(CheckupDTO.ResultSave dto) {
        BizCheckupResult res = resultMapper.selectById(dto.getResultId());
        if (res == null || res.getDelFlag() != null && res.getDelFlag() == 1) {
            throw new BusinessException("结果行不存在或已删除");
        }
        BizCheckupRecord r = recordMapper.selectById(res.getRecordId());
        if (r == null || r.getDelFlag() != null && r.getDelFlag() == 1) {
            throw new BusinessException("体检登记不存在或已删除");
        }
        if (r.getRecordStatus() == CheckupStatusEnum.REPORTED.getCode()) {
            throw new BusinessException("已出报告，结果禁止修改");
        }
        res.setResultValue(dto.getResultValue());
        res.setAbnormalFlag(dto.getAbnormalFlag() == null ? 0 : dto.getAbnormalFlag());
        res.setSummaryText(dto.getSummaryText());
        res.setCheckerName(dto.getCheckerName());
        res.setResultTime(LocalDateTime.now().truncatedTo(java.time.temporal.ChronoUnit.SECONDS));
        res.setUpdateBy(UserUtils.getCurrentUser().getUsername());
        resultMapper.updateById(res);
        if (r.getRecordStatus() == CheckupStatusEnum.REGISTERED.getCode()) {
            r.setRecordStatus(CheckupStatusEnum.IN_PROGRESS.getCode());
            r.setUpdateBy(UserUtils.getCurrentUser().getUsername());
            recordMapper.updateById(r);
        }
        return toResultVo(res);
    }

    /**
     * 总检出报告：要求状态 3 已完成（全部明细已录）且结论必填；4 为终态
     */
    @Transactional(rollbackFor = Exception.class)
    public CheckupVO.RecordVO conclude(CheckupDTO.Conclusion dto) {
        BizCheckupRecord r = recordMapper.selectById(dto.getRecordId());
        if (r == null || r.getDelFlag() != null && r.getDelFlag() == 1) {
            throw new BusinessException("体检登记不存在或已删除");
        }
        if (r.getRecordStatus() == CheckupStatusEnum.REPORTED.getCode()) {
            throw new BusinessException("已出报告，禁止重复总检");
        }
        if (r.getRecordStatus() != CheckupStatusEnum.FINISHED.getCode()) {
            throw new BusinessException("存在未录入的结果项，不能总检出报告");
        }
        r.setConclusion(dto.getConclusion().trim());
        r.setDoctorName(dto.getDoctorName());
        r.setRecordStatus(CheckupStatusEnum.REPORTED.getCode());
        r.setReportTime(LocalDateTime.now().truncatedTo(java.time.temporal.ChronoUnit.SECONDS));
        r.setUpdateBy(UserUtils.getCurrentUser().getUsername());
        recordMapper.updateById(r);
        return toRecordVo(r, true);
    }

    /**
     * 开始体检：1 → 2（仅推进状态）
     */
    @Transactional(rollbackFor = Exception.class)
    public void startCheckup(Long recordId) {
        BizCheckupRecord r = recordMapper.selectById(recordId);
        if (r == null || r.getDelFlag() != null && r.getDelFlag() == 1) {
            throw new BusinessException("体检登记不存在或已删除");
        }
        if (r.getRecordStatus() != CheckupStatusEnum.REGISTERED.getCode()) {
            throw new BusinessException("只有已登记状态可以开始体检");
        }
        r.setRecordStatus(CheckupStatusEnum.IN_PROGRESS.getCode());
        r.setUpdateBy(UserUtils.getCurrentUser().getUsername());
        recordMapper.updateById(r);
    }

    // 转换

    /**
     * 删除登记：仅 1 已登记可删；软删
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteRecord(Long recordId) {
        BizCheckupRecord r = recordMapper.selectById(recordId);
        if (r == null || r.getDelFlag() != null && r.getDelFlag() == 1) {
            throw new BusinessException("体检登记不存在或已删除");
        }
        if (r.getRecordStatus() != CheckupStatusEnum.REGISTERED.getCode()) {
            throw new BusinessException("已开始的体检登记不能删除");
        }
        r.setDelFlag(1);
        r.setUpdateBy(UserUtils.getCurrentUser().getUsername());
        recordMapper.updateById(r);
    }

    private CheckupVO.PackageVO toPackageVo(SysCheckupPackage p) {
        CheckupVO.PackageVO vo = new CheckupVO.PackageVO();
        vo.setId(p.getId());
        vo.setPackageName(p.getPackageName());
        vo.setPackageCode(p.getPackageCode());
        vo.setGenderLimit(p.getGenderLimit());
        vo.setPrice(p.getPrice());
        vo.setDescription(p.getDescription());
        vo.setStatus(p.getStatus());
        vo.setRemark(p.getRemark());
        vo.setItems(packageItemMapper.selectList(new LambdaQueryWrapper<SysCheckupPackageItem>()
                        .eq(SysCheckupPackageItem::getPackageId, p.getId())
                        .eq(SysCheckupPackageItem::getDelFlag, 0)
                        .orderByAsc(SysCheckupPackageItem::getSortOrder)
                        .orderByAsc(SysCheckupPackageItem::getId))
                .stream().map(it -> {
                    CheckupVO.PackageItemVO iv = new CheckupVO.PackageItemVO();
                    iv.setId(it.getId());
                    iv.setItemName(it.getItemName());
                    iv.setItemType(it.getItemType());
                    iv.setRefStandard(it.getRefStandard());
                    iv.setAmount(it.getAmount());
                    return iv;
                }).collect(Collectors.toList()));
        return vo;
    }

    private CheckupVO.RecordVO toRecordVo(BizCheckupRecord r, boolean withResults) {
        CheckupVO.RecordVO vo = new CheckupVO.RecordVO();
        vo.setId(r.getId());
        vo.setRecordNo(r.getRecordNo());
        vo.setPatientId(r.getPatientId());
        vo.setPatientName(r.getPatientName());
        vo.setGender(r.getGender());
        vo.setAge(r.getAge());
        vo.setPhone(r.getPhone());
        vo.setPersonType(r.getPersonType());
        vo.setPackageId(r.getPackageId());
        vo.setPackageName(r.getPackageName());
        vo.setTotalAmount(r.getTotalAmount());
        vo.setCheckupDate(r.getCheckupDate());
        vo.setRecordStatus(r.getRecordStatus());
        vo.setConclusion(r.getConclusion());
        vo.setDoctorName(r.getDoctorName());
        vo.setReportTime(r.getReportTime());
        vo.setRemark(r.getRemark());
        if (withResults) {
            vo.setResults(resultMapper.selectList(new LambdaQueryWrapper<BizCheckupResult>()
                            .eq(BizCheckupResult::getRecordId, r.getId())
                            .eq(BizCheckupResult::getDelFlag, 0)
                            .orderByAsc(BizCheckupResult::getItemType)
                            .orderByAsc(BizCheckupResult::getId))
                    .stream().map(this::toResultVo).collect(Collectors.toList()));
        }
        return vo;
    }

    private CheckupVO.ResultVO toResultVo(BizCheckupResult res) {
        CheckupVO.ResultVO vo = new CheckupVO.ResultVO();
        vo.setId(res.getId());
        vo.setItemName(res.getItemName());
        vo.setItemType(res.getItemType());
        vo.setRefStandard(res.getRefStandard());
        vo.setResultValue(res.getResultValue());
        vo.setAbnormalFlag(res.getAbnormalFlag());
        vo.setSummaryText(res.getSummaryText());
        vo.setCheckerName(res.getCheckerName());
        vo.setResultTime(res.getResultTime());
        return vo;
    }

    /**
     * 总检前置校验复用：全部明细已录（resultValue 非空）才算 3 已完成 —— 在读接口里顺带判定并落状态
     */
    public CheckupVO.RecordVO refreshFinishStatus(Long recordId) {
        BizCheckupRecord r = recordMapper.selectById(recordId);
        if (r == null || r.getDelFlag() != null && r.getDelFlag() == 1) {
            throw new BusinessException("体检登记不存在或已删除");
        }
        if (r.getRecordStatus() == CheckupStatusEnum.IN_PROGRESS.getCode()) {
            long pending = resultMapper.selectCount(new LambdaQueryWrapper<BizCheckupResult>()
                    .eq(BizCheckupResult::getRecordId, r.getId())
                    .eq(BizCheckupResult::getDelFlag, 0)
                    .and(w -> w.isNull(BizCheckupResult::getResultValue)
                            .or().eq(BizCheckupResult::getResultValue, "")));
            if (pending == 0) {
                r.setRecordStatus(CheckupStatusEnum.FINISHED.getCode());
                r.setUpdateBy(UserUtils.getCurrentUser().getUsername());
                recordMapper.updateById(r);
            }
        }
        return toRecordVo(r, true);
    }
}

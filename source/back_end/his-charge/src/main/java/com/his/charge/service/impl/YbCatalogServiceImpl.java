package com.his.charge.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.his.charge.dto.YbCatalogQueryPageDTO;
import com.his.charge.dto.YbCatalogUpsertDTO;
import com.his.charge.entity.BizYbCatalog;
import com.his.charge.mapper.BizYbCatalogMapper;
import com.his.charge.service.YbCatalogService;
import com.his.charge.vo.BizYbCatalogVO;
import com.his.charge.vo.YbImportResultVO;
import com.his.common.base.PageResult;
import com.his.common.exception.BusinessException;
import com.his.system.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

/**
 * 国家医保目录服务实现。
 *
 * <p>口径：目录只启停不删除（uk_yb_code 不含 del_flag，物理删会断追溯链，
 * 软删会占键——143 脚本头铁律）。文本入库前按列宽截断，防 Data too long。
 */
@Service
@RequiredArgsConstructor
public class YbCatalogServiceImpl implements YbCatalogService {

    private final BizYbCatalogMapper catalogMapper;

    @Override
    public PageResult<BizYbCatalogVO> listPage(YbCatalogQueryPageDTO queryDTO) {
        LambdaQueryWrapper<BizYbCatalog> wrapper = new LambdaQueryWrapper<BizYbCatalog>()
                .eq(queryDTO.getCatalogType() != null, BizYbCatalog::getCatalogType, queryDTO.getCatalogType())
                .eq(queryDTO.getStatus() != null, BizYbCatalog::getStatus, queryDTO.getStatus())
                .and(queryDTO.getKeyword() != null && !queryDTO.getKeyword().isBlank(), w -> w
                        .like(BizYbCatalog::getYbCode, queryDTO.getKeyword())
                        .or().like(BizYbCatalog::getYbName, queryDTO.getKeyword()))
                .orderByAsc(BizYbCatalog::getCatalogType)
                .orderByAsc(BizYbCatalog::getYbCode);
        IPage<BizYbCatalog> page = catalogMapper.selectPage(
                new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize()), wrapper);
        List<BizYbCatalogVO> voList = page.getRecords().stream().map(this::toVO).toList();
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), voList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BizYbCatalogVO upsert(YbCatalogUpsertDTO dto) {
        validate(dto);
        BizYbCatalog entity;
        if (dto.getId() == null) {
            entity = new BizYbCatalog();
            entity.setStatus(dto.getStatus() == null ? 1 : dto.getStatus());
            entity.setCreateBy(UserUtils.getCurrentEmployeeName());
        } else {
            entity = catalogMapper.selectById(dto.getId());
            if (entity == null) {
                throw new BusinessException("目录不存在或已删除");
            }
        }
        entity.setCatalogType(dto.getCatalogType());
        entity.setYbCode(dto.getYbCode().trim());
        entity.setYbName(dto.getYbName().trim());
        entity.setSpec(cut(dto.getSpec(), 100));
        entity.setUnit(cut(dto.getUnit(), 20));
        entity.setDosageForm(cut(dto.getDosageForm(), 50));
        entity.setInsuranceLevel(dto.getInsuranceLevel());
        entity.setPayRatio(dto.getPayRatio());
        entity.setEffectiveDate(parseDate(dto.getEffectiveDate()));
        entity.setExpireDate(parseDate(dto.getExpireDate()));
        if (dto.getStatus() != null) {
            entity.setStatus(dto.getStatus());
        }
        entity.setRemark(cut(dto.getRemark(), 500));
        entity.setUpdateBy(UserUtils.getCurrentEmployeeName());
        if (dto.getId() == null) {
            catalogMapper.insert(entity);
        } else {
            catalogMapper.updateById(entity);
        }
        return toVO(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public YbImportResultVO importBatch(List<YbCatalogUpsertDTO> items) {
        YbImportResultVO result = new YbImportResultVO();
        int inserted = 0;
        int updated = 0;
        int skipped = 0;
        for (YbCatalogUpsertDTO item : items == null ? List.<YbCatalogUpsertDTO>of() : items) {
            if (item == null || item.getCatalogType() == null
                    || isBlank(item.getYbCode()) || isBlank(item.getYbName())) {
                skipped++;
                continue;
            }
            item.setId(null);
            BizYbCatalog existed = catalogMapper.selectOne(new LambdaQueryWrapper<BizYbCatalog>()
                    .eq(BizYbCatalog::getYbCode, item.getYbCode().trim())
                    .last("LIMIT 1"));
            if (existed != null) {
                item.setId(existed.getId());
                item.setStatus(existed.getStatus());
                upsert(item);
                updated++;
            } else {
                upsert(item);
                inserted++;
            }
        }
        result.setInserted(inserted);
        result.setUpdated(updated);
        result.setSkipped(skipped);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changeStatus(Long id, Integer status) {
        // C/D 类保留：入参是 @RequestParam 拆开的 Long/Integer（无 DTO 承载），status 0/1 属码值合法性校验
        if (id == null || status == null || (status != 0 && status != 1)) {
            throw new BusinessException("参数不合法：id 与 status(0/1) 必填");
        }
        BizYbCatalog entity = catalogMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException("目录不存在或已删除");
        }
        entity.setStatus(status);
        entity.setUpdateBy(UserUtils.getCurrentEmployeeName());
        catalogMapper.updateById(entity);
    }

    private void validate(YbCatalogUpsertDTO dto) {
        if (dto.getCatalogType() == null || dto.getCatalogType() < 1 || dto.getCatalogType() > 4) {
            throw new BusinessException("目录类型不合法（1-西药/中成药 2-中药饮片 3-医疗服务项目 4-医用耗材）");
        }
        // yb_code 唯一校验（排除自身）
        Long id = dto.getId();
        BizYbCatalog dup = catalogMapper.selectOne(new LambdaQueryWrapper<BizYbCatalog>()
                .eq(BizYbCatalog::getYbCode, dto.getYbCode().trim())
                .ne(id != null, BizYbCatalog::getId, id)
                .last("LIMIT 1"));
        if (dup != null && !Objects.equals(dup.getId(), id)) {
            throw new BusinessException("医保编码已存在：" + dto.getYbCode());
        }
    }

    private BizYbCatalogVO toVO(BizYbCatalog entity) {
        BizYbCatalogVO vo = new BizYbCatalogVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    private LocalDate parseDate(String text) {
        return isBlank(text) ? null : LocalDate.parse(text.trim(), DateTimeFormatter.ISO_LOCAL_DATE);
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    /**
     * 写库文本先截列宽（铁律：Data too long 会把业务失败升级 500）
     */
    private String cut(String s, int max) {
        if (s == null) {
            return null;
        }
        return s.length() <= max ? s : s.substring(0, max);
    }
}

package com.his.charge.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.charge.dto.YbCatalogQueryPageDTO;
import com.his.charge.dto.YbCatalogUpsertDTO;
import com.his.charge.entity.BizYbCatalog;
import com.his.charge.mapper.BizYbCatalogMapper;
import com.his.charge.service.YbCatalogService;
import com.his.charge.vo.BizYbCatalogVO;
import com.his.charge.vo.YbImportResultVO;
import com.his.common.base.PageResult;
import com.his.common.exception.BusinessException;
import com.his.common.util.TextUtil;
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
 */
@Service
@RequiredArgsConstructor
public class YbCatalogServiceImpl extends ServiceImpl<BizYbCatalogMapper, BizYbCatalog> implements YbCatalogService {

    private final BizYbCatalogMapper bizYbCatalogMapper;

    @Override
    public PageResult<BizYbCatalogVO> listPage(YbCatalogQueryPageDTO queryDTO) {
        LambdaQueryWrapper<BizYbCatalog> wrapper = new LambdaQueryWrapper<BizYbCatalog>()
                .eq(queryDTO.getCatalogType() != null, BizYbCatalog::getCatalogType, queryDTO.getCatalogType())
                .eq(queryDTO.getStatus() != null, BizYbCatalog::getStatus, queryDTO.getStatus())
                .and(TextUtil.hasText(queryDTO.getKeyword()), w -> w
                        .like(BizYbCatalog::getYbCode, queryDTO.getKeyword())
                        .or().like(BizYbCatalog::getYbName, queryDTO.getKeyword()))
                .orderByAsc(BizYbCatalog::getCatalogType)
                .orderByAsc(BizYbCatalog::getYbCode);
        IPage<BizYbCatalog> page = bizYbCatalogMapper.selectPage(
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
            entity.setCreateBy(UserUtils.getCurrentUser().getRealName());
        } else {
            entity = bizYbCatalogMapper.selectById(dto.getId());
            if (entity == null) {
                throw new BusinessException("目录不存在或已删除");
            }
        }
        entity.setCatalogType(dto.getCatalogType());
        entity.setYbCode(dto.getYbCode().trim());
        entity.setYbName(dto.getYbName().trim());
        entity.setSpec(TextUtil.cut(dto.getSpec(), 100));
        entity.setUnit(TextUtil.cut(dto.getUnit(), 20));
        entity.setDosageForm(TextUtil.cut(dto.getDosageForm(), 50));
        entity.setInsuranceLevel(dto.getInsuranceLevel());
        entity.setPayRatio(dto.getPayRatio());
        entity.setEffectiveDate(parseDate(dto.getEffectiveDate()));
        entity.setExpireDate(parseDate(dto.getExpireDate()));
        if (dto.getStatus() != null) {
            entity.setStatus(dto.getStatus());
        }
        entity.setRemark(TextUtil.cut(dto.getRemark(), 500));
        entity.setUpdateBy(UserUtils.getCurrentUser().getRealName());
        if (dto.getId() == null) {
            bizYbCatalogMapper.insert(entity);
        } else {
            bizYbCatalogMapper.updateById(entity);
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
                    || !TextUtil.hasText(item.getYbCode()) || !TextUtil.hasText(item.getYbName())) {
                skipped++;
                continue;
            }
            item.setId(null);
            BizYbCatalog existed = bizYbCatalogMapper.selectOne(new LambdaQueryWrapper<BizYbCatalog>()
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
        // D-业务规则：id/status 必填与 status 0/1 码值合法性写在同一条件里，整条保留不拆（拆分会把一条 500 拆成 400+500 两条路径，收益为零）
        if (id == null || status == null || (status != 0 && status != 1)) {
            throw new BusinessException("参数不合法：id 与 status(0/1) 必填");
        }
        BizYbCatalog entity = bizYbCatalogMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException("目录不存在或已删除");
        }
        entity.setStatus(status);
        entity.setUpdateBy(UserUtils.getCurrentUser().getRealName());
        bizYbCatalogMapper.updateById(entity);
    }

    private void validate(YbCatalogUpsertDTO dto) {
        if (dto.getCatalogType() == null || dto.getCatalogType() < 1 || dto.getCatalogType() > 4) {
            throw new BusinessException("目录类型不合法（1-西药/中成药 2-中药饮片 3-医疗服务项目 4-医用耗材）");
        }
        // yb_code 唯一校验（排除自身）
        Long id = dto.getId();
        BizYbCatalog dup = bizYbCatalogMapper.selectOne(new LambdaQueryWrapper<BizYbCatalog>()
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
        return !TextUtil.hasText(text) ? null : LocalDate.parse(text.trim(), DateTimeFormatter.ISO_LOCAL_DATE);
    }
}

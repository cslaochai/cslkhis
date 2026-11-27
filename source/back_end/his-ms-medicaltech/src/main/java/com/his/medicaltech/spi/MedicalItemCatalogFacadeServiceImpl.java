package com.his.medicaltech.spi;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.his.common.enums.CheckupItemSourceEnum;
import com.his.medicaltech.entity.SysInspectionItem;
import com.his.medicaltech.entity.SysLaboratoryItem;
import com.his.medicaltech.mapper.SysInspectionItemMapper;
import com.his.medicaltech.mapper.SysLaboratoryItemMapper;
import com.his.spi.service.MedicalItemCatalogFacadeService;
import com.his.spi.vo.ItemCatalogVO;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 医技项目目录端口实现：只读两张字典表，注入本模块 mapper，不依赖任何编排 Service。
 */
@Service
@RequiredArgsConstructor
public class MedicalItemCatalogFacadeServiceImpl implements MedicalItemCatalogFacadeService {

    /** 参考范围入参列宽（ref_standard varchar(200)） */
    private static final int REF_STANDARD_MAX_LENGTH = 200;

    private final SysLaboratoryItemMapper sysLaboratoryItemMapper;

    private final SysInspectionItemMapper sysInspectionItemMapper;

    @Override
    public List<ItemCatalogVO> listItems(Integer itemSource, String keyword) {
        String kw = StringUtils.trimToEmpty(keyword);
        List<ItemCatalogVO> out = new ArrayList<>();
        if (itemSource == null || itemSource == CheckupItemSourceEnum.LABORATORY.getCode()) {
            out.addAll(listLaboratoryItems(kw));
        }
        if (itemSource == null || itemSource == CheckupItemSourceEnum.INSPECTION.getCode()) {
            out.addAll(listInspectionItems(kw));
        }
        return out;
    }

    private List<ItemCatalogVO> listLaboratoryItems(String kw) {
        List<SysLaboratoryItem> rows = sysLaboratoryItemMapper.selectList(new LambdaQueryWrapper<SysLaboratoryItem>()
                .eq(SysLaboratoryItem::getStatus, 1)
                .and(!kw.isEmpty(), w -> w.like(SysLaboratoryItem::getItemCode, kw)
                        .or().like(SysLaboratoryItem::getItemName, kw))
                .orderByAsc(SysLaboratoryItem::getItemCode));
        List<ItemCatalogVO> out = new ArrayList<>(rows.size());
        for (SysLaboratoryItem r : rows) {
            ItemCatalogVO vo = new ItemCatalogVO();
            vo.setId(r.getId());
            vo.setItemCode(r.getItemCode());
            vo.setItemName(r.getItemName());
            vo.setItemSource(CheckupItemSourceEnum.LABORATORY.getCode());
            vo.setItemType(r.getItemType());
            vo.setPrice(r.getPrice());
            vo.setUnit(r.getUnit());
            vo.setRefStandard(buildRefStandard(r.getReferenceValue()));
            out.add(vo);
        }
        return out;
    }

    private List<ItemCatalogVO> listInspectionItems(String kw) {
        List<SysInspectionItem> rows = sysInspectionItemMapper.selectList(new LambdaQueryWrapper<SysInspectionItem>()
                .eq(SysInspectionItem::getStatus, 1)
                .and(!kw.isEmpty(), w -> w.like(SysInspectionItem::getItemCode, kw)
                        .or().like(SysInspectionItem::getItemName, kw))
                .orderByAsc(SysInspectionItem::getItemCode));
        List<ItemCatalogVO> out = new ArrayList<>(rows.size());
        for (SysInspectionItem r : rows) {
            ItemCatalogVO vo = new ItemCatalogVO();
            vo.setId(r.getId());
            vo.setItemCode(r.getItemCode());
            vo.setItemName(r.getItemName());
            vo.setItemSource(CheckupItemSourceEnum.INSPECTION.getCode());
            vo.setItemType(r.getItemType());
            vo.setPrice(r.getPrice());
            vo.setRefStandard(null);
            out.add(vo);
        }
        return out;
    }

    /**
     * 检验项目的参考范围直接取字典 reference_value（单位单列返回，不拼进范围文本：
     * 字典 unit 常是「10^9/L, 10^12/L, g/L」这类多单位串，拼进范围反而读不通）。
     */
    private String buildRefStandard(String referenceValue) {
        if (StringUtils.isBlank(referenceValue)) {
            return null;
        }
        return StringUtils.left(referenceValue, REF_STANDARD_MAX_LENGTH);
    }
}

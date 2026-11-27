package com.his.clinical.spi;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.his.clinical.entity.SysTreatmentItem;
import com.his.clinical.mapper.SysTreatmentItemMapper;
import com.his.common.enums.CheckupItemSourceEnum;
import com.his.spi.service.TreatmentItemCatalogFacadeService;
import com.his.spi.vo.ItemCatalogVO;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 一般检查（诊疗）项目目录端口实现：只读 {@code sys_treatment_item}，注入本模块 mapper。
 */
@Service
@RequiredArgsConstructor
public class TreatmentItemCatalogFacadeServiceImpl implements TreatmentItemCatalogFacadeService {

    private final SysTreatmentItemMapper sysTreatmentItemMapper;

    @Override
    public List<ItemCatalogVO> listItems(String keyword) {
        String kw = StringUtils.trimToEmpty(keyword);
        List<SysTreatmentItem> rows = sysTreatmentItemMapper.selectList(new LambdaQueryWrapper<SysTreatmentItem>()
                .eq(SysTreatmentItem::getStatus, 1)
                .and(!kw.isEmpty(), w -> w.like(SysTreatmentItem::getItemCode, kw)
                        .or().like(SysTreatmentItem::getItemName, kw))
                .orderByAsc(SysTreatmentItem::getItemCode));
        List<ItemCatalogVO> out = new ArrayList<>(rows.size());
        for (SysTreatmentItem r : rows) {
            ItemCatalogVO vo = new ItemCatalogVO();
            vo.setId(r.getId());
            vo.setItemCode(r.getItemCode());
            vo.setItemName(r.getItemName());
            vo.setItemSource(CheckupItemSourceEnum.GENERAL.getCode());
            vo.setItemType(r.getItemType());
            vo.setPrice(r.getPrice());
            vo.setRefStandard(null);
            out.add(vo);
        }
        return out;
    }
}

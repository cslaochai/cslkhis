package com.his.pharmacy.spi;

import com.his.pharmacy.entity.SysDrug;
import com.his.pharmacy.mapper.SysDrugMapper;
import com.his.spi.service.DrugMasterFacadeService;
import com.his.spi.vo.DrugBriefVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 药品主数据端口实现：只读 sys_drug，不牵发药与库存编排。
 */
@Service
@RequiredArgsConstructor
public class DrugMasterFacadeServiceImpl implements DrugMasterFacadeService {

    private final SysDrugMapper sysDrugMapper;

    @Override
    public DrugBriefVO findDrug(Long drugId) {
        if (drugId == null) {
            return null;
        }
        return toBrief(sysDrugMapper.selectById(drugId));
    }

    @Override
    public List<DrugBriefVO> listDrugs(Collection<Long> drugIds) {
        if (CollectionUtils.isEmpty(drugIds)) {
            return Collections.emptyList();
        }
        List<SysDrug> list = sysDrugMapper.selectBatchIds(drugIds);
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyList();
        }
        return list.stream().map(this::toBrief).collect(Collectors.toList());
    }

    private DrugBriefVO toBrief(SysDrug drug) {
        if (drug == null) {
            return null;
        }
        DrugBriefVO vo = new DrugBriefVO();
        vo.setId(drug.getId());
        vo.setDrugCode(drug.getDrugCode());
        vo.setDrugName(drug.getDrugName());
        vo.setGenericName(drug.getGenericName());
        vo.setDrugType(drug.getDrugType());
        vo.setSpecification(drug.getSpecification());
        vo.setUnit(drug.getUnit());
        vo.setGramPerUnit(drug.getGramPerUnit());
        vo.setRetailPrice(drug.getRetailPrice());
        vo.setIsSkinTest(drug.getIsSkinTest());
        vo.setContraindication(drug.getContraindication());
        return vo;
    }
}

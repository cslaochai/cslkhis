package com.his.pharmacy.spi;

import com.his.pharmacy.service.DrugRationalCheckService;
import com.his.spi.dto.DrugRationalCheckItemDTO;
import com.his.spi.dto.DrugRationalGroupDTO;
import com.his.spi.service.DrugRationalCheckFacadeService;
import com.his.spi.vo.DrugRationalCheckHitVO;
import com.his.spi.vo.DrugRationalGroupVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 合理用药审查端口实现：判定逻辑全部委托内部 {@link DrugRationalCheckService}，
 * 这里只做 spi DTO/VO 与内部类型的双向映射，不复制任何相互作用/极量匹配规则。
 */
@Service
@RequiredArgsConstructor
public class DrugRationalCheckFacadeServiceImpl implements DrugRationalCheckFacadeService {

    private final DrugRationalCheckService drugRationalCheckService;

    @Override
    public List<DrugRationalCheckHitVO> check(List<DrugRationalCheckItemDTO> items) {
        if (CollectionUtils.isEmpty(items)) {
            return List.of();
        }
        List<com.his.pharmacy.dto.DrugRationalItemDTO> internalItems = new ArrayList<>(items.size());
        for (DrugRationalCheckItemDTO item : items) {
            internalItems.add(toInternalItem(item));
        }
        List<com.his.pharmacy.vo.DrugRationalHitVO> hits = drugRationalCheckService.check(internalItems);
        if (CollectionUtils.isEmpty(hits)) {
            return List.of();
        }
        List<DrugRationalCheckHitVO> result = new ArrayList<>(hits.size());
        for (com.his.pharmacy.vo.DrugRationalHitVO hit : hits) {
            result.add(toHit(hit));
        }
        return result;
    }

    @Override
    public Map<Long, DrugRationalGroupVO> checkGroups(List<DrugRationalGroupDTO> groups) {
        Map<Long, DrugRationalGroupVO> result = new LinkedHashMap<>();
        if (CollectionUtils.isEmpty(groups)) {
            return result;
        }
        List<com.his.pharmacy.dto.DrugRationalGroupDTO> internalGroups = new ArrayList<>(groups.size());
        for (DrugRationalGroupDTO group : groups) {
            if (group == null) {
                continue;
            }
            internalGroups.add(toInternalGroup(group));
        }
        List<com.his.pharmacy.vo.DrugRationalGroupVO> internalResults =
                drugRationalCheckService.checkGroups(internalGroups);
        if (CollectionUtils.isEmpty(internalResults)) {
            return result;
        }
        for (com.his.pharmacy.vo.DrugRationalGroupVO group : internalResults) {
            if (group == null || group.getGroupId() == null) {
                continue;
            }
            result.put(group.getGroupId(), toGroupVO(group));
        }
        return result;
    }

    // ---------------------------------------------------------------- 映射

    private com.his.pharmacy.dto.DrugRationalItemDTO toInternalItem(DrugRationalCheckItemDTO src) {
        com.his.pharmacy.dto.DrugRationalItemDTO item = new com.his.pharmacy.dto.DrugRationalItemDTO();
        item.setDrugId(src.getDrugId());
        item.setDrugName(src.getDrugName());
        item.setGenericName(src.getGenericName());
        item.setSpecification(src.getSpecification());
        item.setSingleDosage(src.getSingleDosage());
        item.setFrequency(src.getFrequency());
        return item;
    }

    private com.his.pharmacy.dto.DrugRationalGroupDTO toInternalGroup(DrugRationalGroupDTO src) {
        com.his.pharmacy.dto.DrugRationalGroupDTO group = new com.his.pharmacy.dto.DrugRationalGroupDTO();
        group.setGroupId(src.getGroupId());
        List<DrugRationalCheckItemDTO> srcItems = src.getItems();
        if (CollectionUtils.isEmpty(srcItems)) {
            group.setItems(List.of());
            return group;
        }
        List<com.his.pharmacy.dto.DrugRationalItemDTO> items = new ArrayList<>(srcItems.size());
        for (DrugRationalCheckItemDTO item : srcItems) {
            items.add(toInternalItem(item));
        }
        group.setItems(items);
        return group;
    }

    private DrugRationalCheckHitVO toHit(com.his.pharmacy.vo.DrugRationalHitVO src) {
        DrugRationalCheckHitVO hit = new DrugRationalCheckHitVO();
        hit.setHitType(src.getHitType());
        hit.setBlocked(src.getBlocked());
        hit.setKnowledgeId(src.getKnowledgeId());
        hit.setDrugNameA(src.getDrugNameA());
        hit.setDrugNameB(src.getDrugNameB());
        hit.setMessage(src.getMessage());
        hit.setSuggestion(src.getSuggestion());
        return hit;
    }

    private DrugRationalGroupVO toGroupVO(com.his.pharmacy.vo.DrugRationalGroupVO src) {
        DrugRationalGroupVO vo = new DrugRationalGroupVO();
        vo.setGroupId(src.getGroupId());
        vo.setBlocked(src.getBlocked());
        vo.setBlockMessage(src.getBlockMessage());
        List<com.his.pharmacy.vo.DrugRationalHitVO> srcHits = src.getHits();
        if (CollectionUtils.isEmpty(srcHits)) {
            vo.setHits(List.of());
            return vo;
        }
        List<DrugRationalCheckHitVO> hits = new ArrayList<>(srcHits.size());
        for (com.his.pharmacy.vo.DrugRationalHitVO hit : srcHits) {
            hits.add(toHit(hit));
        }
        vo.setHits(hits);
        return vo;
    }
}

package com.his.charge.service.impl;

import com.his.charge.dto.YbAutoMatchDTO;
import com.his.charge.dto.YbMapDTO;
import com.his.charge.dto.YbMappingQueryPageDTO;
import com.his.charge.entity.BizYbCatalog;
import com.his.charge.entity.BizYbMapping;
import com.his.charge.mapper.BizYbCatalogMapper;
import com.his.charge.mapper.BizYbMappingMapper;
import com.his.charge.service.YbMappingService;
import com.his.charge.vo.YbAutoMatchResultVO;
import com.his.charge.vo.YbMappingListVO;
import com.his.charge.vo.YbMappingStatsVO;
import com.his.charge.vo.YbUnmappedItemVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.his.common.base.PageResult;
import com.his.common.exception.BusinessException;
import com.his.system.utils.UserUtils;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 医保目录对照服务实现。
 *
 * <p>自动对照口径：院内项目名称与启用目录名称<b>精确相等</b>且唯一命中才落
 * match_type=1；0 条=noMatch、多条=ambiguous（留人工，宁缺勿错）。
 * 候选范围随院内类型走：药品 drug_type 1西药/2中成药→目录1、3饮片→目录2，
 * 诊疗/检验→目录3，耗材→目录4。
 */
@Service
@RequiredArgsConstructor
public class YbMappingServiceImpl implements YbMappingService {

    private final BizYbMappingMapper mappingMapper;
    private final BizYbCatalogMapper catalogMapper;

    @Override
    public PageResult<YbMappingListVO> listPage(YbMappingQueryPageDTO queryDTO) {
        IPage<YbMappingListVO> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        page = switch (queryDTO.getItemType() == null ? 0 : queryDTO.getItemType()) {
            case 1 -> mappingMapper.selectDrugPage(page, queryDTO);
            case 2 -> mappingMapper.selectTreatmentPage(page, queryDTO);
            case 3 -> mappingMapper.selectLaboratoryPage(page, queryDTO);
            case 4 -> mappingMapper.selectConsumablePage(page, queryDTO);
            default -> throw new BusinessException("项目类型不合法（1-药品 2-诊疗项目 3-检验项目 4-耗材）");
        };
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), page.getRecords());
    }

    @Override
    public List<YbMappingStatsVO> stats() {
        return mappingMapper.selectStats();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public YbMappingListVO map(YbMapDTO dto) {
        checkItemType(dto.getItemType());
        checkItemExists(dto.getItemType(), dto.getItemId());
        BizYbCatalog catalog = catalogMapper.selectById(dto.getCatalogId());
        if (catalog == null) {
            throw new BusinessException("医保目录不存在或已删除");
        }
        if (catalog.getStatus() == null || catalog.getStatus() != 1) {
            throw new BusinessException("医保目录已停用，不可对照：" + catalog.getYbCode());
        }
        String operator = UserUtils.getCurrentEmployeeName();

        // 换对照 = 覆盖（uk_item 唯一，天然一对一）
        BizYbMapping mapping = mappingMapper.selectOne(new LambdaQueryWrapper<BizYbMapping>()
                .eq(BizYbMapping::getItemType, dto.getItemType())
                .eq(BizYbMapping::getItemId, dto.getItemId())
                .last("LIMIT 1"));
        boolean fresh = (mapping == null);
        if (fresh) {
            mapping = new BizYbMapping();
            mapping.setItemType(dto.getItemType());
            mapping.setItemId(dto.getItemId());
            mapping.setCreateBy(operator);
        }
        ItemSnapshot snapshot = loadSnapshot(dto.getItemType(), dto.getItemId());
        mapping.setItemCode(snapshot.itemCode());
        mapping.setItemName(snapshot.itemName());
        mapping.setCatalogId(catalog.getId());
        mapping.setYbCode(catalog.getYbCode());
        mapping.setYbName(catalog.getYbName());
        mapping.setMatchType(2);
        mapping.setMappedBy(operator);
        mapping.setMappedTime(LocalDateTime.now().truncatedTo(java.time.temporal.ChronoUnit.SECONDS));
        mapping.setUpdateBy(operator);
        if (fresh) {
            mappingMapper.insert(mapping);
        } else {
            mappingMapper.updateById(mapping);
        }

        YbMappingListVO vo = new YbMappingListVO();
        vo.setMappingId(mapping.getId());
        vo.setItemType(dto.getItemType());
        vo.setItemId(dto.getItemId());
        vo.setItemCode(mapping.getItemCode());
        vo.setItemName(mapping.getItemName());
        vo.setYbCode(mapping.getYbCode());
        vo.setYbName(mapping.getYbName());
        vo.setMatchType(mapping.getMatchType());
        vo.setMappedBy(mapping.getMappedBy());
        vo.setMappedTime(mapping.getMappedTime());
        vo.setInsuranceLevel(catalog.getInsuranceLevel());
        vo.setPayRatio(catalog.getPayRatio());
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unmap(Integer itemType, Long itemId) {
        checkItemType(itemType);
        int removed = mappingMapper.purgeByItem(itemType, itemId);
        if (removed == 0) {
            throw new BusinessException("该院内项目没有对照关系，无需解除");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public YbAutoMatchResultVO autoMatch(YbAutoMatchDTO dto) {
        YbAutoMatchResultVO result = new YbAutoMatchResultVO();
        int matched = 0;
        int ambiguous = 0;
        int noMatch = 0;

        List<Integer> types = dto.getItemType() == null
                ? List.of(1, 2, 3, 4)
                : List.of(dto.getItemType());
        for (Integer type : types) {
            List<YbUnmappedItemVO> items = switch (type) {
                case 1 -> mappingMapper.selectUnmappedDrugs();
                case 2 -> mappingMapper.selectUnmappedTreatments();
                case 3 -> mappingMapper.selectUnmappedLaboratories();
                case 4 -> mappingMapper.selectUnmappedConsumables();
                default -> throw new BusinessException("项目类型不合法（1-药品 2-诊疗项目 3-检验项目 4-耗材）");
            };
            for (YbUnmappedItemVO item : items) {
                List<Integer> catalogTypes = candidateCatalogTypes(type, item.getSubType());
                List<BizYbCatalog> hits = catalogMapper.selectList(new LambdaQueryWrapper<BizYbCatalog>()
                        .in(BizYbCatalog::getCatalogType, catalogTypes)
                        .eq(BizYbCatalog::getStatus, 1)
                        .eq(BizYbCatalog::getYbName, item.getItemName()));
                if (hits.isEmpty()) {
                    noMatch++;
                } else if (hits.size() > 1) {
                    // 歧义：同名多条目录，自动对照宁缺勿错，留人工
                    ambiguous++;
                } else {
                    BizYbCatalog catalog = hits.get(0);
                    BizYbMapping mapping = new BizYbMapping();
                    mapping.setItemType(type);
                    mapping.setItemId(item.getItemId());
                    mapping.setItemCode(item.getItemCode());
                    mapping.setItemName(item.getItemName());
                    mapping.setCatalogId(catalog.getId());
                    mapping.setYbCode(catalog.getYbCode());
                    mapping.setYbName(catalog.getYbName());
                    mapping.setMatchType(1);
                    mapping.setMappedBy("system");
                    mapping.setMappedTime(LocalDateTime.now().truncatedTo(java.time.temporal.ChronoUnit.SECONDS));
                    mapping.setCreateBy("system");
                    mappingMapper.insert(mapping);
                    matched++;
                }
            }
        }
        result.setMatched(matched);
        result.setAmbiguous(ambiguous);
        result.setNoMatch(noMatch);
        int afterTotal = (int) mappingMapper.selectStats().stream()
                .mapToLong(s -> s.getTotal() - s.getMapped()).sum();
        result.setAfterTotal(afterTotal);
        result.setBeforeTotal(afterTotal + matched);
        return result;
    }

    // 内部

    private void checkItemType(Integer itemType) {
        if (itemType == null || itemType < 1 || itemType > 4) {
            throw new BusinessException("项目类型不合法（1-药品 2-诊疗项目 3-检验项目 4-耗材）");
        }
    }

    private void checkItemExists(Integer itemType, Long itemId) {
        long count = switch (itemType) {
            case 1 -> mappingMapper.countDrug(itemId);
            case 2 -> mappingMapper.countTreatment(itemId);
            case 3 -> mappingMapper.countLaboratory(itemId);
            case 4 -> mappingMapper.countConsumable(itemId);
            default -> 0;
        };
        if (count == 0) {
            throw new BusinessException("院内项目不存在（type=" + itemType + ", id=" + itemId + "）");
        }
    }

    private List<Integer> candidateCatalogTypes(Integer itemType, Integer subType) {
        return switch (itemType) {
            case 1 -> (subType != null && subType == 3) ? List.of(2) : List.of(1);
            case 2, 3 -> List.of(3);
            case 4 -> List.of(4);
            default -> List.of();
        };
    }

    private ItemSnapshot loadSnapshot(Integer itemType, Long itemId) {
        YbUnmappedItemVO v = switch (itemType) {
            case 1 -> mappingMapper.selectDrugSnapshot(itemId);
            case 2 -> mappingMapper.selectTreatmentSnapshot(itemId);
            case 3 -> mappingMapper.selectLaboratorySnapshot(itemId);
            case 4 -> mappingMapper.selectConsumableSnapshot(itemId);
            default -> null;
        };
        if (v == null) {
            throw new BusinessException("院内项目快照获取失败（type=" + itemType + ", id=" + itemId + "）");
        }
        return new ItemSnapshot(v.getItemCode(), v.getItemName());
    }

    private record ItemSnapshot(String itemCode, String itemName) {
    }
}

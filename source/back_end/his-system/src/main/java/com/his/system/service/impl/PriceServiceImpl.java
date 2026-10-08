package com.his.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.common.base.PageResult;
import com.his.common.exception.BusinessException;
import com.his.common.util.TextUtil;
import com.his.common.util.TimeUtil;
import com.his.system.dto.PriceChangeDTO;
import com.his.system.dto.PriceHistoryQueryPageDTO;
import com.his.system.dto.PriceQueryPageDTO;
import com.his.system.entity.CurrentUser;
import com.his.system.entity.SysPriceChangeHistory;
import com.his.system.mapper.PriceMapper;
import com.his.system.mapper.SysPriceChangeHistoryMapper;
import com.his.system.service.PriceService;
import com.his.system.utils.UserUtils;
import com.his.system.vo.PriceChangeHistoryVO;
import com.his.system.vo.PriceItemVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 价格管理服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PriceServiceImpl extends ServiceImpl<SysPriceChangeHistoryMapper, SysPriceChangeHistory> implements PriceService {

    private static final List<String> SUPPORTED_TYPES =
            Arrays.asList("DRUG", "CONSUMABLE", "INSPECTION", "LABORATORY", "TREATMENT");

    private final PriceMapper priceMapper;
    private final SysPriceChangeHistoryMapper sysPriceChangeHistoryMapper;

    @Override
    public PageResult<PriceItemVO> listPage(PriceQueryPageDTO queryDTO) {
        String itemType = normalizeType(queryDTO.getItemType());
        Page<PriceItemVO> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        IPage<PriceItemVO> result = switch (itemType) {
            case "DRUG" -> priceMapper.selectDrugPage(page, queryDTO.getKeyword(), queryDTO.getStatus());
            case "CONSUMABLE" -> priceMapper.selectConsumablePage(page, queryDTO.getKeyword(), queryDTO.getStatus());
            case "INSPECTION" -> priceMapper.selectInspectionPage(page, queryDTO.getKeyword(), queryDTO.getStatus());
            case "LABORATORY" -> priceMapper.selectLaboratoryPage(page, queryDTO.getKeyword(), queryDTO.getStatus());
            case "TREATMENT" -> priceMapper.selectTreatmentPage(page, queryDTO.getKeyword(), queryDTO.getStatus());
            default -> throw new BusinessException("不支持的项目类型：" + queryDTO.getItemType());
        };
        return PageResult.of(result.getTotal(), result.getCurrent(), result.getSize(),
                result.getPages(), result.getRecords());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PriceItemVO changePrice(PriceChangeDTO changeDTO) {
        String itemType = normalizeType(changeDTO.getItemType());
        BigDecimal newPrice = changeDTO.getNewPrice();
        if (newPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("新价格不能为负数");
        }

        PriceItemVO before = getItemById(itemType, changeDTO.getItemId());
        if (Objects.isNull(before)) {
            throw new BusinessException("项目不存在或已删除，无法调价");
        }
        if (Objects.nonNull(before.getPrice()) && before.getPrice().compareTo(newPrice) == 0) {
            throw new BusinessException("新价格与当前价格相同，无需调价");
        }

        int rows = updatePrice(itemType, changeDTO.getItemId(), newPrice);
        if (rows == 0) {
            // 静默失败比报错更危险：非要害路径也明确抛错
            throw new BusinessException("调价失败：未匹配到可更新的记录");
        }

        SysPriceChangeHistory history = new SysPriceChangeHistory();
        history.setItemType(itemType);
        history.setItemId(changeDTO.getItemId());
        history.setItemCode(before.getItemCode());
        history.setItemName(before.getItemName());
        history.setOldPrice(before.getPrice());
        history.setNewPrice(newPrice);
        history.setChangeReason(changeDTO.getReason());
        fillOperator(history);
        // DATETIME(0)：截断到秒，避免四舍五入
        history.setChangeTime(TimeUtil.nowSeconds());
        sysPriceChangeHistoryMapper.insert(history);

        before.setPrice(newPrice);
        log.info("调价成功：type={}, id={}, {} -> {}, 原因={}",
                itemType, changeDTO.getItemId(), history.getOldPrice(), newPrice, changeDTO.getReason());
        return before;
    }

    @Override
    public PageResult<PriceChangeHistoryVO> historyListPage(PriceHistoryQueryPageDTO queryDTO) {
        LambdaQueryWrapper<SysPriceChangeHistory> wrapper = new LambdaQueryWrapper<>();
        if (TextUtil.hasText(queryDTO.getItemType())) {
            wrapper.eq(SysPriceChangeHistory::getItemType, normalizeType(queryDTO.getItemType()));
        }
        if (TextUtil.hasText(queryDTO.getKeyword())) {
            String kw = queryDTO.getKeyword();
            wrapper.and(w -> w.like(SysPriceChangeHistory::getItemCode, kw)
                    .or().like(SysPriceChangeHistory::getItemName, kw));
        }
        wrapper.orderByDesc(SysPriceChangeHistory::getChangeTime)
                .orderByDesc(SysPriceChangeHistory::getId);

        Page<SysPriceChangeHistory> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        sysPriceChangeHistoryMapper.selectPage(page, wrapper);
        List<PriceChangeHistoryVO> voList = page.getRecords().stream().map(h -> {
            PriceChangeHistoryVO vo = new PriceChangeHistoryVO();
            BeanUtils.copyProperties(h, vo);
            return vo;
        }).collect(Collectors.toList());
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), voList);
    }

    @Override
    public Long countAll() {
        return priceMapper.countAllPriceItems();
    }

    private PriceItemVO getItemById(String itemType, Long itemId) {
        return switch (itemType) {
            case "DRUG" -> priceMapper.selectDrugById(itemId);
            case "CONSUMABLE" -> priceMapper.selectConsumableById(itemId);
            case "INSPECTION" -> priceMapper.selectInspectionById(itemId);
            case "LABORATORY" -> priceMapper.selectLaboratoryById(itemId);
            case "TREATMENT" -> priceMapper.selectTreatmentById(itemId);
            default -> throw new BusinessException("不支持的项目类型：" + itemType);
        };
    }

    private int updatePrice(String itemType, Long itemId, BigDecimal newPrice) {
        return switch (itemType) {
            case "DRUG" -> priceMapper.updateDrugPrice(itemId, newPrice);
            case "CONSUMABLE" -> priceMapper.updateConsumablePrice(itemId, newPrice);
            case "INSPECTION" -> priceMapper.updateInspectionPrice(itemId, newPrice);
            case "LABORATORY" -> priceMapper.updateLaboratoryPrice(itemId, newPrice);
            case "TREATMENT" -> priceMapper.updateTreatmentPrice(itemId, newPrice);
            default -> throw new BusinessException("不支持的项目类型：" + itemType);
        };
    }

    /**
     * 操作人：员工身份优先（与站内信、签名口径一致）。
     * 姓名单一口径取 {@code realName}，不再回落 {@code employeeName}/{@code username} ——
     * 那两个与库里其他操作人字段不是同一口径，回落出来的值事后按人名检索会对不上。
     * 取不到登录态就整段留空：变价历史是留痕，缺操作人能查出来，塞个假名字不行。
     */
    private void fillOperator(SysPriceChangeHistory history) {
        CurrentUser currentUser = UserUtils.getCurrentUser();
        if (Objects.isNull(currentUser)) {
            return;
        }
        history.setOperatorId(Objects.nonNull(currentUser.getEmployeeId())
                ? currentUser.getEmployeeId() : currentUser.getUserId());
        history.setOperatorName(TextUtil.hasText(currentUser.getRealName()) ? currentUser.getRealName() : null);
    }

    private String normalizeType(String itemType) {
        // D-业务规则：非空与码值白名单混在同一段归一化里，且列表接口必填、历史查询把它当可选筛选条件，DTO 注解无法一刀切，保留
        if (!TextUtil.hasText(itemType)) {
            throw new BusinessException("项目类型不能为空");
        }
        String normalized = itemType.trim().toUpperCase(Locale.ROOT);
        if (!SUPPORTED_TYPES.contains(normalized)) {
            throw new BusinessException("不支持的项目类型：" + itemType
                    + "，可选：" + String.join("/", SUPPORTED_TYPES));
        }
        return normalized;
    }
}

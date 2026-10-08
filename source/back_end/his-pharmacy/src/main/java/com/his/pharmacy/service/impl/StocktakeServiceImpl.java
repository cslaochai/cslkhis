package com.his.pharmacy.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.common.base.PageResult;
import com.his.common.exception.BusinessException;
import com.his.common.service.RedisSequenceService;
import com.his.common.util.NumUtil;
import com.his.common.util.TextUtil;
import com.his.common.util.TimeUtil;
import com.his.pharmacy.dto.*;
import com.his.pharmacy.entity.BizStocktake;
import com.his.pharmacy.entity.BizStocktakeItem;
import com.his.pharmacy.enums.StocktakePostFlagEnum;
import com.his.pharmacy.enums.StocktakeStatusEnum;
import com.his.pharmacy.mapper.BizDrugStockLogMapper;
import com.his.pharmacy.mapper.BizStocktakeItemMapper;
import com.his.pharmacy.mapper.BizStocktakeMapper;
import com.his.pharmacy.service.PharmacyService;
import com.his.pharmacy.service.StocktakeService;
import com.his.pharmacy.vo.BizDrugStockLogVO;
import com.his.pharmacy.vo.StocktakeItemVO;
import com.his.pharmacy.vo.StocktakeVO;
import com.his.system.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * 药房盘点服务实现（sql/127）
 */
@Service
@RequiredArgsConstructor
public class StocktakeServiceImpl extends ServiceImpl<BizStocktakeMapper, BizStocktake> implements StocktakeService {

    /** 状态：1-盘点中 2-待复核 3-已过账 4-已关单（唯一口径 StocktakeStatusEnum） */
    /**
     * 明细过账标记：0-未过账 1-已盘盈亏过账 2-无差异免过账（唯一口径 StocktakePostFlagEnum）
     */

    private final BizStocktakeMapper bizStocktakeMapper;
    private final BizStocktakeItemMapper bizStocktakeItemMapper;
    private final BizDrugStockLogMapper bizDrugStockLogMapper;
    private final PharmacyService pharmacyService;
    private final RedisSequenceService redisSequenceService;

    /**
     * 流水上的差异说明：药名 + 批号 + 账面/实盘 + 药师写的原因，够复核与事后审计看一眼就懂
     */
    private static String diffReason(BizStocktakeItem item) {
        String label = TextUtil.hasText(item.getDrugName()) ? item.getDrugName() : "药品#" + item.getDrugId();
        if (TextUtil.hasText(item.getBatchNo())) {
            label = label + " 批号" + item.getBatchNo();
        }
        String reason = "盘点差异：" + label + " 账面" + NumUtil.plain(NumUtil.orZero(item.getBookQuantity()))
                + "，实盘" + NumUtil.plain(NumUtil.orZero(item.getCountedQuantity())) + "，差" + NumUtil.plain(NumUtil.orZero(item.getDiffQuantity()));
        return TextUtil.hasText(item.getRemark()) ? reason + "；" + item.getRemark() : reason;
    }

    private static String diffText(BigDecimal counted, BigDecimal diff) {
        if (counted == null) {
            return "未录入";
        }
        if (diff == null || diff.compareTo(BigDecimal.ZERO) == 0) {
            return "无差异";
        }
        return diff.compareTo(BigDecimal.ZERO) > 0 ? "盘盈" : "盘亏";
    }

    private static String drugTypeText(Integer drugType) {
        if (drugType == null) {
            return "全部药品";
        }
        return switch (drugType) {
            case 1 -> "西药";
            case 2 -> "中成药";
            case 3 -> "中药饮片";
            default -> "其他";
        };
    }

    private static String scopeDesc(Integer drugType, String keyword) {
        if (drugType == null && !TextUtil.hasText(keyword)) {
            return "全部库存批次";
        }
        StringBuilder sb = new StringBuilder();
        if (drugType != null) {
            sb.append(drugTypeText(drugType));
        }
        if (TextUtil.hasText(keyword)) {
            if (sb.length() > 0) {
                sb.append(" · ");
            }
            sb.append("关键字「").append(keyword).append("」");
        }
        return sb.toString();
    }

    @Override
    public PageResult<StocktakeVO> listPage(StocktakeQueryPageDTO query) {
        StocktakeQueryPageDTO q = query == null ? new StocktakeQueryPageDTO() : query;
        Page<StocktakeVO> page = bizStocktakeMapper.selectStocktakePage(
                new Page<>(q.getPageNum(), q.getPageSize()),
                TextUtil.trimToNull(q.getStocktakeNo()), TextUtil.trimToNull(q.getStocktakeTitle()),
                q.getStatus(), TextUtil.trimToNull(q.getDateStart()), TextUtil.trimToNull(q.getDateEnd()));
        page.getRecords().forEach(v -> v.setStatusText(StocktakeStatusEnum.getText(v.getStatus())));
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), page.getRecords());
    }

    @Override
    public StocktakeVO getDetailById(Long id) {
        StocktakeVO vo = bizStocktakeMapper.selectStocktakeById(id);
        if (vo == null) {
            throw new BusinessException("盘点单不存在或已删除");
        }
        vo.setStatusText(StocktakeStatusEnum.getText(vo.getStatus()));
        List<StocktakeItemVO> items = bizStocktakeItemMapper.selectByStocktakeId(id);
        items.forEach(i -> i.setDiffTypeText(diffText(i.getCountedQuantity(), i.getDiffQuantity())));
        vo.setItems(items);
        List<BizDrugStockLogVO> logs = bizDrugStockLogMapper.selectByStocktakeId(id);
        logs.forEach(BizDrugStockLogVO::fillTexts);
        vo.setLogs(logs);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public StocktakeVO upsert(StocktakeUpsertDTO dto) {
        Integer drugType = dto.getScopeDrugType();
        String keyword = TextUtil.trimToNull(dto.getScopeKeyword());
        String title = dto.getStocktakeTitle().trim();
        String operator = UserUtils.getCurrentUser().getRealName();
        LocalDateTime now = TimeUtil.nowSeconds();

        if (dto.getId() == null) {
            BizStocktake head = new BizStocktake();
            head.setStocktakeNo(redisSequenceService.generateStocktakeNo());
            head.setStocktakeTitle(title);
            head.setScopeDrugType(drugType);
            head.setScopeKeyword(keyword);
            head.setScopeDesc(scopeDesc(drugType, keyword));
            head.setSnapshotTime(now);
            head.setStatus(StocktakeStatusEnum.COUNTING.getCode());
            head.setTotalItems(0);
            head.setCountedItems(0);
            head.setDiffItems(0);
            head.setProfitItems(0);
            head.setLossItems(0);
            head.setDiffQuantity(BigDecimal.ZERO);
            head.setDiffAmount(BigDecimal.ZERO);
            head.setRemark(TextUtil.cut(dto.getRemark(), 500));
            head.setCreateBy(operator);
            head.setUpdateBy(operator);
            head.setCreateTime(now);
            head.setUpdateTime(now);
            if (bizStocktakeMapper.insert(head) != 1) {
                throw new BusinessException("生成盘点单失败");
            }
            snapshot(head.getId(), drugType, keyword, operator, now);
            return getDetailById(head.getId());
        }

        BizStocktake cur = lock(dto.getId());
        requireStatus(cur, StocktakeStatusEnum.COUNTING.getCode(), "修改盘点范围");
        boolean scopeChanged = !Objects.equals(cur.getScopeDrugType(), drugType)
                || !Objects.equals(TextUtil.trimToNull(cur.getScopeKeyword()), keyword);

        bizStocktakeMapper.update(null, Wrappers.<BizStocktake>lambdaUpdate()
                .eq(BizStocktake::getId, cur.getId())
                .set(BizStocktake::getStocktakeTitle, title)
                .set(BizStocktake::getRemark, TextUtil.cut(dto.getRemark(), 500))
                .set(BizStocktake::getUpdateBy, operator)
                .set(BizStocktake::getUpdateTime, now));

        if (scopeChanged) {
            // 重抓快照 = 清空明细重录：留着上一轮范围的实盘数会串账（明细ID 都不在了）
            bizStocktakeItemMapper.purgeByStocktakeId(cur.getId());
            bizStocktakeMapper.update(null, Wrappers.<BizStocktake>lambdaUpdate()
                    .eq(BizStocktake::getId, cur.getId())
                    .set(BizStocktake::getScopeDrugType, drugType)
                    .set(BizStocktake::getScopeKeyword, keyword)
                    .set(BizStocktake::getScopeDesc, scopeDesc(drugType, keyword))
                    .set(BizStocktake::getSnapshotTime, now)
                    .set(BizStocktake::getUpdateBy, operator)
                    .set(BizStocktake::getUpdateTime, now));
            snapshot(cur.getId(), drugType, keyword, operator, now);
        } else {
            refreshSummary(cur.getId(), operator, now);
        }
        return getDetailById(cur.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public StocktakeVO saveCount(StocktakeCountUpsertDTO dto) {
        BizStocktake head = lock(dto.getId());
        requireStatus(head, StocktakeStatusEnum.COUNTING.getCode(), "录入实盘数");
        String operator = UserUtils.getCurrentUser().getRealName();
        LocalDateTime now = TimeUtil.nowSeconds();

        for (StocktakeCountUpsertDTO.Item row : dto.getItems()) {
            BizStocktakeItem item = bizStocktakeItemMapper.selectByIdForUpdate(row.getItemId());
            if (item == null || !Objects.equals(item.getStocktakeId(), head.getId())) {
                throw new BusinessException("盘点明细不存在或不属于该盘点单（明细#" + row.getItemId() + "）");
            }
            BigDecimal counted = row.getCountedQuantity().setScale(2, RoundingMode.HALF_UP);
            BigDecimal diff = counted.subtract(item.getBookQuantity());
            BigDecimal cost = item.getCostPrice() == null ? BigDecimal.ZERO : item.getCostPrice();
            // 显式 set：remark 被清空时 updateById 会跳过 null 字段，留下上一轮的旧说明
            bizStocktakeItemMapper.update(null, Wrappers.<BizStocktakeItem>lambdaUpdate()
                    .eq(BizStocktakeItem::getId, item.getId())
                    .set(BizStocktakeItem::getCountedQuantity, counted)
                    .set(BizStocktakeItem::getDiffQuantity, diff)
                    .set(BizStocktakeItem::getDiffAmount, diff.multiply(cost).setScale(2, RoundingMode.HALF_UP))
                    .set(BizStocktakeItem::getPosted, StocktakePostFlagEnum.NONE.getCode())
                    .set(BizStocktakeItem::getRemark, TextUtil.cut(row.getRemark(), 500))
                    .set(BizStocktakeItem::getUpdateBy, operator)
                    .set(BizStocktakeItem::getUpdateTime, now));
        }
        refreshSummary(head.getId(), operator, now);
        return getDetailById(head.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public StocktakeVO submit(StocktakeIdDTO dto) {
        BizStocktake head = lock(dto.getId());
        requireStatus(head, StocktakeStatusEnum.COUNTING.getCode(), "提交盘点");
        String operator = UserUtils.getCurrentUser().getRealName();
        LocalDateTime now = TimeUtil.nowSeconds();

        long uncounted = bizStocktakeItemMapper.countUncounted(head.getId());
        if (uncounted > 0) {
            throw new BusinessException("还有 " + uncounted + " 个批次未录入实盘数，未盘完不能提交"
                    + "（漏盘的批次会被当成无差异，账实不符就永久留在账上）");
        }
        refreshSummary(head.getId(), operator, now);
        BizStocktake sum = bizStocktakeItemMapper.selectSummary(head.getId());
        int status = sum.getDiffItems() == 0 ? StocktakeStatusEnum.CLOSED.getCode() : StocktakeStatusEnum.AUDITING.getCode();
        if (status == StocktakeStatusEnum.CLOSED.getCode()) {
            bizStocktakeItemMapper.markNoDiff(head.getId(), operator, now);
        }
        bizStocktakeMapper.update(null, Wrappers.<BizStocktake>lambdaUpdate()
                .eq(BizStocktake::getId, head.getId())
                .set(BizStocktake::getStatus, status)
                .set(BizStocktake::getSubmitBy, operator)
                .set(BizStocktake::getSubmitTime, now)
                .set(BizStocktake::getUpdateBy, operator)
                .set(BizStocktake::getUpdateTime, now));
        return getDetailById(head.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public StocktakeVO audit(StocktakeAuditDTO dto) {
        BizStocktake head = lock(dto.getId());
        requireStatus(head, StocktakeStatusEnum.AUDITING.getCode(), "复核盘点单");
        String operator = UserUtils.getCurrentUser().getRealName();
        LocalDateTime now = TimeUtil.nowSeconds();
        String remark = TextUtil.cut(dto.getRemark(), 500);

        if (!Boolean.TRUE.equals(dto.getPass())) {
            // 退回：状态回盘点中，实盘数原样留着（重录只需要改有争议的那几条）；流水一行都没发生
            bizStocktakeMapper.update(null, Wrappers.<BizStocktake>lambdaUpdate()
                    .eq(BizStocktake::getId, head.getId())
                    .set(BizStocktake::getStatus, StocktakeStatusEnum.COUNTING.getCode())
                    .set(BizStocktake::getAuditBy, operator)
                    .set(BizStocktake::getAuditTime, now)
                    .set(BizStocktake::getAuditRemark, remark)
                    .set(BizStocktake::getUpdateBy, operator)
                    .set(BizStocktake::getUpdateTime, now));
            return getDetailById(head.getId());
        }

        if (bizStocktakeItemMapper.countUncounted(head.getId()) > 0) {
            throw new BusinessException("有批次未录入实盘数，不能复核过账");
        }
        List<BizStocktakeItem> diffItems = bizStocktakeItemMapper.selectList(Wrappers.<BizStocktakeItem>lambdaQuery()
                .eq(BizStocktakeItem::getStocktakeId, head.getId())
                .ne(BizStocktakeItem::getDiffQuantity, BigDecimal.ZERO)
                .orderByAsc(BizStocktakeItem::getId));
        if (diffItems.isEmpty()) {
            throw new BusinessException("这张盘点单没有差异，无需过账（请直接关单）");
        }
        // 逐条过账 + 逐条落流水；任一条被库存闸门挡下就整单回滚，不留"过了一半"的账
        for (BizStocktakeItem item : diffItems) {
            pharmacyService.postStocktakeDiff(item.getStockId(), item.getDiffQuantity(),
                    head.getId(), head.getStocktakeNo(), operator, diffReason(item));
            bizStocktakeItemMapper.update(null, Wrappers.<BizStocktakeItem>lambdaUpdate()
                    .eq(BizStocktakeItem::getId, item.getId())
                    .set(BizStocktakeItem::getPosted, StocktakePostFlagEnum.POSTED.getCode())
                    .set(BizStocktakeItem::getUpdateBy, operator)
                    .set(BizStocktakeItem::getUpdateTime, now));
        }
        bizStocktakeItemMapper.markNoDiff(head.getId(), operator, now);
        bizStocktakeMapper.update(null, Wrappers.<BizStocktake>lambdaUpdate()
                .eq(BizStocktake::getId, head.getId())
                .set(BizStocktake::getStatus, StocktakeStatusEnum.POSTED.getCode())
                .set(BizStocktake::getAuditBy, operator)
                .set(BizStocktake::getAuditTime, now)
                .set(BizStocktake::getAuditRemark, remark)
                .set(BizStocktake::getUpdateBy, operator)
                .set(BizStocktake::getUpdateTime, now));
        return getDetailById(head.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(Long id) {
        BizStocktake head = lock(id);
        if (!Objects.equals(head.getStatus(), StocktakeStatusEnum.COUNTING.getCode())) {
            throw new BusinessException("只有「盘点中」的盘点单可以删除（当前：" + StocktakeStatusEnum.labelOrUnknown(head.getStatus())
                    + "）；待复核请复核人退回，已过账/已关单是留档凭证不能抹");
        }
        bizStocktakeItemMapper.purgeByStocktakeId(head.getId());
        if (bizStocktakeMapper.purgeById(head.getId()) != 1) {
            throw new BusinessException("删除盘点单失败");
        }
    }

    /**
     * 抓账面快照：范围内每个批次一行明细（含 quantity=0 的批次——账面 0、实盘有货正是盘盈）。
     * 范围为空时直接拒绝建单，不生成一张"盘了个寂寞"的空单。
     */
    private void snapshot(Long stocktakeId, Integer drugType, String keyword, String operator, LocalDateTime now) {
        List<StocktakeItemVO> rows = bizStocktakeItemMapper.selectSnapshot(drugType, keyword);
        if (rows.isEmpty()) {
            throw new BusinessException("该范围内没有任何库存批次，请先入库或换个范围再开盘点单");
        }
        for (StocktakeItemVO row : rows) {
            BizStocktakeItem item = new BizStocktakeItem();
            item.setStocktakeId(stocktakeId);
            item.setStockId(row.getStockId());
            item.setDrugId(row.getDrugId());
            item.setDrugCode(row.getDrugCode());
            item.setDrugName(row.getDrugName());
            item.setSpecification(row.getSpecification());
            item.setUnit(row.getUnit());
            item.setBatchNo(row.getBatchNo());
            item.setProductionDate(row.getProductionDate());
            item.setExpiryDate(row.getExpiryDate());
            item.setLocation(row.getLocation());
            item.setCostPrice(row.getCostPrice());
            item.setLockedQuantity(row.getLockedQuantity());
            item.setBookQuantity(row.getBookQuantity());
            item.setCountedQuantity(null);
            item.setDiffQuantity(BigDecimal.ZERO);
            item.setDiffAmount(BigDecimal.ZERO);
            item.setPosted(StocktakePostFlagEnum.NONE.getCode());
            item.setCreateBy(operator);
            item.setUpdateBy(operator);
            item.setCreateTime(now);
            item.setUpdateTime(now);
            bizStocktakeItemMapper.insert(item);
        }
        refreshSummary(stocktakeId, operator, now);
    }

    /**
     * 把明细汇总的六个口径数回写主单（列表页的盈/亏/净差直接读主单，不再逐单算明细）
     */
    private void refreshSummary(Long stocktakeId, String operator, LocalDateTime now) {
        BizStocktake sum = bizStocktakeItemMapper.selectSummary(stocktakeId);
        bizStocktakeMapper.update(null, Wrappers.<BizStocktake>lambdaUpdate()
                .eq(BizStocktake::getId, stocktakeId)
                .set(BizStocktake::getTotalItems, sum.getTotalItems())
                .set(BizStocktake::getCountedItems, sum.getCountedItems())
                .set(BizStocktake::getDiffItems, sum.getDiffItems())
                .set(BizStocktake::getProfitItems, sum.getProfitItems())
                .set(BizStocktake::getLossItems, sum.getLossItems())
                .set(BizStocktake::getDiffQuantity, sum.getDiffQuantity())
                .set(BizStocktake::getDiffAmount, sum.getDiffAmount())
                .set(BizStocktake::getUpdateBy, operator)
                .set(BizStocktake::getUpdateTime, now));
    }

    private BizStocktake lock(Long id) {
        BizStocktake head = bizStocktakeMapper.selectByIdForUpdate(id);
        if (head == null) {
            throw new BusinessException("盘点单不存在或已删除");
        }
        return head;
    }

    private void requireStatus(BizStocktake head, int expected, String action) {
        if (!Objects.equals(head.getStatus(), expected)) {
            throw new BusinessException("当前状态不能" + action + "（现在：" + StocktakeStatusEnum.labelOrUnknown(head.getStatus()) + "）");
        }
    }

}

package com.his.pharmacy.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.his.common.base.PageResult;
import com.his.common.base.RedisSequenceService;
import com.his.common.exception.BusinessException;
import com.his.pharmacy.dto.StocktakeAuditDTO;
import com.his.pharmacy.dto.StocktakeCountUpsertDTO;
import com.his.pharmacy.dto.StocktakeIdDTO;
import com.his.pharmacy.dto.StocktakeQueryPageDTO;
import com.his.pharmacy.dto.StocktakeUpsertDTO;
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
import com.his.security.UserUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;

/**
 * 药房盘点服务实现（sql/127）
 *
 * <p>三条口径值得先说清楚，它们都是"看起来更省事但会出错"的那类：
 * <ul>
 *   <li><b>差异按快照算，过账按差量加</b>：建单那一刻的批次数量固化成 book_quantity，
 *       复核过账时在<b>当前</b>余额上加减差量，而不是把余额覆盖成实盘数——
 *       快照之后发生的发药/入库都有自己的流水，覆盖等于把它们抹掉。</li>
 *   <li><b>盘点期间不冻结库存</b>（学习阶段与真实系统的差异，见 sql/127 头注第 5 条）：
 *       一张盘点单不该卡住整条发药链，代价就是上面那条"差量加减"必须成立。</li>
 *   <li><b>过账有闸门</b>：过账后余额为负、或小于已锁定数量（已开方未发药）时整单回滚，
 *       宁可让药师回去重点差异，也不能把可用库存打成负数让下一次发药凭空多扣。</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class StocktakeServiceImpl implements StocktakeService {

    /** 状态：1-盘点中 2-待复核 3-已过账 4-已关单（唯一口径 StocktakeStatusEnum） */
    /** 明细过账标记：0-未过账 1-已盘盈亏过账 2-无差异免过账（唯一口径 StocktakePostFlagEnum） */

    private final BizStocktakeMapper stocktakeMapper;
    private final BizStocktakeItemMapper itemMapper;
    private final BizDrugStockLogMapper stockLogMapper;
    private final PharmacyService pharmacyService;
    private final RedisSequenceService redisSequenceService;

    @Override
    public PageResult<StocktakeVO> listPage(StocktakeQueryPageDTO query) {
        StocktakeQueryPageDTO q = query == null ? new StocktakeQueryPageDTO() : query;
        Page<StocktakeVO> page = stocktakeMapper.selectStocktakePage(
                new Page<>(q.getPageNum(), q.getPageSize()),
                trimToNull(q.getStocktakeNo()), trimToNull(q.getStocktakeTitle()),
                q.getStatus(), trimToNull(q.getDateStart()), trimToNull(q.getDateEnd()));
        page.getRecords().forEach(v -> v.setStatusText(statusText(v.getStatus())));
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), page.getRecords());
    }

    @Override
    public StocktakeVO getDetailById(Long id) {
        StocktakeVO vo = stocktakeMapper.selectStocktakeById(id);
        if (vo == null) {
            throw new BusinessException("盘点单不存在或已删除");
        }
        vo.setStatusText(statusText(vo.getStatus()));
        List<StocktakeItemVO> items = itemMapper.selectByStocktakeId(id);
        items.forEach(i -> i.setDiffTypeText(diffText(i.getCountedQuantity(), i.getDiffQuantity())));
        vo.setItems(items);
        List<BizDrugStockLogVO> logs = stockLogMapper.selectByStocktakeId(id);
        logs.forEach(BizDrugStockLogVO::fillTexts);
        vo.setLogs(logs);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public StocktakeVO upsert(StocktakeUpsertDTO dto) {
        Integer drugType = dto.getScopeDrugType();
        if (drugType != null && (drugType < 1 || drugType > 3)) {
            throw new BusinessException("药品类型只能是 1-西药 / 2-中成药 / 3-中药饮片（不传=全部）");
        }
        String keyword = trimToNull(dto.getScopeKeyword());
        String title = dto.getStocktakeTitle().trim();
        String operator = UserUtils.getCurrentEmployeeName();
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

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
            head.setRemark(cut(dto.getRemark(), 500));
            head.setCreateBy(operator);
            head.setUpdateBy(operator);
            head.setCreateTime(now);
            head.setUpdateTime(now);
            if (stocktakeMapper.insert(head) != 1) {
                throw new BusinessException("生成盘点单失败");
            }
            snapshot(head.getId(), drugType, keyword, operator, now);
            return getDetailById(head.getId());
        }

        BizStocktake cur = lock(dto.getId());
        requireStatus(cur, StocktakeStatusEnum.COUNTING.getCode(), "修改盘点范围");
        boolean scopeChanged = !Objects.equals(cur.getScopeDrugType(), drugType)
                || !Objects.equals(trimToNull(cur.getScopeKeyword()), keyword);

        stocktakeMapper.update(null, Wrappers.<BizStocktake>lambdaUpdate()
                .eq(BizStocktake::getId, cur.getId())
                .set(BizStocktake::getStocktakeTitle, title)
                .set(BizStocktake::getRemark, cut(dto.getRemark(), 500))
                .set(BizStocktake::getUpdateBy, operator)
                .set(BizStocktake::getUpdateTime, now));

        if (scopeChanged) {
            // 重抓快照 = 清空明细重录：留着上一轮范围的实盘数会串账（明细ID 都不在了）
            itemMapper.purgeByStocktakeId(cur.getId());
            stocktakeMapper.update(null, Wrappers.<BizStocktake>lambdaUpdate()
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
        String operator = UserUtils.getCurrentEmployeeName();
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

        for (StocktakeCountUpsertDTO.Item row : dto.getItems()) {
            BizStocktakeItem item = itemMapper.selectByIdForUpdate(row.getItemId());
            if (item == null || !Objects.equals(item.getStocktakeId(), head.getId())) {
                throw new BusinessException("盘点明细不存在或不属于该盘点单（明细#" + row.getItemId() + "）");
            }
            BigDecimal counted = row.getCountedQuantity().setScale(2, RoundingMode.HALF_UP);
            BigDecimal diff = counted.subtract(item.getBookQuantity());
            BigDecimal cost = item.getCostPrice() == null ? BigDecimal.ZERO : item.getCostPrice();
            // 显式 set：remark 被清空时 updateById 会跳过 null 字段，留下上一轮的旧说明
            itemMapper.update(null, Wrappers.<BizStocktakeItem>lambdaUpdate()
                    .eq(BizStocktakeItem::getId, item.getId())
                    .set(BizStocktakeItem::getCountedQuantity, counted)
                    .set(BizStocktakeItem::getDiffQuantity, diff)
                    .set(BizStocktakeItem::getDiffAmount, diff.multiply(cost).setScale(2, RoundingMode.HALF_UP))
                    .set(BizStocktakeItem::getPosted, StocktakePostFlagEnum.NONE.getCode())
                    .set(BizStocktakeItem::getRemark, cut(row.getRemark(), 500))
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
        String operator = UserUtils.getCurrentEmployeeName();
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

        long uncounted = itemMapper.countUncounted(head.getId());
        if (uncounted > 0) {
            throw new BusinessException("还有 " + uncounted + " 个批次未录入实盘数，未盘完不能提交"
                    + "（漏盘的批次会被当成无差异，账实不符就永久留在账上）");
        }
        refreshSummary(head.getId(), operator, now);
        BizStocktake sum = itemMapper.selectSummary(head.getId());
        int status = sum.getDiffItems() == 0 ? StocktakeStatusEnum.CLOSED.getCode() : StocktakeStatusEnum.AUDITING.getCode();
        if (status == StocktakeStatusEnum.CLOSED.getCode()) {
            itemMapper.markNoDiff(head.getId(), operator, now);
        }
        stocktakeMapper.update(null, Wrappers.<BizStocktake>lambdaUpdate()
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
        String operator = UserUtils.getCurrentEmployeeName();
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        String remark = cut(dto.getRemark(), 500);

        if (!Boolean.TRUE.equals(dto.getPass())) {
            // 退回：状态回盘点中，实盘数原样留着（重录只需要改有争议的那几条）；流水一行都没发生
            stocktakeMapper.update(null, Wrappers.<BizStocktake>lambdaUpdate()
                    .eq(BizStocktake::getId, head.getId())
                    .set(BizStocktake::getStatus, StocktakeStatusEnum.COUNTING.getCode())
                    .set(BizStocktake::getAuditBy, operator)
                    .set(BizStocktake::getAuditTime, now)
                    .set(BizStocktake::getAuditRemark, remark)
                    .set(BizStocktake::getUpdateBy, operator)
                    .set(BizStocktake::getUpdateTime, now));
            return getDetailById(head.getId());
        }

        if (itemMapper.countUncounted(head.getId()) > 0) {
            throw new BusinessException("有批次未录入实盘数，不能复核过账");
        }
        List<BizStocktakeItem> diffItems = itemMapper.selectList(Wrappers.<BizStocktakeItem>lambdaQuery()
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
            itemMapper.update(null, Wrappers.<BizStocktakeItem>lambdaUpdate()
                    .eq(BizStocktakeItem::getId, item.getId())
                    .set(BizStocktakeItem::getPosted, StocktakePostFlagEnum.POSTED.getCode())
                    .set(BizStocktakeItem::getUpdateBy, operator)
                    .set(BizStocktakeItem::getUpdateTime, now));
        }
        itemMapper.markNoDiff(head.getId(), operator, now);
        stocktakeMapper.update(null, Wrappers.<BizStocktake>lambdaUpdate()
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
            throw new BusinessException("只有「盘点中」的盘点单可以删除（当前：" + statusText(head.getStatus())
                    + "）；待复核请复核人退回，已过账/已关单是留档凭证不能抹");
        }
        itemMapper.purgeByStocktakeId(head.getId());
        if (stocktakeMapper.purgeById(head.getId()) != 1) {
            throw new BusinessException("删除盘点单失败");
        }
    }

    /**
     * 抓账面快照：范围内每个批次一行明细（含 quantity=0 的批次——账面 0、实盘有货正是盘盈）。
     * 范围为空时直接拒绝建单，不生成一张"盘了个寂寞"的空单。
     */
    private void snapshot(Long stocktakeId, Integer drugType, String keyword, String operator, LocalDateTime now) {
        List<StocktakeItemVO> rows = itemMapper.selectSnapshot(drugType, keyword);
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
            itemMapper.insert(item);
        }
        refreshSummary(stocktakeId, operator, now);
    }

    /** 把明细汇总的六个口径数回写主单（列表页的盈/亏/净差直接读主单，不再逐单算明细） */
    private void refreshSummary(Long stocktakeId, String operator, LocalDateTime now) {
        BizStocktake sum = itemMapper.selectSummary(stocktakeId);
        stocktakeMapper.update(null, Wrappers.<BizStocktake>lambdaUpdate()
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
        BizStocktake head = stocktakeMapper.selectByIdForUpdate(id);
        if (head == null) {
            throw new BusinessException("盘点单不存在或已删除");
        }
        return head;
    }

    private void requireStatus(BizStocktake head, int expected, String action) {
        if (!Objects.equals(head.getStatus(), expected)) {
            throw new BusinessException("当前状态不能" + action + "（现在：" + statusText(head.getStatus()) + "）");
        }
    }

    /** 流水上的差异说明：药名 + 批号 + 账面/实盘 + 药师写的原因，够复核与事后审计看一眼就懂 */
    private static String diffReason(BizStocktakeItem item) {
        String label = StringUtils.hasText(item.getDrugName()) ? item.getDrugName() : "药品#" + item.getDrugId();
        if (StringUtils.hasText(item.getBatchNo())) {
            label = label + " 批号" + item.getBatchNo();
        }
        String reason = "盘点差异：" + label + " 账面" + plain(item.getBookQuantity())
                + "，实盘" + plain(item.getCountedQuantity()) + "，差" + plain(item.getDiffQuantity());
        return StringUtils.hasText(item.getRemark()) ? reason + "；" + item.getRemark() : reason;
    }

    /** 数量显示：5.00 → 5，2.50 → 2.5（报错文案里不想看到一串尾零） */
    private static String plain(BigDecimal value) {
        return value == null ? "0" : value.stripTrailingZeros().toPlainString();
    }

    private static String statusText(Integer status) {
        if (status == null) {
            return "未知";
        }
        return StocktakeStatusEnum.labelOf(status);
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
        if (drugType == null && !StringUtils.hasText(keyword)) {
            return "全部库存批次";
        }
        StringBuilder sb = new StringBuilder();
        if (drugType != null) {
            sb.append(drugTypeText(drugType));
        }
        if (StringUtils.hasText(keyword)) {
            if (sb.length() > 0) {
                sb.append(" · ");
            }
            sb.append("关键字「").append(keyword).append("」");
        }
        return sb.toString();
    }

    private static String trimToNull(String s) {
        return StringUtils.hasText(s) ? s.trim() : null;
    }

    /** 写库文本一律先截到列宽（超长会把业务失败升级成 500，AGENTS §3） */
    private static String cut(String text, int max) {
        if (text == null) {
            return null;
        }
        String t = text.trim();
        return t.length() <= max ? t : t.substring(0, max);
    }
}

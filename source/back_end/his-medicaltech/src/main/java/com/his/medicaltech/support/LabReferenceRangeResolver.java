package com.his.medicaltech.support;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.his.common.util.TextUtil;
import com.his.system.entity.SysLaboratoryItemDetail;
import com.his.system.mapper.SysLaboratoryItemDetailMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 参考区间来源优先级解析器。
 * <p>
 * <b>优先级：检验报告上的区间 &gt; 检验项目主数据。</b>
 * 报告上的参考区间是检验科针对本次检测实际出具的口径（可能因方法学、仪器而不同），
 * 主数据只是默认值。所以传入值只要能解析就用传入值，<b>不静默覆盖为主数据</b> ——
 * 悄悄改掉医院写在报告上的区间，比不判定还糟。
 * <p>
 * 只有传入区间为空或无法解析时，才回落到检验项目组套明细
 * （按 itemCode 精确匹配，其次按 itemName）。
 * 两条路都拿不到时返回 {@link LabReferenceRange#unparsable}，由判定器如实记录原因。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LabReferenceRangeResolver {

    private static final long CACHE_TTL_MS = 300_000L;

    private final SysLaboratoryItemDetailMapper sysLaboratoryItemDetailMapper;
    private final AtomicLong loadedAt = new AtomicLong(0L);
    private volatile Map<String, String> byCode = Collections.emptyMap();
    private volatile Map<String, String> byName = Collections.emptyMap();

    /**
     * 解析某检验结果条目的参考区间。
     *
     * @param rawRange 报告传入的区间文本，可为空
     * @param itemCode 检验项目编码，可为空
     * @param itemName 检验项目名称，可为空
     * @param gender   患者性别（1-男 2-女），区间含性别分支时必需
     */
    public LabReferenceRange resolve(String rawRange, String itemCode, String itemName, Integer gender) {
        LabReferenceRange fromReport = LabReferenceRangeParser.parse(rawRange, gender);
        if (fromReport.usable()) {
            return fromReport;
        }

        String masterRange = masterRangeOf(itemCode, itemName);
        if (TextUtil.hasText(masterRange)) {
            LabReferenceRange fromMaster = LabReferenceRangeParser.parse(masterRange, gender);
            if (fromMaster.usable()) {
                return fromMaster;
            }
            log.debug("[检验判定] 主数据区间也无法解析：item={}/{} range=[{}]",
                    itemCode, itemName, masterRange);
        }

        // 把「传入为什么没用上」如实带到 note 里，方便事后追为什么没判定
        return LabReferenceRange.unparsable(TextUtil.hasText(rawRange)
                ? rawRange
                : (TextUtil.hasText(masterRange) ? masterRange : ""));
    }

    /**
     * 让主数据缓存立即失效（维护检验项目后调用）
     */
    public void refresh() {
        loadedAt.set(0L);
    }

    private String masterRangeOf(String itemCode, String itemName) {
        ensureLoaded();
        if (TextUtil.hasText(itemCode)) {
            String hit = byCode.get(itemCode.trim().toUpperCase());
            if (TextUtil.hasText(hit)) {
                return hit;
            }
        }
        if (TextUtil.hasText(itemName)) {
            String hit = byName.get(itemName.trim());
            if (TextUtil.hasText(hit)) {
                return hit;
            }
        }
        return null;
    }

    private void ensureLoaded() {
        long now = System.currentTimeMillis();
        if (!byCode.isEmpty() && now - loadedAt.get() <= CACHE_TTL_MS) {
            return;
        }
        synchronized (this) {
            if (!byCode.isEmpty() && now - loadedAt.get() <= CACHE_TTL_MS) {
                return;
            }
            try {
                List<SysLaboratoryItemDetail> rows = sysLaboratoryItemDetailMapper.selectList(
                        new LambdaQueryWrapper<SysLaboratoryItemDetail>()
                                .eq(SysLaboratoryItemDetail::getStatus, 1));
                Map<String, String> codes = new HashMap<>();
                Map<String, String> names = new HashMap<>();
                for (SysLaboratoryItemDetail row : rows) {
                    if (!TextUtil.hasText(row.getReferenceRange())) {
                        continue;
                    }
                    if (TextUtil.hasText(row.getItemCode())) {
                        codes.putIfAbsent(row.getItemCode().trim().toUpperCase(), row.getReferenceRange());
                    }
                    if (TextUtil.hasText(row.getItemName())) {
                        names.putIfAbsent(row.getItemName().trim(), row.getReferenceRange());
                    }
                }
                byCode = codes;
                byName = names;
                loadedAt.set(now);
                log.debug("[检验判定] 参考区间主数据装载完成，编码 {} 条、名称 {} 条", codes.size(), names.size());
            } catch (Exception ex) {
                // 主数据读不到不该让「录入检验结果」失败，降级为「无主数据可用」
                log.error("[检验判定] 读取检验项目明细失败，本次仅依据报告区间判定", ex);
                loadedAt.set(now);
            }
        }
    }
}

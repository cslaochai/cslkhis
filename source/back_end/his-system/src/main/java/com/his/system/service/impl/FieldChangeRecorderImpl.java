package com.his.system.service.impl;

import com.his.common.util.DateFormats;
import com.his.common.util.TextUtil;
import com.his.system.entity.CurrentUser;
import com.his.system.entity.SysFieldChangeLog;
import com.his.system.enums.MaskEnum;
import com.his.system.mapper.SysFieldChangeLogMapper;
import com.his.system.service.FieldChangeRecorder;
import com.his.system.support.FieldSpec;
import com.his.system.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * FieldChangeRecorder 的实现：反射读两个快照、按 FieldSpec 渲染打码、批量落库。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FieldChangeRecorderImpl implements FieldChangeRecorder {

    /**
     * 单次最多落多少条：防"整对象反射"式误用把表撑爆
     */
    private static final int MAX_ROWS = 60;

    /**
     * 值列宽（与表 VARCHAR(500) 对齐）
     */
    private static final int VALUE_MAX = 500;

    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * 类 → (属性名 → getter)，反射一次缓存住，别每次保存都 Introspector 一遍
     */
    private static final Map<Class<?>, Map<String, Method>> GETTER_CACHE = new ConcurrentHashMap<>();

    private final SysFieldChangeLogMapper sysFieldChangeLogMapper;

    private static Object readProperty(Object bean, String name) {
        Method getter = GETTER_CACHE
                .computeIfAbsent(bean.getClass(), FieldChangeRecorderImpl::descriptors)
                .get(name);
        if (getter == null) {
            log.warn("字段变更留痕：{} 上没有可读属性 {}，请检查 FieldSpec 拼写", bean.getClass().getSimpleName(), name);
            return null;
        }
        try {
            return getter.invoke(bean);
        } catch (Exception e) {
            log.warn("字段变更留痕：读取 {}.{} 失败", bean.getClass().getSimpleName(), name, e);
            return null;
        }
    }

    private static Map<String, Method> descriptors(Class<?> clazz) {
        Map<String, Method> map = new ConcurrentHashMap<>();
        try {
            for (PropertyDescriptor pd : Introspector.getBeanInfo(clazz).getPropertyDescriptors()) {
                Method read = pd.getReadMethod();
                if (read != null && !"class".equals(pd.getName())) {
                    map.put(pd.getName(), read);
                }
            }
        } catch (Exception e) {
            log.error("字段变更留痕：解析 {} 的属性失败", clazz, e);
        }
        return map;
    }

    /**
     * 值 → 文本。null / 空白一律归一化成 null（空白不是"有值"）。
     */
    private static String toText(Object v) {
        if (v == null) {
            return null;
        }
        if (v instanceof LocalDateTime t) {
            return t.format(DateFormats.DATETIME);
        }
        if (v instanceof LocalDate d) {
            return d.toString();
        }
        if (v instanceof BigDecimal b) {
            // 100.00 直接 toString 是 "100.00"，stripTrailingZeros 后出现科学计数法，必须 toPlainString
            return b.stripTrailingZeros().toPlainString();
        }
        String s = String.valueOf(v).trim();
        return s.isEmpty() ? null : s;
    }

    private static String mask(String s, MaskEnum maskEnum) {
        if (s == null || maskEnum == null || maskEnum == MaskEnum.NONE) {
            return s;
        }
        return switch (maskEnum) {
            case ID_CARD -> keepHeadTail(s, 6, 4);
            // 手机号固定 11 位，取前 3（号段）后 4；座机/其它长度退化为前 2 后 2
            case PHONE -> s.length() == 11 ? keepHeadTail(s, 3, 4) : keepHeadTail(s, 2, 2);
            case BANK_NO -> keepHeadTail(s, 4, 4);
            case NAME -> s.length() <= 1 ? "*" : s.charAt(0) + "*".repeat(s.length() - 1);
            case ADDRESS -> s.length() <= 6 ? "***" : s.substring(0, 6) + "***";
            case NONE -> s;
        };
    }

    /**
     * 保留头尾各若干位，中间全打星；值本身比 head+tail 还短时全部打星（留头尾等于没打码）。
     */
    private static String keepHeadTail(String s, int head, int tail) {
        if (s.length() <= head + tail) {
            return "*".repeat(s.length());
        }
        return s.substring(0, head) + "*".repeat(s.length() - head - tail) + s.substring(s.length() - tail);
    }

    /**
     * 批次号：FC + 年月日时分秒 + 6 位随机 —— 同一毫秒内两次保存靠随机位区分，且人能念出来。
     */
    private static String newBatchNo() {
        return "FC" + LocalDateTime.now().format(DateFormats.COMPACT_DATETIME)
                + String.format("%06d", RANDOM.nextInt(1_000_000));
    }

    @Override
    public String record(String bizType, Object bizId, String bizNo, String bizName,
                         Object oldObj, Object newObj, List<FieldSpec> specs) {
        if (!TextUtil.hasText(bizType) || bizId == null || specs == null || specs.isEmpty()) {
            return null;
        }
        try {
            boolean insert = oldObj == null;
            List<SysFieldChangeLog> rows = new ArrayList<>();
            for (FieldSpec spec : specs) {
                String oldText = render(oldObj, spec);
                String newText = render(newObj, spec);
                // 建档只记"填了值"的字段；修改只记"真的变了"的字段。
                // null 与 "" 归一化后再比 —— 否则"空改成空字符串"会记一条毫无意义的日志。
                if (insert ? newText == null : Objects.equals(oldText, newText)) {
                    continue;
                }
                rows.add(build(bizType, bizId, bizNo, bizName, spec, oldText, newText,
                        insert ? TYPE_INSERT : TYPE_UPDATE, null));
                if (rows.size() >= MAX_ROWS) {
                    break;
                }
            }
            if (rows.isEmpty()) {
                return null;
            }
            String batchNo = newBatchNo();
            for (SysFieldChangeLog row : rows) {
                row.setBatchNo(batchNo);
                sysFieldChangeLogMapper.insert(row);
            }
            return batchNo;
        } catch (Exception e) {
            // 旁路：留痕失败不能让业务保存失败，但必须留下证据
            log.error("字段变更日志写入失败 bizType={} bizId={}", bizType, bizId, e);
            return null;
        }
    }

    @Override
    public void recordAction(String bizType, Object bizId, String bizNo, String bizName, String operation) {
        if (!TextUtil.hasText(bizType) || bizId == null || !TextUtil.hasText(operation)) {
            return;
        }
        try {
            SysFieldChangeLog row = build(bizType, bizId, bizNo, bizName,
                    new FieldSpec("-", operation, MaskEnum.NONE, null), null, null,
                    TYPE_ACTION, null);
            row.setBatchNo(newBatchNo());
            sysFieldChangeLogMapper.insert(row);
        } catch (Exception e) {
            log.error("字段变更日志（操作留痕）写入失败 bizType={} bizId={}", bizType, bizId, e);
        }
    }

    private SysFieldChangeLog build(String bizType, Object bizId, String bizNo, String bizName,
                                    FieldSpec spec, String oldText, String newText,
                                    String changeType, String remark) {
        SysFieldChangeLog row = new SysFieldChangeLog();
        row.setBizType(TextUtil.cut(bizType, 32));
        row.setBizId(TextUtil.cut(String.valueOf(bizId), 64));
        row.setBizNo(TextUtil.cut(bizNo, 64));
        row.setBizName(TextUtil.cut(bizName, 128));
        row.setFieldName(TextUtil.cut(spec.name(), 64));
        row.setFieldLabel(TextUtil.cut(spec.label(), 64));
        row.setOldValue(TextUtil.cut(oldText, VALUE_MAX));
        row.setNewValue(TextUtil.cut(newText, VALUE_MAX));
        row.setChangeType(changeType);
        row.setRemark(TextUtil.cut(remark, 500));
        row.setChangeTime(LocalDateTime.now());
        row.setDelFlag(0);
        // 操作人一律服务端取，不接受调用方传 —— 传进来的可以被伪造，等于没有审计
        CurrentUser user = UserUtils.getCurrentUser();
        if (user != null) {
            row.setOperatorId(user.getUserId());
            // 姓名单一口径取 realName：employeeName/username 回落会让同一张表里
            // 不同来源的操作人格式不一致，事后按人名检索会漏
            row.setOperatorName(TextUtil.cut(user.getRealName(), 64));
            row.setDeptId(user.getDeptId());
            row.setDeptName(TextUtil.cut(user.getDeptName(), 64));
        }
        return row;
    }

    /**
     * 取值 → 渲染码值 → 打码 → 归一化。三步顺序不能反：先翻成人话再打码，否则打码的是 1/2/3。
     */
    private String render(Object bean, FieldSpec spec) {
        if (bean == null) {
            return null;
        }
        Object raw = readProperty(bean, spec.name());
        String text = spec.renderer() != null ? spec.renderer().apply(raw) : toText(raw);
        return mask(text, spec.effectiveMask());
    }
}

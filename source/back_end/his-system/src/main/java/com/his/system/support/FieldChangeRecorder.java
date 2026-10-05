package com.his.system.support;

import com.his.security.entity.CurrentUser;
import com.his.security.UserUtils;
import com.his.system.entity.SysFieldChangeLog;
import com.his.system.enums.MaskEnum;
import com.his.system.mapper.SysFieldChangeLogMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 字段级修改日志落库器（字段级修改日志，sql/159）。
 *
 * <p><b>一句话职责：把"改之前是啥、改之后是啥"钉进表里。</b>
 * 三本账（sql/158）只回答谁调了接口，审计日志的 content 是自由文本拼出来的，
 * 都翻不出结构化的 old→new —— 这个类就是补那一刀的。
 *
 * <p><b>用法（业务侧三步）：</b>
 * <pre>{@code
 *   Object old = service.getById(id);          // ① 改之前先取快照
 *   service.updateXxx(dto);                    // ② 正常跑业务
 *   Object now = service.getById(id);          // ③ 改完再取一次，diff 两个真实快照
 *   fieldChangeRecorder.record("PATIENT", id, no, name, old, now, PATIENT_FIELDS);
 * }</pre>
 * 第③步取"落库后的真实值"而不是拿 DTO 直接比，是有意的：DTO 里 null 的字段在
 * MyBatis-Plus 的 updateById 下<b>不会被写</b>，拿 DTO 比会把"没传"误记成"清空成空"；
 * 而且服务端的副作用（如按身份证补出生日期）只有落库后才看得见。
 *
 * <p><b>旁路写入：</b>留痕失败只记 error 日志，绝不让业务保存跟着失败 ——
 * 但也不静默吞，出问题时日志里能看出来（与 SysAuditLogService 同一口径）。
 *
 * <p><b>这个类是"日志写入端"不是"审计服务"：</b>不判断什么该记什么不该记，
 * 记哪些字段由调用方用 {@link FieldSpec} 声明 —— 判业务该不该留痕是业务自己的事。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FieldChangeRecorder {

    /** 变更类型：建档（只有新值） */
    public static final String TYPE_INSERT = "INSERT";

    /** 变更类型：修改（新旧都有） */
    public static final String TYPE_UPDATE = "UPDATE";

    /** 变更类型：操作留痕（没有字段级新旧值，只记"做了什么"） */
    public static final String TYPE_ACTION = "ACTION";

    /** 单次最多落多少条：防"整对象反射"式误用把表撑爆 */
    private static final int MAX_ROWS = 60;

    /** 值列宽（与表 VARCHAR(500) 对齐） */
    private static final int VALUE_MAX = 500;

    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final SecureRandom RANDOM = new SecureRandom();

    /** 类 → (属性名 → getter)，反射一次缓存住，别每次保存都 Introspector 一遍 */
    private static final Map<Class<?>, Map<String, Method>> GETTER_CACHE = new ConcurrentHashMap<>();

    private final SysFieldChangeLogMapper fieldChangeLogMapper;

    /**
     * 比对两个对象快照并落库。
     *
     * @param oldObj 变更前快照（建档传 null）
     * @param newObj 变更后快照（从库里重新查出来的真实值，不是 DTO）
     * @return 批次号；没有任何字段发生变化时返回 null（没有变化就不该产生日志行）
     */
    public String record(String bizType, Object bizId, String bizNo, String bizName,
                         Object oldObj, Object newObj, List<FieldSpec> specs) {
        if (!StringUtils.hasText(bizType) || bizId == null || specs == null || specs.isEmpty()) {
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
                fieldChangeLogMapper.insert(row);
            }
            return batchNo;
        } catch (Exception e) {
            // 旁路：留痕失败不能让业务保存失败，但必须留下证据
            log.error("字段变更日志写入失败 bizType={} bizId={}", bizType, bizId, e);
            return null;
        }
    }

    /**
     * 记一条非字段级的操作留痕（创建 / 提交 / 归档 / 作废这类"对对象做了什么"）。
     *
     * <p>与 {@code SysAuditLogService} 的分工：那本账记"谁对哪个对象做了什么"，
     * 这本账记"哪个字段变了什么值"。两边都写不重复 —— 前者是人读的一句话，
     * 后者是可核对的结构化 diff，检查时各看各的。
     */
    public void recordAction(String bizType, Object bizId, String bizNo, String bizName, String operation) {
        if (!StringUtils.hasText(bizType) || bizId == null || !StringUtils.hasText(operation)) {
            return;
        }
        try {
            SysFieldChangeLog row = build(bizType, bizId, bizNo, bizName,
                    new FieldSpec("-", operation, MaskEnum.NONE, null), null, null,
                    TYPE_ACTION, null);
            row.setBatchNo(newBatchNo());
            fieldChangeLogMapper.insert(row);
        } catch (Exception e) {
            log.error("字段变更日志（操作留痕）写入失败 bizType={} bizId={}", bizType, bizId, e);
        }
    }

    // 内部实现

    private SysFieldChangeLog build(String bizType, Object bizId, String bizNo, String bizName,
                                    FieldSpec spec, String oldText, String newText,
                                    String changeType, String remark) {
        SysFieldChangeLog row = new SysFieldChangeLog();
        row.setBizType(cut(bizType, 32));
        row.setBizId(cut(String.valueOf(bizId), 64));
        row.setBizNo(cut(bizNo, 64));
        row.setBizName(cut(bizName, 128));
        row.setFieldName(cut(spec.name(), 64));
        row.setFieldLabel(cut(spec.label(), 64));
        row.setOldValue(cut(oldText, VALUE_MAX));
        row.setNewValue(cut(newText, VALUE_MAX));
        row.setChangeType(changeType);
        row.setRemark(cut(remark, 500));
        row.setChangeTime(LocalDateTime.now());
        row.setDelFlag(0);
        // 操作人一律服务端取，不接受调用方传 —— 传进来的可以被伪造，等于没有审计
        CurrentUser user = UserUtils.getCurrentUser();
        if (user != null) {
            row.setOperatorId(user.getUserId());
            row.setOperatorName(cut(StringUtils.hasText(user.getEmployeeName())
                    ? user.getEmployeeName() : user.getRealName(), 64));
            row.setDeptId(user.getDeptId());
            row.setDeptName(cut(user.getDeptName(), 64));
        }
        return row;
    }

    /** 取值 → 渲染码值 → 打码 → 归一化。三步顺序不能反：先翻成人话再打码，否则打码的是 1/2/3。 */
    private String render(Object bean, FieldSpec spec) {
        if (bean == null) {
            return null;
        }
        Object raw = readProperty(bean, spec.name());
        String text = spec.renderer() != null ? spec.renderer().apply(raw) : toText(raw);
        return mask(text, spec.effectiveMask());
    }

    private static Object readProperty(Object bean, String name) {
        Method getter = GETTER_CACHE
                .computeIfAbsent(bean.getClass(), FieldChangeRecorder::descriptors)
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

    /** 值 → 文本。null / 空白一律归一化成 null（空白不是"有值"）。 */
    private static String toText(Object v) {
        if (v == null) {
            return null;
        }
        if (v instanceof LocalDateTime t) {
            return t.format(TS);
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

    /** 保留头尾各若干位，中间全打星；值本身比 head+tail 还短时全部打星（留头尾等于没打码）。 */
    private static String keepHeadTail(String s, int head, int tail) {
        if (s.length() <= head + tail) {
            return "*".repeat(s.length());
        }
        return s.substring(0, head) + "*".repeat(s.length() - head - tail) + s.substring(s.length() - tail);
    }

    /**
     * 按字段名兜底嗅探打码方式：新增字段忘了标 mask 时，靠名字也能挡住最要命的那几类。
     * （护照/军官证/医保卡号都落在 BANK_NO 这一档 —— 都是"前 4 后 4"的证件号口径。）
     */
    static MaskEnum sniffMask(String fieldName) {
        String f = fieldName.toLowerCase();
        if (f.contains("idcard")) {
            return MaskEnum.ID_CARD;
        }
        if (f.contains("phone") || f.contains("mobile") || f.contains("tel")) {
            return MaskEnum.PHONE;
        }
        if (f.contains("bank") || f.contains("cardno") || f.contains("insuranceno") || f.contains("cardno")) {
            return MaskEnum.BANK_NO;
        }
        return MaskEnum.NONE;
    }

    /** 批次号：FC + 年月日时分秒 + 6 位随机 —— 同一毫秒内两次保存靠随机位区分，且人能念出来。 */
    private static String newBatchNo() {
        return "FC" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + String.format("%06d", RANDOM.nextInt(1_000_000));
    }

    private static String cut(String s, int max) {
        return s == null || s.length() <= max ? s : s.substring(0, max);
    }
}

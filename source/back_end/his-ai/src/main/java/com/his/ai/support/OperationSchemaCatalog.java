package com.his.ai.support;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 运营问数的可查询表目录 —— 白名单的唯一事实源。
 * <p>
 * 三处消费同源：提示词的表结构文本、/schema 接口的「可查询数据域」展示、
 * 安全闸门的表名校验。新增可查询表只改这里，三处自动同步；
 * 列清单同时是给模型的「允许使用的列」边界，因此只收录实测核对过列名的列。
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OperationSchemaCatalog {

    private static final List<TableDef> TABLES = List.of(
            new TableDef("sys_department", "科室字典",
                    "dept_id(科室ID), dept_code(科室编码), dept_name(科室名称), dept_type(科室类型), is_open(是否开诊), status(状态)"),
            new TableDef("sys_ward", "病区与床位",
                    "ward_id(病区ID), ward_name(病区名称), total_beds(总床位数), occupied_beds(已占床数), status(0-停用 1-正常)"),
            new TableDef("biz_admission", "住院记录",
                    "admit_dept_id(入院科室ID), ward_id(病区ID), admit_status(0-已出院 1-在院), "
                            + "nursing_level(1-特级 2-一级 3-二级 4-三级护理), admit_time(入院时间), discharge_time(出院时间)"),
            new TableDef("biz_visit", "门诊就诊",
                    "visit_status(0-已取消 1-进行中 2-已完成), start_time(就诊时间), total_amount(费用金额)"),
            new TableDef("biz_fee_record", "记账行（费用事实）",
                    "amount(金额), fee_status(1-待结算 2-已锁定 3-已结算 4-已红冲), "
                            + "item_type(1-挂号费 2-西药 3-中成药 4-中药饮片 5-检查 6-检验 7-治疗 8-耗材), "
                            + "catalog_type(0-自费…3-丙类), encounter_type(1-门诊 2-住院), book_time(记账时间)"),
            new TableDef("biz_settlement_bill", "结算账单",
                    "bill_status(1-待支付 2-部分支付 3-已支付 4-已作废 5-已退费), bill_type(账单类型), "
                            + "settlement_mode(1-自费 2-医保), total_amount(总金额), discount_amount(优惠金额), "
                            + "pool_amount(统筹金额), account_amount(个账金额), self_amount(自付金额), "
                            + "payable_amount(应收金额), paid_amount(已支付金额), refund_amount(退费金额), bill_time(结算时间)"),
            new TableDef("biz_payment_txn", "收退款流水",
                    "direction(1-收款 2-退款), amount(金额：收款为正退款为负), txn_status(1-成功 2-已冲正), "
                            + "pay_method(1-现金 2-微信 3-支付宝 4-医保个账 5-院内余额 6-银行卡 7-转账), txn_time(交易时间)"),
            new TableDef("biz_prescription", "处方",
                    "prescription_status(1-草稿 2-已提交待审方 3-已审方 4-已发药 5-已作废 6-已退药 7-审方退回), "
                            + "payment_status(取值0/1/2), visit_date(就诊日期), total_amount(处方金额), "
                            + "dept_name(开方科室), doctor_name(开方医生)"),
            new TableDef("biz_prescription_detail", "处方用药明细",
                    "drug_name(药品名称), quantity(数量), amount(金额), detail_status(明细状态)"),
            new TableDef("biz_laboratory_record", "检验记录",
                    "record_status(1-已登记…7-已发布 8-已取消), visit_date(就诊日期), apply_dept_name(申请科室), price(价格)"),
            new TableDef("biz_operation_apply", "手术申请",
                    "operation_status(0-待排期…3-已完成 4-已取消), operation_level(1~4级手术), "
                            + "anesthesia_type(1~5), is_emergency(是否急诊), apply_time(申请时间)"));
    private static final Set<String> TABLE_NAMES = TABLES.stream()
            .map(TableDef::tableName)
            .collect(Collectors.toUnmodifiableSet());

    public static List<TableDef> tables() {
        return TABLES;
    }

    public static boolean isAllowed(String tableName) {
        return tableName != null && TABLE_NAMES.contains(tableName.toLowerCase(Locale.ROOT));
    }

    /**
     * 提示词用的表结构文本（{{schema}} 变量）
     */
    public static String schemaText() {
        StringBuilder text = new StringBuilder();
        for (TableDef table : TABLES) {
            text.append(table.tableName()).append("（").append(table.usage()).append("）可用列：")
                    .append(table.columns()).append('\n');
        }
        return text.toString().trim();
    }

    /**
     * @param tableName 表名（白名单判定的键）
     * @param usage     用途说明（人读）
     * @param columns   可用列与码值口径（模型读，列名必须与库一致）
     */
    public record TableDef(String tableName, String usage, String columns) {
    }
}

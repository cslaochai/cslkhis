package com.his.patient.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * 护理质控入参（外层空壳 + 内层静态类，同 {@code NurseScheduleDTO}）。
 *
 * <p>时间入参：月份用 {@code yyyy-MM} 字符串（列本来就是 CHAR(7)），检查日期用
 * {@code yyyy-MM-dd}（LocalDate），与前端 el-date-picker 的 {@code value-format} 同口径，
 * 不接收 ISO T 分隔（AGENTS §3 宽进严出）。
 *
 * <p>长度口径：PDCA 三段文本（存在问题/原因分析/整改措施）不在入参层卡长度，
 * 服务端 {@code cut(x, 500)} 兜底截断；月份/合格数这类结构化字段保留格式与非负校验，
 * 格式错是请求非法不是写得啰嗦。
 *
 * <p><b>故意没有的入参</b>：合格率、得分、得分率、抽查总数统统不让前端传 ——
 * 主表的六个数字只能由明细求和与应得分×合格/抽查算出来（口径 d），
 * 否则「页面自己算的汇总」和「台账重算用的汇总」迟早对不上。
 */
public class NursingQcDTO {

    /** 检查单保存（新增与修改同一个 upsert：命中唯一键即整单覆盖明细） */
    @Data
    public static class CheckUpsert {
        /** 病区ID */
        @NotNull(message = "请选择病区")
        private Long wardId;
        @Pattern(regexp = "^\\d{4}-\\d{2}$", message = "检查月份格式必须为 yyyy-MM")
        @NotNull(message = "请选择检查月份")
        private String checkMonth;
        @JsonFormat(pattern = "yyyy-MM-dd")
        @NotNull(message = "请选择现场检查日期")
        private LocalDate checkDate;
        /** 类别 */
        @NotNull(message = "请选择检查类别")
        private Integer category;
        /** 检查人（护士长/护理部质控组）；空=记当前登录人 */
        private Long inspectorId;
        /** 本轮小结；长度由服务端截到列宽 500 */
        private String summary;
        /** 明细项集合 */
        @Valid
        @NotEmpty(message = "请至少录入一项检查结果")
        private List<CheckItemInput> items;
    }

    /** 一条明细：只填「抽查多少例、合格多少例」和 PDCA 文本，得分由服务端算 */
    @Data
    public static class CheckItemInput {
        @NotNull(message = "缺少检查项目")
        private Long itemId;
        @NotNull(message = "抽查例数不能为空")
        @Min(value = 0, message = "抽查例数不能为负")
        private Integer checkedNum;
        @NotNull(message = "合格例数不能为空")
        @Min(value = 0, message = "合格例数不能为负")
        private Integer qualifiedNum;
        private String problem;
        private String causeAnalysis;
        private String rectifyMeasure;
        /** 备注 */
        private String remark;
    }

    /** 检查单确认/退回（2-已确认冻结明细，1-退回草稿才能改） */
    @Data
    public static class CheckStatus {
        @NotNull(message = "缺少检查单ID")
        private Long id;
        @NotNull(message = "请指定状态（1-草稿 2-已确认）")
        private Integer status;
    }

    /** 检查单分页 */
    @Data
    public static class CheckQueryPage {
        /** 关键字 */
        private String keyword;
        /** 病区ID */
        private Long wardId;
        /** 科室ID */
        private Long deptId;
        /** 类别 */
        private Integer category;
        private Integer status;
        @Pattern(regexp = "^$|^\\d{4}-\\d{2}$", message = "起始月份格式必须为 yyyy-MM")
        private String startMonth;
        @Pattern(regexp = "^$|^\\d{4}-\\d{2}$", message = "结束月份格式必须为 yyyy-MM")
        private String endMonth;
        /** 页码 */
        private Integer pageNum = 1;
        /** 每页条数 */
        private Integer pageSize = 10;
    }

    /** 台账分页 */
    @Data
    public static class LedgerQueryPage {
        /** 关键字 */
        private String keyword;
        /** 病区ID */
        private Long wardId;
        /** 科室ID */
        private Long deptId;
        private String indicatorCode;
        private Integer reportStatus;
        /** 统计月份（yyyy-MM） */
        @Pattern(regexp = "^$|^\\d{4}-\\d{2}$", message = "统计月份格式必须为 yyyy-MM")
        private String statMonth;
        @Pattern(regexp = "^$|^\\d{4}-\\d{2}$", message = "起始月份格式必须为 yyyy-MM")
        private String startMonth;
        @Pattern(regexp = "^$|^\\d{4}-\\d{2}$", message = "结束月份格式必须为 yyyy-MM")
        private String endMonth;
        /** 页码 */
        private Integer pageNum = 1;
        /** 每页条数 */
        private Integer pageSize = 10;
    }

    /** 月度 KPI：wardId 空=当前岗位可见范围全院合并 */
    @Data
    public static class MonthQuery {
        /** 病区ID */
        private Long wardId;
        /** 统计月份（yyyy-MM） */
        @Pattern(regexp = "^\\d{4}-\\d{2}$", message = "统计月份格式必须为 yyyy-MM")
        @NotNull(message = "请选择统计月份")
        private String statMonth;
    }

    /** 趋势：一条指标一条线，页面切换指标就换一个 code 重查 */
    @Data
    public static class TrendQuery {
        /** 病区ID */
        private Long wardId;
        @Pattern(regexp = "^\\d{4}-\\d{2}$", message = "统计月份格式必须为 yyyy-MM")
        @NotNull(message = "请选择统计指标")
        private String indicatorCode;
        @Pattern(regexp = "^$|^\\d{4}-\\d{2}$", message = "起始月份格式必须为 yyyy-MM")
        private String startMonth;
        @Pattern(regexp = "^$|^\\d{4}-\\d{2}$", message = "结束月份格式必须为 yyyy-MM")
        private String endMonth;
    }

    /** 病区对比：某月某指标各病区落点 */
    @Data
    public static class CompareQuery {
        /** 统计月份（yyyy-MM） */
        @Pattern(regexp = "^\\d{4}-\\d{2}$", message = "统计月份格式必须为 yyyy-MM")
        @NotNull(message = "请选择统计月份")
        private String statMonth;
        @NotNull(message = "请选择统计指标")
        private String indicatorCode;
    }

    /** 台账重算（wardId 空=当前岗位可见范围的全部病区） */
    @Data
    public static class RecalcCommand {
        /** 病区ID */
        private Long wardId;
        /** 统计月份（yyyy-MM） */
        @Pattern(regexp = "^\\d{4}-\\d{2}$", message = "统计月份格式必须为 yyyy-MM")
        @NotNull(message = "请选择要重算的月份")
        private String statMonth;
    }

    /** 上报 / 退回（2-上报锁定，1-退回未上报后才能被重算改掉） */
    @Data
    public static class ReportCommand {
        /** 统计月份（yyyy-MM） */
        @Pattern(regexp = "^\\d{4}-\\d{2}$", message = "统计月份格式必须为 yyyy-MM")
        @NotNull(message = "请选择统计月份")
        private String statMonth;
        /** 病区ID */
        private Long wardId;
        @NotNull(message = "请指定上报状态（1-退回未上报 2-上报）")
        private Integer reportStatus;
    }
}

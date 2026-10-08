package com.his.emr.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 出院随访任务看板（服务端聚合，前端不数当前页）。
 */
@Data
public class FollowupStatVO implements Serializable {

    /**
     * 待随访
     */
    private Long pendingCount;

    /**
     * 随访中
     */
    private Long doingCount;

    /**
     * 已完成
     */
    private Long doneCount;

    /**
     * 已取消
     */
    private Long cancelledCount;

    /**
     * 任务总数（不含已取消口径由前端自行判断，这里给全量）
     */
    private Long totalCount;

    /**
     * 今日应随访（待随访+随访中且计划时间是今天）
     */
    private Long todayDueCount;

    /**
     * 逾期未随访（待随访+随访中且计划时间已过）
     */
    private Long overdueCount;

    /**
     * 今日已完成
     */
    private Long doneTodayCount;

    /**
     * 由随访生成的复诊号数（闭环能看出「随访带来了多少回院」）
     */
    private Long revisitCount;

    /**
     * 完成率 = 已完成 /（总数 - 已取消）；已取消不是失败，把它算进分母会把率压低
     */
    private BigDecimal completeRate;

    /**
     * 按随访方式分布
     */
    private List<StatItem> byType;

    /**
     * 科室待办 TOP10
     */
    private List<DeptPending> byDeptPending;

    /**
     * 统计时间（看板是瞬时值，标出来免得两个屏幕对不上数时无法自证）
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime statTime;

    /**
     * 通用分布项（key=码值，name=字典名，count=条数）
     */
    @Data
    public static class StatItem implements Serializable {

        private String key;

        /**
         * 名称
         */
        private String name;

        private Long count;

        public StatItem(String key, String name, Long count) {
            this.key = key;
            this.name = name;
            this.count = count;
        }
    }

    /**
     * 科室待办项
     */
    @Data
    public static class DeptPending implements Serializable {

        /**
         * 科室ID
         */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long deptId;

        /**
         * 科室名称
         */
        private String deptName;

        /**
         * 未完成条数
         */
        private Long pendingCount;

        /**
         * 该科室已完成条数（一起给，看得出是「活多」还是「没干」）
         */
        private Long doneCount;
    }
}

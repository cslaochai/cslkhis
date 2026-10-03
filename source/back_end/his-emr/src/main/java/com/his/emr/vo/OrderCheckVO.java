package com.his.emr.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 医生站开单偏离预检出参。
 *
 * <p>软约束口径：只报偏离不拦截。comparable=false 表示模板全部是自由文本步骤
 * （纯文书模板），前端不做偏离提示。
 */
@Data
public class OrderCheckVO implements Serializable {

    private Boolean enrolled;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long enrollId;

    private String pathwayName;

    private String version;

    /** 当前路径日 */
    private Integer dayNo;

    private Integer totalDays;

    /** 模板是否带编码步骤（可机器比对） */
    private Boolean comparable;

    /** 当前路径日计划步骤 */
    private List<PathwayStepVO> planSteps;

    /** 本单中偏离路径的条目 */
    private List<Deviation> deviations = new ArrayList<>();

    @Data
    public static class Deviation implements Serializable {
        private String itemCode;
        /** 项目名称 */
        private String itemName;

        public Deviation() {
        }

        public Deviation(String itemCode, String itemName) {
            this.itemCode = itemCode;
            this.itemName = itemName;
        }
    }
}

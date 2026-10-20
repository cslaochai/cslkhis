package com.his.emr.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 医生站开单偏离预检出参。
 */
@Data
public class OrderCheckVO implements Serializable {

    /** 本次住院是否有在径的路径入径记录（false 时其余字段全为空） */
    private Boolean enrolled;

    /** 入径记录ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long enrollId;

    /** 路径名称 */
    private String pathwayName;

    /** 入径时快照的路径版本号 */
    private String version;

    /**
     * 当前路径日
     */
    private Integer dayNo;

    /** 路径总日数 */
    private Integer totalDays;

    /**
     * 模板是否带编码步骤（可机器比对）
     */
    private Boolean comparable;

    /**
     * 当前路径日计划步骤
     */
    private List<PathwayStepVO> planSteps;

    /**
     * 本单中偏离路径的条目
     */
    private List<Deviation> deviations = new ArrayList<>();

    @Data
    @NoArgsConstructor
    public static class Deviation implements Serializable {
        private String itemCode;
        /**
         * 项目名称
         */
        private String itemName;

        public Deviation(String itemCode, String itemName) {
            this.itemCode = itemCode;
            this.itemName = itemName;
        }
    }
}

package com.his.equipment.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 设备档案/维保/计量 DTO 集合。
 *
 * <p>日期入参一律宽进：LocalDate 用 yyyy-MM-dd（AGENTS.md 日期格式铁律）。
 */
public class EquipmentDTO {

    /** 设备台账分页查询 */
    @Data
    public static class QueryPage implements Serializable {

        /** 关键词：设备编码/名称/型号/科室模糊 */
        private String keyword;

        /** 设备类别（字典 his_equipment_category） */
        private Integer category;

        /** 状态（1-在用 2-停用 3-维修中 4-报废） */
        private Integer status;

        /** 页码 */
        private Integer pageNum = 1;

        /** 每页条数 */
        private Integer pageSize = 10;
    }

    /** 维保登记 */
    @Data
    public static class MaintainCreate implements Serializable {

        @NotNull(message = "设备ID不能为空")
        private Long equipmentId;

        /** 维保类型:1-保养 2-维修 3-巡检 */
        @NotNull(message = "维保类型不能为空")
        private Integer maintainType;

        @NotNull(message = "维保日期不能为空")
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate maintainDate;

        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate nextMaintainDate;

        private BigDecimal cost;

        /** 故障描述（维修类型时建议填） */
        private String faultDesc;

        private String handleResult;

        /** 维保结果:1-正常 2-异常 */
        private Integer maintainResult;

        /** 维保人（不填取当前登录人） */
        private String handlerName;
    }

    /** 维保记录分页 */
    @Data
    public static class MaintainQueryPage implements Serializable {

        @NotNull(message = "设备ID不能为空")
        private Long equipmentId;

        private Integer maintainType;

        /** 页码 */
        private Integer pageNum = 1;

        /** 每页条数 */
        private Integer pageSize = 10;
    }

    /** 计量登记 */
    @Data
    public static class MeteringCreate implements Serializable {

        @NotNull(message = "设备ID不能为空")
        private Long equipmentId;

        /** 计量类型:1-强检 2-校准 */
        @NotNull(message = "计量类型不能为空")
        private Integer meteringType;

        @NotNull(message = "计量日期不能为空")
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate meteringDate;

        @NotNull(message = "有效期至不能为空")
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate validUntil;

        /** 计量结果:1-合格 2-不合格 */
        private Integer meteringResult;

        private String certNo;

        private String agency;
    }

    /** 计量记录分页 */
    @Data
    public static class MeteringQueryPage implements Serializable {

        @NotNull(message = "设备ID不能为空")
        private Long equipmentId;

        private Integer meteringType;

        /** 页码 */
        private Integer pageNum = 1;

        /** 每页条数 */
        private Integer pageSize = 10;
    }

    /** 维保记录删除（录错可删） */
    @Data
    public static class MaintainDelete implements Serializable {

        /** 主键 */
        @NotNull(message = "记录ID不能为空")
        private Long id;

        /** 原因 */
        @NotBlank(message = "删除原因不能为空")
        private String reason;
    }
}

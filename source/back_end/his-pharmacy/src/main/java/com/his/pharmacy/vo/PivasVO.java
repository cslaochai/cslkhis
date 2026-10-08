package com.his.pharmacy.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 静配主单 VO（主单 + 明细）。
 */
@Data
public class PivasVO implements Serializable {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /** 静配单号 */
    private String pivasNo;

    /** 调配日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate admixDate;

    /** 入院ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long admissionId;

    /** 患者ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;

    /** 患者编号 */
    private String patientNo;

    /** 患者姓名 */
    private String patientName;

    /** 病区ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long wardId;

    /** 病区名称 */
    private String wardName;

    /**
     * 主单状态（聚合派生）：1-待审方 2-待排队 3-待调配 4-待核对 5-已完成 6-全拒配
     */
    private Integer status;

    /** 明细条数 */
    private Integer itemCount;

    /** 生成人 */
    private String generateBy;

    /** 生成时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime generateTime;

    /** 打标签（排队） */
    private String labelBy;

    /** 打标签时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime labelTime;

    /** 备注 */
    private String remark;

    /**
     * 明细（getDetailById 时填充；列表页为 null）
     */
    private List<PivasItemVO> items;
}

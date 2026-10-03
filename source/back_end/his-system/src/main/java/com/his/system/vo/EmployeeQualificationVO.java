package com.his.system.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDate;

/**
 * 员工资格证书出参
 */
@Data
public class EmployeeQualificationVO {

    /** 主键（雪花ID） */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /** 员工ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long employeeId;

    /** 证书类型（2-医师执业证 3-护士执业证 4-药师资格证 5-技术职称聘书 9-其他） */
    private String certType;

    /** 证书编号 */
    private String certNo;

    /** 发证机关 */
    private String issueOrg;

    /** 发证日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate issueDate;

    /** 有效期至（null = 长期有效） */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate validUntil;

    /** 备注 */
    private String remark;
}

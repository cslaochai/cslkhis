package com.his.appoint.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.util.List;

/**
 * 医生诊室信息VO
 */
@Data
public class DoctorConsultingVO {

    /**
     * 排班ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long scheduleId;

    /**
     * 诊室ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long roomId;

    /**
     * 诊室名称
     */
    private String roomName;

    /**
     * 医生ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long doctorId;

    /**
     * 医生姓名
     */
    private String doctorName;

    /**
     * 接诊状态（从Redis获取）
     * 0-空闲 1-接诊中 2-暂停
     * null表示Redis中无状态记录，不显示
     */
    private Integer consultStatus;

    /**
     * 当前就诊中的患者列表
     */
    private List<ConsultingPatientVO> consultingPatients;
}

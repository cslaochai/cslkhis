package com.his.emr.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;

/**
 * 科室快照（就诊锚点带出的科室 ID + 科室名）。
 *
 * <p>单据上的科室一律「ID 现查 + 名字以科室表为准」，不用单据里的历史快照名：
 * 科室改过名要跟着改，撤掉的科室查不到就留 null 交给上层显示「未指定」。
 */
@Data
public class DeptSnapshotVO implements Serializable {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;

    private String deptName;
}
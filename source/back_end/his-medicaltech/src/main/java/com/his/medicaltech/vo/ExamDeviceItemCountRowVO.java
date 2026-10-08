package com.his.medicaltech.vo;

import lombok.Data;

import java.io.Serializable;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

/**
 * 各检查设备已开展项目数（{@code BizExamDeviceMapper#countItemsByDevice} 一行）。
 *
 * <p>列表页一次统计返回，不逐行查：设备台位数不多，但项目表逐行 count 是 N+1。
 */
@Data
public class ExamDeviceItemCountRowVO implements Serializable {

    /**
     * 设备ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deviceId;

    /**
     * 已开展项目数
     */
    private Long itemCount;
}
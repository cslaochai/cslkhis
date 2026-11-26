package com.his.iot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 药品冷链温湿度 GSP 归档（biz_coldchain_temp_log）。
 * <p>
 * 与时序库的分工：TDengine 只保留 90 天原始点/聚合；本表按 5 分钟一轮长期留档（GSP 要求至少 5 年），
 * 唯一键 (device_no, ts) 保证定时任务重跑不会写重复。
 */
@Data
@TableName("biz_coldchain_temp_log")
public class IotColdChainTempLog implements Serializable {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String deviceNo;

    private String deptName;

    private String cabinetName;

    /** 窗口平均温度 */
    private Double temp;

    /** 窗口最低温度 */
    private Double tempMin;

    /** 窗口最高温度 */
    private Double tempMax;

    /** 窗口平均湿度 */
    private Double humidity;

    /** 窗口内上报点数 */
    private Integer pointCount;

    /** 柜门状态（0-关闭 1-开启），归档时取窗口内是否开过门 */
    private Integer door;

    /** 供电状态（0-异常 1-正常） */
    private Integer power;

    /** 记录类型（1-常规 2-超限加密） */
    private Integer recordType;

    /** 窗口起始时间 */
    private LocalDateTime ts;

    private LocalDateTime createTime;
}

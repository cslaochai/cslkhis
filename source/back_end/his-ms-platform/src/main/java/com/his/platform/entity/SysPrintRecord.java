package com.his.platform.entity;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 打印台账（标签打印留痕）。
 * <p>不存标签文件：码值 + 字段快照 + 业务表数据即可重放同一张标签。</p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_print_record")
public class SysPrintRecord extends BaseEntity {

    /** 逻辑删除标志（0-正常 1-删除） */
    @TableLogic
    private Integer delFlag;

    /** 备注 */
    private String remark;

    /**
     * 业务类型（1-腕带 2-检验标本管 3-血袋 4-静配输液贴 5-口服药袋 6-CSSD器械包 7-病理标本 8-检查影像袋）
     */
    private Integer bizType;

    /**
     * 业务对象ID
     */
    private Long bizId;

    /**
     * 标签码值
     */
    private String codeValue;

    /**
     * 患者ID
     */
    private Long patientId;

    /**
     * 第几次打印（1-首次，2起为补打）
     */
    private Integer printNo;

    /**
     * 补打原因
     */
    private String printReason;

    /**
     * 渲染器标识
     */
    private String renderCode;

    /**
     * 纸张规格
     */
    private String paperSpec;

    /**
     * 打印机名称
     */
    private String printerName;

    /**
     * 打印终端IP
     */
    private String terminalIp;

    /**
     * 打印时字段快照（JSON）
     */
    private String snapshotJson;

    /**
     * 打印人ID
     */
    private Long operatorId;

    /**
     * 打印人姓名
     */
    private String operatorName;

    /**
     * 打印时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime printTime;
}

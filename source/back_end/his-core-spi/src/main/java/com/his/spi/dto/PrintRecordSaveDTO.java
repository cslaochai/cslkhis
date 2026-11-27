package com.his.spi.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 打印台账写入入参（跨模块契约）。
 * <p>标签版式写死在渲染端，本对象只描述「打了哪个对象的哪一次」，不含任何模板信息。</p>
 */
@Data
public class PrintRecordSaveDTO implements Serializable {

    /**
     * 业务类型，取 PrintBizTypeEnum 的码值
     */
    private Integer bizType;

    /**
     * 业务对象ID（业务表主键）
     */
    private Long bizId;

    /**
     * 标签码值（腕带住院号 / 标本号 / 血袋号 / 器械包条码）
     */
    private String codeValue;

    /**
     * 患者ID（非患者对象传 0 或空）
     */
    private Long patientId;

    /**
     * 补打原因；第 2 次及以后必填
     */
    private String printReason;

    /**
     * 渲染器标识（代码内写死的版式ID，如 specimen_v1）
     */
    private String renderCode;

    /**
     * 纸张规格（如 50x25）
     */
    private String paperSpec;

    /**
     * 打印机名称（终端绑定）
     */
    private String printerName;

    /**
     * 打印时字段快照（JSON 文本）
     */
    private String snapshotJson;
}

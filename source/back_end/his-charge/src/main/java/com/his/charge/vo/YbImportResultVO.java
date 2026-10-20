package com.his.charge.vo;

import lombok.Data;

/**
 * 目录批量导入结果（值即结论）。
 */
@Data
public class YbImportResultVO {

    /**
     * 新增条数
     */
    private Integer inserted;

    /**
     * 更新条数（yb_code 已存在，按名称/规格等覆盖）
     */
    private Integer updated;

    /**
     * 跳过条数（缺编码/名称等无效行）
     */
    private Integer skipped;
}

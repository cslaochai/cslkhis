package com.his.platform.vo;

import lombok.Data;

/**
 * 手术操作编码下拉候选：手术申请与手术类医嘱选项目时的出参。
 */
@Data
public class SysIcd9cm3SelectListVO {

    /**
     * 手术操作编码
     */
    private String opCode;

    /**
     * 手术操作名称
     */
    private String opName;

    /**
     * 编码口径（1-医保版贯标码 0-历史缩写码）
     */
    private Integer codeStd;
}

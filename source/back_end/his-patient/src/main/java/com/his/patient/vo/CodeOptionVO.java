package com.his.patient.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 通用码值下拉项（文书类型 / 护理类型等）。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CodeOptionVO implements Serializable {

    /**
     * 码值
     */
    private Integer code;

    /**
     * 文案
     */
    private String label;
}

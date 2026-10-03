package com.his.patient.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 通用码值下拉项（文书类型 / 护理类型等）。
 *
 * <p>为什么要后端给而不是前端写死：这些码值是**库里的数据契约**（会进统计、会进质控），
 * 前端写死一份就等于多了一个会漂移的定义。前端只负责渲染。
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

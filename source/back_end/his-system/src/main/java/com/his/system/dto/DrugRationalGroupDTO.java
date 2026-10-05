package com.his.system.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 合理用药审查的一组入参（一组 = 一张处方/一条医嘱的全部药品行）
 * <p>
 * 之所以按组批量而不是逐行：相互作用是<b>行与行之间</b>的关系，单行永远判不出来；
 * 而 groupId 只是调用方自己的定位标识（审方列表用它把命中结果贴回对应处方行），服务端不解释其含义。
 */
@Data
public class DrugRationalGroupDTO {

    /**
     * 调用方定位标识（通常是处方ID，字符串化避免前端精度丢失）
     */
    @NotBlank(message = "分组标识不能为空")
    private String groupId;

    /**
     * 明细项集合
     */
    @NotEmpty(message = "药品明细不能为空")
    @Valid
    private List<DrugRationalItemDTO> items;
}

package com.his.system.vo;

import lombok.Data;

import java.util.List;

/**
 * 一组药品行的审查结果（一组 = 一张处方）
 */
@Data
public class DrugRationalGroupVO {

    /**
     * 与入参 groupId 原样回传，调用方据此贴回自己的行
     */
    private Long groupId;

    /**
     * 本组是否存在应当拦截审方通过的命中（只有相互作用禁忌级为 true）
     */
    private Boolean blocked;

    /**
     * 拦截时给药师/医生看的合并理由（逐字由命中项正文拼成，不再加工）
     */
    private String blockMessage;

    private List<DrugRationalHitVO> hits;
}

package com.his.supplies.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * CSSD 器械包模板 VO（详情附带组成明细）。
 */
@Data
public class CssdPackTemplateVO {

    /** 器械包模板ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /** 包编码 */
    private String templateCode;
    /** 器械包名称 */
    private String packName;

    /** 默认灭菌方式（1-高压蒸汽 2-环氧乙烷 3-低温等离子） */
    private Integer sterilizeMethod;
    private String sterilizeMethodText;

    /** 状态（1-启用 0-停用） */
    private Integer status;

    /** 备注 */
    private String remark;

    /** 明细条数（列表用） */
    private Integer itemCount;

    /** 明细项集合 */
    private List<CssdPackTemplateItemVO> items;

    /** 创建人 */
    private String createBy;
    /** 创建时间 */
    private LocalDateTime createTime;
}

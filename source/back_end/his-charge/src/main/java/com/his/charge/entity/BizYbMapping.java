package com.his.charge.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableLogic;


/**
 * 医保目录对照表（院内项目 ↔ 国家医保编码，一对一）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_yb_mapping")
public class BizYbMapping extends BaseEntity {
    /** 逻辑删除标志（0 未删除 1 已删除） */
    @TableLogic
    private Integer delFlag;

    /** 备注 */
    private String remark;



    /**
     * 院内项目类型（1-药品 2-诊疗项目 3-检验项目 4-耗材）
     */
    private Integer itemType;

    /**
     * 院内项目ID（药品字典/治疗项目字典/检验项目字典/耗材字典的ID）
     */
    private Long itemId;

    /**
     * 院内项目编码（对照时快照）
     */
    private String itemCode;

    /**
     * 院内项目名称（对照时快照）
     */
    private String itemName;

    /**
     * 医保目录ID（国家医保目录的ID）
     */
    private Long catalogId;

    /**
     * 国家医保编码（对照时快照）
     */
    private String ybCode;

    /**
     * 目录名称（对照时快照）
     */
    private String ybName;

    /**
     * 对照方式（1-自动名称精确 2-人工 3-导入）
     */
    private Integer matchType;

    /**
     * 对照人（自动对照记 system）
     */
    private String mappedBy;

    /**
     * 对照时间
     */
    private LocalDateTime mappedTime;
}

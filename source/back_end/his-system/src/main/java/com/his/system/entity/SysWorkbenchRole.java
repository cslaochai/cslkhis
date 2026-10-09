package com.his.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.baomidou.mybatisplus.annotation.TableLogic;


/**
 * 角色 → 工作台卡片配置实体（"谁的工作台长什么样"的唯一答案）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_workbench_role")
public class SysWorkbenchRole extends BaseEntity {
    /** 逻辑删除标志（0 未删除 1 已删除） */
    @TableLogic
    private Integer delFlag;

    /** 备注 */
    private String remark;



    /**
     * 角色ID（刻意存 id 不存角色码，角色改名不影响配置）
     */
    private Long roleId;
    /**
     * 卡片ID
     */
    private Long widgetId;
    /**
     * 该角色下的卡片顺序
     */
    private Integer sortOrder;
    /**
     * 1-展示 0-关掉但不删，便于回滚
     */
    private Integer visible;
    /**
     * 落点（0-默认 1-一律工作台 2-一律患者工作站），服务端取 MAX 兜底
     */
    private Integer landingScope;
}

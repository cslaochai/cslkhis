package com.his.ai.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_ai_prompt_template")
public class SysAiPromptTemplate extends BaseEntity {

    /** 模板编码 */
    private String templateCode;

    /** 模板名称 */
    private String templateName;

    /** 版本号 */
    private String version;

    /** 提示词内容 */
    private String content;

    /** 是否当前生效：1-是 0-否 */
    private Integer isActive;

    /** 备注 */
    private String remark;
}

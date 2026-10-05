package com.his.system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 工作台卡片新增/修改入参
 */
@Data
public class WorkbenchWidgetUpsertDTO {

    /**
     * 卡片ID，新增时为空
     */
    private Long id;

    /**
     * 卡片编码
     */
    @NotBlank(message = "卡片编码不能为空")
    private String widgetCode;

    /**
     * 卡片标题
     */
    @NotBlank(message = "卡片名称不能为空")
    private String widgetName;

    /**
     * 归属区域
     */
    @NotBlank(message = "归属区域不能为空")
    private String area;

    /**
     * 取数来源标识
     */
    @NotBlank(message = "取数标识不能为空")
    private String apiKey;

    /**
     * 可见所需权限码，留空=登录即可；填了必须是菜单里真实存在的 permission
     */
    private String permission;

    /**
     * 栅格占宽
     */
    @NotNull(message = "栅格占宽不能为空")
    private Integer defaultSpan;

    /**
     * 展示顺序
     */
    @NotNull(message = "排序号不能为空")
    private Integer sortOrder;

    /**
     * 状态（1-已上线可挂载 0-注册表先占位）
     */
    @NotNull(message = "状态不能为空")
    private Integer status;

    /**
     * 备注
     */
    private String remark;
}

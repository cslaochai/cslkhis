package com.his.miniapp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 常见问题新增/修改（后台维护）。
 *
 * <p>答案长度用截断而不是校验：运营粘贴一段长说明进来，
 * 报「答案超长」逼他回去删字，不如服务端截到列宽并把完整内容留下 ——
 * 但截断了必须让他知道，所以接口返回实际入库长度。
 */
@Data
@Schema(name = "FaqUpsertDTO", description = "常见问题新增或修改")
public class FaqUpsertDTO {

    /** 主键（为空则新增） */
    private String id;

    /** 分类编码 */
    @NotBlank(message = "分类编码不能为空")
    @Size(max = 32, message = "分类编码过长")
    private String categoryCode;

    /** 分类名称 */
    @NotBlank(message = "分类名称不能为空")
    @Size(max = 64, message = "分类名称过长")
    private String categoryName;

    /** 问题 */
    @NotBlank(message = "问题不能为空")
    @Size(max = 200, message = "问题最多200字")
    private String question;

    /** 答案 */
    @NotBlank(message = "答案不能为空")
    @Size(max = 1000, message = "答案最多1000字")
    private String answer;

    /** 检索关键词（顿号分隔，含口语同义词） */
    @Size(max = 500, message = "关键词最多500字")
    private String keywords;

    /** 热门（0-否 1-是） */
    private Integer hotFlag;

    /** 状态（0-停用 1-启用） */
    private Integer status;

    /** 排序号 */
    private Integer sortOrder;
}

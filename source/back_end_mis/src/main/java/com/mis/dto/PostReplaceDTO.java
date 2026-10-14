package com.mis.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 员工岗位整单替换入参（null = 未提交不处理；空列表 = 清空该员工全部岗位）
 */
@Data
public class PostReplaceDTO implements Serializable {

    @NotNull(message = "员工ID不能为空")
    private Long employeeId;

    private List<PostItemDTO> posts;
}

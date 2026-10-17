package com.his.ai.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 问数结果的一行数据。列头由问题动态决定（交叉表形态），
 */
@Data
@Schema(description = "问数结果行，cells 与 columns 按下标一一对应")
public class OperationRowVO {

    @Schema(description = "各列取值，日期已格式化为文本")
    private List<Object> cells = new ArrayList<>();
}

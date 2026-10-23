package com.his.clinical.dto;

import com.his.spi.dto.DrugRationalCheckItemDTO;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import java.util.List;

/**
 * 开方实时审方入参：医生工作台未保存的处方行数据。
 *
 * <p>行上名称与规格取前端快照（与保存后单据快照同源），复用药品域判定。
 */
@Data
public class DrugRationalDraftQueryDTO {

    /**
     * 药品行（至少一行；空行在前端过滤掉再提交）
     */
    @NotEmpty(message = "药品行不能为空")
    private List<DrugRationalCheckItemDTO> items;
}

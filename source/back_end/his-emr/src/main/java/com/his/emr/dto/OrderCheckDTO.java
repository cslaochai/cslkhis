package com.his.emr.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 医生站开单偏离预检入参（orderCheck）：把本次新开医嘱条目交给服务端与在径模板比对。
 */
@Data
public class OrderCheckDTO implements Serializable {

    /** 入院ID */
    @NotNull(message = "admissionId 不能为空")
    private Long admissionId;

    /** 本次开立的医嘱条目（无编码的条目按名称精确比对） */
    private List<CheckItem> items;

    @Data
    public static class CheckItem implements Serializable {
        private String itemCode;
        /** 项目名称 */
        private String itemName;
    }
}

package com.his.medicaltech.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 健康档案分组（过敏史 / 既往史 / 手术史 / 家族史 / 用药史 / 联系人）。
 *
 * <p>这些是**患者级**信息，不属于任何一次就诊，所以不放在时间轴上，
 * 单独成组放在身份卡后面。每项给一个 {@code summary} 和可选的明细。
 */
@Data
@Schema(description = "健康档案分组")
public class CdrProfileGroupVO {

    @Schema(description = "分组标识：allergy/pastDisease/surgery/family/medication/contact")
    private String key;

    @Schema(description = "分组中文名")
    private String label;

    @Schema(description = "条数")
    private Integer count;

    /** 明细项集合 */
    @Schema(description = "条目")
    private List<CdrProfileItemVO> items;

    @Data
    @Schema(description = "健康档案条目")
    public static class CdrProfileItemVO {

        @Schema(description = "主键（字符串）")
        @JsonSerialize(using = ToStringSerializer.class)
        private Long id;

        @Schema(description = "标题，如过敏原 / 诊断名")
        private String title;

        /** 小结 */
        @Schema(description = "说明")
        private String summary;

        @Schema(description = "时间（可空，如过敏发生时间 / 手术日期）")
        private String time;

        @Schema(description = "数据归属档案ID")
        @JsonSerialize(using = ToStringSerializer.class)
        private Long ownerPatientId;
    }
}

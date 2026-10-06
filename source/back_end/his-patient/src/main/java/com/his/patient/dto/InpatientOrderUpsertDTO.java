package com.his.patient.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.his.common.validation.InEnum;
import com.his.patient.enums.OrderTypeEnum;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 住院医嘱开立 / 修改入参（命名遵循 AGENTS.md：新增和修改用 `xxxUpsertDTO`）。
 *
 * <p>一次提交 = 一个「组套」（可只含 1 条医嘱）。同一 `orderGroup` 内必须**同起同停**，
 * 所以时间、类型这类"整组共享"的字段放在主单上，明细里只放与项目相关的字段。
 */
@Data
public class InpatientOrderUpsertDTO implements Serializable {

    /**
     * 医嘱ID（为空=新开；有值=修改，仅「待校对」状态可改）
     */
    private Long id;

    /**
     * 入院ID（必填：医嘱一定挂在一次住院上）
     */
    @NotNull(message = "入院ID不能为空")
    private Long admissionId;

    /**
     * 医嘱类型：1-长期 2-临时（必填）
     */
    @NotNull(message = "医嘱类型不能为空（1-长期 2-临时）")
    @InEnum(value = OrderTypeEnum.class, message = "医嘱类型取值不合法（应为 1-长期 2-临时）")
    private Integer orderType;

    /**
     * 组套号（不传则由服务端生成；同一组的医嘱必须同起同停）
     */
    private String orderGroup;

    /**
     * 医嘱来源：1-医生 2-模板 3-组套（默认 1-医生）
     */
    private Integer source;

    /**
     * 是否加急：0-否 1-是
     */
    private Integer isUrgent;

    /**
     * 医嘱开始时间（不传取当前时间）
     *
     * <p>必须显式声明 pattern：前端 el-date-picker 的 value-format 传的是空格分隔
     * {@code yyyy-MM-dd HH:mm:ss}，Jackson 默认的 JSR-310 只认 ISO 的 {@code T} 分隔，
     * 于是"带开始时间的医嘱"整单被兜成 400「请求体格式不正确」（G12/G14 同族坑）。
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    /**
     * 计划结束时间（长期医嘱可为空 = 到停医嘱/出院为止）
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime planEndTime;

    /**
     * 备注
     */
    private String remark;

    /**
     * 医嘱明细（≥1 条）
     */
    @NotEmpty(message = "医嘱明细不能为空（至少 1 条项目）")
    private List<InpatientOrderItemDTO> items;
}

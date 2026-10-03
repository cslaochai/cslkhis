package com.his.operation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 麻醉记录单更新入参（时间轴 + 出入量 + 效果 + 去向）。
 *
 * <p><b>已提交后不可再改</b>：一旦 {@code record_status=1}，这条接口只接待 submit 之后的审核动作，
 * 改内容会被拒。理由是"术后回头改一条术中记载"本身就是伪造，
 * 与"术后补一条术前核对记录是伪造"同一条原则。
 */
@Data
public class AnesthesiaRecordUpdateUpsertDTO implements Serializable {

    @NotNull(message = "麻醉记录单ID不能为空")
    private Long recordId;

    /** 麻醉方式（1-全麻 2-椎管内 3-神经阻滞 4-局麻 5-其他） */
    private Integer anesthesiaType;

    /** 麻醉方法描述 */
    private String anesthesiaMethodDetail;

    /** 气道管理方式（0-无 1-气管插管 2-喉罩 3-面罩 4-其他） */
    private Integer airwayDevice;

    /** 气道器具规格 */
    private String airwayDeviceSpec;

    /** 通气方式（1-自主呼吸 2-辅助通气 3-控制通气） */
    private Integer ventilationMode;

    /** 麻醉医师ID（员工ID） */
    private Long anesthetistId;

    /** 麻醉助手姓名 */
    private String assistantAnesthetistName;

    /** 入手术室时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime enterRoomTime;

    /** 麻醉开始时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime anesthesiaStartTime;

    /** 手术开始时间（切皮） */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime operationStartTime;

    /** 手术结束时间（关腹/关胸） */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime operationEndTime;

    /** 麻醉结束时间（停药） */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime anesthesiaEndTime;

    /** 出手术室时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime leaveRoomTime;

    /** 晶体液入量（ml） */
    private Integer crystalloid;

    /** 胶体液入量（ml） */
    private Integer colloid;

    /** 异体输血量（ml） */
    private Integer bloodTransfusion;

    /** 自体血回输量（ml） */
    private Integer autotransfusion;

    /** 术中尿量（ml） */
    private Integer urineOutput;

    /** 术中出血量（ml） */
    private Integer bloodLoss;

    /** 是否发生麻醉不良事件（0-无 1-有） */
    private Integer adverseEventFlag;

    /** 不良事件经过与处理 */
    private String adverseEventNote;

    /** 麻醉效果（1-满意 2-欠佳 3-失败改麻醉方式） */
    private Integer anesthesiaEffect;

    /** 术后去向（1-回病房 2-入PACU 3-入ICU） */
    private Integer postopDisposition;

    /** 备注 */
    private String remark;
}

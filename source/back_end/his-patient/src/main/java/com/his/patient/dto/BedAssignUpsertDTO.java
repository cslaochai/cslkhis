package com.his.patient.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 安排床位入参（含跨科调配）
 *
 * <p><b>安排 = 预留，不是占用</b>：这张床立刻被置成 3-锁定并挂上患者ID，
 * 别人安排不到它，但患者的入院记录还没建立 —— 正式占用发生在 {@code /queue/admit}。
 * 少了这一步，"给他留的床"在系统里无处表达，护士只能靠"别让别人用"这句话来保证。
 *
 * <p><b>允许选到别的科室的床</b>：这是"全院床位调配"的核心动作。
 * 选了外科室的床，台账上 alloc_type=2 跨科调配，
 * 患者入院科室取床位所属科室（人躺在哪个科就归哪个科），
 * 而不是他原来想去的那个科 —— 否则会出现"人在骨科床、系统在呼吸科"的分裂状态。
 */
@Data
public class BedAssignUpsertDTO {

    /** 来源等床记录ID */
    @NotNull(message = "排队记录不能为空")
    private Long waitId;

    /** 床位ID */
    @NotNull(message = "床位不能为空")
    private Long bedId;

    /** 备注 */
    private String remark;
}

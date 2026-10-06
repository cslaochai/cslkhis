package com.his.patient.dto;

import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;

/**
 * 住院病历文书分页查询入参（命名遵循 AGENTS.md：分页查询用 `xxxQueryPageDTO`）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class InpatientRecordQueryPageDTO extends PageParam implements Serializable {

    /**
     * 入院ID
     */
    private Long admissionId;

    /**
     * 患者ID
     */
    private Long patientId;

    /**
     * 文书类型：1-入院记录 2-首次病程 3-日常病程 4-术前小结 5-手术记录 6-术后首次病程 7-出院记录 8-死亡记录
     */
    private Integer recordType;

    /**
     * 文书状态：1-草稿 2-已提交 3-已归档
     */
    private Integer recordStatus;

    /**
     * 关键字（文书号 / 标题 / 主诉 / 诊断名称）
     */
    private String keyword;

    /**
     * 科室数据权限收敛集合（M6）—— 只由服务端按 {@code DeptScopeProvider} 填充，
     * listPage 入口先置 null，前端传什么都忽略。受限且未指定科室时非空。
     */
    private List<Long> scopeDeptIds;
}

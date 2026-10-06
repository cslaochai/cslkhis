package com.his.medicaltech.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 检查/检验影像帧（检查影像帧，sql/137）
 *
 * <p>锚点是<b>申请单</b>（bizType + applyId），不是记录也不是报告：影像在物理上属于这一次申请，
 * 而报告是从记录派生的（report → record.apply_id）。口径详见 sql/137 文件头。
 *
 * <p>删除为<b>物理删</b> + 审计日志留痕，del_flag 恒 0。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_exam_image")
public class BizExamImage extends BaseEntity {

    /**
     * 单据类型（1-检查申请单 2-检验申请单，与报告单的报告类型同码）
     */
    private Integer bizType;

    /**
     * 申请单ID
     */
    private Long applyId;

    /**
     * 申请单号（快照）
     */
    private String applyNo;

    /**
     * 执行记录ID（冗余展示用，不作为查询条件）
     */
    private Long recordId;

    /**
     * 患者ID（快照）
     */
    private Long patientId;

    /**
     * 患者姓名（快照）
     */
    private String patientName;

    /**
     * 检查/检验项目名称（快照）
     */
    private String itemName;

    /**
     * 检查部位（快照，检验为空）
     */
    private String bodyPart;

    /**
     * 影像模态（1-CT 2-MR 3-DR 4-超声 5-心电 6-内镜 7-其他）
     */
    private Integer modality;

    /**
     * 本申请单内帧序号（服务端 max+1）
     */
    private Integer seq;

    /**
     * 原始文件名
     */
    private String fileName;

    /**
     * 相对访问路径（uploads/examImage/yyyyMMdd/xxx.png，前端拼 /api/）
     */
    private String fileUrl;

    /**
     * 文件字节数
     */
    private Long fileSize;

    /**
     * 文件MIME类型
     */
    private String mimeType;

    /**
     * 来源（1-工作站上传 2-模拟 DICOM 导入），见 ExamImageSourceEnum
     */
    private Integer source;
}

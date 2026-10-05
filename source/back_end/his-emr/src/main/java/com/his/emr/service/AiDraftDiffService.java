package com.his.emr.service;

import com.his.common.base.PageResult;
import com.his.emr.dto.AiDraftDiffQueryPageDTO;
import com.his.emr.vo.AiDraftDiffListVO;

/**
 * 病历草稿 AI 留痕服务（G-10 数据飞轮）
 */
public interface AiDraftDiffService {

    /**
     * 病历保存终审后留痕：draftText 非空才落行；与终稿相同落 changed=0
     * （「医生一字未改」同样是训练信号）。
     *
     * @return 是否落了行
     */
    boolean record(Long recordId, Long registId, Long patientId, String patientNo, String patientName,
                   Long deptId, String deptName, Long doctorId, String doctorName,
                   String draftText, String finalText);

    /**
     * 分页（AI 管理台「草稿留痕」签页）
     */
    PageResult<AiDraftDiffListVO> listPage(AiDraftDiffQueryPageDTO dto);
}

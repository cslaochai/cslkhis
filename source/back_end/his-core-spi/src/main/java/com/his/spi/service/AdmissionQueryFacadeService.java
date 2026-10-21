package com.his.spi.service;

import com.his.spi.vo.AdmissionActiveBriefVO;

/**
 * 入院登记只读取数窄端口：管床/责任护士关系建立前校验在院状态、取科室与床位快照。
 * 数据宿主为住院域（biz_admission），本端口只暴露只读摘要，不开放写权。
 */
public interface AdmissionQueryFacadeService {

    /**
     * 按入院ID取登记摘要（含在院状态与科室/床位快照）。
     *
     * @param admissionId 入院ID（可空，为空直接返回 null）
     * @return 不存在或已删除时返回 {@code null}
     */
    AdmissionActiveBriefVO findAdmissionBrief(Long admissionId);
}

package com.his.patient.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.patient.dto.ConsultationAcceptDTO;
import com.his.patient.dto.ConsultationCancelDTO;
import com.his.patient.dto.ConsultationFinishDTO;
import com.his.patient.dto.ConsultationQueryPageDTO;
import com.his.patient.dto.ConsultationUpsertDTO;
import com.his.patient.vo.ConsultationVO;

/**
 * 住院会诊服务（P4.1：申请 → 应答 → 会诊记录 → 完成 → 回写病历）。
 *
 * <p>状态机（不可跳步）：
 * <pre>
 *   0 待应答 --accept--> 3 已应答 --finish--> 1 已完成（同时回写住院病历）
 *      \--cancel--> 2 已取消        （仅 0 可取消；3 之后必须走完成）
 * </pre>
 */
public interface InpatientConsultationService {

    /**
     * 会诊分页
     */
    IPage<ConsultationVO> listPage(ConsultationQueryPageDTO query);

    /**
     * 会诊详情
     */
    ConsultationVO getDetailById(Long consultationId);

    /**
     * 申请 / 修改会诊申请
     *
     * @return 会诊号
     */
    String save(ConsultationUpsertDTO dto);

    /**
     * 会诊方应答（接诊）：接诊人一律为当前登录用户
     */
    void accept(ConsultationAcceptDTO dto);

    /**
     * 完成会诊：必须带结论，并回写住院病历
     *
     * @return 回写的住院病历ID
     */
    String finish(ConsultationFinishDTO dto);

    /**
     * 取消会诊申请（仅「待应答」）
     */
    void cancel(ConsultationCancelDTO dto);

    /**
     * 未完成会诊数（待应答 + 已应答）
     */
    long countUnfinished(Long toDeptId, Long admissionId);
}

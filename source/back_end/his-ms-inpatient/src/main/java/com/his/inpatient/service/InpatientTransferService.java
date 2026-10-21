package com.his.inpatient.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.inpatient.dto.InpatientTransferAcceptDTO;
import com.his.inpatient.dto.InpatientTransferCancelDTO;
import com.his.inpatient.dto.InpatientTransferPageQueryDTO;
import com.his.inpatient.dto.InpatientTransferUpsertDTO;
import com.his.inpatient.vo.InpatientTransferVO;

import java.util.List;

/**
 * 住院转科（P4.2：发起 → 转入科室接收 → 停原医嘱 + 换科室换床 + 回写病历）。
 */
public interface InpatientTransferService {

    /**
     * 转科记录分页
     */
    IPage<InpatientTransferVO> listPage(InpatientTransferPageQueryDTO query);

    /**
     * 转科详情
     */
    InpatientTransferVO getDetailById(Long transferId);

    /**
     * 发起转科（返回转科单号；此时**尚未生效**，床位/科室/医嘱都不动）
     */
    String save(InpatientTransferUpsertDTO dto);

    /**
     * 转入科室接收 —— 转科在这一刻真正生效：
     * 停原科室长期医嘱 → 释放原床/占用新床 → 更新入院科室 → 更新病案首页（草稿）→ 回写转科记录病历
     */
    void accept(InpatientTransferAcceptDTO dto);

    /**
     * 取消转科申请（仅「待接收」）
     */
    void cancel(InpatientTransferCancelDTO dto);

    /**
     * 某次住院的转科轨迹（按发生顺序升序；供病案首页与病历侧回看）
     */
    List<InpatientTransferVO> listByAdmission(Long admissionId);

    /**
     * 待接收转科数（转入科室工作台角标）
     */
    long countPending(Long toDeptId, Long admissionId);
}

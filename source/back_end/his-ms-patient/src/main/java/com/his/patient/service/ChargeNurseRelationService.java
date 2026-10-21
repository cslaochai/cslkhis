package com.his.patient.service;

import com.his.patient.dto.ChargeNurseBindDTO;
import com.his.patient.vo.ChargeNurseRelationVO;

import java.util.List;

/**
 * 住院责任护士关系服务。
 */
public interface ChargeNurseRelationService {

    /**
     * 某次住院的责任护士关系（默认只列有效，可传 status=0 看历史交接）。
     */
    List<ChargeNurseRelationVO> listByAdmission(Long admissionId, Integer status);

    /**
     * 某位护士名下负责哪些患者（默认只列有效）。
     */
    List<ChargeNurseRelationVO> listByEmployee(Long nurseId, Integer status);

    /**
     * 建立/转交责任护士。一次住院只允许一名有效主责，重新指派时旧记录置失效留痕、不删行。
     */
    Long bind(ChargeNurseBindDTO dto);

    /**
     * 结束一段责任护士关系（置失效，保留行）。
     */
    void endById(Long id);

    /**
     * 物理删（唯一键不含删除标志，误建的必须真删才能重建）。
     */
    void deleteById(Long id);
}

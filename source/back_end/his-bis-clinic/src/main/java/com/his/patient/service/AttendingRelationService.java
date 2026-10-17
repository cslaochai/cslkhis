package com.his.patient.service;

import com.his.patient.dto.AttendingBindDTO;
import com.his.patient.vo.AttendingRelationVO;

import java.util.List;

/**
 * 住院管床关系服务（sql/202）。
 */
public interface AttendingRelationService {

    /**
     * 某次住院的管床关系（默认只列有效的）。
     */
    List<AttendingRelationVO> listByAdmission(Long admissionId, Integer status);

    /**
     * 某位医生名下管着哪些患者（默认只列有效的）。
     */
    List<AttendingRelationVO> listByEmployee(Long employeeId, Integer status);

    /**
     * 建立/转交管床关系。
     *
     * <p>主管、主诊组长这类「唯一负责」的关系，重新指派时会自动把上一段置失效
     * （写 expire_time），而不是删行 —— 转交是有痕迹的动作，谁在什么时候交出去必须查得出来。
     */
    Long bind(AttendingBindDTO dto);

    /**
     * 结束一段关系（置失效，保留行）。
     */
    void endById(Long id);

    /**
     * 物理删（唯一键不含删除标志，误建的关系必须真删才能重建）。
     */
    void deleteById(Long id);
}

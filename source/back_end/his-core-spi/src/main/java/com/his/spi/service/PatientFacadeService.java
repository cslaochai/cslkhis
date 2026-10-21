package com.his.spi.service;

import com.his.spi.dto.EmergencyPatientCreateDTO;
import com.his.spi.vo.AllergyFactVO;
import com.his.spi.vo.PatientBriefVO;
import com.his.spi.vo.PatientMasterVO;
import com.his.spi.vo.PatientProfileScoreVO;
import com.his.spi.vo.PatientTagBriefVO;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * 患者域对外端口（依赖倒置）：主档 / 就诊人绑定 / 过敏史 / 完整度评分 / 患者标签。
 * 住院、病历文书、护理事实等住院侧事实见 {@link InpatientFacadeService}。
 */
public interface PatientFacadeService {

    /**
     * 按患者ID取患者摘要。
     *
     * @param patientId 患者ID（可空，为空直接返回 null）
     * @return 不存在时返回 {@code null}
     */
    PatientBriefVO findPatient(Long patientId);

    // ---------- 就诊人绑定 / 越权判定 ----------

    /**
     * 当前登录账号能否访问该就诊人（患者端读写接口的统一收口）。
     */
    boolean canAccessPatient(Long patientId);

    /**
     * 院内接口上的越权判定：员工账号一律放行，患者账号只有访问自己绑定的就诊人才放行。
     */
    boolean patientScopeViolated(Long patientId);

    /**
     * 绑定当前账号的微信 openid（订阅消息发送用，重复绑定以最新为准）
     */
    void bindOpenid(String openid);

    // ---------- 患者主档 ----------

    /**
     * 患者主档；不存在返回 {@code null}
     */
    PatientMasterVO getPatient(Long patientId);

    /**
     * 结构化过敏史列表
     */
    List<AllergyFactVO> listAllergies(Long patientId);

    /**
     * 急诊建档：创建患者主档（三无患者允许没有身份证，姓名和性别不能省）。
     * 患者号取号、身份证补出生日期、写入口校验都在提供方做。
     *
     * @return 落库后的主档（含 id 与 patientNo）
     */
    PatientMasterVO createEmergencyPatient(EmergencyPatientCreateDTO dto);

    /**
     * 结诊回写「首次就诊」冗余组
     */
    void markFirstVisit(Long patientId, LocalDateTime visitTime, Long deptId, String deptName,
                        Long doctorId, String doctorName);

    /**
     * 结诊回写「最近就诊」冗余组
     */
    void markLastVisit(Long patientId, LocalDateTime visitTime, Long deptId, String deptName,
                       Long doctorId, String doctorName);

    /**
     * 主档完整度评分（文本字段评分 + 结构化档案覆盖修正）。
     *
     * @param coveredKeys 结构化档案已覆盖的字段 pkey 集合
     * @return 患者不存在返回 {@code null}
     */
    PatientProfileScoreVO profileScore(Long patientId, Set<String> coveredKeys);

    /**
     * 挂号补全主档费别/医保号：仅当主档尚缺时才写（录入新患者未补全的场景），保证口径单一。
     */
    void fillInsuranceIfAbsent(Long patientId, String medicalInsuranceType, String medicalInsuranceNo);

    // ---------- EMPI ----------

    /**
     * EMPI 归并：返回该患者所属主档下的全部档案ID（含自己）。
     * 凡"按患者维度聚合数据"的地方都必须经过它。
     */
    List<Long> resolvePatientIds(Long patientId);

    // ---------- 患者标签 ----------

    /**
     * 按标签ID批量取患者标签摘要（主档健康档案打标用）。
     *
     * @param tagIds 标签ID集合（可空/空集合，返回空列表）
     */
    List<PatientTagBriefVO> listPatientTagsByIds(Collection<Long> tagIds);

    /**
     * 按标签名批量取患者标签摘要（静态标签点名：老年/儿童/幼儿等）。
     *
     * @param tagNames 标签名集合（可空/空集合，返回空列表）
     */
    List<PatientTagBriefVO> listPatientTagsByNames(Collection<String> tagNames);
}

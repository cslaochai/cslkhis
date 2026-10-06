package com.his.patient.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.his.common.base.PageResult;
import com.his.patient.dto.PatientQueryPageDTO;
import com.his.patient.dto.PatientRegisterDTO;
import com.his.patient.dto.PatientSearchScopeDTO;
import com.his.patient.dto.PatientUpsertDTO;
import com.his.patient.entity.BizPatient;
import com.his.patient.vo.PatientDetailVO;
import com.his.patient.vo.PatientRegisterVO;
import com.his.patient.vo.PatientVO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 患者服务接口
 */
public interface PatientService extends IService<BizPatient> {

    /**
     * 分页查询患者列表
     */
    PageResult<BizPatient> selectPatientPage(String patientName, String phone, String patientNo,
                                             Integer patientType, String keyword, int pageNum, int pageSize);

    /**
     * 分页查询患者列表（支持标签过滤）
     */
    PageResult<BizPatient> selectPatientPage(String patientName, String phone, String patientNo,
                                             Integer patientType, String keyword, int pageNum, int pageSize,
                                             List<Long> tagPatientIds);

    /**
     * 分页查询患者列表（标签过滤 + 今日就诊置顶）
     *
     * @param scope 就诊范围权重（今日就诊 / 我的今日就诊）；为空则按建档时间倒序。
     *              排序必须在**分页之前**生效，否则今日患者会被其他档案挤出第一页，
     *              前端再排也来不及。
     */
    PageResult<BizPatient> selectPatientPage(String patientName, String phone, String patientNo,
                                             Integer patientType, String keyword, int pageNum, int pageSize,
                                             List<Long> tagPatientIds, PatientSearchScopeDTO scope);

    /**
     * 根据患者号查询患者
     */
    BizPatient selectByPatientNo(String patientNo);

    /**
     * 根据身份证号查询患者
     */
    BizPatient selectByIdCard(String idCard);

    /**
     * 新增患者
     */
    boolean addPatient(BizPatient patient);

    /**
     * 批量取「患者 → 挂号（预约）次数」映射；入参为空返回空 Map
     */
    Map<Long, Integer> mapAppointCount(List<Long> patientIds);

    /**
     * 患者分页（出参已是 VO）：标签与挂号次数批量回填、今日就诊置顶与标注、列表脱敏都在这里一处完成。
     */
    PageResult<PatientVO> queryPatientPage(PatientQueryPageDTO queryDTO);

    /**
     * 按主键取患者出参。<b>编辑回显口径，敏感字段保持明文</b>：表单是「回填 → 整对象回写」，
     * 回显打码值会把真号洗成星号。
     */
    PatientVO getPatientVOById(Long patientId);

    /**
     * 按患者号取患者出参。
     */
    PatientVO getPatientVOByNo(String patientNo);

    /**
     * 患者完整信息（展示口径：敏感字段脱敏 + 按岗位裁剪临床内容）。
     */
    PatientDetailVO getPatientDetail(Long patientId);

    /**
     * 新增或修改患者：建档校验、出生日期/年龄补齐、字段级留痕都在这里。
     *
     * @return 落库后的患者出参（前端新增后需要立即拿到 id / patientNo 用于后续挂号）
     */
    PatientVO upsertPatient(PatientUpsertDTO patientUpsertDTO);

    /**
     * 删除患者，失败时抛业务异常
     */
    void removePatient(Long patientId);

    /**
     * 患者自助注册（小程序端）：建档 + 开通 user_type=3 登录账号。
     * 校验不通过时抛业务异常，文案即原来直接返回给前端的提示。
     */
    PatientRegisterVO register(PatientRegisterDTO dto);

    /**
     * 结诊回写「最近就诊」冗余组（last_visit_*）。仅把 SQL 落库（WHERE 条件见
     * {@code BizPatientMapper#markLastVisit}，一处定义不重复判断）；失败抛异常由调用方处理。
     */
    void markLastVisit(Long patientId, LocalDateTime visitTime, Long deptId, String deptName,
                       Long doctorId, String doctorName);

    /**
     * 结诊回写「首次就诊」冗余组（first_visit_*）。仅当尚无首次就诊或本次更早时不覆盖。
     */
    void markFirstVisit(Long patientId, LocalDateTime visitTime, Long deptId, String deptName,
                        Long doctorId, String doctorName);
}

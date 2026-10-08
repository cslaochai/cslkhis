package com.his.patient.mapper;

import com.his.patient.entity.BizPatient;
import com.his.patient.vo.PatientDataCountVO;
import com.his.patient.vo.PatientIndexStatVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Collection;
import java.util.List;

/**
 * 患者主索引统计 Mapper（P5.1 EMPI）
 *
 * <p>为什么要跨模块裸 SQL：这些表分属 his-appoint（挂号）、his-charge（收费）、
 * his-medicaltech（检查/检验）、his-emr（病历）等模块，而 his-patient 只依赖 common/system。
 * 为一个计数引入跨模块实体依赖得不偿失 —— 与 {@code BizPatientMapper.countRegistByPatientIds}
 * 同一路数：**只取聚合结果，不引实体**。
 *
 * <p>代价要说清：表结构变了这里不会编译报错，只会静默算错。所以
 * ① 只用 patient_id / del_flag / 计数，不碰业务字段；
 * ② 每张表的 del_flag 情况逐个核过（就诊次、治疗申请单没有 del_flag）。
 */
@Mapper
public interface PatientIndexMapper {

    /**
     * 批量统计每份档案关联的业务数据量。
     *
     * <p>一次 UNION ALL 往返拿全 10 张表，而不是逐表逐档案查 —— 重复检测页一次要展示
     * 组内所有档案的数据量，N+1 会直接变成请求风暴。
     *
     * @param ids 患者ID集合（调用方需保证非空，空集合会让 IN () 语法出错）
     * @return 每行 = 一份档案在一张表下的条数，没有数据的档案不出现在结果里
     */
    @Select("<script>" +
            "SELECT 'regist' AS dataTable, patient_id AS patientId, COUNT(*) AS cnt FROM biz_appoint_info " +
            " WHERE del_flag = 0 AND patient_id IN <foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach> GROUP BY patient_id " +
            "UNION ALL SELECT 'visit', patient_id, COUNT(*) FROM biz_visit " +
            " WHERE patient_id IN <foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach> GROUP BY patient_id " +
            "UNION ALL SELECT 'outpatientRecord', patient_id, COUNT(*) FROM biz_medical_record " +
            " WHERE del_flag = 0 AND patient_id IN <foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach> GROUP BY patient_id " +
            "UNION ALL SELECT 'prescription', patient_id, COUNT(*) FROM biz_prescription " +
            " WHERE del_flag = 0 AND patient_id IN <foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach> GROUP BY patient_id " +
            "UNION ALL SELECT 'charge', patient_id, COUNT(*) FROM biz_settlement_bill " +
            " WHERE del_flag = 0 AND patient_id IN <foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach> GROUP BY patient_id " +
            "UNION ALL SELECT 'admission', patient_id, COUNT(*) FROM biz_admission " +
            " WHERE del_flag = 0 AND patient_id IN <foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach> GROUP BY patient_id " +
            "UNION ALL SELECT 'inspection', patient_id, COUNT(*) FROM biz_inspection_apply " +
            " WHERE del_flag = 0 AND patient_id IN <foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach> GROUP BY patient_id " +
            "UNION ALL SELECT 'laboratory', patient_id, COUNT(*) FROM biz_laboratory_apply " +
            " WHERE del_flag = 0 AND patient_id IN <foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach> GROUP BY patient_id " +
            "UNION ALL SELECT 'treatment', patient_id, COUNT(*) FROM biz_treatment_apply " +
            " WHERE patient_id IN <foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach> GROUP BY patient_id " +
            "UNION ALL SELECT 'inpatientRecord', patient_id, COUNT(*) FROM biz_inpatient_record " +
            " WHERE del_flag = 0 AND patient_id IN <foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach> GROUP BY patient_id " +
            "</script>")
    List<PatientDataCountVO> countDataByPatientIds(@Param("ids") Collection<Long> ids);

    /**
     * 拉出"有可能存在重复"的候选档案（重复检测的输入集）。
     *
     * <p>绝不做全表两两比对：候选集只用三个**已被索引的键**（姓名 / 身份证 / 手机号）
     * 的重复组筛出来，服务端再按成组键线性分组。44 个患者的库跑得动，
     * 真实规模（几万患者）也跑得动，因为筛的是"重复键"不是"全表"。
     *
     * <p>三个键各自的重复判定都要求非空：空值一律不成组 ——
     * 否则所有没填身份证的患者会被聚成一个巨大的假重复组。
     */
    @Select("<script>" +
            "SELECT * FROM biz_patient " +
            " WHERE del_flag = 0 AND merge_status = 0 AND (" +
            "   patient_name IN (SELECT patient_name FROM (SELECT patient_name FROM biz_patient " +
            "     WHERE del_flag = 0 AND merge_status = 0 AND patient_name IS NOT NULL AND patient_name &lt;&gt; '' " +
            "     GROUP BY patient_name HAVING COUNT(*) &gt; 1) a) " +
            "   OR id_card IN (SELECT id_card FROM (SELECT id_card FROM biz_patient " +
            "     WHERE del_flag = 0 AND merge_status = 0 AND id_card IS NOT NULL AND id_card &lt;&gt; '' " +
            "     GROUP BY id_card HAVING COUNT(*) &gt; 1) b) " +
            "   OR phone IN (SELECT phone FROM (SELECT phone FROM biz_patient " +
            "     WHERE del_flag = 0 AND merge_status = 0 AND phone IS NOT NULL AND phone &lt;&gt; '' " +
            "     GROUP BY phone HAVING COUNT(*) &gt; 1) c) " +
            " ) " +
            "<if test='keyword != null and keyword != \"\"'>" +
            " AND (patient_name LIKE CONCAT('%', #{keyword}, '%') OR patient_no LIKE CONCAT('%', #{keyword}, '%') " +
            "   OR phone LIKE CONCAT('%', #{keyword}, '%') OR id_card LIKE CONCAT('%', #{keyword}, '%')) " +
            "</if>" +
            " ORDER BY patient_name, id LIMIT 2000" +
            "</script>")
    List<BizPatient> selectSuspectPatients(@Param("keyword") String keyword);

    /**
     * EMPI 概览指标（唯一性 / 完整性维度）。
     *
     * <p>唯一性口径：**强重复组数 / 患者总数**。强重复 = 身份证号（非空）出现多次的一组
     * —— 手机号重复不计入，因为实测一号码挂 10 人（造数共用），那不叫重复档案。
     */
    @Select("SELECT " +
            " (SELECT COUNT(*) FROM biz_patient WHERE del_flag = 0) AS patientTotal, " +
            " (SELECT COUNT(*) FROM biz_patient WHERE del_flag = 0 AND merge_status = 1) AS mergedCount, " +
            " (SELECT COUNT(*) FROM (SELECT id_card FROM biz_patient " +
            "    WHERE del_flag = 0 AND id_card IS NOT NULL AND id_card <> '' " +
            "    GROUP BY id_card HAVING COUNT(*) > 1) t) AS strongDupGroups, " +
            " (SELECT COUNT(*) FROM biz_patient WHERE del_flag = 0 AND (id_card IS NULL OR id_card = '')) AS idCardMissing, " +
            " (SELECT COUNT(*) FROM biz_patient WHERE del_flag = 0 AND (phone IS NULL OR phone = '')) AS phoneMissing, " +
            " (SELECT COUNT(*) FROM biz_patient WHERE del_flag = 0 AND (allergy_history IS NULL OR allergy_history = '')) AS allergyMissing")
    PatientIndexStatVO selectIndexStats();
}

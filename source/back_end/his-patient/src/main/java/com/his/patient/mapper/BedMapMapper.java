package com.his.patient.mapper;

import com.his.patient.vo.BedMapVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 病区床位图聚合 Mapper（列名已按 information_schema 逐表核对）。
 *
 * <p><b>占用者为什么不 JOIN</b>：dev 库一张床挂着多条 {@code admit_status=1} 的记录
 * （历史验证夹具残留，如「全科 02 床」6 条），直接 join 会让一张床渲染出六张卡，
 * 卡片数也再对不上该科床位总数。这里用相关子查询取「床位上记的那个患者优先、
 * 否则最近入院的一条」，保证<b>一床一行</b>。
 *
 * <p>科室ID/病区ID 用 {@code IS NULL} 表达"不收口"，与本模块
 * {@code selectBedList} 同口径；服务层永远传非空 deptId，不受限账号也会先落到主岗位科室。
 */
@Mapper
public interface BedMapMapper {

    /** 床位占用者：优先患者ID与床位一致的那条在院记录，其次最近入院的一条 */
    String OCCUPANT_JOIN = "LEFT JOIN biz_admission a ON a.admission_id = ("
            + "SELECT a2.admission_id FROM biz_admission a2 "
            + "WHERE a2.bed_id = b.bed_id AND a2.del_flag = 0 AND a2.admit_status = 1 "
            + "ORDER BY (a2.patient_id = b.patient_id) DESC, a2.admit_time DESC, a2.admission_id DESC LIMIT 1) ";

    /**
     * 在途预留派生表：<b>一床最多一行</b>。
     *
     * <p>为什么先 GROUP BY bed_id 取 MAX(id) 再回表：一张床历史上可能有多条调配单
     * （预留→释放→再预留），直接 LEFT JOIN 床位调配台账会让一张床渲染出多张卡，
     * 与「一床一行」的前提冲突。取最新那条在途预留（alloc_status=1）即可，
     * 已释放/已转入院/已作废的都不算在途。
     */
    String RESERVE_JOIN = "LEFT JOIN ("
            + "SELECT al.* FROM biz_bed_allocate al "
            + "JOIN (SELECT bed_id, MAX(id) AS mid FROM biz_bed_allocate "
            + "      WHERE del_flag = 0 AND alloc_status = 1 GROUP BY bed_id) m ON m.mid = al.id"
            + ") r ON r.bed_id = b.bed_id AND r.del_flag = 0 ";

    @Select("""
            SELECT b.bed_id     AS bedId,
                   b.bed_no     AS bedNo,
                   b.bed_type   AS bedType,
                   b.bed_status AS bedStatus,
                   b.ward_id    AS wardId,
                   IFNULL(w.ward_name, '') AS wardName,
                   b.dept_id    AS deptId,
                   IFNULL(d.dept_name, '') AS deptName,
                   b.remark     AS remark,
                   r.wait_id      AS reservedWaitId,
                   IFNULL(bw.wait_no, '') AS reservedWaitNo,
                   r.patient_id   AS reservedPatientId,
                   IFNULL(r.patient_name, '') AS reservedPatientName,
                   bw.priority    AS reservedPriority,
                   r.allocate_no  AS allocateNo,
                   r.alloc_type   AS allocType,
                   CASE WHEN r.use_dept_id IS NOT NULL AND r.own_dept_id IS NOT NULL
                             AND r.use_dept_id <> r.own_dept_id THEN 1 ELSE 0 END AS crossDept,
                   DATE_FORMAT(r.operate_time, '%Y-%m-%d %H:%i') AS reserveTime,
                   a.admission_id AS admissionId,
                   a.patient_id   AS patientId,
                   IFNULL(p.patient_name, '') AS patientName,
                   p.gender     AS gender,
                   p.age        AS age,
                   a.admit_doctor_id AS doctorId,
                   IFNULL(e.emp_name, '') AS doctorName,
                   DATE_FORMAT(a.admit_time, '%Y-%m-%d %H:%i') AS admitTime,
                   CASE WHEN a.admit_time IS NULL THEN NULL
                        ELSE GREATEST(TIMESTAMPDIFF(DAY, DATE(a.admit_time), CURDATE()), 0) END AS admitDays,
                   CASE WHEN a.admit_time IS NOT NULL AND DATE(a.admit_time) = CURDATE() THEN 1 ELSE 0 END AS newToday,
                   (SELECT n.nursing_level FROM biz_nursing_record n
                     WHERE n.admission_id = a.admission_id AND n.del_flag = 0 AND n.nursing_level IS NOT NULL
                     ORDER BY n.measure_time DESC, n.id DESC LIMIT 1) AS nursingLevel,
                   (SELECT o.operation_name FROM biz_inpatient_operation o
                     WHERE o.admission_id = a.admission_id AND o.del_flag = 0
                     ORDER BY (o.is_main = 1) DESC, o.operation_date DESC LIMIT 1) AS mainOperationName,
                   (SELECT DATEDIFF(CURDATE(), DATE(o.operation_date)) FROM biz_inpatient_operation o
                     WHERE o.admission_id = a.admission_id AND o.del_flag = 0 AND o.is_main = 1
                       AND o.operation_date IS NOT NULL
                     ORDER BY o.operation_date DESC LIMIT 1) AS postOpDays,
                   (SELECT CASE WHEN s.is_critical = 1 THEN 1 ELSE 0 END FROM biz_inpatient_summary s
                     WHERE s.admission_id = a.admission_id AND s.del_flag = 0 ORDER BY s.id LIMIT 1) AS critical,
                   IFNULL(p.allergy_history, '') AS allergyHistory,
                   (SELECT COUNT(*) FROM biz_inpatient_order o
                     WHERE o.admission_id = a.admission_id AND o.del_flag = 0 AND o.order_status IN (2, 3)) AS activeOrderCount
            FROM sys_bed b
                     LEFT JOIN sys_ward w ON w.ward_id = b.ward_id
                     LEFT JOIN sys_department d ON d.id = b.dept_id AND d.del_flag = 0
            """ + OCCUPANT_JOIN + """
                     LEFT JOIN biz_patient p ON p.id = a.patient_id AND p.del_flag = 0
                     LEFT JOIN sys_employee e ON e.id = a.admit_doctor_id AND e.del_flag = 0
            """ + RESERVE_JOIN + """
                     LEFT JOIN biz_bed_wait bw ON bw.id = r.wait_id AND bw.del_flag = 0
            WHERE b.del_flag = 0
              AND (#{deptId} IS NULL OR b.dept_id = #{deptId})
              AND (#{wardId} IS NULL OR b.ward_id = #{wardId})
            ORDER BY b.ward_id, LENGTH(b.bed_no), b.bed_no
            """)
    List<BedMapVO.BedCard> selectBedCards(@Param("deptId") Long deptId, @Param("wardId") Long wardId);

    /** 有床位的科室（授权收口在服务层做，这里不带权限语义） */
    @Select("""
            SELECT b.dept_id AS deptId,
                   IFNULL(d.dept_name, CONCAT('科室#', b.dept_id)) AS deptName,
                   COUNT(b.bed_id) AS total,
                   IFNULL(SUM(CASE WHEN b.bed_status = 2 THEN 1 ELSE 0 END), 0) AS occupied,
                   IFNULL(SUM(CASE WHEN b.bed_status = 3 THEN 1 ELSE 0 END), 0) AS reserved
            FROM sys_bed b
                     LEFT JOIN sys_department d ON d.id = b.dept_id AND d.del_flag = 0
            WHERE b.del_flag = 0
            GROUP BY b.dept_id, d.dept_name
            ORDER BY deptName
            """)
    List<BedMapVO.DeptOption> selectDeptOptions();

    /** 当前科室下真正有床位的病区 */
    @Select("""
            SELECT b.ward_id AS wardId,
                   IFNULL(w.ward_name, CONCAT('病区#', b.ward_id)) AS wardName,
                   b.dept_id AS deptId,
                   COUNT(b.bed_id) AS total,
                   IFNULL(SUM(CASE WHEN b.bed_status = 2 THEN 1 ELSE 0 END), 0) AS occupied,
                   IFNULL(SUM(CASE WHEN b.bed_status = 3 THEN 1 ELSE 0 END), 0) AS reserved
            FROM sys_bed b
                     LEFT JOIN sys_ward w ON w.ward_id = b.ward_id
            WHERE b.del_flag = 0 AND b.dept_id = #{deptId}
            GROUP BY b.ward_id, w.ward_name, b.dept_id
            ORDER BY wardName
            """)
    List<BedMapVO.WardOption> selectWardOptions(@Param("deptId") Long deptId);

    /** 病区归属（校验传入 wardId 是否落在已收口的科室内，防止跨科窥探） */
    @Select("SELECT ward_id AS wardId, ward_name AS wardName, dept_id AS deptId "
            + "FROM sys_ward WHERE ward_id = #{wardId}")
    BedMapVO.WardOption selectWardOwner(@Param("wardId") Long wardId);
}

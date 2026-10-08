package com.his.patient.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.patient.entity.BizBedAllocate;
import com.his.patient.vo.BedMatchVO;
import com.his.patient.vo.BedOverviewVO;
import com.his.patient.vo.BedPoolVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 床位服务中心聚合 Mapper（列名已按 information_schema 逐表核对）。
 */
@Mapper
public interface BedCenterMapper extends BaseMapper<BizBedAllocate> {

    /**
     * 全院床位池（含占用者与预留去向）。
     *
     * <p>占用者取「床位上记的那个患者优先、否则最近入院的一条」—— 与 BedMapMapper 同口径，
     * 因为 dev 库存在一张床挂着多条 admit_status=1 的历史夹具，直接 JOIN 会让池里同一张床出多行。
     */
    @Select("""
            SELECT b.bed_id     AS bedId,
                   b.bed_no     AS bedNo,
                   b.bed_type   AS bedType,
                   b.bed_status AS bedStatus,
                   b.ward_id    AS wardId,
                   IFNULL(w.ward_name, '') AS wardName,
                   b.dept_id    AS deptId,
                   IFNULL(d.dept_name, '') AS deptName,
                   a.admission_id AS admissionId,
                   a.patient_id   AS patientId,
                   IFNULL(p.patient_name, '') AS patientName,
                   DATE_FORMAT(a.admit_time, '%Y-%m-%d %H:%i') AS admitTime,
                   al.id          AS allocateId,
                   al.wait_id     AS waitId,
                   al.use_dept_id AS useDeptId,
                   IFNULL(al.use_dept_name, '') AS useDeptName,
                   IFNULL(al.patient_name, '')  AS reservedPatientName,
                   DATE_FORMAT(al.operate_time, '%Y-%m-%d %H:%i') AS reservedTime,
                   b.remark     AS remark
            FROM sys_bed b
                     LEFT JOIN sys_ward w ON w.ward_id = b.ward_id
                     LEFT JOIN sys_department d ON d.id = b.dept_id AND d.del_flag = 0
                     LEFT JOIN biz_admission a ON a.admission_id = (
                          SELECT a2.admission_id FROM biz_admission a2
                           WHERE a2.bed_id = b.bed_id AND a2.del_flag = 0 AND a2.admit_status = 1
                           ORDER BY (a2.patient_id = b.patient_id) DESC, a2.admit_time DESC, a2.admission_id DESC LIMIT 1)
                     LEFT JOIN biz_patient p ON p.id = a.patient_id AND p.del_flag = 0
                     LEFT JOIN biz_bed_allocate al ON al.id = (
                          SELECT al2.id FROM biz_bed_allocate al2
                           WHERE al2.bed_id = b.bed_id AND al2.del_flag = 0 AND al2.alloc_status = 1
                           ORDER BY al2.operate_time DESC LIMIT 1)
            WHERE b.del_flag = 0
              AND (#{deptId} IS NULL OR b.dept_id = #{deptId})
              AND (#{wardId} IS NULL OR b.ward_id = #{wardId})
              AND (#{bedStatus} IS NULL OR b.bed_status = #{bedStatus})
              AND (#{bedType} IS NULL OR b.bed_type = #{bedType})
              AND (#{keyword} IS NULL OR b.bed_no LIKE CONCAT('%', #{keyword}, '%')
                   OR p.patient_name LIKE CONCAT('%', #{keyword}, '%'))
            ORDER BY b.dept_id, b.ward_id, LENGTH(b.bed_no), b.bed_no
            LIMIT #{offset}, #{size}
            """)
    List<BedPoolVO.BedRow> selectBedPool(@Param("deptId") Long deptId,
                                         @Param("wardId") Long wardId,
                                         @Param("bedStatus") Integer bedStatus,
                                         @Param("bedType") String bedType,
                                         @Param("keyword") String keyword,
                                         @Param("offset") long offset,
                                         @Param("size") long size);

    /**
     * 床位池分页版（游标用 IPage 时 MP 自己在上面套 count，这里只提供取数）
     */
    @Select("""
            SELECT COUNT(*) FROM sys_bed b
                     LEFT JOIN biz_admission a ON a.admission_id = (
                          SELECT a2.admission_id FROM biz_admission a2
                           WHERE a2.bed_id = b.bed_id AND a2.del_flag = 0 AND a2.admit_status = 1
                           ORDER BY (a2.patient_id = b.patient_id) DESC, a2.admit_time DESC, a2.admission_id DESC LIMIT 1)
                     LEFT JOIN biz_patient p ON p.id = a.patient_id AND p.del_flag = 0
            WHERE b.del_flag = 0
              AND (#{deptId} IS NULL OR b.dept_id = #{deptId})
              AND (#{wardId} IS NULL OR b.ward_id = #{wardId})
              AND (#{bedStatus} IS NULL OR b.bed_status = #{bedStatus})
              AND (#{bedType} IS NULL OR b.bed_type = #{bedType})
              AND (#{keyword} IS NULL OR b.bed_no LIKE CONCAT('%', #{keyword}, '%')
                   OR p.patient_name LIKE CONCAT('%', #{keyword}, '%'))
            """)
    long countBedPool(@Param("deptId") Long deptId,
                      @Param("wardId") Long wardId,
                      @Param("bedStatus") Integer bedStatus,
                      @Param("bedType") String bedType,
                      @Param("keyword") String keyword);

    /**
     * 全院床位总览：各状态床数实时 COUNT。
     *
     * <p>使用率的分母是「可用床」（总数 - 维修），不是总床数 —— 把维修床算进分母，
     * 病区一报修就"使用率下降"，等于奖励不修床。
     */
    @Select("""
            SELECT COUNT(*) AS totalBeds,
                   IFNULL(SUM(CASE WHEN bed_status = 0 THEN 1 ELSE 0 END), 0) AS repairBeds,
                   IFNULL(SUM(CASE WHEN bed_status = 1 THEN 1 ELSE 0 END), 0) AS freeBeds,
                   IFNULL(SUM(CASE WHEN bed_status = 2 THEN 1 ELSE 0 END), 0) AS occupiedBeds,
                   IFNULL(SUM(CASE WHEN bed_status = 3 THEN 1 ELSE 0 END), 0) AS lockedBeds
            FROM sys_bed WHERE del_flag = 0
            """)
    BedOverviewVO.Summary selectHospitalSummary();

    /**
     * 科室床位排行（含空闲/预留/占用），只列有床位的科室
     */
    @Select("""
            SELECT b.dept_id AS deptId,
                   IFNULL(d.dept_name, CONCAT('科室#', b.dept_id)) AS deptName,
                   COUNT(b.bed_id) AS totalBeds,
                   IFNULL(SUM(CASE WHEN b.bed_status = 1 THEN 1 ELSE 0 END), 0) AS freeBeds,
                   IFNULL(SUM(CASE WHEN b.bed_status = 2 THEN 1 ELSE 0 END), 0) AS occupiedBeds,
                   IFNULL(SUM(CASE WHEN b.bed_status = 3 THEN 1 ELSE 0 END), 0) AS lockedBeds,
                   (SELECT COUNT(*) FROM biz_bed_allocate al
                     WHERE al.del_flag = 0 AND al.alloc_status = 1 AND al.own_dept_id = b.dept_id) AS lentOutBeds
            FROM sys_bed b
                     LEFT JOIN sys_department d ON d.id = b.dept_id AND d.del_flag = 0
            WHERE b.del_flag = 0
            GROUP BY b.dept_id, d.dept_name
            ORDER BY freeBeds ASC, deptName ASC
            """)
    List<BedOverviewVO.DeptRow> selectDeptRows();

    /**
     * 科室名称（科室主键是 id，不是 dept_id —— 与床位的科室ID 的映射在这里做）
     */
    @Select("SELECT dept_name FROM sys_department WHERE id = #{deptId} AND del_flag = 0")
    String selectDeptName(@Param("deptId") Long deptId);

    @Select("SELECT ward_name FROM sys_ward WHERE ward_id = #{wardId}")
    String selectWardName(@Param("wardId") Long wardId);

    /**
     * 候选床位（智能匹配用）：只取「空闲且没人排给它」的床。
     *
     * <p>为什么必须排除已被预留的床：安排床位会把床位改成 3-锁定，
     * 床位池里看得见、其他队列安排不到，这里顺理成章也要排除 —— 否则会出现
     * 「同一张床被两条排队记录同时安排」，而两条都是 locked 状态、谁都查不出错在谁。
     */
    @Select("""
            SELECT b.bed_id AS bedId,
                   b.bed_no AS bedNo,
                   b.bed_type AS bedType,
                   b.ward_id AS wardId,
                   IFNULL(w.ward_name, '') AS wardName,
                   b.dept_id AS deptId,
                   IFNULL(d.dept_name, '') AS deptName
            FROM sys_bed b
                     LEFT JOIN sys_ward w ON w.ward_id = b.ward_id
                     LEFT JOIN sys_department d ON d.id = b.dept_id AND d.del_flag = 0
            WHERE b.del_flag = 0 AND b.bed_status = 1 AND b.patient_id IS NULL
              AND NOT EXISTS (SELECT 1 FROM biz_bed_allocate al
                               WHERE al.bed_id = b.bed_id AND al.del_flag = 0 AND al.alloc_status = 1)
            ORDER BY b.dept_id, b.ward_id, LENGTH(b.bed_no), b.bed_no
            """)
    List<BedMatchVO> selectMatchableBeds();
}

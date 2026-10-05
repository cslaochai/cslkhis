package com.his.patient.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.patient.entity.SysBed;
import com.his.patient.vo.BedVO;
import com.his.patient.vo.WardVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 床位 Mapper
 * <p><b>床位占用一律以床位为准</b>：病区.total_beds/occupied_beds 是演示数据，
 * 与实际床位行数不符，只允许作为参考展示，禁止用于容量判断。
 */
@Mapper
public interface SysBedMapper extends BaseMapper<SysBed> {

    /**
     * 床位列表（可按病区 / 科室 / 状态过滤）
     */
    @Select("""
            SELECT b.bed_id     AS bedId,
                   b.bed_no     AS bedNo,
                   b.ward_id    AS wardId,
                   w.ward_name  AS wardName,
                   b.dept_id    AS deptId,
                   d.dept_name  AS deptName,
                   b.bed_type   AS bedType,
                   b.bed_status AS bedStatus,
                   b.patient_id AS patientId,
                   p.patient_name AS patientName,
                   b.remark     AS remark
            FROM sys_bed b
                     LEFT JOIN sys_ward w ON w.ward_id = b.ward_id
                     LEFT JOIN sys_department d ON d.id = b.dept_id AND d.del_flag = 0
                     LEFT JOIN biz_patient p ON p.id = b.patient_id AND p.del_flag = 0
            WHERE b.del_flag = 0
              AND (#{wardId} IS NULL OR b.ward_id = #{wardId})
              AND (#{deptId} IS NULL OR b.dept_id = #{deptId})
              AND (#{bedStatus} IS NULL OR b.bed_status = #{bedStatus})
            ORDER BY b.ward_id, b.bed_no
            """)
    List<BedVO> selectBedList(@Param("wardId") Long wardId,
                              @Param("deptId") Long deptId,
                              @Param("bedStatus") Integer bedStatus);

    /**
     * 病区列表，床位数实时取自床位（不读病区的两个演示字段）
     */
    @Select("""
            SELECT w.ward_id     AS wardId,
                   w.ward_code   AS wardCode,
                   w.ward_name   AS wardName,
                   w.dept_id     AS deptId,
                   d.dept_name   AS deptName,
                   COUNT(b.bed_id) AS totalBeds,
                   SUM(CASE WHEN b.bed_status = 1 THEN 1 ELSE 0 END) AS freeBeds,
                   SUM(CASE WHEN b.bed_status = 2 THEN 1 ELSE 0 END) AS occupiedBeds,
                   SUM(CASE WHEN b.bed_status = 0 THEN 1 ELSE 0 END) AS brokenBeds
            FROM sys_ward w
                     LEFT JOIN sys_department d ON d.id = w.dept_id AND d.del_flag = 0
                     LEFT JOIN sys_bed b ON b.ward_id = w.ward_id AND b.del_flag = 0
            WHERE w.status = 1
            GROUP BY w.ward_id, w.ward_code, w.ward_name, w.dept_id, d.dept_name
            ORDER BY w.ward_id
            """)
    List<WardVO> selectWardList();

    /**
     * 按状态统计床位（统计卡片用）
     */
    @Select("SELECT COUNT(*) FROM sys_bed WHERE del_flag = 0 AND bed_status = #{bedStatus}")
    long countByStatus(@Param("bedStatus") Integer bedStatus);

    /**
     * 单个病区（含所属科室），用于入院时推导科室与病区名
     */
    @Select("""
            SELECT w.ward_id   AS wardId,
                   w.ward_code AS wardCode,
                   w.ward_name AS wardName,
                   w.dept_id   AS deptId,
                   d.dept_name AS deptName
            FROM sys_ward w
                     LEFT JOIN sys_department d ON d.id = w.dept_id AND d.del_flag = 0
            WHERE w.ward_id = #{wardId}
            """)
    WardVO selectWardById(@Param("wardId") Long wardId);

    /**
     * 把病区.occupied_beds 重算为床位的真实占用数
     * <p>目的只是让那个演示字段不再与事实打架；<b>任何容量判断都必须直接读床位</b>。
     */
    @Update("UPDATE sys_ward SET occupied_beds = " +
            "(SELECT COUNT(*) FROM sys_bed WHERE ward_id = #{wardId} AND bed_status = 2 AND del_flag = 0) " +
            "WHERE ward_id = #{wardId}")
    int syncWardOccupied(@Param("wardId") Long wardId);
}

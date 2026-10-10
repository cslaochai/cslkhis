package com.his.pharmacy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.pharmacy.entity.BizPivasBatch;
import com.his.pharmacy.vo.PivasVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

/**
 * 静配中心主单 Mapper。
 */
@Mapper
public interface BizPivasBatchMapper extends BaseMapper<BizPivasBatch> {

    /**
     * 分页查主单（病区/日期/状态/患者名模糊；排序补 id 二级键防同秒行序抖动）
     */
    @Select("""
            <script>
            SELECT d.*
              FROM biz_pivas_batch d
             WHERE d.del_flag = 0
               <if test="wardId != null"> AND d.ward_id = #{wardId}</if>
               <if test="wardIds != null"> AND d.ward_id IN <foreach collection="wardIds" item="w" open="(" separator="," close=")">#{w}</foreach></if>
               <if test="admixDate != null"> AND d.admix_date = #{admixDate}</if>
               <if test="status != null"> AND d.status = #{status}</if>
               <if test="patientName != null and patientName != ''"> AND d.patient_name LIKE CONCAT('%', #{patientName}, '%')</if>
             ORDER BY d.admix_date DESC, d.id DESC
            </script>
            """)
    List<PivasVO> selectBatchPage(com.baomidou.mybatisplus.core.metadata.IPage<PivasVO> page,
                                  @Param("wardId") Long wardId,
                                  @Param("wardIds") List<Long> wardIds,
                                  @Param("admixDate") LocalDate admixDate,
                                  @Param("patientName") String patientName,
                                  @Param("status") Integer status);

    /**
     * 详情（主单快照）
     */
    @Select("""
            SELECT d.* FROM biz_pivas_batch d WHERE d.id = #{id} AND d.del_flag = 0
            """)
    PivasVO selectBatchById(@Param("id") Long id);

    /**
     * 病区名快照（病区档案由 his-patient 管辖，跨模块裸 SQL 取，取不到返回 NULL 不编造）
     */
    @Select("SELECT ward_name FROM sys_ward WHERE ward_id = #{wardId}")
    String selectWardName(@Param("wardId") Long wardId);

    /**
     * 病区所属科室ID（数据权限折算用：ward_id 是 sys_ward 主键不是科室ID，须经 dept_id 判权限；取不到返回 NULL）
     */
    @Select("SELECT dept_id FROM sys_ward WHERE ward_id = #{wardId}")
    Long selectWardDeptId(@Param("wardId") Long wardId);

    /**
     * 授权科室集合折算成可见病区ID集合（数据权限收口；科室没绑病区时返回空集合）
     */
    @Select("""
            <script>
            SELECT ward_id FROM sys_ward
             WHERE dept_id IN
            <foreach collection="deptIds" item="d" open="(" separator="," close=")">#{d}</foreach>
            </script>
            """)
    List<Long> selectWardIdsByDeptIds(@Param("deptIds") List<Long> deptIds);
}

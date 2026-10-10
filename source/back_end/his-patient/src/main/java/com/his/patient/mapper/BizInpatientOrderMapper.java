package com.his.patient.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.patient.dto.InpatientOrderQueryPageDTO;
import com.his.patient.entity.BizInpatientOrder;
import com.his.patient.vo.InpatientOrderVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 住院医嘱 Mapper。
 */
@Mapper
public interface BizInpatientOrderMapper extends BaseMapper<BizInpatientOrder> {

    /**
     * 医嘱分页（医生站 / 护士站共用）
     * <p>{@code scopeDeptIds} 是科室数据权限（M6）的服务端收敛集合，前端不可见：按<b>患者当前科室</b>
     * （biz_admission.dept_id，随转科更新）收口，而非医嘱开立时的科室快照——转科后新病区要接手历史医嘱。
     */
    @Select("""
            <script>
            SELECT o.*,
                   (SELECT COUNT(*)
                    FROM biz_inpatient_order_exec e
                    WHERE e.del_flag = 0
                      AND e.order_id = o.id
                      AND e.exec_status IN (2, 3)
                      AND e.plan_date = CURDATE()) AS todayExecCount,
                   (SELECT COUNT(*)
                    FROM biz_inpatient_order_exec e
                    WHERE e.del_flag = 0
                      AND e.order_id = o.id
                      AND e.exec_status = 1)        AS pendingExecCount
            FROM biz_inpatient_order o
                     LEFT JOIN biz_admission a ON a.admission_id = o.admission_id AND a.del_flag = 0
            WHERE o.del_flag = 0
              AND (#{q.admissionId} IS NULL OR o.admission_id = #{q.admissionId})
              AND (#{q.patientId} IS NULL OR o.patient_id = #{q.patientId})
              AND (#{q.orderType} IS NULL OR o.order_type = #{q.orderType})
              AND (#{q.orderStatus} IS NULL OR o.order_status = #{q.orderStatus})
              AND (#{q.orderClass} IS NULL OR o.order_class = #{q.orderClass})
              AND (#{q.orderGroup} IS NULL OR o.order_group = #{q.orderGroup})
              AND (#{q.keyword} IS NULL OR #{q.keyword} = ''
                   OR o.item_name LIKE CONCAT('%', #{q.keyword}, '%')
                   OR o.order_no LIKE CONCAT('%', #{q.keyword}, '%'))
              AND (#{q.pendingVerifyOnly} IS NULL OR #{q.pendingVerifyOnly} = 0 OR o.order_status = 1)
              <if test="q.scopeDeptIds != null and q.scopeDeptIds.size() > 0">
                AND a.dept_id IN
                <foreach collection="q.scopeDeptIds" item="sd" open="(" separator="," close=")">#{sd}</foreach>
              </if>
            ORDER BY o.order_status ASC, o.order_time DESC, o.id DESC
            </script>
            """)
    IPage<InpatientOrderVO> selectOrderPage(IPage<InpatientOrderVO> page, @Param("q") InpatientOrderQueryPageDTO query);

    /**
     * 在院患者的有效医嘱条数（长期未停 + 临时未完成），医生站/护士站首页卡片用
     */
    @Select("""
            SELECT COUNT(*) FROM biz_inpatient_order
            WHERE del_flag = 0
              AND admission_id = #{admissionId}
              AND order_status IN (1, 2, 3)
            """)
    long countActiveByAdmission(@Param("admissionId") Long admissionId);

    /**
     * 待校对医嘱数（护士站首页卡片）：admissionId 为空时按科室数据权限集合收敛（M6），与 selectOrderPage 同口径
     */
    @Select("""
            <script>
            SELECT COUNT(*)
            FROM biz_inpatient_order o
                     LEFT JOIN biz_admission a ON a.admission_id = o.admission_id AND a.del_flag = 0
            WHERE o.del_flag = 0
              AND o.order_status = 1
              AND (#{admissionId} IS NULL OR o.admission_id = #{admissionId})
              <if test="scopeDeptIds != null and scopeDeptIds.size() > 0">
                AND a.dept_id IN
                <foreach collection="scopeDeptIds" item="sd" open="(" separator="," close=")">#{sd}</foreach>
              </if>
            </script>
            """)
    long countPendingVerify(@Param("admissionId") Long admissionId,
                            @Param("scopeDeptIds") java.util.List<Long> scopeDeptIds);
}

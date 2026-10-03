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
 *
 * <p>自定义 {@code @Select} 不受 {@code @TableLogic} 影响 → JOIN 里必须显式写 {@code del_flag = 0}。
 * 医嘱行本身已经把患者/科室/病区/床位做成快照，所以列表**不需要 JOIN 患者表**，
 * 唯一要补的是"今日已执行次数"（队列与列表都要看）。
 */
@Mapper
public interface BizInpatientOrderMapper extends BaseMapper<BizInpatientOrder> {

    /**
     * 医嘱分页（医生站 / 护士站共用）
     */
    @Select("""
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
            ORDER BY o.order_status ASC, o.order_time DESC, o.id DESC
            """)
    IPage<InpatientOrderVO> selectOrderPage(IPage<InpatientOrderVO> page, @Param("q") InpatientOrderQueryPageDTO query);

    /**
     * 当天已生成的医嘱号条数（用于医嘱号序号）
     */
    @Select("SELECT COUNT(*) FROM biz_inpatient_order WHERE del_flag = 0 AND order_no LIKE CONCAT(#{prefix}, '%')")
    long countByOrderNoPrefix(@Param("prefix") String prefix);

    /**
     * 当天已用过的组套号个数（用于组套号序号；按 DISTINCT 计，避免一个组套多条医嘱把序号顶飞）
     */
    @Select("SELECT COUNT(DISTINCT order_group) FROM biz_inpatient_order "
            + "WHERE del_flag = 0 AND order_group LIKE CONCAT(#{prefix}, '%')")
    long countByOrderGroupPrefix(@Param("prefix") String prefix);

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
}

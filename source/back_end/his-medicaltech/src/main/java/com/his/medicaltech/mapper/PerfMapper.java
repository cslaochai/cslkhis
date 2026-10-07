package com.his.medicaltech.mapper;

import com.his.medicaltech.vo.PerfDeptRevenueRowVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;

/**
 * 绩效成本 Mapper：科室收入从 L1 费用记账流水聚合（跨模块裸 SQL，一处收口）。
 * 注解值必须是编译期常量，用 + 拼接（不能用 formatted()）。
 */
@Mapper
public interface PerfMapper {

    /**
     * 记账行带符号，红冲即负行，净额 SUM 现算；账务归属月看 book_time
     */
    String REVENUE = "SUM(f.amount)";

    /**
     * 不 GROUP BY：同一 dept_id 若有多份科室名快照，分组会返回多行让 Map 接收方直接炸
     */
    String DEPT_REVENUE_SQL = "SELECT IFNULL(MAX(f.dept_name), '未分配科室') deptName, "
            + "ROUND(IFNULL(" + REVENUE + ", 0), 2) revenue, "
            + "ROUND(IFNULL(SUM(CASE WHEN f.item_type IN (2,3,4) THEN f.amount ELSE 0 END), 0), 2) drugRevenue "
            + "FROM biz_fee_record f "
            + "WHERE f.del_flag = 0 AND f.dept_id = #{deptId} "
            + "AND DATE_FORMAT(f.book_time, '%Y-%m') = #{month}";
    String HOSPITAL_REVENUE_SQL = "SELECT ROUND(IFNULL(" + REVENUE + ", 0), 2) revenue FROM biz_fee_record f "
            + "WHERE f.del_flag = 0 AND DATE_FORMAT(f.book_time, '%Y-%m') = #{month}";

    /**
     * 月度科室收入（净额）+ 药品收入（item_type 2西药 3中成药 4中药饮片）
     */
    @Select(DEPT_REVENUE_SQL)
    PerfDeptRevenueRowVO sumDeptRevenue(@Param("deptId") Long deptId, @Param("month") String month);

    /**
     * 全院月度收入（补未分配口径用）
     */
    @Select(HOSPITAL_REVENUE_SQL)
    BigDecimal sumHospitalRevenue(@Param("month") String month);
}

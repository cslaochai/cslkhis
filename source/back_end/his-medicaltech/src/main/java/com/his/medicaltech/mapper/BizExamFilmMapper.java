package com.his.medicaltech.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.medicaltech.entity.BizExamFilm;
import com.his.medicaltech.vo.ExamFilmVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 胶片用量 Mapper（sql/138）。
 */
@Mapper
public interface BizExamFilmMapper extends BaseMapper<BizExamFilm> {

    @Select("""
            <script>
            SELECT f.*, rec.apply_id, rec.apply_no, rec.inspection_item_code AS item_code_ref,
                   rec.inspection_item_name AS item_name_ref, rec.body_part AS body_part_ref
              FROM biz_exam_film f
              LEFT JOIN biz_inspection_record rec ON rec.id = f.record_id
             WHERE f.del_flag = 0
               <if test="keyword != null and keyword != ''">
                 AND (f.patient_name LIKE CONCAT('%', #{keyword}, '%')
                   OR f.patient_no  LIKE CONCAT('%', #{keyword}, '%')
                   OR f.film_no     LIKE CONCAT('%', #{keyword}, '%')
                   OR f.record_no   LIKE CONCAT('%', #{keyword}, '%'))
               </if>
               <if test="recordId != null"> AND f.record_id = #{recordId}</if>
               <if test="filmStatus != null"> AND f.film_status = #{filmStatus}</if>
               <if test="chargeFlag != null"> AND f.charge_flag = #{chargeFlag}</if>
               <if test="startDate != null and startDate != ''"> AND f.create_time &gt;= #{startDate}</if>
               <if test="endDate != null and endDate != ''"> AND f.create_time &lt;= CONCAT(#{endDate}, ' 23:59:59')</if>
             ORDER BY f.create_time DESC, f.id DESC
            </script>
            """)
    java.util.List<ExamFilmVO> selectFilmPage(IPage<ExamFilmVO> page,
                                              @Param("keyword") String keyword,
                                              @Param("recordId") Long recordId,
                                              @Param("filmStatus") Integer filmStatus,
                                              @Param("chargeFlag") Integer chargeFlag,
                                              @Param("startDate") String startDate,
                                              @Param("endDate") String endDate);

    /**
     * 今日（或指定日期区间）胶片汇总，给页面顶部那排数字用。
     *
     * <p>只数未作废的：作废行既没发实物也没收钱，混进统计会让「今天打了多少张片」
     * 对不上打印机计数器。
     *
     * <p><b>本方法没有 {@code <script>} 包裹，所以实体转义一律不写</b>：
     * MyBatis 只对带 {@code <script>} 的注解 SQL 做 XML 解析，这里的文本是原样发给 MySQL 的。
     * 写成 {@code &lt;=} 会被当成四个字符下发 → "syntax error near '= '2026-09-28'"，
     * 且这种错只有真调到这个接口才炸，启动时零征兆。
     * 同理不要写 {@code <>}：没有 XML 解析时它是合法的（但带 {@code <script>} 的兄弟方法不行），
     * 为了两边一致这里统一用 {@code NOT IN}。
     */
    @Select("""
            SELECT COALESCE(COUNT(*), 0)            AS row_count,
                   COALESCE(SUM(quantity), 0)       AS total_quantity,
                   COALESCE(SUM(amount), 0)         AS total_amount,
                   COALESCE(SUM(CASE WHEN charge_flag = 1 THEN amount ELSE 0 END), 0) AS charged_amount
              FROM biz_exam_film
             WHERE del_flag = 0 AND film_status NOT IN (4)
               AND create_time >= #{startDate}
               AND create_time <= CONCAT(#{endDate}, ' 23:59:59')
            """)
    ExamFilmVO.FilmStats selectStats(@Param("startDate") String startDate,
                                     @Param("endDate") String endDate);

    /**
     * 当天已用到的最大胶片单号序号（给 {@code newFilmNo()} 取号用）。
     *
     * <p>取 MAX 而不是 COUNT：删行 / 清数据后 COUNT 会回退，下一个号就撞 {@code uk_film_no}。
     * 返回 0 表示今天还没有胶片，取号方 +1 后就是 00001。
     */
    @Select("""
            SELECT COALESCE(MAX(CAST(RIGHT(film_no, 5) AS UNSIGNED)), 0)
              FROM biz_exam_film
             WHERE film_no LIKE CONCAT(#{prefix}, '%')
            """)
    long selectMaxSeqOfDay(@Param("prefix") String prefix);
}

package com.his.appoint.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDate;

/**
 * 日终结转用的裸 SQL。
 */
@Mapper
public interface DayEndSettleMapper {

    /**
     * 该日「挂了号但没到院」的挂号数 = regist_status=1 且<b>一条队列行都没有</b>。
     */
    @Select("""
            SELECT COUNT(*) FROM biz_appoint_info r
             WHERE r.del_flag = 0 AND r.visit_date = #{visitDate} AND r.regist_status = 1
               AND NOT EXISTS (SELECT 1 FROM biz_queue q WHERE q.regist_id = r.id AND q.del_flag = 0)
            """)
    long countNoShow(@Param("visitDate") LocalDate visitDate);

    /**
     * 该日「到过院但没走完就诊」的挂号数 —— <b>必须与 {@link #markUnvisited} 的 WHERE 逐字对齐</b>。
     *
     * <p>这里踩过一次坑，是这套逻辑里最容易复发的错：计数版一开始只写了
     * regist_status IN (2,3) OR 名下有队列行，漏了终态排除。
     * 结果「已就诊(4) 但名下有队列行」「上一轮已判成未就诊(8) 且队列行还在」的行
     * 每轮都被算作「还有遗留」，而 UPDATE 因 {@code NOT IN (4,5,6,7,8)} 一条都不改 ——
     * 于是 {@code selectEarliestUnsettledDate} 永远指着同一天，日终结转每次触发都从那天重跑，
     * <b>且永远跑不完</b>（实测：某天计数 9、实改 0）。
     *
     * <p>对齐后「计数 == 本轮 UPDATE 行数」恒成立，跑完第二遍必然全 0，这也是可重入的判据。
     */
    @Select("""
            SELECT COUNT(*) FROM biz_appoint_info r
             WHERE r.del_flag = 0 AND r.visit_date = #{visitDate}
               AND r.regist_status NOT IN (4, 5, 6, 7, 8)
               AND ( r.regist_status IN (2, 3)
                     OR EXISTS (SELECT 1 FROM biz_queue q WHERE q.regist_id = r.id AND q.del_flag = 0) )
            """)
    long countUnvisited(@Param("visitDate") LocalDate visitDate);

    /**
     * 其中「已接诊但从未结诊」的条数（挂号停在 3），单独报出来提醒人工核对
     */
    @Select("""
            SELECT COUNT(*) FROM biz_appoint_info
             WHERE del_flag = 0 AND visit_date = #{visitDate} AND regist_status = 3
            """)
    long countStuckConsulting(@Param("visitDate") LocalDate visitDate);

    /**
     * 该日还挂在队列里的行数（queue_status 2 候诊中 / 3 就诊中）
     */
    @Select("""
            SELECT COUNT(*) FROM biz_queue
             WHERE del_flag = 0 AND visit_date = #{visitDate} AND queue_status IN (2, 3)
            """)
    long countOpenQueue(@Param("visitDate") LocalDate visitDate);

    /**
     * 其中「挂号已退号/已过号，队列却还挂着候诊中/就诊中」的错位行数
     */
    @Select("""
            SELECT COUNT(*) FROM biz_queue q
              JOIN biz_appoint_info r ON r.id = q.regist_id AND r.del_flag = 0
             WHERE q.del_flag = 0 AND q.visit_date = #{visitDate}
               AND q.queue_status IN (2, 3) AND r.regist_status IN (5, 6)
            """)
    long countQueueRegistMismatch(@Param("visitDate") LocalDate visitDate);

    /**
     * 最早一天还留着「未收尾」记录的就诊日；没有则返回 null。
     *
     * <p>懒触发靠它算出要补跑哪一段，而不是死盯着「昨天」——
     * 系统停几天再开，中间的每一天都得补上，否则那几天的遗留永远留在库里。
     *
     * <p>判据必须与下面的 UPDATE <b>逐条对齐</b>，否则会出现「永远补不完」：
     * 查询说这天还有遗留、UPDATE 却一条都不改，于是每次触发都从这里开始。
     */
    @Select("""
            SELECT MIN(visit_date) FROM (
              SELECT visit_date FROM biz_appoint_info
               WHERE del_flag = 0 AND visit_date < CURDATE() AND regist_status IN (1, 2, 3)
              UNION ALL
              SELECT visit_date FROM biz_queue
               WHERE del_flag = 0 AND visit_date < CURDATE() AND queue_status IN (2, 3)
            ) t
            """)
    LocalDate selectEarliestUnsettledDate();

    /**
     * ① 队列行跟随挂号终态：挂号已退号/已过号，队列还挂着候诊中的，按挂号对齐。
     *
     * <p>为什么单独一步：这类行是「退号时没同步队列」留下的历史错位（库里近 30 天有 9 行）。
     * 不修的话它们会被当成「还在候诊」参与统计，而正确的做法是跟着挂号走
     * （退号 → 队列已退号；过号 → 队列已过号）。
     */
    @Update("""
            UPDATE biz_queue q
              JOIN biz_appoint_info r ON r.id = q.regist_id AND r.del_flag = 0
               SET q.queue_status = r.regist_status,
                   q.end_time = COALESCE(q.end_time, TIMESTAMP(#{visitDate}, '23:59:59')),
                   q.update_by = #{operator},
                   q.update_time = NOW()
             WHERE q.del_flag = 0 AND q.visit_date = #{visitDate}
               AND q.queue_status IN (2, 3) AND r.regist_status IN (5, 6)
            """)
    int alignQueueToRegistTerminal(@Param("visitDate") LocalDate visitDate,
                                   @Param("operator") String operator);

    /**
     * ② 未签到 → 7 爽约（判据见类注释：没有队列行才算没来）
     */
    @Update("""
            UPDATE biz_appoint_info r
               SET r.regist_status = 7,
                   r.remark = CASE WHEN r.remark LIKE '%日终结转%' THEN r.remark
                                   ELSE CONCAT(COALESCE(r.remark, ''), #{marker}) END,
                   r.update_by = #{operator},
                   r.update_time = NOW()
             WHERE r.del_flag = 0 AND r.visit_date = #{visitDate} AND r.regist_status = 1
               AND NOT EXISTS (SELECT 1 FROM biz_queue q WHERE q.regist_id = r.id AND q.del_flag = 0)
            """)
    int markNoShow(@Param("visitDate") LocalDate visitDate,
                   @Param("marker") String marker,
                   @Param("operator") String operator);

    /**
     * ③ 到过院但没走完就诊 → 8 未就诊（含「已接诊未结诊」，见 countStuckConsulting）
     */
    @Update("""
            UPDATE biz_appoint_info r
               SET r.regist_status = 8,
                   r.remark = CASE WHEN r.remark LIKE '%日终结转%' THEN r.remark
                                   ELSE CONCAT(COALESCE(r.remark, ''), #{marker}) END,
                   r.update_by = #{operator},
                   r.update_time = NOW()
             WHERE r.del_flag = 0 AND r.visit_date = #{visitDate}
               AND r.regist_status NOT IN (4, 5, 6, 7, 8)
               AND ( r.regist_status IN (2, 3)
                     OR EXISTS (SELECT 1 FROM biz_queue q WHERE q.regist_id = r.id AND q.del_flag = 0) )
            """)
    int markUnvisited(@Param("visitDate") LocalDate visitDate,
                      @Param("marker") String marker,
                      @Param("operator") String operator);

    /**
     * ④ 队列行收「已失效」。
     *
     * <p>必须排在 ③ 之后：③ 判「名下有队列行」时会用到还在 2/3 的队列行。
     *
     * <p>{@code end_time} 写的是<b>那一天</b>的 23:59:59 而不是 NOW()：
     * 队列行的结束时间会被门诊日志拿来算就诊时长，补跑历史时用 NOW() 会算出「就诊 3 天」。
     *
     * <p>不动 {@code is_overdue} / {@code overdue_reason}：这行不是过号，别把它伪装成过号。
     */
    @Update("""
            UPDATE biz_queue
               SET queue_status = 7,
                   end_time = TIMESTAMP(#{visitDate}, '23:59:59'),
                   update_by = #{operator},
                   update_time = NOW()
             WHERE del_flag = 0 AND visit_date = #{visitDate} AND queue_status IN (2, 3)
            """)
    int expireQueue(@Param("visitDate") LocalDate visitDate,
                    @Param("operator") String operator);
}

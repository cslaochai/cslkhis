package com.his.operation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.operation.dto.OperationApplyQueryPageDTO;
import com.his.operation.entity.BizOperationApply;
import com.his.operation.vo.OperationApplyVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 住院手术申请单 Mapper。
 *
 * <p>自定义 {@code @Select} 不受 {@code @TableLogic} 影响 → JOIN 里必须显式写 {@code del_flag = 0}。
 *
 * <p>三处刻意的做法：
 * <ol>
 *   <li><b>患者姓名/床号/科室名用主表快照，不再 JOIN 取</b>：转科、换床、科室改名之后，
 *       历史手术单上的"当时在哪个科、哪张床"不能被改写 —— 那正是手术单要证明的事。</li>
 *   <li><b>未完成排在前面用 FIELD() 显式指定顺序</b>：状态码是 0-待排期 / 1-已排期 /
 *       2-术前核对完成 / 3-已完成 / 4-已取消，按码值升序刚好可用，但仍显式写出来 ——
 *       以后加码值（如 5-停手术待审）时不会悄悄把顺序搞乱。</li>
 *   <li><b>排台冲突用"区间重叠"判定</b>（{@code start < other_end AND end > other_start}），
 *       不是"同一天同房间就算冲突" —— 后者会把上午下午两台正常手术判成冲突。</li>
 * </ol>
 */
@Mapper
public interface BizOperationApplyMapper extends BaseMapper<BizOperationApply> {

    /**
     * 列表/详情公共投影。
     *
     * <p>只 JOIN 主表**没有快照**的三样：患者号、入院在院状态、回写病历号。
     * 刻意不 JOIN 患者姓名/床号 —— 那些主表已经有快照列，两个同名列同时出现在
     * ResultSet 里，取到哪一个取决于驱动，属于会静默出错的那类写法。
     */
    String PROJECTION = """
            SELECT c.*,
                   p.patient_no,
                   a.admit_status,
                   r.record_no
            FROM biz_operation_apply c
                     LEFT JOIN biz_patient p ON p.id = c.patient_id AND p.del_flag = 0
                     LEFT JOIN biz_admission a ON a.admission_id = c.admission_id AND a.del_flag = 0
                     LEFT JOIN biz_inpatient_record r ON r.id = c.record_id AND r.del_flag = 0
            """;

    /** 手术申请分页（手术室排台工作台 / 病区申请方工作台共用） */
    @Select(PROJECTION + """
            WHERE c.del_flag = 0
              AND (#{q.admissionId} IS NULL OR c.admission_id = #{q.admissionId})
              AND (#{q.patientId} IS NULL OR c.patient_id = #{q.patientId})
              AND (#{q.applyDeptId} IS NULL OR c.apply_dept_id = #{q.applyDeptId})
              AND (#{q.surgeonId} IS NULL OR c.surgeon_id = #{q.surgeonId})
              AND (#{q.operationStatus} IS NULL OR c.operation_status = #{q.operationStatus})
              AND (#{q.unfinishedOnly} IS NULL OR #{q.unfinishedOnly} = 0
                   OR c.operation_status IN (0, 1, 2))
              AND (#{q.operationRoom} IS NULL OR #{q.operationRoom} = ''
                   OR c.operation_room = #{q.operationRoom})
              AND (#{q.plannedDateFrom} IS NULL OR c.planned_start_time >= #{q.plannedDateFrom})
              AND (#{q.plannedDateTo} IS NULL OR c.planned_start_time < #{q.plannedDateTo})
              AND (#{q.keyword} IS NULL OR #{q.keyword} = ''
                   OR c.apply_no LIKE CONCAT('%', #{q.keyword}, '%')
                   OR c.admission_no LIKE CONCAT('%', #{q.keyword}, '%')
                   OR c.patient_name LIKE CONCAT('%', #{q.keyword}, '%')
                   OR p.patient_no LIKE CONCAT('%', #{q.keyword}, '%')
                   OR c.planned_operation_name LIKE CONCAT('%', #{q.keyword}, '%')
                   OR c.actual_operation_name LIKE CONCAT('%', #{q.keyword}, '%')
                   OR c.surgeon_name LIKE CONCAT('%', #{q.keyword}, '%'))
            ORDER BY FIELD(c.operation_status, 0, 1, 2, 3, 4),
                     c.planned_start_time IS NULL, c.planned_start_time ASC, c.id DESC
            """)
    IPage<OperationApplyVO> selectApplyPage(IPage<OperationApplyVO> page,
                                            @Param("q") OperationApplyQueryPageDTO query);

    /** 手术申请详情 */
    @Select(PROJECTION + " WHERE c.del_flag = 0 AND c.id = #{applyId}")
    OperationApplyVO selectApplyById(@Param("applyId") Long applyId);

    /** 某次住院的全部手术申请（病案首页/病程里回看这条链用） */
    @Select(PROJECTION + """
             WHERE c.del_flag = 0 AND c.admission_id = #{admissionId}
             ORDER BY c.apply_time ASC, c.id ASC
            """)
    List<OperationApplyVO> selectByAdmission(@Param("admissionId") Long admissionId);

    /** 当天已生成的手术单号条数（单号序号用） */
    @Select("SELECT COUNT(*) FROM biz_operation_apply WHERE del_flag = 0 AND apply_no LIKE CONCAT(#{prefix}, '%')")
    long countByNoPrefix(@Param("prefix") String prefix);

    /** 未完成手术数（待排期 + 已排期 + 术前核对完成）：工作台角标用 */
    @Select("""
            SELECT COUNT(*) FROM biz_operation_apply
            WHERE del_flag = 0 AND operation_status IN (0, 1, 2)
              AND (#{admissionId} IS NULL OR admission_id = #{admissionId})
            """)
    long countUnfinished(@Param("admissionId") Long admissionId);

    /**
     * 同一次住院是否已有"在途"的同一术式申请（防重复发起）。
     *
     * <p>口径与"同住院+同会诊科室不允许并存两条未完成会诊"一致：
     * 同一台手术申请两次，四核对里就是"重复"。
     */
    @Select("""
            SELECT COUNT(*) FROM biz_operation_apply
            WHERE del_flag = 0
              AND admission_id = #{admissionId}
              AND operation_status IN (0, 1, 2)
              AND planned_operation_name = #{operationName}
            """)
    long countUnfinishedSameName(@Param("admissionId") Long admissionId,
                                 @Param("operationName") String operationName);

    /**
     * 同一次住院是否已有"主要手术"申请（未取消的都算）。
     *
     * <p>首页主要手术只能 1 条，与"主要诊断必须且只能 1 条"同口径。
     *
     * @param excludeId 要排除的申请ID（修改/排台时排除自己），可传 null
     */
    @Select("""
            SELECT COUNT(*) FROM biz_operation_apply
            WHERE del_flag = 0
              AND admission_id = #{admissionId}
              AND is_main = 1
              AND operation_status <> 4
              AND (#{excludeId} IS NULL OR id <> #{excludeId})
            """)
    long countMainOperation(@Param("admissionId") Long admissionId,
                            @Param("excludeId") Long excludeId);

    /**
     * 排台冲突数：同一手术间、时间区间重叠、且处于在途/已完成状态的手术。
     *
     * <p>区间重叠的判定是新.start < 旧.end AND 新.end > 旧.start，
     * 端点相接（上一台 10:00 结束、下一台 10:00 开始）不算冲突 —— 这是事实上的正确口径。
     */
    @Select("""
            SELECT COUNT(*) FROM biz_operation_apply
            WHERE del_flag = 0
              AND operation_room = #{room}
              AND operation_status IN (1, 2, 3)
              AND planned_start_time IS NOT NULL
              AND planned_end_time IS NOT NULL
              AND planned_start_time < #{endTime}
              AND planned_end_time > #{startTime}
              AND (#{excludeId} IS NULL OR id <> #{excludeId})
            """)
    long countRoomConflict(@Param("room") String room,
                           @Param("startTime") LocalDateTime startTime,
                           @Param("endTime") LocalDateTime endTime,
                           @Param("excludeId") Long excludeId);

    /**
     * 与目标时段冲突的在途手术（仅用于把冲突讲清楚：拒绝时必须点出"和哪一台撞了"）。
     *
     * <p>只回必要几列，按计划开始时间升序 —— 让提示语能写成
     * "1号手术间 09:00~11:00 已有 SS202609190001（患者丙腹腔镜胆囊切除术）在此排台"。
     */
    @Select("""
            SELECT id, apply_no, patient_name, planned_operation_name,
                   planned_start_time, planned_end_time, operation_status
            FROM biz_operation_apply
            WHERE del_flag = 0
              AND operation_room = #{room}
              AND operation_status IN (1, 2, 3)
              AND planned_start_time IS NOT NULL
              AND planned_end_time IS NOT NULL
              AND planned_start_time < #{endTime}
              AND planned_end_time > #{startTime}
              AND (#{excludeId} IS NULL OR id <> #{excludeId})
            ORDER BY planned_start_time ASC
            """)
    List<BizOperationApply> selectRoomConflicts(@Param("room") String room,
                                                @Param("startTime") LocalDateTime startTime,
                                                @Param("endTime") LocalDateTime endTime,
                                                @Param("excludeId") Long excludeId);

    /**
     * 排台总表：某时段内已排台/核对完成/已完成的手术（矩阵列内的卡片）。
     *
     * <p>按计划开始时间升序 —— 总表就是手术室一天的日程，顺序即时间轴。
     */
    @Select(PROJECTION + """
             WHERE c.del_flag = 0 AND c.operation_status IN (1, 2, 3)
               AND c.planned_start_time >= #{from} AND c.planned_start_time < #{to}
             ORDER BY c.planned_start_time ASC, c.id ASC
            """)
    List<OperationApplyVO> selectScheduledBetween(@Param("from") LocalDateTime from,
                                                  @Param("to") LocalDateTime to);

    /** 排台总表：待排期申请（矩阵底部「待排期」暂存区，拖进手术间列才算排台） */
    @Select(PROJECTION + """
             WHERE c.del_flag = 0 AND c.operation_status = 0
             ORDER BY c.is_emergency DESC, c.apply_time ASC, c.id ASC
            """)
    List<OperationApplyVO> selectUnscheduled();

    /** 已用过的手术间（下拉候选；取不到就返回空列表，由前端允许自由输入） */
    @Select("""
            SELECT DISTINCT operation_room FROM biz_operation_apply
            WHERE del_flag = 0 AND operation_room IS NOT NULL AND operation_room <> ''
            ORDER BY operation_room
            """)
    List<String> selectRoomList();

    /** 科室名（取不到返回 null，由调用方渲染「未知科室(ID=x)」，绝不编一个名字） */
    @Select("SELECT dept_name FROM sys_department WHERE id = #{deptId} AND del_flag = 0")
    String selectDeptName(@Param("deptId") Long deptId);

    /** 员工姓名（主刀/麻醉/助手/核对人一律服务端查名，不信任前端传来的姓名） */
    @Select("SELECT emp_name FROM sys_employee WHERE id = #{empId} AND del_flag = 0")
    String selectEmployeeName(@Param("empId") Long empId);
}

package com.his.emr.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.his.emr.entity.BizArchiveCodeTask;
import com.his.emr.vo.ArchiveCodeTaskStatsVO;
import com.his.emr.vo.ArchiveCodeTaskVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 病案编码任务 Mapper
 */
@Mapper
public interface BizArchiveCodeTaskMapper extends BaseMapper<BizArchiveCodeTask> {

    /**
     * 任务分页
     *
     * ⚠ ORDER BY 必须补唯一二级键 id（同秒创建顺序不稳定 → 翻页重复+丢行，且不报错）
     */
    @Select("<script>" +
            "SELECT t.* FROM biz_archive_code_task t " +
            "WHERE t.del_flag = 0 " +
            "<if test='taskNo != null and taskNo != \"\"'> AND t.task_no LIKE CONCAT('%', #{taskNo}, '%') </if> " +
            "<if test='status != null'> AND t.status = #{status} </if> " +
            "<if test='coderId != null'> AND t.coder_id = #{coderId} </if> " +
            "<if test='keyword != null and keyword != \"\"'> AND (t.task_no LIKE CONCAT('%', #{keyword}, '%') " +
            "   OR t.record_no LIKE CONCAT('%', #{keyword}, '%') OR t.patient_name LIKE CONCAT('%', #{keyword}, '%')) </if> " +
            "ORDER BY t.create_time DESC, t.id DESC" +
            "</script>")
    Page<ArchiveCodeTaskVO> selectTaskPage(Page<ArchiveCodeTaskVO> page,
                                           @Param("taskNo") String taskNo,
                                           @Param("status") Integer status,
                                           @Param("coderId") Long coderId,
                                           @Param("keyword") String keyword);

    /**
     * 详情
     */
    @Select("SELECT t.* FROM biz_archive_code_task t WHERE t.del_flag = 0 AND t.id = #{id}")
    ArchiveCodeTaskVO selectTaskById(@Param("id") Long id);

    /**
     * 按主键取任务并加行锁（提交/审核的并发闸门）
     */
    @Select("SELECT * FROM biz_archive_code_task WHERE id = #{id} AND del_flag = 0 FOR UPDATE")
    BizArchiveCodeTask selectByIdForUpdate(@Param("id") Long id);

    /**
     * 待同步的归档记录：archive_status IN (1,2) 且尚无任务（含已删除任务，避免重建历史脏单）
     */
    @Select("SELECT a.id, a.record_no, a.patient_name, a.dept_name, a.diagnosis " +
            "FROM biz_medical_record_archive a " +
            "WHERE a.del_flag = 0 AND a.archive_status IN (1, 2) " +
            "  AND NOT EXISTS (SELECT 1 FROM biz_archive_code_task t WHERE t.archive_id = a.id)")
    List<Map<String, Object>> selectUnsyncedArchives();

    /**
     * 编码员姓名（跨模块读 his-system 的员工，铁律用裸 SQL；只认在职）
     */
    @Select("SELECT emp_name FROM sys_employee WHERE id = #{empId} AND status = 1")
    String selectEmployeeName(@Param("empId") Long empId);

    /**
     * 工作台统计：待编码 / 已提交 / 已完成 / 已退修
     */
    @Select("SELECT " +
            "  (SELECT COUNT(*) FROM biz_archive_code_task WHERE del_flag = 0 AND status = 1) AS pending, " +
            "  (SELECT COUNT(*) FROM biz_archive_code_task WHERE del_flag = 0 AND status = 2) AS submitted, " +
            "  (SELECT COUNT(*) FROM biz_archive_code_task WHERE del_flag = 0 AND status = 3) AS done, " +
            "  (SELECT COUNT(*) FROM biz_archive_code_task WHERE del_flag = 0 AND status = 4) AS rework")
    ArchiveCodeTaskStatsVO selectStats();
}

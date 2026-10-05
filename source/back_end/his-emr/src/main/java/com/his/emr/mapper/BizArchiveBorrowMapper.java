package com.his.emr.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.his.emr.entity.BizArchiveBorrow;
import com.his.emr.vo.ArchiveBorrowStatsVO;
import com.his.emr.vo.ArchiveBorrowVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 病案借阅/复印 Mapper
 */
@Mapper
public interface BizArchiveBorrowMapper extends BaseMapper<BizArchiveBorrow> {

    /**
     * 借阅/复印分页
     * <p>
     * ⚠ ORDER BY 必须补唯一二级键 id（同秒创建顺序不稳定 → 翻页重复+丢行，且不报错）
     */
    @Select("<script>" +
            "SELECT b.* FROM biz_archive_borrow b " +
            "WHERE b.del_flag = 0 " +
            "<if test='borrowNo != null and borrowNo != \"\"'> AND b.borrow_no LIKE CONCAT('%', #{borrowNo}, '%') </if> " +
            "<if test='borrowType != null'> AND b.borrow_type = #{borrowType} </if> " +
            "<if test='status != null'> AND b.status = #{status} </if> " +
            "<if test='keyword != null and keyword != \"\"'> AND (b.record_no LIKE CONCAT('%', #{keyword}, '%') " +
            "   OR b.patient_name LIKE CONCAT('%', #{keyword}, '%') OR b.purpose LIKE CONCAT('%', #{keyword}, '%')) </if> " +
            "ORDER BY b.create_time DESC, b.id DESC" +
            "</script>")
    Page<ArchiveBorrowVO> selectBorrowPage(Page<ArchiveBorrowVO> page,
                                           @Param("borrowNo") String borrowNo,
                                           @Param("borrowType") Integer borrowType,
                                           @Param("status") Integer status,
                                           @Param("keyword") String keyword);

    /**
     * 详情
     */
    @Select("SELECT b.* FROM biz_archive_borrow b WHERE b.del_flag = 0 AND b.id = #{id}")
    ArchiveBorrowVO selectBorrowById(@Param("id") Long id);

    /**
     * 按主键取单并加行锁（审核/归还的并发闸门）
     */
    @Select("SELECT * FROM biz_archive_borrow WHERE id = #{id} AND del_flag = 0 FOR UPDATE")
    BizArchiveBorrow selectByIdForUpdate(@Param("id") Long id);

    /**
     * 同一归档记录下同类型的未结单（待审核/已借出）数量 —— 防重复申请
     */
    @Select("SELECT COUNT(*) FROM biz_archive_borrow " +
            "WHERE del_flag = 0 AND archive_id = #{archiveId} AND borrow_type = #{borrowType} AND status IN (1, 2)")
    int countOpenByArchive(@Param("archiveId") Long archiveId, @Param("borrowType") Integer borrowType);

    /**
     * 工作台统计：待审核 / 已借出 / 超期未还（应还日期早于今日且未归还）/ 已归还
     */
    @Select("SELECT " +
            "  (SELECT COUNT(*) FROM biz_archive_borrow WHERE del_flag = 0 AND status = 1) AS pending, " +
            "  (SELECT COUNT(*) FROM biz_archive_borrow WHERE del_flag = 0 AND status = 2) AS lentOut, " +
            "  (SELECT COUNT(*) FROM biz_archive_borrow WHERE del_flag = 0 AND status = 2 " +
            "     AND expect_return_date IS NOT NULL AND expect_return_date < CURDATE()) AS overdue, " +
            "  (SELECT COUNT(*) FROM biz_archive_borrow WHERE del_flag = 0 AND status = 3) AS returned")
    ArchiveBorrowStatsVO selectStats();
}

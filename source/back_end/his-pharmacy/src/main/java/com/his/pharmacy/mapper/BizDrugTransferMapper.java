package com.his.pharmacy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.his.pharmacy.entity.BizDrugTransfer;
import com.his.pharmacy.vo.DrugTransferVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 药品调拨单 Mapper（sql/154）
 */
@Mapper
public interface BizDrugTransferMapper extends BaseMapper<BizDrugTransfer> {

    /** 列表与详情共用列（自定义 SQL 不走 MP 的 @TableLogic，del_flag 必须手写） */
    String COLUMNS = "t.id, t.transfer_no, t.transfer_type, t.from_room, t.to_room, t.reason, t.status, "
            + "t.total_items, t.total_quantity, t.out_quantity, t.in_quantity, t.total_amount, "
            + "t.out_by, t.out_time, t.in_by, t.in_time, t.cancel_by, t.cancel_time, t.cancel_reason, "
            + "t.create_by, t.create_time, t.update_by, t.update_time, t.remark ";

    /**
     * 调拨单分页
     * <p>⚠ ORDER BY 带唯一二级键 id：同秒建单时顺序不稳定，翻页会重复+丢行
     * <p>⚠ 日期过滤用 DATE(create_time)：{@code create_time <= 'yyyy-MM-dd'} 会漏掉当天全部单据
     */
    @Select("<script>" +
            "SELECT " + COLUMNS +
            "FROM biz_drug_transfer t WHERE t.del_flag = 0 " +
            "<if test='transferNo != null and transferNo != \"\"'> AND t.transfer_no LIKE CONCAT('%', #{transferNo}, '%') </if> " +
            "<if test='transferType != null'> AND t.transfer_type = #{transferType} </if> " +
            "<if test='status != null'> AND t.status = #{status} </if> " +
            "<if test='keyword != null and keyword != \"\"'> AND (t.reason LIKE CONCAT('%', #{keyword}, '%') " +
            "       OR t.transfer_no LIKE CONCAT('%', #{keyword}, '%')) </if> " +
            "<if test='dateStart != null and dateStart != \"\"'> AND DATE(t.create_time) &gt;= #{dateStart} </if> " +
            "<if test='dateEnd != null and dateEnd != \"\"'> AND DATE(t.create_time) &lt;= #{dateEnd} </if> " +
            "ORDER BY t.create_time DESC, t.id DESC" +
            "</script>")
    Page<DrugTransferVO> selectTransferPage(Page<DrugTransferVO> page,
                                            @Param("transferNo") String transferNo,
                                            @Param("transferType") Integer transferType,
                                            @Param("status") Integer status,
                                            @Param("keyword") String keyword,
                                            @Param("dateStart") String dateStart,
                                            @Param("dateEnd") String dateEnd);

    @Select("SELECT " + COLUMNS + "FROM biz_drug_transfer t WHERE t.del_flag = 0 AND t.id = #{id}")
    DrugTransferVO selectTransferById(@Param("id") Long id);

    /**
     * 按主键取单并加行锁
     * <p>发出/接收都是「判状态 → 动库存 → 回写状态」的多步动作，不锁行会出现两人同时点确认、
     * 同一批货被搬两次。
     */
    @Select("SELECT * FROM biz_drug_transfer WHERE id = #{id} AND del_flag = 0 FOR UPDATE")
    BizDrugTransfer selectByIdForUpdate(@Param("id") Long id);

    /**
     * ⚠ 物理删：uk_transfer_no 不含 del_flag，软删的行仍占着单号
     * （AGENTS §3：本表族删除走物理删，见 sql/154 表注）。
     */
    @Delete("DELETE FROM biz_drug_transfer WHERE id = #{id}")
    int purgeById(@Param("id") Long id);
}

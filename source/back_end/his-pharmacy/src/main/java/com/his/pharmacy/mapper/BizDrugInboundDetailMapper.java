package com.his.pharmacy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.pharmacy.entity.BizDrugInboundDetail;
import com.his.pharmacy.vo.DrugBriefVO;
import com.his.pharmacy.vo.DrugInboundDetailVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import com.his.pharmacy.mapper.BizDrugStockMapper;

/**
 * 药品入库单明细 Mapper
 */
@Mapper
public interface BizDrugInboundDetailMapper extends BaseMapper<BizDrugInboundDetail> {

    /**
     * 某入库单的全部明细（含已取消行，前端按 detail_status 显示）
     */
    @Select("SELECT id, inbound_id, inbound_no, drug_id, drug_code, drug_name, specification, unit, " +
            "       batch_no, production_date, expiry_date, quantity, cost_price, amount, detail_status, remark " +
            "FROM biz_drug_inbound_detail WHERE del_flag = 0 AND inbound_id = #{inboundId} " +
            "ORDER BY id ASC")
    List<DrugInboundDetailVO> selectByInboundId(@Param("inboundId") Long inboundId);

    /**
     * 有效明细条数（detail_status<>3）
     */
    @Select("SELECT COUNT(*) FROM biz_drug_inbound_detail " +
            "WHERE del_flag = 0 AND inbound_id = #{inboundId} AND detail_status <> 3")
    long countActiveByInbound(@Param("inboundId") Long inboundId);

    /**
     * 入库时把明细标记为「已入库」（detail_status=2）
     */
    @Update("UPDATE biz_drug_inbound_detail SET detail_status = 2, update_time = NOW() " +
            "WHERE inbound_id = #{inboundId} AND del_flag = 0 AND detail_status = 1")
    int markStockedIn(@Param("inboundId") Long inboundId);

    /**
     * 取消时把明细标记为「已取消」（detail_status=3）
     * 不物理删除：入库单是凭证，取消痕迹要留（谁取消、取消了什么）。
     */
    @Update("UPDATE biz_drug_inbound_detail SET detail_status = 3, update_time = NOW() " +
            "WHERE inbound_id = #{inboundId} AND del_flag = 0")
    int markCancelled(@Param("inboundId") Long inboundId);

    /**
     * 药品字典快照查询（生成入库单明细时取编码/名称/规格/单位）
     * 直查药品字典，不引入 his-system 模块依赖（同 BizDrugStockMapper 的做法）。
     */
    @Select("SELECT id, drug_code, drug_name, specification, unit FROM sys_drug " +
            "WHERE id = #{drugId} AND del_flag = 0")
    DrugBriefVO selectDrugBrief(@Param("drugId") Long drugId);
}

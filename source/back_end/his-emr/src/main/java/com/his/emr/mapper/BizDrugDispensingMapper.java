package com.his.emr.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.his.emr.entity.BizDrugDispensing;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 药品发药记录Mapper
 */
@Mapper
public interface BizDrugDispensingMapper extends BaseMapper<BizDrugDispensing> {

    /**
     * 发药明细分页（患者姓名/处方号模糊 + 状态过滤）
     */
    @Select("<script>" +
            "SELECT * FROM biz_drug_dispensing " +
            "WHERE del_flag = 0 " +
            "<if test='patientId != null'> AND patient_id = #{patientId} </if> " +
            "<if test='patientName != null and patientName != \"\"'> AND patient_name LIKE CONCAT('%', #{patientName}, '%') </if> " +
            "<if test='prescriptionNo != null and prescriptionNo != \"\"'> AND prescription_no LIKE CONCAT('%', #{prescriptionNo}, '%') </if> " +
            "<if test='dispensingStatus != null'> AND dispensing_status = #{dispensingStatus} </if> " +
            "ORDER BY create_time DESC, id DESC" +
            "</script>")
    Page<BizDrugDispensing> selectDispensingPage(Page<BizDrugDispensing> page,
                                                 @Param("patientId") Long patientId,
                                                 @Param("patientName") String patientName,
                                                 @Param("prescriptionNo") String prescriptionNo,
                                                 @Param("dispensingStatus") Integer dispensingStatus);

    /**
     * 这些处方明细里「已发药但还没办退药」的发药记录（退费闸门用，sql/154）
     *
     * <p>状态 2 是唯一需要拦的：1-待发药的钱退了由退费流程自己置 4-已取消，库存根本没动；
     * 3-已退药说明药师已经实物验收并回库，此时退钱才是账实相符的。
     * <br>必须一次 IN 查完：一张账单十几条记账行逐条查就是 N+1，而这是每次退费都要走的路径。
     */
    @Select("<script>" +
            "SELECT id, dispensing_no, prescription_no, prescription_detail_id, patient_name, " +
            "       drug_name, quantity, dispensing_status, pharmacist_name, dispensing_time " +
            "FROM biz_drug_dispensing " +
            "WHERE del_flag = 0 AND dispensing_status = 2 AND prescription_detail_id IN " +
            "<foreach collection='detailIds' item='did' open='(' separator=',' close=')'>#{did}</foreach>" +
            " ORDER BY id ASC" +
            "</script>")
    List<BizDrugDispensing> selectDispensedByDetailIds(@Param("detailIds") List<Long> detailIds);
}

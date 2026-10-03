package com.his.pharmacy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.pharmacy.entity.BizAntibioticAuth;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface BizAntibioticAuthMapper extends BaseMapper<BizAntibioticAuth> {

    /**
     * 同日授权编号最大值（生成 KJ+yyyyMMdd+4位序号用）。
     */
    @Select("SELECT MAX(auth_no) FROM biz_antibiotic_auth WHERE auth_no LIKE CONCAT('KJ', #{day}, '%')")
    String selectMaxAuthNo(@Param("day") String day);

    /**
     * 取某医师<b>当前有效</b>的最高授权级别（开方闸唯一口径）。
     *
     * <p>"有效" = status=1 且 expire_date &gt;= 今天。暂停/取消/过期一律不算 —— 开方闸只认这里，
     * 前端另有一份同样的判定逻辑（{@code AntibioticService}），两侧必须一致。
     * 返回 null 表示没有任何有效授权。
     */
    @Select("""
            SELECT MAX(auth_level) FROM biz_antibiotic_auth
            WHERE doctor_id = #{doctorId}
              AND status = 1
              AND expire_date >= #{today}
            """)
    Integer selectMaxValidLevel(@Param("doctorId") Long doctorId, @Param("today") LocalDate today);

    /** 某医师的全部授权记录（含暂停/取消，供授权台账展示） */
    @Select("""
            SELECT * FROM biz_antibiotic_auth
            WHERE doctor_id = #{doctorId}
            ORDER BY auth_level
            """)
    List<BizAntibioticAuth> selectByDoctor(@Param("doctorId") Long doctorId);
}

package com.his.patient.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.patient.entity.BizDeathCertificateCause;
import com.his.patient.vo.DeathCertificateVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 死因链明细 Mapper。
 *
 * <p>本表<b>没有 del_flag</b>，而唯一键 {@code uk_cert_part_seq(cert_id, part, seq_no)} 也不含 del_flag，
 * 所以「整体替换死因链」必须物理删（软删行继续占键，第二次保存同一行序必然 Duplicate entry，
 * AGENTS.md 第 3 条 L12 坑）。死因链没有留档价值，原证的追溯靠死亡医学证明书本身。
 */
@Mapper
public interface BizDeathCertificateCauseMapper extends BaseMapper<BizDeathCertificateCause> {

    @Select("""
            SELECT id, cert_id, part, seq_no, icd_code, icd_name, interval_text
              FROM biz_death_certificate_cause
             WHERE cert_id = #{certId}
             ORDER BY part ASC, seq_no ASC
            """)
    List<DeathCertificateVO.CauseVO> selectByCertId(@Param("certId") Long certId);

    /**
     * 物理删除某张证明的全部死因链行（撞的是 uk_cert_part_seq）
     */
    @Delete("DELETE FROM biz_death_certificate_cause WHERE cert_id = #{certId}")
    int purgeByCertId(@Param("certId") Long certId);

    /**
     * 死因链Ⅰ部分行数（签发前必须至少一行，国标「直接死因」必填）
     */
    @Select("SELECT COUNT(*) FROM biz_death_certificate_cause WHERE cert_id = #{certId} AND part = 1")
    int countChainRows(@Param("certId") Long certId);

    /**
     * Ⅰ部分未补全 ICD-10 编码的行数（签发门禁：编码是死因统计的唯一归口）
     */
    @Select("""
            SELECT COUNT(*) FROM biz_death_certificate_cause
             WHERE cert_id = #{certId} AND part = 1 AND (icd_code IS NULL OR icd_code = '')
            """)
    int countChainRowsMissingIcd(@Param("certId") Long certId);
}

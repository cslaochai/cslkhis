package com.his.common.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.common.entity.BizEmrSignature;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface BizEmrSignatureMapper extends BaseMapper<BizEmrSignature> {

    /**
     * 取对象最近一次签名（用于签名链的 prev 指针）
     */
    @Select("SELECT * FROM biz_emr_signature WHERE del_flag = 0 "
            + "AND biz_type = #{bizType} AND biz_id = #{bizId} "
            + "ORDER BY chain_no DESC, id DESC LIMIT 1")
    BizEmrSignature selectLastByBiz(@Param("bizType") Integer bizType, @Param("bizId") Long bizId);

    /**
     * 对象当前有效的签名（sign_status=1），可能为空
     */
    @Select("SELECT * FROM biz_emr_signature WHERE del_flag = 0 "
            + "AND biz_type = #{bizType} AND biz_id = #{bizId} AND sign_status = 1 "
            + "ORDER BY chain_no DESC, id DESC LIMIT 1")
    BizEmrSignature selectCurrentByBiz(@Param("bizType") Integer bizType, @Param("bizId") Long bizId);

    /**
     * 对象已有签名条数 → chain_no = count + 1
     */
    @Select("SELECT COUNT(*) FROM biz_emr_signature WHERE del_flag = 0 "
            + "AND biz_type = #{bizType} AND biz_id = #{bizId}")
    int countByBiz(@Param("bizType") Integer bizType, @Param("bizId") Long bizId);

    /**
     * 签名号已用条数（SIG+yyyyMMdd 前缀）
     */
    @Select("SELECT COUNT(*) FROM biz_emr_signature WHERE del_flag = 0 AND sign_no LIKE CONCAT(#{prefix}, '%')")
    long countBySignNoPrefix(@Param("prefix") String prefix);

    /**
     * 今天新增签名数
     */
    @Select("SELECT COUNT(*) FROM biz_emr_signature WHERE del_flag = 0 AND signed_time >= #{from}")
    long countSignedAfter(@Param("from") java.time.LocalDateTime from);

    @Select("SELECT MAX(signed_time) FROM biz_emr_signature WHERE del_flag = 0")
    java.time.LocalDateTime selectLastSignedTime();
}

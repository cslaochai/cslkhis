package com.his.medicaltech.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.medicaltech.entity.BizTransfusionBag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 输血血袋明细 Mapper。
 */
@Mapper
public interface BizTransfusionBagMapper extends BaseMapper<BizTransfusionBag> {

    /**
     * 血袋号是否已被<b>别的</b>申请单占用（一袋血只能给一个人）。
     *
     * <p>刻意<b>不建 DB 级 UNIQUE</b>：本表是逻辑删除（删除标记），
     * UNIQUE 会与"删过的行还留着"打架 —— 验证脚本重复执行时必然炸，
     * 而生产上"删了再补一袋同号"其实是数据错误、应该由服务层给出可读文案而不是 SQL 异常。
     * 同排台冲突、主要手术唯一，都是同一套做法。
     *
     * @param excludeApplyId 同一张申请单内的重复提交由服务层用集合去重，这里排除自己即可
     * @return 被别的申请单占用的条数（>0 = 冲突）
     */
    @Select("""
            SELECT COUNT(*) FROM biz_transfusion_bag
            WHERE del_flag = 0
              AND bag_no = #{bagNo}
              AND (#{excludeApplyId} IS NULL OR apply_id <> #{excludeApplyId})
            """)
    long countByBagNo(@Param("bagNo") String bagNo,
                      @Param("excludeApplyId") Long excludeApplyId);

    /**
     * 某张申请单下的血袋（按录入顺序升序 = 配血顺序）
     */
    @Select("""
            SELECT * FROM biz_transfusion_bag
            WHERE del_flag = 0 AND apply_id = #{applyId}
            ORDER BY id ASC
            """)
    List<BizTransfusionBag> selectByApply(@Param("applyId") Long applyId);

    /**
     * 某张申请单下已配血的血袋数（配血齐不齐的判定依据）
     */
    @Select("""
            SELECT COUNT(*) FROM biz_transfusion_bag
            WHERE del_flag = 0 AND apply_id = #{applyId}
            """)
    long countByApply(@Param("applyId") Long applyId);

    /**
     * 某张申请单下"配血不合"的血袋数（>0 → crossmatch_status = 3，不可发血）
     */
    @Select("""
            SELECT COUNT(*) FROM biz_transfusion_bag
            WHERE del_flag = 0 AND apply_id = #{applyId} AND crossmatch_result = 2
            """)
    long countIncompatible(@Param("applyId") Long applyId);
}

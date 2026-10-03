package com.his.emr.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.his.emr.entity.BizTcmDecoct;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 中药代煎单 Mapper
 */
@Mapper
public interface BizTcmDecoctMapper extends BaseMapper<BizTcmDecoct> {

    /**
     * 代煎台账分页（状态 + 单号/患者名模糊）。
     *
     * <p>返回 {@code Page} 而不是 List：MP 分页拦截器只往 Page 对象里回填 records，
     * 声明成 List 会让调用方拿到空列表（L12 踩过）。
     */
    @Select("<script>" +
            "SELECT * FROM biz_tcm_decoct " +
            "WHERE del_flag = 0 " +
            "<if test='decoctStatus != null'> AND decoct_status = #{decoctStatus} </if> " +
            "<if test='keyword != null and keyword != \"\"'> " +
            "  AND (decoct_no LIKE CONCAT('%', #{keyword}, '%') " +
            "    OR prescription_no LIKE CONCAT('%', #{keyword}, '%') " +
            "    OR patient_name LIKE CONCAT('%', #{keyword}, '%')) " +
            "</if> " +
            "ORDER BY FIELD(decoct_status, 1, 2, 3, 9), create_time DESC, id DESC" +
            "</script>")
    Page<BizTcmDecoct> selectDecoctPage(Page<BizTcmDecoct> page,
                                        @Param("decoctStatus") Integer decoctStatus,
                                        @Param("keyword") String keyword);
}

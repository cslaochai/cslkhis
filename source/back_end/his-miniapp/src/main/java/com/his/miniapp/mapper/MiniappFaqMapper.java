package com.his.miniapp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.miniapp.entity.SysFaq;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 患者端常见问题读侧。
 *
 * <p>检索在 Java 侧打分排序（见 {@code FaqSearchSupport}），Mapper 只负责把启用的条目取出来：
 * 全库FAQ是几十条量级、且由人工维护，拉全量比在 SQL 里拼十几个 LIKE 更好测、也更好调序。
 */
@Mapper
public interface MiniappFaqMapper extends BaseMapper<SysFaq> {

    /** 已有最大编号，用于新增时生成下一个（FAQ + 4 位序号） */
    @Select("SELECT MAX(faq_no) FROM sys_faq")
    String maxFaqNo();

    /**
     * 物理删。
     * <p>{@code uk_faq_no(faq_no)} 不含 del_flag，软删的行仍占着唯一键，
     * 删掉再新增同一编号必撞键 —— 这张表的删除只能是物理删。
     */
    @Delete("DELETE FROM sys_faq WHERE id = #{id}")
    int purgeById(@Param("id") Long id);
}

package com.his.miniapp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.miniapp.entity.SysFaq;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 患者端常见问题读侧。
 */
@Mapper
public interface MiniFaqMapper extends BaseMapper<SysFaq> {

    /**
     * 物理删。
     * <p>{@code uk_faq_no(faq_no)} 不含 del_flag，软删的行仍占着唯一键，
     * 删掉再新增同一编号必撞键 —— 这张表的删除只能是物理删。
     */
    @Delete("DELETE FROM sys_faq WHERE id = #{id}")
    int purgeById(@Param("id") Long id);
}

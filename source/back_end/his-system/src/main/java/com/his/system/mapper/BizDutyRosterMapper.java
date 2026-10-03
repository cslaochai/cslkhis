package com.his.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.system.entity.BizDutyRoster;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 全院总值班排班 Mapper
 */
@Mapper
public interface BizDutyRosterMapper extends BaseMapper<BizDutyRoster> {

    /**
     * 物理删除。
     *
     * <p>为什么不用 {@code deleteById}：全院总值班排班的唯一键
     * {@code uk_duty_date_shift_role(duty_date, shift_type, role_type)} <b>不含 del_flag</b>，
     * 而 BaseEntity.delFlag 带 {@code @TableLogic}，软删的行照样占着唯一键 ——
     * 「删掉今天白班主班再重新排一个人」必撞 Duplicate entry。排班是配置、没有留档价值，
     * 所以这里的删除一律是真删（见 AGENTS.md §3）。
     */
    @Delete("DELETE FROM biz_duty_roster WHERE id = #{id}")
    int purgeById(@Param("id") Long id);
}

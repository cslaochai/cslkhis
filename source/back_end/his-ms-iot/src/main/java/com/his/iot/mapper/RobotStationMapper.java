package com.his.iot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.iot.entity.RobotStation;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface RobotStationMapper extends BaseMapper<RobotStation> {

    /**
     * 站点表唯一键 uk_robot_station_code 不含 del_flag，软删会留下残行占着站点编码，
     * 再建同名站点必撞 Duplicate entry。删除一律走这里物理删。
     */
    @Delete("DELETE FROM biz_robot_station WHERE id = #{id}")
    int purgeById(@Param("id") Long id);
}

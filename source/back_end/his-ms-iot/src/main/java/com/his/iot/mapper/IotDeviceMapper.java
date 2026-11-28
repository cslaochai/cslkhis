package com.his.iot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.iot.entity.IotDevice;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 物联网设备档案（biz_iot_device）——设备科台账，只做设备存在性/启停判断。
 * 「现在挂在谁身上」不在本表：见 {@link IotDeviceBindMapper}。
 */
@Mapper
public interface IotDeviceMapper extends BaseMapper<IotDevice> {

    /**
     * 派车前按车号锁行（SELECT ... FOR UPDATE）。
     * <p>
     * 为什么必须锁：两张单并发派车时，双方都会读到"该车没有在途单"，然后各自下发 ASSIGN_TASK，
     * 结果是同一台车接两张单。锁行把同车号的派车串行化，后者仍会走 {@code assertRobotIdle}
     * 复核，能拿到的才是真空闲车。只在机器人调度里用，别处不要调（会拿到行锁直到事务结束）。
     */
    @Select("SELECT * FROM biz_iot_device WHERE device_no = #{deviceNo} AND device_type = 'robot' "
            + "AND del_flag = 0 LIMIT 1 FOR UPDATE")
    IotDevice selectRobotForUpdate(@Param("deviceNo") String deviceNo);
}

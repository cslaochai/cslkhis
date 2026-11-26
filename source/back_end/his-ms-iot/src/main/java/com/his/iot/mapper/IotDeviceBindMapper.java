package com.his.iot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.iot.entity.IotDeviceBind;
import com.his.iot.vo.IotBindCandidateDeviceVO;
import com.his.iot.vo.IotDeviceBindVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 设备—患者绑定关系（biz_iot_device_bind）。
 */
@Mapper
public interface IotDeviceBindMapper extends BaseMapper<IotDeviceBind> {

    /**
     * 当前在用绑定（护士站把设备挂到患者卡上）。
     * 设备名称从档案表取，避免绑定表里再存一份名称造成漂移。
     */
    @Select("SELECT b.id, b.device_no AS deviceNo, b.device_type AS deviceType, "
            + "d.device_name AS deviceName, b.patient_id AS patientId, b.admission_id AS admissionId, "
            + "b.bed_id AS bedId, b.bed_no AS bedNo, b.ward_id AS wardId, b.dept_id AS deptId, "
            + "b.bind_source AS bindSource, b.bind_by AS bindBy, b.bind_time AS bindTime "
            + "FROM biz_iot_device_bind b "
            + "LEFT JOIN biz_iot_device d ON d.device_no = b.device_no AND d.del_flag = 0 "
            + "WHERE b.del_flag = 0 AND b.bind_status = 1 "
            + "AND (#{deviceType} IS NULL OR b.device_type = #{deviceType}) "
            + "ORDER BY b.bind_time DESC")
    List<IotDeviceBindVO> selectActiveBinds(@Param("deviceType") String deviceType);

    /**
     * 可绑定设备：启用、且当前没有任何在用绑定。绑定弹框的候选清单（同时用来校验手工输入是否合法）。
     */
    @Select("SELECT d.device_no AS deviceNo, d.device_name AS deviceName, d.device_type AS deviceType, "
            + "d.location, d.dept_name AS deptName "
            + "FROM biz_iot_device d "
            + "WHERE d.del_flag = 0 AND d.enabled = 1 AND d.device_type = #{deviceType} "
            + "AND NOT EXISTS (SELECT 1 FROM biz_iot_device_bind b "
            + "WHERE b.device_no = d.device_no AND b.bind_status = 1 AND b.del_flag = 0) "
            + "ORDER BY d.device_no")
    List<IotBindCandidateDeviceVO> selectBindableDevices(@Param("deviceType") String deviceType);
}

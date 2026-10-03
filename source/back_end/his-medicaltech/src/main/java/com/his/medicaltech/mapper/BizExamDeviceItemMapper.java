package com.his.medicaltech.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.medicaltech.entity.BizExamDeviceItem;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface BizExamDeviceItemMapper extends BaseMapper<BizExamDeviceItem> {

    /**
     * 物理删除某设备的全部项目映射（覆盖式保存的第一步）。
     *
     * <p>必须物理删：本表有唯一索引 uk_exam_device_item(device_id, item_id)，而
     * {@code mapper.delete()} 走 MyBatis-Plus 的 @TableLogic 只把 del_flag 置 1 ——
     * 索引不含 del_flag，下次再配同一个「设备+项目」直接 Duplicate entry，
     * 表现为"取消映射后再也配不回来"。
     */
    @Delete("DELETE FROM biz_exam_device_item WHERE device_id = #{deviceId}")
    int hardDeleteByDevice(@Param("deviceId") Long deviceId);
}

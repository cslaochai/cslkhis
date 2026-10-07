package com.his.medicaltech.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.medicaltech.entity.BizExamDevice;
import com.his.medicaltech.vo.ExamDeviceItemCountRowVO;
import com.his.medicaltech.vo.ExamEquipmentOptionRowVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface BizExamDeviceMapper extends BaseMapper<BizExamDevice> {

    /**
     * 行锁读设备档位：号源生成与占号都按同一顺序先锁设备行，
     * 避免「并发改设备开放时间 + 并发生成号源」把网格算成两套。
     */
    @Select("SELECT * FROM biz_exam_device WHERE id = #{id} AND del_flag = 0 FOR UPDATE")
    BizExamDevice selectForUpdate(@Param("id") Long id);

    /**
     * device_code 的唯一索引不含 del_flag —— 逻辑删除的那条仍然占着编码位。
     * 不先探出来，@TableLogic 过滤后的重复校验会放行，最后由 DuplicateKey 兜成 500。
     */
    @Select("SELECT COUNT(*) FROM biz_exam_device WHERE device_code = #{code} AND del_flag = 1")
    int countDeletedByCode(@Param("code") String code);

    /**
     * 设备台账候选（只读挂接医疗设备台账，档案归 G22 域，本模块不建不改）。
     *
     * <p>列名先对过 information_schema：医疗设备台账只有 equipment_code/equipment_name/
     * category/status/dept_id 等，没有 unit、spec —— 裸 SQL 猜列名会编译期不报错、
     * 运行期 Unknown column 被兜成 500。
     */
    @Select("SELECT id, equipment_code AS equipmentCode, equipment_name AS equipmentName, "
            + "category, status FROM sys_equipment "
            + "WHERE del_flag = 0 ORDER BY equipment_code ASC")
    List<ExamEquipmentOptionRowVO> selectEquipmentOptions();

    /**
     * 各设备已开展项目数（列表页一次统计，不逐行查）
     */
    @Select("SELECT device_id AS deviceId, COUNT(*) AS itemCount FROM biz_exam_device_item "
            + "WHERE del_flag = 0 GROUP BY device_id")
    List<ExamDeviceItemCountRowVO> countItemsByDevice();
}

package com.his.medicaltech.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.medicaltech.entity.BizDialysisMachine;
import com.his.medicaltech.vo.DialysisVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 透析机位 Mapper。
 */
@Mapper
public interface BizDialysisMachineMapper extends BaseMapper<BizDialysisMachine> {

    @Select("""
            <script>
            SELECT m.*,
                   (SELECT COUNT(*) FROM biz_dialysis_session s
                     WHERE s.del_flag = 0 AND s.machine_id = m.id) AS session_total
              FROM biz_dialysis_machine m
             WHERE m.del_flag = 0
               <if test="machineNo != null and machineNo != ''">
                 AND m.machine_no LIKE CONCAT('%', #{machineNo}, '%')
               </if>
               <if test="roomName != null and roomName != ''">
                 AND m.room_name LIKE CONCAT('%', #{roomName}, '%')
               </if>
               <if test="status != null"> AND m.status = #{status}</if>
             ORDER BY m.machine_no ASC
            </script>
            """)
    List<DialysisVO.MachineVO> selectMachinePage(IPage<DialysisVO.MachineVO> page,
                                                 @Param("machineNo") String machineNo,
                                                 @Param("roomName") String roomName,
                                                 @Param("status") Integer status);

    /**
     * 排班可选机位（维修/停用的不上下拉，但看板仍展示）
     */
    @Select("SELECT * FROM biz_dialysis_machine WHERE del_flag = 0 AND status = 1 ORDER BY machine_no ASC")
    List<BizDialysisMachine> selectUsable();

    @Select("""
            <script>
            SELECT COUNT(*) FROM biz_dialysis_machine
             WHERE del_flag = 0 AND machine_no = #{machineNo}
               <if test="id != null"> AND id &lt;&gt; #{id}</if>
            </script>
            """)
    int countByNo(@Param("machineNo") String machineNo, @Param("id") Long id);
}

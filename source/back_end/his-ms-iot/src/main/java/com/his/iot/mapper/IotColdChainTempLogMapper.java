package com.his.iot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.iot.entity.IotColdChainTempLog;
import com.his.iot.vo.IotColdChainRecordVO;
import com.his.iot.vo.IotColdChainSummaryVO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 冷链 GSP 归档表。批量写入用 INSERT IGNORE：定时任务重跑、TDengine 回补都会命中唯一键，
 * 不会把同一窗口写成两行；不用 saveBatch，因为它遇唯一键会整批抛异常。
 */
@Mapper
public interface IotColdChainTempLogMapper extends BaseMapper<IotColdChainTempLog> {

    @Select("SELECT MAX(ts) FROM biz_coldchain_temp_log")
    LocalDateTime selectMaxTs();

    @Select("SELECT ts, temp, temp_min AS tempMin, temp_max AS tempMax, humidity, point_count AS pointCount"
            + " FROM biz_coldchain_temp_log WHERE device_no = #{deviceNo}"
            + " AND ts >= #{start} AND ts <= #{end} ORDER BY ts ASC")
    List<IotColdChainRecordVO> selectRange(@Param("deviceNo") String deviceNo,
                                           @Param("start") LocalDateTime start,
                                           @Param("end") LocalDateTime end);

    /**
     * 近 N 小时按设备汇总。窗口数 = 归档的 5 分钟窗口数（一天满档 288），
     * 用来判断「这一天的曲线是完整的，还是中间断了一段」。
     */
    @Select("SELECT device_no AS deviceNo, MIN(temp_min) AS minTemp, MAX(temp_max) AS maxTemp,"
            + " COUNT(*) AS windowCount, COALESCE(SUM(point_count), 0) AS sampleCount,"
            + " MIN(ts) AS firstTs, MAX(ts) AS lastTs"
            + " FROM biz_coldchain_temp_log WHERE ts >= #{start} GROUP BY device_no")
    List<IotColdChainSummaryVO> selectSummary(@Param("start") LocalDateTime start);

    @Insert("<script>"
            + "INSERT IGNORE INTO biz_coldchain_temp_log"
            + " (id, device_no, dept_name, cabinet_name, temp, temp_min, temp_max, humidity, point_count,"
            + " door, power, record_type, ts, create_time) VALUES "
            + "<foreach collection='list' item='r' separator=','>"
            + " (#{r.id}, #{r.deviceNo}, #{r.deptName}, #{r.cabinetName}, #{r.temp}, #{r.tempMin}, #{r.tempMax},"
            + " #{r.humidity}, #{r.pointCount}, #{r.door}, #{r.power}, #{r.recordType}, #{r.ts}, #{r.createTime})"
            + "</foreach>"
            + "</script>")
    int insertIgnoreBatch(@Param("list") List<IotColdChainTempLog> list);
}

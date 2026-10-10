package com.his.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.system.entity.SysSignCert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;

@Mapper
public interface SysSignCertMapper extends BaseMapper<SysSignCert> {

    /**
     * 取员工当前可用的证书：状态有效 + 落在有效期内。
     *
     * <p>刻意为"同时刻只应有一张"做兜底（ORDER BY id DESC LIMIT 1）：
     * 并发自动签发可能造出两张，取其一好过抛 TooManyResultsException 把
     * 数据问题伪装成 500。
     */
    @Select("SELECT * FROM sys_sign_cert WHERE del_flag = 0 AND emp_id = #{empId} "
            + "AND cert_status = 1 AND valid_from <= #{now} AND valid_to >= #{now} "
            + "ORDER BY id DESC LIMIT 1")
    SysSignCert selectActiveByEmp(@Param("empId") Long empId, @Param("now") LocalDateTime now);
}

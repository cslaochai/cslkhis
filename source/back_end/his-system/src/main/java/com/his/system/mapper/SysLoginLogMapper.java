package com.his.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.system.entity.SysLoginLog;
import com.his.system.vo.LoginFailAccountVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 登录日志 Mapper（写入由 {@code SysLoginLogService} 旁路落库，读取走日志查看页）。
 */
@Mapper
public interface SysLoginLogMapper extends BaseMapper<SysLoginLog> {

    /**
     * 近一段时间内登录失败次数达到阈值的账号（口令爆破嫌疑）。
     *
     * <p>{@code HAVING} 的阈值走参数 {@code minFailCount}：等保三级的「重要安全事件」
     * 认定线是配置项，不是常量，写死 SQL 就成了改一次要动代码的事。
     */
    @Select("SELECT user_name AS userName, COUNT(*) AS failCount, MAX(login_time) AS lastFailTime "
            + "FROM sys_login_log "
            + "WHERE del_flag = 0 AND login_status = 1 AND login_time >= #{since} "
            + "  AND user_name IS NOT NULL AND user_name <> '' "
            + "GROUP BY user_name "
            + "HAVING COUNT(*) >= #{minFailCount} "
            + "ORDER BY failCount DESC")
    List<LoginFailAccountVO> selectFailAccounts(@Param("since") LocalDateTime since,
                                                @Param("minFailCount") int minFailCount);
}

package com.his.charge.dto;

import com.his.common.base.PageParam;
import lombok.Data;

/**
 * 床旁结算病区榜查询入参。
 *
 * <p>科室范围不接受前端传 deptId：病区由服务端按当前岗位科室强制收口
 * （URS-COM-09，数据范围后端说了算），前端只负责翻页和搜人。
 */
@Data
public class BedsideWardBoardQueryDTO extends PageParam {

    /**
     * 患者姓名/患者号模糊
     */
    private String keyword;
}

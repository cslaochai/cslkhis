package com.his.report.vo;

import com.his.appoint.vo.BizAppointInfoListVO;
import lombok.Data;

import java.util.List;

/**
 * 门诊统计出参
 */
@Data
public class OutpatientStatsVO {

    /**
     * 就诊总数
     */
    private Long totalCount;

    /**
     * 男性就诊数
     */
    private Long maleCount;

    /**
     * 女性就诊数
     */
    private Long femaleCount;

    /**
     * 挂号明细列表
     */
    private List<BizAppointInfoListVO> registList;
}

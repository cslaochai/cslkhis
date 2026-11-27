package com.his.medicaltech.service;

import com.his.medicaltech.vo.BiNationalVO;
import com.his.medicaltech.vo.BiOverviewVO;

/**
 * BI 驾驶舱数据服务：运营总览与国考指标聚合取数的业务收口。
 */
public interface BiService {

    /**
     * 驾驶舱总览：今日挂号/入院/在院/出院/手术/收入、药占比、床位使用、7 日趋势、科室 TOP5 与收入结构，
     * 昨日对照值随环比一并返回。用于经营决策「BI 驾驶舱」页面首屏取数。
     */
    BiOverviewVO overview();

    /**
     * 运营效率指标近 30 日值：平均住院日、床位周转、耗占比、CMI、门诊次均、住院次均，分母一并返回供核对口径。
     * 用于「BI 驾驶舱」页面效率指标卡片取数。
     */
    BiNationalVO nationalMetrics();
}

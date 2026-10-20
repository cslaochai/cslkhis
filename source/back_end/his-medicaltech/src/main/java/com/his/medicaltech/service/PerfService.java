package com.his.medicaltech.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.medicaltech.dto.PerfDTO;
import com.his.medicaltech.vo.PerfVO;

/**
 * 绩效与成本核算服务：科室月度成本登记与「收入减结余乘提成系数」这条核算链的业务收口。
 */
public interface PerfService {

    /**
     * 录入科室月度成本（人力、药品、耗材、折旧、其他，合计由明细现算）；同科室同月只允许一条。
     * 用于绩效与成本核算页的成本录入。
     */
    PerfVO.CostRow saveCost(PerfDTO.CostSave dto);

    /**
     * 科室月度成本分页，按当前岗位可见科室收口。
     * 用于绩效与成本核算页的成本查询表格。
     */
    IPage<PerfVO.CostRow> costPage(PerfDTO.CostQuery dto);

    /**
     * 核算前预览：聚合该科室当月收入与药占比，叠加已登记成本快照并算出结余。
     * 用于执行核算弹框打开时的取数，也供核算动作复用同一份收入与成本口径。
     */
    PerfVO.RevenueInfo revenueInfo(Long deptId, String month);

    /**
     * 执行核算并落绩效结果：结余为正时按提成系数计绩效金额、药占比现算，同科室同月重算覆盖。
     * 用于绩效与成本核算页的「执行核算」动作。
     */
    PerfVO.PerfRow calc(PerfDTO.PerfCalc dto);

    /**
     * 绩效结果分页，按当前岗位可见科室收口，月份倒序、同月按绩效金额倒序。
     * 用于绩效与成本核算页的「绩效核算结果」表格。
     */
    IPage<PerfVO.PerfRow> perfPage(PerfDTO.PerfQuery dto);
}

package com.his.emr.service;

import com.his.common.base.PageResult;
import com.his.emr.dto.*;
import com.his.emr.vo.DisputeCaseVO;
import com.his.emr.vo.DisputeStatVO;

/**
 * 医疗纠纷 / 投诉登记服务。
 *
 * <p>闭环：登记（待受理）→ 受理（调查中，按需封存病历）→ 调查/处理跟踪（处理中）
 * → 结案（终态，收口途径/责任/赔偿）或撤销（终态，原因必填）。
 */
public interface DisputeService {

    /**
     * 分页（keyword/caseType/status/level/deptId/openOnly/登记日期区间）
     */
    PageResult<DisputeCaseVO> listPage(DisputeQueryPageDTO dto);

    /**
     * 详情（主单 + 处理跟踪台账 + 按钮可用性 + 受理天数）
     */
    DisputeCaseVO getDetailById(Long id);

    /**
     * 登记 / 修改（仅待受理可改）
     */
    DisputeCaseVO caseUpsert(DisputeCaseUpsertDTO dto);

    /**
     * 受理（待受理→调查中；needSeal=1 时联动封存已归档病历）
     */
    DisputeCaseVO accept(DisputeActionDTO dto);

    /**
     * 登记处理跟踪（追加流水；toStatus=2/3 可推进，不得直接跳结案）
     */
    DisputeCaseVO follow(DisputeFollowDTO dto);

    /**
     * 补封存（受理时暂无已归档病历的单据，归档后回来补封）
     */
    DisputeCaseVO sealNow(DisputeActionDTO dto);

    /**
     * 结案（调查中/处理中→已结案；途径+责任+赔偿+结论四项必填）
     */
    DisputeCaseVO close(DisputeCloseDTO dto);

    /**
     * 撤销（非终态→已撤销；原因必填）
     */
    DisputeCaseVO revoke(DisputeActionDTO dto);

    /**
     * 删除（软删；仅待受理且无跟踪流水）
     */
    boolean deleteById(Long id);

    /**
     * 统计（服务端 group by 出，不让前端数当前页）
     *
     * @param dateFrom 登记起始 yyyy-MM-dd（可空）
     * @param dateTo   登记截止 yyyy-MM-dd（可空）
     */
    DisputeStatVO stat(String dateFrom, String dateTo);
}

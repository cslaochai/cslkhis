package com.his.patient.service;

import com.his.common.base.PageResult;
import com.his.patient.dto.DietConfirmDTO;
import com.his.patient.dto.DietPlanQueryPageDTO;
import com.his.patient.dto.DietPlanStopDTO;
import com.his.patient.dto.DietPlanUpsertDTO;
import com.his.patient.entity.BizInpatientOrder;
import com.his.patient.vo.DietPlanVO;
import com.his.patient.vo.DietTypeOptionVO;
import com.his.patient.vo.WardVO;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 膳食方案服务（sql/168 §2）。
 *
 * <p>方案有两条来路：① orderClass=10 的住院医嘱<b>校对通过时同事务派生</b>
 * （{@link #deriveFromOrder}），停嘱/作废同步跟随；② 营养师手工登记。
 * 订餐与营养科工作台都只读这一张表 —— 医嘱侧和膳食侧各存一份口径必然漂移。
 */
public interface DietPlanService {

    /**
     * 饮食类型下拉（不含 TO_DETERMINE 占位档）
     */
    List<DietTypeOptionVO> dietTypeOptions();

    /**
     * 病区下拉（参照数据：订餐台按病区批量生成餐单时选范围）。
     * <p>不配页面权限码 —— 病区名不是敏感信息，挂上 {@code ipd:diet:*} 会让别的岗位一进下拉就 403。
     */
    List<WardVO> wardOptions();

    PageResult<DietPlanVO> planListPage(DietPlanQueryPageDTO query);

    List<DietPlanVO> planListByAdmission(Long admissionId);

    /**
     * 手工登记 / 修改方案（类别、途径、默认餐次由服务端按饮食码带出）
     */
    DietPlanVO planUpsert(DietPlanUpsertDTO dto);

    /**
     * 营养科批量接收或退回（全成功或全不生效）
     */
    int planConfirm(DietConfirmDTO dto);

    /**
     * 手工停餐
     */
    DietPlanVO planStop(DietPlanStopDTO dto);

    /**
     * 删除误录方案（物理删，撞 uk_diet_plan_order）
     */
    int planDeleteById(Long id);

    // 医嘱链钩子（由 InpatientOrderService 同事务调用）

    /**
     * 医嘱校对通过 → 派生膳食方案。
     *
     * <p>幂等：同一 orderId 已有方案则原样返回（重复校对/重放不会多出第二条）。
     * 认不出饮食类型时落 {@code TO_DETERMINE} 占位，进营养科待接收队列，绝不静默跳过。
     *
     * @return 方案ID（新建或已存在）
     */
    Long deriveFromOrder(BizInpatientOrder order);

    /**
     * 医嘱停止 → 方案停止
     */
    void stopFromOrder(Long orderId, LocalDateTime stopTime, String reason);

    /**
     * 医嘱作废（撤销）→ 方案作废
     */
    void cancelFromOrder(Long orderId);
}

package com.his.patient.support;

import com.his.common.exception.BusinessException;
import com.his.common.util.TextUtil;
import com.his.patient.dto.InpatientOrderItemDTO;
import com.his.patient.enums.OrderClassEnum;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * 住院医嘱明细行的硬规则（开立医嘱与「保存为模板」共用同一份）。
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class InpatientOrderItemRules {

    /**
     * 医嘱类别合法区间（与字典 his_order_class、{@link OrderClassEnum} 同源）
     */
    public static final int ORDER_CLASS_MIN = 1;
    public static final int ORDER_CLASS_MAX = 10;

    /**
     * 医嘱类别 → 记账项目类型（`费用记账流水的项目类型`）。
     */
    public static int chargeItemTypeOf(Integer orderClass) {
        if (orderClass == null) {
            return 7;
        }
        return switch (orderClass) {
            case 1 -> 2;
            case 2 -> 5;
            case 3 -> 6;
            default -> 7;
        };
    }

    /**
     * 校验一条医嘱明细。药品医嘱必须给全「单次剂量 / 剂量单位 / 给药途径」——
     * 这三项缺一个，护士执行时只能靠猜或打电话问医生，是真实会被护理部扣分的项，
     * 不该由前端"提醒一下"了事。
     */
    public static void validate(InpatientOrderItemDTO item) {
        // C-非 web 入参：本共享校验器由开立医嘱 / 组套 / 模板三处在 service 内部直调明细行，不经 @RequestBody 绑定，
        // Bean Validation 不覆盖，保留（药品三项按医嘱类别条件必填，DTO 注解同样表达不了）
        if (item == null) {
            throw new BusinessException("医嘱明细存在空项");
        }
        if (!TextUtil.hasText(item.getItemName())) {
            throw new BusinessException("医嘱项目名称不能为空");
        }
        if (item.getOrderClass() == null) {
            throw new BusinessException("医嘱类别不能为空：" + item.getItemName());
        }
        if (item.getOrderClass() < ORDER_CLASS_MIN || item.getOrderClass() > ORDER_CLASS_MAX) {
            throw new BusinessException("医嘱类别不合法（应为 1~10，见「医嘱类别」字典）："
                    + item.getItemName() + "，当前值 " + item.getOrderClass());
        }
        if (OrderClassEnum.isDrug(item.getOrderClass())) {
            if (item.getDosage() == null) {
                throw new BusinessException("药品医嘱必须填写单次剂量：" + item.getItemName());
            }
            if (!TextUtil.hasText(item.getDosageUnit())) {
                throw new BusinessException("药品医嘱必须填写剂量单位：" + item.getItemName());
            }
            if (!TextUtil.hasText(item.getRoute())) {
                throw new BusinessException("药品医嘱必须填写给药途径：" + item.getItemName());
            }
        }
    }
}

package com.his.patient.support;

import com.his.common.exception.BusinessException;
import com.his.patient.dto.InpatientOrderItemDTO;
import com.his.patient.enums.OrderClassEnum;
import org.springframework.util.StringUtils;

/**
 * 住院医嘱明细行的硬规则（开立医嘱与「保存为模板」共用同一份）。
 *
 * <p><b>为什么模板保存也要跑这套校验</b>：模板是医嘱的半成品，如果允许存一条缺途径的药品明细，
 * 模板就成了绕过硬规则的后门 —— 错误只是从「保存时」推迟到「套用时」，而且那时医生已经点了提交，
 * 现场更难解释。校验口径必须单点，否则两处规则一旦漂移，就会出现"模板里合法、开立时被拒"。
 */
public final class InpatientOrderItemRules {

    /**
     * 医嘱类别合法区间（与字典 his_order_class、{@link OrderClassEnum} 同源）
     */
    public static final int ORDER_CLASS_MIN = 1;
    public static final int ORDER_CLASS_MAX = 10;

    private InpatientOrderItemRules() {
    }

    /**
     * 医嘱类别 → 记账项目类型（`费用记账流水的项目类型`）。
     *
     * <p><b>这是一处既有限制，不是设计选择</b>：本项目 `item_type` 只有
     * 「1挂号费 2西药 3中成药 4中药饮片 5检查 6检验 7治疗」七类，
     * <b>没有护理/床位/手术/输血/监护/临床营养类目</b>，所以 5~10 一律归到 7（治疗）。
     * 后果：靠 `item_type` 区分不了"护理费"和"治疗费"。真实 HIS 会有完整的收费项目类别字典，
     * 本项目要到补价表/项目类别字典（P3 日清单范围）时才能细化。
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
        // 保留（类别②非 web 入口入参）：这个共享校验器被开立医嘱 / 保存模板 / 组套三处在 service 内部
        // 直接调用（还包含服务端从其他来源拼出来的明细行），不经 @RequestBody 绑定，
        // Bean Validation 根本不会跑 —— 注解只挡得住其中一个入口，口径会漂移，只能留在这里。
        // 其中「药品三项（单次剂量 / 剂量单位 / 给药途径）」按医嘱类别条件必填（类别①），
        // 非药品明细不该被要求填途径，同样挂不了注解。
        if (item == null) {
            throw new BusinessException("医嘱明细存在空项");
        }
        if (!StringUtils.hasText(item.getItemName())) {
            throw new BusinessException("医嘱项目名称不能为空");
        }
        if (item.getOrderClass() == null) {
            throw new BusinessException("医嘱类别不能为空：" + item.getItemName());
        }
        if (item.getOrderClass() < ORDER_CLASS_MIN || item.getOrderClass() > ORDER_CLASS_MAX) {
            // 类目码是**封闭枚举**（字典 his_order_class，1~10），不是随手填的序号。
            // 放任越界码存进去有两处静默后果：① 列表渲染成「未知(55)」，护士看不懂；
            // ② chargeItemTypeOf 的 default 分支把 55 归成 7（治疗）去记账，
            //    等于一条谁都说不清是什么的医嘱照收治疗费 —— 四核对时对不上账。
            // 建库至今全库只有 1/2/3/4/9/10（2026-09-28 核对），加闸不会挡存量数据。
            throw new BusinessException("医嘱类别不合法（应为 1~10，见「医嘱类别」字典）："
                    + item.getItemName() + "，当前值 " + item.getOrderClass());
        }
        if (OrderClassEnum.isDrug(item.getOrderClass())) {
            if (item.getDosage() == null) {
                throw new BusinessException("药品医嘱必须填写单次剂量：" + item.getItemName());
            }
            if (!StringUtils.hasText(item.getDosageUnit())) {
                throw new BusinessException("药品医嘱必须填写剂量单位：" + item.getItemName());
            }
            if (!StringUtils.hasText(item.getRoute())) {
                throw new BusinessException("药品医嘱必须填写给药途径：" + item.getItemName());
            }
        }
    }
}

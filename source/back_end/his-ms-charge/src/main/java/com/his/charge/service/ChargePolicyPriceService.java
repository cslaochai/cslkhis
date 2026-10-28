package com.his.charge.service;

import com.his.charge.dto.PolicyPriceChangeDTO;
import com.his.charge.dto.PolicyPriceStatusDTO;
import com.his.charge.vo.PolicyPriceAdminVO;
import com.his.spi.vo.PolicyPriceVO;

import java.util.List;

/**
 * 收费政策价服务：运行时取价（急诊记账/复诊拆行/号源默认价）+ 价格管理页「政策价」维护。
 *
 * <p>价格权威在价表（sys_charge_policy / sys_charge_policy_price），每次实时查库不缓存，
 * 价格管理页调价即时生效。
 */
public interface ChargePolicyPriceService {

    /**
     * 运行时取价：政策级 + 档位级属性一次拿齐。
     *
     * @param policyCode 政策编码（ChargeFacadeService.POLICY_*）
     * @param tierCode   档位码（1-普通 2-副主任 3-主任 4-知名专家）
     * @return 价目单；null = 政策/档位缺行或停用，调用方自行降级
     */
    PolicyPriceVO policyPrice(String policyCode, Integer tierCode);

    /**
     * 管理端列表：全部政策 × 档位平铺（政策内按 sort_order 排）。
     */
    List<PolicyPriceAdminVO> listAdmin();

    /**
     * 调价：更新全额（可选加收额）+ 写调价历史（itemType=POLICY）。
     */
    PolicyPriceAdminVO changePrice(PolicyPriceChangeDTO changeDTO);

    /**
     * 档位启停：停用后运行时按「缺行」降级。
     */
    void changeStatus(PolicyPriceStatusDTO statusDTO);
}

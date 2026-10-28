package com.his.charge.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 收费政策价目（政策 × 档位的价格行）。
 *
 * <p>业务唯一键 policy_code + tier_code，物理删除（不设 del_flag）。
 * surcharge 只在「主项目 + 加收子码」结构下有值（门诊副主任及以上）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_charge_policy_price")
public class SysChargePolicyPrice extends BaseEntity {

    /** 政策编码（sys_charge_policy.policy_code） */
    private String policyCode;

    /** 档位码（对齐 ClinicDiagnosisTierEnum：1-普通 2-副主任 3-主任 4-知名专家） */
    private Integer tierCode;

    /** 档位名称 */
    private String tierLabel;

    /** 国家医保编码（门诊档位子码 011102020010000~003 / 急诊 110200003） */
    private String ybCode;

    /** 全额（元） */
    private BigDecimal amount;

    /** 加收额（元）：无加收为 null */
    private BigDecimal surcharge;

    /** 排序号 */
    private Integer sortOrder;

    /** 状态：0-停用 1-启用（停用后该档按缺行降级） */
    private Integer status;

    /** 备注 */
    private String remark;
}

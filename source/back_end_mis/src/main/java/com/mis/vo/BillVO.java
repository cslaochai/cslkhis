package com.mis.vo;

import com.mis.entity.MssBill;
import com.mis.entity.MssFeeItem;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;

/**
 * 结算单出参（含费用明细）
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BillVO extends MssBill implements Serializable {

    private List<MssFeeItem> items;
}

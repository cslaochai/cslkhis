package com.his.charge.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 扣款通知单详情（当前单据 + 全过程留痕，按时间正序）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DeductNoticeDetailVO extends DeductNoticeListVO {

    /**
     * 处理留痕（新建/申诉/结果/确认/缴回/作废）
     */
    private List<DeductLogVO> logs;
}

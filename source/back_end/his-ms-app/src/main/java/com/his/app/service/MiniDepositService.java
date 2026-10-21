package com.his.app.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.spi.dto.PrepayPageQueryDTO;
import com.his.spi.vo.PrepayBalanceVO;
import com.his.spi.vo.PrepayVO;
import com.his.app.vo.MiniAdmiSelectListVO;

import java.util.List;

/**
 * 患者端住院押金读侧：余额口径与院内一致（走 his-charge 账户 service 的流水累加）。
 */
public interface MiniDepositService {

    /**
     * 名下角色的住院记录列表。
     * 用于小程序「住院押金」页选择要查押金的住院单。
     */
    List<MiniAdmiSelectListVO> myAdmissions(Long patientId);

    /**
     * 单笔住院的押金余额（充值合计/退款合计/余额）。
     * 用于「住院押金」页选中住院单后的余额汇总展示。
     */
    PrepayBalanceVO balance(Long admissionId);

    /**
     * 押金收支流水分页。
     * 用于「住院押金」页流水明细分段加载。
     */
    IPage<PrepayVO> prepayListPage(PrepayPageQueryDTO queryDTO);
}

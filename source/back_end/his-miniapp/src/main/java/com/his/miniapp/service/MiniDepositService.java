package com.his.miniapp.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.charge.dto.PrepayQueryPageDTO;
import com.his.charge.vo.PrepayBalanceVO;
import com.his.charge.vo.PrepayVO;
import com.his.miniapp.vo.MiniAdmiSelectListVO;

import java.util.List;

/**
 * 患者端住院押金读侧：余额口径与院内一致（走 his-charge 账户 service 的流水累加）。
 */
public interface MiniDepositService {

    List<MiniAdmiSelectListVO> myAdmissions(Long patientId);

    PrepayBalanceVO balance(Long admissionId);

    IPage<PrepayVO> prepayListPage(PrepayQueryPageDTO query);
}

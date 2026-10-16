package com.his.patient.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.patient.dto.ReferralDTO;
import com.his.patient.vo.ReferralVO;

public interface ReferralService {

    ReferralVO create(ReferralDTO.Create dto);

    IPage<ReferralVO> listPage(ReferralDTO.QueryPage q);

    ReferralVO getDetailById(Long referralId);

    ReferralVO audit(ReferralDTO.Audit dto);

    ReferralVO finish(ReferralDTO.Finish dto);

    ReferralVO cancel(ReferralDTO.Cancel dto);

    int escalatePendingToDuty();
}

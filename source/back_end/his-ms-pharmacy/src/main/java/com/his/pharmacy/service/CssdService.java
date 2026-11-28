package com.his.pharmacy.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.pharmacy.dto.CssdDTO;
import com.his.pharmacy.dto.CssdPackApplyDTO;
import com.his.pharmacy.dto.CssdPackConfirmDTO;
import com.his.pharmacy.dto.CssdPackMyPageQueryDTO;
import com.his.pharmacy.dto.CssdPackSignDTO;
import com.his.pharmacy.vo.CssdPackVO;

public interface CssdService {

    CssdPackVO receive(CssdDTO.Receive dto);

    CssdPackVO advance(CssdDTO.Advance dto);

    IPage<CssdPackVO> listPage(CssdDTO.PageQuery q);

    CssdPackVO getDetailById(Long packId);

    /** 临床科室发起回收申请（落 0-待回收），供应室列表可见 */
    CssdPackVO apply(CssdPackApplyDTO dto);

    /** 供应室确认回收（0-待回收 → 1-已回收） */
    CssdPackVO receiveConfirm(CssdPackConfirmDTO dto);

    /** 申领科室签收（6-已发放 → 7-已签收） */
    CssdPackVO sign(CssdPackSignDTO dto);

    /** 当前岗位科室的器械包（待回收 / 待签收 / 已签收） */
    IPage<CssdPackVO> myListPage(CssdPackMyPageQueryDTO q);
}

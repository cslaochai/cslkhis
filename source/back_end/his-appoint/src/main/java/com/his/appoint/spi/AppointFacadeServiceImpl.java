package com.his.appoint.spi;

import com.his.appoint.entity.AppointInfo;
import com.his.appoint.service.AppointService;
import com.his.spi.dto.AppointUpsertDTO;
import com.his.spi.dto.AppointQueryDTO;
import com.his.spi.dto.RevisitFeePreviewDTO;
import com.his.common.base.PageResult;
import com.his.spi.vo.BizAppointInfoListVO;
import com.his.spi.vo.RevisitFeePreviewVO;
import com.his.spi.vo.RevisitRecordSelectVO;
import com.his.spi.service.AppointFacadeService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 挂号编排端口实现：建单、退号、复诊预估等编排动作全部下沉 {@code AppointService}，本类只做契约转接。
 */
@Service
@RequiredArgsConstructor
public class AppointFacadeServiceImpl implements AppointFacadeService {

    private final AppointService appointService;

    @Override
    public BizAppointInfoListVO addAppoint(AppointUpsertDTO dto) {
        return toListVO(appointService.addAppoint(dto));
    }

    @Override
    public boolean cancelRegist(Long id, String reason) {
        return appointService.cancelRegist(id, reason);
    }

    @Override
    public PageResult<BizAppointInfoListVO> listRegistPage(AppointQueryDTO query) {
        return appointService.listPage(query);
    }

    @Override
    public RevisitFeePreviewVO revisitFeePreview(RevisitFeePreviewDTO dto) {
        return appointService.revisitFeePreview(dto);
    }

    @Override
    public List<RevisitRecordSelectVO> revisitRecordSelectList(Long patientId, int limit) {
        return appointService.revisitRecordSelectList(patientId, limit);
    }

    private BizAppointInfoListVO toListVO(AppointInfo entity) {
        if (entity == null) {
            return null;
        }
        BizAppointInfoListVO vo = new BizAppointInfoListVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }
}

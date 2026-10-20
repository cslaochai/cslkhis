package com.his.spi.service;

import com.his.common.base.PageResult;
import com.his.spi.dto.AppointQueryDTO;
import com.his.spi.dto.AppointUpsertDTO;
import com.his.spi.dto.RevisitFeePreviewDTO;
import com.his.spi.vo.BizAppointInfoListVO;
import com.his.spi.vo.RevisitFeePreviewVO;
import com.his.spi.vo.RevisitRecordSelectVO;

import java.util.List;

/**
 * 挂号编排端口：建单、退号、复诊预估这类要动号源/收费/状态机的编排动作对外入口。
 * <p>
 * 只读挂号/队列/急诊事实请走 {@link RegistFacadeService}，排队动作请走 {@link QueueFacadeService}
 * ——编排实现牵本模块服务图，纯读取方依赖它会与调用方构成 Bean 环。
 */
public interface AppointFacadeService {

    /**
     * 患者挂号（预约池扣号 + 收费策略判定 + 判重 + 建挂号费账单，全在提供方口径内）。
     *
     * @return 落库后的挂号详情
     */
    BizAppointInfoListVO addAppoint(AppointUpsertDTO dto);

    /**
     * 退号（含退款编排），返回是否成功。
     *
     * @param id     挂号 ID
     * @param reason 退号原因
     */
    boolean cancelRegist(Long id, String reason);

    /**
     * 挂号分页查询（患者端「我的预约」用；归属校验归调用方）
     */
    PageResult<BizAppointInfoListVO> listRegistPage(AppointQueryDTO query);

    /**
     * 复诊费用预估（按复诊收费策略，只读）
     */
    RevisitFeePreviewVO revisitFeePreview(RevisitFeePreviewDTO dto);

    /**
     * 复诊「原病历」候选：按患者取最近若干次真正就诊过的病历
     */
    List<RevisitRecordSelectVO> revisitRecordSelectList(Long patientId, int limit);
}

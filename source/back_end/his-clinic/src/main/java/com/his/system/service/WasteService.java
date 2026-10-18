package com.his.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.system.dto.WasteDTO;
import com.his.system.vo.WasteVO;

/** 医疗废物登记服务：登记 → 交接 → 处置三态闭环与交接单号生成的业务收口。 */
public interface WasteService {

    /**
     * 医废登记：生成当日顺延的交接单号并落「已登记」态，收集人缺省取当前登录人。
     * 用于医废管理页查询卡右侧「医废登记」弹框提交。
     */
    WasteVO create(WasteDTO.Create dto);

    /**
     * 医废交接：登记交接人与交接时刻，状态由已登记推进到已交接，此后不可逆。
     * 用于医废管理页行内「交接」弹框确认。
     */
    WasteVO handover(WasteDTO.Handover dto);

    /**
     * 处置确认：登记处置公司与处置时刻，状态推进到已处置，闭环结束。
     * 用于医废管理页行内「处置确认」弹框。
     */
    WasteVO dispose(WasteDTO.Dispose dto);

    /**
     * 删除医废登记：仅未交接的登记可删，已交接的单子是与处置公司的对外凭证。
     * 用于医废管理页「已登记」行的删除操作。
     */
    void deleteById(Long id);

    /**
     * 医废登记分页：按单号或产生科室关键字、医废类别、状态与收集日期区间收口，日期按整天边界取。
     * 用于医废管理页查询卡与表格列表。
     */
    IPage<WasteVO> listPage(WasteDTO.PageQuery q);
}

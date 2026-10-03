package com.his.patient.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.patient.dto.OrderSetQueryPageDTO;
import com.his.patient.dto.OrderSetUpsertDTO;
import com.his.patient.vo.OrderSetDetailVO;
import com.his.patient.vo.OrderSetListVO;
import com.his.patient.vo.OrderSetSelectListVO;

import java.util.List;

/**
 * 医嘱组套模板（个人 / 科室 / 全院三级共享，sql/142）。
 *
 * <p>与个人模板（{@link InpatientOrderTemplateService}）共用主表与明细表，
 * 差别只在 {@code scope}：个人模板是「我自己攒的快捷方式」，组套是「科室/全院统一口径的一套医嘱」
 * （如术前常规、雾化套餐）。两套数据放一张表，是为了让套用侧只有一个来源 ——
 * 医生在开立弹窗里不该先判断「我要点哪个按钮」。
 *
 * <p><b>可见与可写是两件事</b>：可见 = 全院 ∪ 本科室 ∪ 本人；可写 = 全院级凭权限码、
 * 科室级限本科室、个人级限本人。列表出参带 {@code editable}，前端据此决定按钮显隐，
 * 不做「点了才报错」的体验。
 */
public interface InpatientOrderSetService {

    /**
     * 新增/修改组套，返回组套ID
     */
    Long upsert(OrderSetUpsertDTO dto);

    /**
     * 组套明细（含明细行），编辑回显与预览共用
     */
    OrderSetDetailVO getDetailById(Long id);

    /**
     * 组套分页（管理页，只落在可见集内）
     */
    IPage<OrderSetListVO> listPage(OrderSetQueryPageDTO query);

    /**
     * 组套下拉候选（开立弹窗「套用组套」，可见集全量，有上限）
     */
    List<OrderSetSelectListVO> selectList();

    /**
     * 删除组套（主表逻辑删 + 明细物理删；不影响已按它开出的医嘱）
     */
    void deleteById(Long id);
}

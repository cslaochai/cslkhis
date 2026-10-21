package com.his.inpatient.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.inpatient.dto.OrderSetPageQueryDTO;
import com.his.inpatient.dto.OrderSetUpsertDTO;
import com.his.inpatient.vo.OrderSetDetailVO;
import com.his.inpatient.vo.OrderSetListVO;
import com.his.inpatient.vo.OrderSetSelectListVO;

import java.util.List;

/**
 * 医嘱组套模板（个人 / 科室 / 全院三级共享，sql/142）。
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
    IPage<OrderSetListVO> listPage(OrderSetPageQueryDTO query);

    /**
     * 组套下拉候选（开立弹窗「套用组套」，可见集全量，有上限）
     */
    List<OrderSetSelectListVO> selectList();

    /**
     * 删除组套（主表逻辑删 + 明细物理删；不影响已按它开出的医嘱）
     */
    void deleteById(Long id);
}

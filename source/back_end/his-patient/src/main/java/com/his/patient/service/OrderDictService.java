package com.his.patient.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.patient.dto.OrderDictQueryPageDTO;
import com.his.patient.dto.OrderDictUpsertDTO;
import com.his.patient.vo.OrderDictListVO;

import java.util.List;

/**
 * 医嘱基础字典：给药途径 / 用药频次 / 剂量单位（sql/142）。
 */
public interface OrderDictService {

    /**
     * 字典分页（管理页按类型分 Tab）
     */
    IPage<OrderDictListVO> listPage(OrderDictQueryPageDTO query);

    /**
     * 启用的字典项（下拉用，按排序升序；与前端字典缓存同一份数据）
     */
    List<OrderDictListVO> selectList(String dictType);

    /**
     * 新增/修改字典项，返回字典数据ID
     */
    Long upsert(OrderDictUpsertDTO dto);

    /**
     * 删除字典项（逻辑删；被医嘱引用的值也能删，历史医嘱仍按原值渲染文案）
     */
    void deleteById(Long id, String dictType);
}

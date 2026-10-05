package com.his.patient.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.patient.dto.OrderDictQueryPageDTO;
import com.his.patient.dto.OrderDictUpsertDTO;
import com.his.patient.vo.OrderDictListVO;

import java.util.List;

/**
 * 医嘱基础字典：给药途径 / 用药频次 / 剂量单位（sql/142）。
 *
 * <p>这三类此前是前端硬编码（{@code src/lib/drugUsage.js}），库里查不到字典，
 * 结果就是医生站与护士站各按各的常量渲染 —— 库里真实出现过「静脉泵入」而前端没有，
 * Nurses 看到的值和医生选的值从此对不上。现在统一由本服务读写字典表，页面下拉从字典取。
 *
 * <p>两条刻意收得很紧的约束：
 * <ol>
 *   <li><b>只认三种 dictType</b>：这个口子不能改别的字典（那是 system:dict:* 的领地）。</li>
 *   <li><b>值不可改、只可停用</b>：值被存量医嘱行引用，改值等于让历史医嘱渲染成「未知(xxx)」。</li>
 * </ol>
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

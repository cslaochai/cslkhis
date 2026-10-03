package com.his.pharmacy.service;

import com.his.common.base.PageResult;
import com.his.pharmacy.dto.SupplierQueryPageDTO;
import com.his.pharmacy.dto.SupplierUpsertDTO;
import com.his.pharmacy.vo.SysSupplierSelectListVO;
import com.his.pharmacy.vo.SysSupplierVO;

import java.util.List;

/**
 * 供应商服务
 */
public interface SupplierService {

    /** 分页查询 */
    PageResult<SysSupplierVO> page(SupplierQueryPageDTO queryDTO);

    /** 下拉选择（只返回启用中的供应商） */
    List<SysSupplierSelectListVO> selectList();

    /**
     * 详情
     *
     * ⚠ 方法名刻意**不叫 getById**：MP 的 {@code IService} 已经有 {@code getById(Serializable)} 返回实体，
     * 再声明一个 {@code getById(Long)} 就在同一接口里构成重载 —— Service 实现里写
     * {@code this.getById(supplierId)} 会精确匹配到这个返回 VO 的方法，
     * 编译器报"VO 无法转换为实体"，而错误位置看起来像是 MP 出了问题。改名彻底消除歧义。
     */
    SysSupplierVO getDetailById(Long supplierId);

    /** 新增/修改（返回主键） */
    Long upsert(SupplierUpsertDTO dto);

    /** 删除（被采购订单引用则拒绝） */
    void deleteById(Long supplierId);
}

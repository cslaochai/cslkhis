package com.his.pharmacy.service;

import com.his.pharmacy.dto.BizDrugPackageUpsertDTO;
import com.his.pharmacy.vo.BizDrugPackageVO;

import java.util.List;

/**
 * 医生个人药品/耗材套餐服务（套餐头 + 条目明细整体保存/删除）。
 */
public interface DrugPackageService {

    /**
     * 当前登录医生名下的套餐清单（按创建时间倒序）。
     */
    List<BizDrugPackageVO> listDrugPackages();

    /**
     * 按套餐主键取全套条目明细；不存在返回 {@code null}。
     */
    BizDrugPackageVO getDrugPackageDetail(Long packageId);

    /**
     * 新建套餐：套餐头与条目明细一并落库，只新增不改单。
     */
    boolean saveDrugPackage(BizDrugPackageUpsertDTO upsertDTO);

    /**
     * 删除套餐：连名下条目明细一起清掉。
     */
    boolean deleteDrugPackage(Long id);
}

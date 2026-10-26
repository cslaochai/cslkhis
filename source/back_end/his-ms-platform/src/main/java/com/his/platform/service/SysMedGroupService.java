package com.his.platform.service;

import com.his.common.base.PageResult;
import com.his.platform.dto.MedGroupQueryDTO;
import com.his.platform.dto.MedGroupUpsertDTO;
import com.his.platform.vo.MedGroupVO;

import java.util.List;

/**
 * 医疗组 / 主诊组服务。
 *
 * <p>医疗组挂在科室下，是三级查房与主诊医师负责制的组织落点
 * （《三级医院评审标准(2022年版)实施细则》2.2.27）。
 */
public interface SysMedGroupService {

    /**
     * 分页查询医疗组。
     */
    PageResult<MedGroupVO> listPage(MedGroupQueryDTO queryDTO);

    /**
     * 按条件取全部医疗组（不分页）。
     */
    List<MedGroupVO> list(MedGroupQueryDTO queryDTO);

    /**
     * 医疗组下拉：传 deptId 时只返回该科室的医疗组。
     */
    List<MedGroupVO> selectList(Long deptId);

    /**
     * 取医疗组详情。
     */
    MedGroupVO getInfo(Long id);

    /**
     * 新增或修改医疗组。
     */
    String upsert(MedGroupUpsertDTO upsertDTO);

    /**
     * 删除医疗组。组内还有医师时拦下。
     */
    void delete(Long id);
}

package com.his.system.service;

import com.his.common.base.PageResult;
import com.his.system.dto.DepartmentQueryDTO;
import com.his.system.dto.DepartmentSelectDTO;
import com.his.system.dto.DepartmentUpsertDTO;
import com.his.system.vo.DepartmentSelectListVO;
import com.his.system.vo.DepartmentVO;

import java.util.List;

public interface SysDepartmentService {

    List<DepartmentVO> tree();

    PageResult<DepartmentVO> listPage(DepartmentQueryDTO queryDTO);

    List<DepartmentVO> list(DepartmentQueryDTO queryDTO);

    /**
     * 科室下拉。数据范围默认按当前人收口，只有显式索取全部时才放开。
     *
     * <p>收口失败的方向必须是「看不到」而不是「看到太多」，所以授权集合为空时返回空列表，
     * 不退化成全院。
     */
    List<DepartmentSelectListVO> selectList(DepartmentSelectDTO selectDTO);

    DepartmentVO getInfo(Long deptId);

    /**
     * @return 面向用户的操作结果文案（新增/修改两条路文案不同）
     */
    String upsert(DepartmentUpsertDTO upsertDTO);

    void delete(Long deptId);
}

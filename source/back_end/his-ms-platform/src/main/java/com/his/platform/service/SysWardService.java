package com.his.platform.service;

import com.his.common.base.PageResult;
import com.his.platform.dto.WardQueryDTO;
import com.his.platform.dto.WardUpsertDTO;
import com.his.platform.vo.WardVO;

import java.util.List;

/**
 * 病区主数据服务：病区与科室的多对多关系维护。
 */
public interface SysWardService {

    /**
     * 分页查询病区。deptId 过滤走多对多（含跨科收治的病区），不只本位科室。
     */
    PageResult<WardVO> listPage(WardQueryDTO queryDTO);

    /**
     * 按条件取全部病区（不分页），供下拉与对账使用。
     */
    List<WardVO> list(WardQueryDTO queryDTO);

    /**
     * 病区下拉：传 deptId 时返回该科室可收治的病区（多对多口径）；不传返回全部启用的病区。
     */
    List<WardVO> selectList(Long deptId);

    /**
     * 取病区详情，含收治科室清单。
     */
    WardVO getInfo(Long wardId);

    /**
     * 新增或修改病区，同时维护收治科室集合（多对多）。
     *
     * @return 操作结果提示
     */
    String upsert(WardUpsertDTO upsertDTO);

    /**
     * 删除病区。有病床、有在院患者或有人挂在该病区时拦下。
     */
    void delete(Long wardId);
}

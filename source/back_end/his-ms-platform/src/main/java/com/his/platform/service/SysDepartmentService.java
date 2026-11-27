package com.his.platform.service;

import com.his.common.base.PageResult;
import com.his.platform.dto.DepartmentQueryDTO;
import com.his.platform.dto.DepartmentSelectDTO;
import com.his.platform.dto.DepartmentUpsertDTO;
import com.his.platform.vo.DepartmentTreeSelectVO;
import com.his.platform.vo.DepartmentVO;

import java.util.List;

/**
 * 科室档案服务：全院组织架构树、科室编码生成与层级归属的业务收口。
 */
public interface SysDepartmentService {

    /**
     * 科室树：从顶级往下给出完整层级，每个节点带子节点。
     * 用于「科室管理」「部门管理」页的树形表格，也用于诊室、员工、用户表单挑上级归属。
     */
    List<DepartmentVO> tree();

    /**
     * 科室分页：按名称模糊、科室类型（多值类型命中其一即算）筛选。
     * 用于「科室管理」页查询卡与表格。
     */
    PageResult<DepartmentVO> listPage(DepartmentQueryDTO queryDTO);

    /**
     * 科室平铺列表（不分页）：与分页查询同一套筛选条件，不带层级。
     * 供要一次拿全量科室行、不做分页的取数场景使用。
     */
    List<DepartmentVO> list(DepartmentQueryDTO queryDTO);

    /**
     * 科室树形下拉：按当前人数据范围收口，不可选节点保留在树上做分组，逐节点标注 disabled 与原因。
     *
     * <p>选择模式由 {@code selectDTO.getMode()} 决定（LEAF/TREE/ORG），节点可选规则在服务端算好，
     * 前端只按出参 disabled 渲染。
     */
    List<DepartmentTreeSelectVO> selectTree(DepartmentSelectDTO selectDTO);

    /**
     * 单条科室详情：按主键取全量字段并带出负责人姓名。
     * 用于「部门管理」页的详情查看与修改弹框回显。
     */
    DepartmentVO getInfo(Long deptId);

    /**
     * 新增/修改科室：顶级节点的上级归属不许被改动，其余情况按传入父节点调整层级。
     * 用于科室与部门管理页的新增/修改弹框保存。
     *
     * @return 面向用户的操作结果文案（新增/修改两条路文案不同）
     */
    String upsert(DepartmentUpsertDTO upsertDTO);

    /**
     * 删除科室：下面还挂着子科室、或本身是顶级节点时直接拒。
     * 用于科室与部门管理页操作列的删除，避免组织架构被删成断枝。
     */
    void delete(Long deptId);
}

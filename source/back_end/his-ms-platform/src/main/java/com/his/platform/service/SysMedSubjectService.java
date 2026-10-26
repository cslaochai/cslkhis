package com.his.platform.service;

import com.his.platform.dto.DeptSubjectMapUpsertDTO;
import com.his.platform.dto.MedSubjectUpsertDTO;
import com.his.platform.vo.DeptSubjectVO;
import com.his.platform.vo.MedSubjectVO;

import java.util.List;

/**
 * 诊疗科目与科室映射服务。
 *
 * <p>业务背景：病案首页、卫生统计直报、医保结算清单要求填「科室」时，用的是国家卫健委
 * 《医疗机构诊疗科目名录》的**诊疗科目代码**，不是本院自编的 dept_code。本服务维护
 * 科目主数据以及「院内科室 → 诊疗科目」的对照关系。
 */
public interface SysMedSubjectService {

    /**
     * 科目树（一级科目 + children 二级），带「已映射科室数」。含停用科目，由前端按 status 过滤。
     */
    List<MedSubjectVO> listTree();

    /**
     * 新增或修改科目。传主键即改、不传即建。
     */
    String upsertSubject(MedSubjectUpsertDTO upsertDTO);

    /**
     * 删除科目。被科室映射引用、或下级还有二级科目时拦下。
     */
    void deleteSubject(Long id);

    /**
     * 取某科室的科目对照（含主科目）。
     */
    DeptSubjectVO getDeptMap(Long deptId);

    /**
     * 反向查询：某科目被哪些科室用（只取 is_primary=1，即把它当上报主科目的科室）。
     */
    List<DeptSubjectVO> listDeptMapBySubject(String subjectCode);

    /**
     * 保存某科室的科目映射（整单替换，同科室只保留一条主科目）。
     */
    String saveDeptMap(DeptSubjectMapUpsertDTO upsertDTO);

    /**
     * 还没配科目的**临床**科室（门诊/住院/急诊类型，且不是有下级的分组目录节点）。
     *
     * <p>病案首页上报取不到科目就是这个清单里的科室，属于数据治理待办。
     */
    List<DeptSubjectVO> listUnmappedDepts();
}

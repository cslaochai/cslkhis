package com.his.system.service;

import com.his.system.dto.EmployeeQualificationBatchUpsertDTO;
import com.his.system.dto.EmployeeQualificationUpsertDTO;
import com.his.system.vo.EmployeeQualificationVO;

import java.util.List;

/**
 * 员工资格证书服务（一人多证，见 sql/112）。
 */
public interface EmployeeQualificationService {

    /**
     * 某员工的证书列表（按类型、发证日期排序）
     */
    List<EmployeeQualificationVO> listByEmployee(Long employeeId);

    /**
     * 新增或修改一本证书。
     * 同（员工, 类型, 编号）唯一，重复提交直接业务异常，不依赖撞库报 500。
     */
    void upsert(EmployeeQualificationUpsertDTO upsertDTO);

    /**
     * 批量保存一个人的多本证书：整批一个事务，任一条撞车（重复/不存在）整批回滚，
     * 报错带上是哪一本，避免「存了一半」没人知道。
     */
    void batchUpsert(EmployeeQualificationBatchUpsertDTO batchDTO);

    /**
     * 删除一本证书（本表无 del_flag，物理删）
     */
    void deleteById(Long id);
}

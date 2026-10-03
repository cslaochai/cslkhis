package com.his.operation.service;

import com.his.operation.dto.OperationRoomUpsertDTO;
import com.his.operation.vo.OperationRoomVO;

import java.util.List;

/**
 * 手术间主数据服务（sql/134）—— 排台总表的台位来源。
 */
public interface OperationRoomService {

    /** 全部手术间（含停用，按 sortOrder 升序；手术间管理表格用） */
    List<OperationRoomVO> listAll();

    /** 启用中的手术间（排台候选下拉用） */
    List<OperationRoomVO> selectEnabled();

    /** 新增 / 修改手术间（编码与名称全局唯一），返回ID字符串 */
    String upsert(OperationRoomUpsertDTO dto);

    /** 物理删除手术间（唯一键不含 del_flag，软删占键；历史申请单是文本快照不受影响） */
    void deleteById(Long roomId);
}

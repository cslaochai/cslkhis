package com.his.patient.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.his.patient.dto.PatientTagBatchUpsertDTO;
import com.his.patient.dto.PatientTagUpsertDTO;
import com.his.patient.dto.PatientTagDelDTO;
import com.his.patient.entity.BizPatientTagRelation;
import com.his.system.vo.SysPatientTagVO;

import java.util.List;
import java.util.Map;

public interface BizPatientTagRelationService extends IService<BizPatientTagRelation> {

    /** 绑定了某标签的患者ID 列表（用于列表页按标签过滤） */
    List<Long> listPatientIdsByTagId(Long tagId);

    /** 查询单个患者的标签列表 */
    List<SysPatientTagVO> listTagsByPatientId(Long patientId);

    /**
     * 批量取「患者 → 标签」映射；无标签的患者不会出现在 map 里。
     */
    Map<Long, List<SysPatientTagVO>> mapTagsByPatientIds(List<Long> patientIds);

    /** 给患者添加标签；已存在时抛业务异常 */
    void addTag(PatientTagUpsertDTO tagDTO);

    /** 移除患者的标签 */
    void deleteTag(PatientTagDelDTO delDTO);

    /** 批量给患者添加标签（已存在的跳过） */
    void batchAdd(PatientTagBatchUpsertDTO batchDTO);
}

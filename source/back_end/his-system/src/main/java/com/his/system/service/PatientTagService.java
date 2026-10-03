package com.his.system.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.his.common.base.PageResult;
import com.his.system.dto.SysPatientTagQueryDTO;
import com.his.system.dto.SysPatientTagUpsertDTO;
import com.his.system.entity.SysPatientTag;
import com.his.system.vo.SysPatientTagVO;

import java.util.List;

public interface PatientTagService extends IService<SysPatientTag> {

    /** 患者标签分页 */
    PageResult<SysPatientTagVO> queryTagPage(SysPatientTagQueryDTO queryDTO);

    /** 患者标签列表（不分页） */
    List<SysPatientTagVO> queryTagList(SysPatientTagQueryDTO queryDTO);

    /** 单个标签详情 */
    SysPatientTagVO getTagInfo(Long tagId);

    /** 新增或修改标签，返回结果文案 */
    String upsertTag(SysPatientTagUpsertDTO upsertDTO);

    /** 删除标签 */
    void removeTag(Long tagId);
}

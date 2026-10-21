package com.his.inpatient.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.inpatient.dto.DischargeDrugDTO;
import com.his.inpatient.vo.DischargeDrugSelectListVO;
import com.his.inpatient.vo.DischargeDrugVO;

import java.util.List;

/**
 * 出院带药服务：出院带药单的开具修改、药房发药与删除的业务收口。
 */
public interface DischargeDrugService {

    /**
     * 开具或修改出院带药单：仅待发药状态可改。
     * 用于「出院带药」页新增与修改弹框提交。
     */
    DischargeDrugVO upsert(DischargeDrugDTO.Upsert dto);

    /**
     * 带药单分页：按住院次、患者、药品名称与发药状态筛。
     * 用于「出院带药」页查询卡。
     */
    IPage<DischargeDrugVO> listPage(DischargeDrugDTO.PageQuery q);

    /**
     * 一次住院名下的全部带药单。
     * 供出院结算与发药前按患者核对带药清单的环节取数。
     */
    List<DischargeDrugSelectListVO> listByAdmission(Long admissionId);

    /**
     * 单条带药单详情：用法用量与发药结果。
     * 供发药与核对环节按单据回看明细时取数。
     */
    DischargeDrugVO getDetailById(Long id);

    /**
     * 批量发药：把勾选的带药单一次性推进为已发药，状态单向不可回退。
     * 用于「出院带药」页勾选多行后的批量发药。
     */
    List<DischargeDrugVO> dispense(DischargeDrugDTO.Dispense dto);

    /**
     * 删除带药单：仅待发药状态可删。
     * 用于「出院带药」页行内删除。
     */
    void deleteById(Long id);
}

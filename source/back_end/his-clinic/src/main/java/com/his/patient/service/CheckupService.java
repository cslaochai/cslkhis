package com.his.patient.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.patient.dto.CheckupDTO;
import com.his.patient.vo.CheckupVO;

/**
 * 体检服务：体检套餐维护与体检登记、结果录入、总检出报告的业务收口。
 */
public interface CheckupService {

    /**
     * 新增或修改体检套餐，并按本次提交整单替换套餐项目明细。
     * 用于「体检套餐」页新增与修改弹框提交。
     */
    CheckupVO.PackageVO savePackage(CheckupDTO.PackageSave dto);

    /**
     * 体检套餐分页：按名称关键字与启用状态筛。
     * 用于「体检套餐」页查询卡，登记体检时选套餐也取这份清单。
     */
    IPage<CheckupVO.PackageVO> packagePage(CheckupDTO.PackageQuery dto);

    /**
     * 单条体检套餐详情（含项目明细）。
     * 用于「体检套餐」页修改与详情弹框回显。
     */
    CheckupVO.PackageVO getPackage(Long id);

    /**
     * 停用体检套餐：停用后不再出现在登记选单里。
     * 用于「体检套餐」页行内停用。
     */
    void disablePackage(Long id);

    /**
     * 体检登记：按所选套餐预生成待录入的结果空行。
     * 用于「体检管理」页新增登记弹框提交。
     */
    CheckupVO.RecordVO createRecord(CheckupDTO.RecordCreate dto);

    /**
     * 体检登记分页：按患者、人员类别、登记状态筛。
     * 用于「体检管理」页查询卡。
     */
    IPage<CheckupVO.RecordVO> recordPage(CheckupDTO.RecordQuery dto);

    /**
     * 读体检登记与其结果明细，不改动单据状态。
     * 供出总检报告前核对全部明细结果的环节取数。
     */
    CheckupVO.RecordVO getRecord(Long id);

    /**
     * 录入或修改单个项目的体检结果（结论、数值与异常标记）。
     * 用于「体检管理」页详情弹框的逐项结果录入。
     */
    CheckupVO.ResultVO saveResult(CheckupDTO.ResultSave dto);

    /**
     * 总检并出具体检报告：写入总检结论与报告建议。
     * 用于「体检管理」页总检弹框提交。
     */
    CheckupVO.RecordVO conclude(CheckupDTO.Conclusion dto);

    /**
     * 开始体检：把已登记的单推进到检查中。
     * 用于「体检管理」页行内开始体检。
     */
    void startCheckup(Long recordId);

    /**
     * 删除体检登记：仅尚未开始检查的单可删。
     * 用于「体检管理」页行内删除。
     */
    void deleteRecord(Long recordId);

    /**
     * 读体检登记，并在全部明细结果录完时现算「已完成」状态落库。
     * 用于「体检管理」页详情弹框取数时顺带收口进度。
     */
    CheckupVO.RecordVO refreshFinishStatus(Long recordId);
}

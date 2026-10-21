package com.his.clinical.service;

import com.his.common.base.PageResult;
import com.his.clinical.dto.SingleDiseaseDTO;
import com.his.clinical.vo.SingleDiseaseAutoEnrollStatVO;
import com.his.clinical.vo.SingleDiseaseVO;

import java.util.List;

/**
 * 单病种质控服务：病种目录、病例纳入、质控判级、上报打标与病种指标的业务收口。
 */
public interface SingleDiseaseService {

    /**
     * 病种目录清单：按病种编码升序，每个病种带上已纳入例数。
     * 用于「单病种质控」页「病种目录」页签的表格与「手工纳入」弹框的病种下拉。
     */
    List<SingleDiseaseVO.Disease> diseaseList();

    /**
     * 病种目录新增与修改：病种编码唯一，ICD-10 前缀归一（去空格、去空段、统一大写）后落库。
     * 用于「单病种质控」页「病种目录」页签的「病种维护」弹框保存。
     */
    SingleDiseaseVO.Disease diseaseUpsert(SingleDiseaseDTO.DiseaseUpsert dto);

    /**
     * 删除病种目录项：已纳入病例的病种拒绝删除，配置表无留档价值故物理删。
     * 用于「单病种质控」页「病种目录」页签行上的删除。
     */
    void diseaseDelete(Long id);

    /**
     * 手工纳入病例：拉住院病案首页做快照，校验主诊断命中该病种 ICD 前缀且同病种同住院不重复入组。
     * 用于「单病种质控」页的「手工纳入病例」弹框保存。
     */
    SingleDiseaseVO.Case enroll(SingleDiseaseDTO.Enroll dto);

    /**
     * 自动扫描纳入：按病种 ICD 前缀扫指定出院区间内的病案首页，逐份纳入未入组病例，返回扫描/纳入/跳过计数。
     * 用于「单病种质控」页「病种目录」行上的自动扫描操作，单份失败只跳过不带崩整批。
     */
    SingleDiseaseAutoEnrollStatVO autoEnroll(SingleDiseaseDTO.AutoEnroll dto);

    /**
     * 质控判级：按首页完整性出通过或异常并记异常项，死亡病例的疗效判定必须是死亡；已上报的病例不许改判。
     * 用于「单病种质控」页病例列表行的「质控判级」弹框保存。
     */
    SingleDiseaseVO.Case qc(SingleDiseaseDTO.Qc dto);

    /**
     * 上报打标：只有质控通过的病例可置为已上报并记上报时间，重复上报直接拒。
     * 用于「单病种质控」页病例列表行的上报操作。
     */
    SingleDiseaseVO.Case report(Long id);

    /**
     * 纳入病例分页：按病种、质控状态、上报状态与患者/病例号关键字筛选。
     * 用于「单病种质控」页「病例列表」页签的查询卡与表格。
     */
    PageResult<SingleDiseaseVO.Case> casePage(SingleDiseaseDTO.CaseQuery query);

    /**
     * 病种指标：逐病种由服务端按纳入病例复算纳入例数、治愈率、死亡率、平均住院日与平均费用。
     * 用于「单病种质控」页「病种指标」页签的指标表。
     */
    List<SingleDiseaseVO.Metric> metrics();
}

package com.his.emr.service;

import com.his.common.base.PageResult;
import com.his.emr.dto.AmpouleReturnDTO;
import com.his.emr.dto.NarcoticRegisterQueryPageDTO;
import com.his.emr.entity.BizDrugDispensing;
import com.his.emr.vo.BizNarcoticRegisterVO;
import com.his.emr.vo.NarcoticPrecheckVO;
import com.his.emr.vo.NarcoticRegisterCountVO;
import com.his.emr.vo.NarcoticViolationVO;

import java.util.List;
import java.util.Map;

/**
 * 麻精药品特殊管理服务（G10）。
 */
public interface NarcoticControlService {

    /**
     * 限量档位：该药品每张处方最多可开的**天数**。
     *
     * <ul>
     *   <li>麻醉药品 / 第一类精神药品：注射剂 1 日、控缓释制剂 7 日、其他剂型 3 日</li>
     *   <li>第二类精神药品：7 日</li>
     *   <li>毒性药品：2 日（二日极量）</li>
     *   <li>普通药品：{@code null}（不限）</li>
     * </ul>
     *
     * @return 允许的最大天数；普通药品返回 {@code null}
     */
    Integer limitDaysOf(Integer specialFlag, String dosageForm);

    /**
     * 该品种是否属于麻精毒（需要专册登记）
     */
    boolean isControlled(Integer specialFlag);

    /**
     * 是否必须双人复核。
     *
     * <ul>
     *   <li>麻醉药品、第一类精神药品 ——《医疗机构麻醉药品、第一类精神药品管理规定》第 17 条</li>
     *   <li>毒性药品 ——《医疗用毒性药品管理办法》第 9 条第 2 款
     *       （「由配方人员及具有药师以上技术职称的复核人员签名盖章后方可发出」）</li>
     * </ul>
     * 第二类精神药品**不需要**：地西泮片这类日常用量大，一并要求双人会把药房堵死，且于法无据。
     */
    boolean requiresDualCheck(Integer specialFlag);

    /**
     * 是否须登记空安瓿回收：**仅**麻醉药品与第一类精神药品的**注射剂**。
     *
     * <p>注意不要写成 {@code requiresDualCheck(...) && isInjection(...)} ——
     * 毒性药品也走双人复核，但法规没有"回收空安瓿"这条；顺带带上会让专册里
     * 永远挂着一批回收不了的空安瓿。
     */
    boolean requiresAmpouleTracking(Integer specialFlag, String dosageForm);

    /**
     * 校验整张处方的麻精限量与诊断完整性。
     *
     * @param prescriptionId  处方ID
     * @param overLimitReason 超量理由（《处方管理办法》第 24 条：第二类精神药品慢性病等超 7 日
     *                        须医师注明理由）。给空则二类精神超限同样被拦。
     * @return 违规清单；**空列表 = 通过**
     */
    List<NarcoticViolationVO> checkPrescription(Long prescriptionId, String overLimitReason);

    /**
     * 发药闸门：麻精毒需要双人复核时，校验复核人有效且与发药人不同人。
     *
     * <p>复核人必须是**在职的药剂师岗位员工**（法条要求"具有药师以上技术职称"），
     * 姓名**按 ID 从员工表反查**，不取前端传值 —— 复核是签名性质的动作，
     * 姓名可由前端随便给的话，复核记录就成了自述。
     *
     * @param drugId      药品ID
     * @param dispenserId 发药人（服务端从登录态取，不信前端）
     * @param checkerId   复核人ID（前端选定的在场复核药师）
     * @return 复核人姓名；该药品不需要双人复核时返回 {@code null}
     * @throws com.his.common.exception.BusinessException 未指定复核人、复核人非在职药剂师、或与发药人同一人
     */
    String resolveAndAssertChecker(Long drugId, Long dispenserId, Long checkerId);

    /**
     * 发药成功后写专册。
     *
     * <p>批号从药品库存流水按本次发药记录回查（FEFO 实际扣减批次），
     * **不取前端传值**。
     *
     * @return 专册登记号；非麻精药品返回 {@code null}（不登记）
     */
    String registerOnDispense(BizDrugDispensing dispensing, Long checkerId, String checkerName, String overLimitReason);

    /**
     * 空安瓿回收 / 剩余液销毁登记（只补记回收字段，不动业务字段）
     *
     * @return 更新后的专册行
     */
    BizNarcoticRegisterVO updateAmpouleReturn(AmpouleReturnDTO dto);

    /**
     * 专册分页查询
     */
    PageResult<BizNarcoticRegisterVO> listPage(NarcoticRegisterQueryPageDTO query);

    /**
     * 专册三态计数（总登记数 / 待回收空安瓿 / 已回收）
     */
    NarcoticRegisterCountVO statusCount();

    /**
     * 处方发药前预检：一次说清「能不能发 / 要不要复核药师 / 每个管制品种限量多少」。
     *
     * <p>与 {@link #checkPrescription} 的区别：那个只回答"有没有问题"，
     * 而处方全部合规时它返回空列表 —— 前端据此无法判断要不要选复核人。
     * 预检把管制明细清单也带出来，发药窗口才不用"点一次、错一次、再猜一次"。
     *
     * @param overLimitReason 第二类精神药品超 7 日的医师理由（先按无理由预检，拿到
     *                        {@code overLimitReasonRequired=true} 再带理由重问一次）
     */
    NarcoticPrecheckVO precheck(Long prescriptionId, String overLimitReason);

    /**
     * 批量取药品管制分类（drugId → specialFlag），供列表出参补 {@code specialFlag} 用。
     *
     * <p>做成批量而不是逐行查：发药列表一页 10~100 行，逐行查就是 N 次库往返。
     * 返回 Map 里**没有**的药品 ID 表示该药不在药品字典（或已删），
     * 调用方按"分类未知"处理，不要补 0 —— 0 是"普通药品"，会静默免掉全部麻精管制。
     */
    Map<Long, Integer> specialFlagMap(List<Long> drugIds);
}

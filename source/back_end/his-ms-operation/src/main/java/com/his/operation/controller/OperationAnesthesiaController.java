package com.his.operation.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.common.base.Result;
import com.his.operation.dto.*;
import com.his.operation.service.AnesthesiaRecordService;
import com.his.operation.service.AnesthesiaVisitService;
import com.his.operation.vo.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 手术麻醉链端点（术前访视 → 麻醉记录单 → 计费联动）。
 */
@Tag(name = "手术麻醉")
@RestController
@RequestMapping("/patient/inpatient/anesthesia")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ipd:anesthesia:list')")
public class OperationAnesthesiaController {

    private final AnesthesiaVisitService anesthesiaVisitService;
    private final AnesthesiaRecordService anesthesiaRecordService;

    // 一、术前访视

    @Operation(summary = "术前访视分页（住院/手术/结论/关键字）")
    @GetMapping("/visitListPage")
    public Result<IPage<AnesthesiaVisitVO>> visitListPage(@Valid AnesthesiaVisitPageQueryDTO query) {
        return Result.success(anesthesiaVisitService.listPage(query));
    }

    /**
     * 一条术前访视的全量字段。
     * 访视定稿后回看评估明细与结论时取这一份。
     */
    @Operation(summary = "术前访视详情")
    @GetMapping("/visitGetDetailById")
    public Result<AnesthesiaVisitVO> visitGetDetailById(@RequestParam Long visitId) {
        return Result.success(anesthesiaVisitService.getDetailById(visitId));
    }

    /**
     * 按手术申请单取它名下的术前访视。
     * 麻醉开立前核对这台手术访视到没到位时用这一份。
     *
     * @return 出参 data 为 null 表示该台手术尚无访视记录
     */
    @Operation(summary = "某台手术的术前访视（没有则 data 为 null）")
    @GetMapping("/visitGetByApply")
    public Result<AnesthesiaVisitVO> visitGetByApply(@RequestParam Long applyId) {
        return Result.success(anesthesiaVisitService.getByApply(applyId));
    }

    /**
     * 保存术前访视草稿（新增与修改同一入口，按 id 分支）。
     * 用于术前访视填写弹框的暂存：草稿不出结论，不能作为开麻醉的依据。
     *
     * @return 访视单号
     */
    @PreAuthorize("hasAuthority('ipd:anesthesia:add')")
    @Operation(summary = "保存术前访视（新增/修改草稿），返回访视单号")
    @PostMapping("/visitSave")
    public Result<String> visitSave(@RequestBody @Valid AnesthesiaVisitUpsertDTO dto) {
        return Result.success("术前访视已保存（尚未给出结论，不能作为麻醉依据）", anesthesiaVisitService.save(dto));
    }

    /**
     * 术前访视定稿并出具访视结论。
     * 用于麻醉工作站「术前访视」页签行上的「完成访视」动作；结论不是「可施行麻醉」时必须写说明，否则过不了校验。
     */
    @PreAuthorize("hasAuthority('ipd:anesthesia:edit')")
    @Operation(summary = "完成术前访视（结论出账；结论非「可施行麻醉」必须写说明）")
    @PostMapping("/visitFinish")
    public Result<Void> visitFinish(@RequestBody @Valid AnesthesiaVisitFinishDTO dto) {
        anesthesiaVisitService.finish(dto);
        return Result.success("术前访视已完成", null);
    }

    /**
     * 已完成的手术中缺少合格术前访视的台数。
     * 用于麻醉工作台的待补账角标，提醒急诊超前麻醉事后补访视；只出计数不出明细。
     */
    @Operation(summary = "已完成但没有合格术前访视的手术台数（急诊超前麻醉的待补账）")
    @GetMapping("/countFinishedWithoutVisit")
    public Result<Long> countFinishedWithoutVisit() {
        return Result.success(anesthesiaVisitService.countFinishedWithoutVisit());
    }

    // 二、麻醉记录单

    @Operation(summary = "麻醉记录单分页（住院/手术/麻醉医师/状态/关键字/未计费）")
    @GetMapping("/recordListPage")
    public Result<IPage<AnesthesiaRecordVO>> recordListPage(@Valid AnesthesiaRecordPageQueryDTO query) {
        return Result.success(anesthesiaRecordService.listPage(query));
    }

    /**
     * 麻醉记录单全量字段，生命体征与用药明细一并带回。
     * 记录详情弹框与记录编辑弹框回显时取这一份。
     */
    @Operation(summary = "麻醉记录单详情（含生命体征与用药）")
    @GetMapping("/recordGetDetailById")
    public Result<AnesthesiaRecordVO> recordGetDetailById(@RequestParam Long recordId) {
        return Result.success(anesthesiaRecordService.getDetailById(recordId));
    }

    /**
     * 按手术申请单取它名下的麻醉记录单。
     * 一台手术是否已经开单做麻醉的核对环节用这一份。
     *
     * @return 出参 data 为 null 表示该台手术尚未开立麻醉记录
     */
    @Operation(summary = "某台手术的麻醉记录单（没有则 data 为 null）")
    @GetMapping("/recordGetByApply")
    public Result<AnesthesiaRecordVO> recordGetByApply(@RequestParam Long applyId) {
        return Result.success(anesthesiaRecordService.getByApply(applyId));
    }

    /**
     * 开立麻醉记录单，落初态供术中持续补记。
     * 用于麻醉记录新增弹框；这台手术没有结论为「可施行麻醉」的访视时开不出来。
     *
     * @return 麻醉记录单号
     */
    @PreAuthorize("hasAuthority('ipd:anesthesia:add')")
    @Operation(summary = "开立麻醉记录单（需先有「可施行麻醉」的术前访视），返回麻醉记录单号")
    @PostMapping("/recordCreate")
    public Result<String> recordCreate(@RequestBody @Valid AnesthesiaRecordUpsertDTO dto) {
        return Result.success("麻醉记录单已开立（记录中）", anesthesiaRecordService.create(dto));
    }

    /**
     * 修订麻醉记录单的术中过程信息（时间轴与出入量）。
     * 用于麻醉记录编辑弹框保存；提交定稿后单据不再可改。
     */
    @PreAuthorize("hasAuthority('ipd:anesthesia:edit')")
    @Operation(summary = "更新麻醉记录单（仅「记录中」可改）")
    @PostMapping("/recordUpdate")
    public Result<Void> recordUpdate(@RequestBody @Valid AnesthesiaRecordUpdateUpsertDTO dto) {
        anesthesiaRecordService.update(dto);
        return Result.success("麻醉记录已更新", null);
    }

    /**
     * 追加一条术中生命体征采样。
     * 用于麻醉记录页的「生命体征」弹框；同一采样时刻只允许一条，重复追加被拒。
     */
    @PreAuthorize("hasAuthority('ipd:anesthesia:add')")
    @Operation(summary = "追加一条生命体征（仅「记录中」可加；同一时刻不允许两条）")
    @PostMapping("/addVital")
    public Result<Void> addVital(@RequestBody @Valid AnesthesiaVitalUpsertDTO dto) {
        anesthesiaRecordService.addVital(dto);
        return Result.success("生命体征已记录", null);
    }

    /**
     * 一条麻醉记录名下的生命体征，按采样时刻升序。
     * 术中体征曲线与采样明细回看时按记录整组取回。
     */
    @Operation(summary = "麻醉生命体征列表（按采样时刻升序）")
    @GetMapping("/listVitals")
    public Result<List<AnesthesiaVitalVO>> listVitals(@RequestParam Long recordId) {
        return Result.success(anesthesiaRecordService.listVitals(recordId));
    }

    /**
     * 追加一条麻醉给药记录。
     * 用于麻醉记录页的「用药」弹框，只在单据仍是记录中时可加。
     */
    @PreAuthorize("hasAuthority('ipd:anesthesia:add')")
    @Operation(summary = "追加一条麻醉用药（仅「记录中」可加）")
    @PostMapping("/addMed")
    public Result<Void> addMed(@RequestBody @Valid AnesthesiaMedUpsertDTO dto) {
        anesthesiaRecordService.addMed(dto);
        return Result.success("麻醉用药已记录", null);
    }

    /**
     * 一条麻醉记录名下的用药明细，按给药时刻升序。
     * 术中用药回看以及提交前自查用药是否记全时按记录整组取回。
     */
    @Operation(summary = "麻醉用药列表（按给药时刻升序）")
    @GetMapping("/listMeds")
    public Result<List<AnesthesiaMedVO>> listMeds(@RequestParam Long recordId) {
        return Result.success(anesthesiaRecordService.listMeds(recordId));
    }

    /**
     * 提交麻醉记录定稿：体征与用药同时锁死，并触发按时长与监护项的计费。
     * 用于麻醉记录列表的「提交」动作；麻醉方式与起止时间缺失、一条体征都没有都提交不了。
     *
     * @return 计费结果汇总，个别项失败不回滚提交，失败原因随出参带回
     */
    @PreAuthorize("hasAuthority('ipd:anesthesia:add')")
    @Operation(summary = "提交麻醉记录（记录中→已提交；体征与用药之后锁死；自动联动计费）")
    @PostMapping("/recordSubmit")
    public Result<OperationChargeSummaryVO> recordSubmit(@RequestBody @Valid AnesthesiaActionDTO dto) {
        OperationChargeSummaryVO summary = anesthesiaRecordService.submit(dto);
        return Result.success(summary.hasFailure()
                        ? "麻醉记录已提交，但计费存在失败项（详见 messages）"
                        : "麻醉记录已提交",
                summary);
    }

    /**
     * 审核已提交的麻醉记录，单据走到已审核（终态）。
     * 用于麻醉记录列表的「审核」动作：落审核人与审核时间，签字后不再回退，同一单据重复审核被拒。
     */
    @PreAuthorize("hasAuthority('ipd:anesthesia:edit')")
    @Operation(summary = "审核麻醉记录（已提交→已审核）")
    @PostMapping("/recordAudit")
    public Result<Void> recordAudit(@RequestBody @Valid AnesthesiaActionDTO dto) {
        anesthesiaRecordService.audit(dto);
        return Result.success("麻醉记录已审核", null);
    }

    /**
     * 对一张麻醉记录重跑计费。
     * 用于收费对账环节补救失败项；提交前的单据不计费，已成功的项目不会重复记账。
     *
     * @return 本次计费结果汇总（成功数与失败明细）
     */
    @PreAuthorize("hasAuthority('ipd:anesthesia:edit')")
    @Operation(summary = "麻醉计费（失败项重试；已成功的项幂等跳过）")
    @PostMapping("/recordCharge")
    public Result<OperationChargeSummaryVO> recordCharge(@RequestBody @Valid AnesthesiaActionDTO dto) {
        return Result.success(anesthesiaRecordService.charge(dto));
    }

    /**
     * 尚未完成计费的麻醉记录单数量。
     * 用于麻醉工作台的收费对账角标，提醒补计费；只出计数不出明细。
     */
    @Operation(summary = "尚未计费的麻醉记录单数（收费对账入口）")
    @GetMapping("/countUncharged")
    public Result<Long> countUncharged() {
        return Result.success(anesthesiaRecordService.countUncharged());
    }

    /**
     * 一台手术名下的麻醉计费明细，逐项一行。
     * 收费对账环节据此定位「该收但没计上」的项目行。
     */
    @Operation(summary = "某台手术的计费明细（每项一行；status=2 的行就是「该收但没计上」）")
    @GetMapping("/listChargeItems")
    public Result<List<OperationChargeItemVO>> listChargeItems(@RequestParam Long applyId) {
        return Result.success(anesthesiaRecordService.listChargeItems(applyId));
    }
}

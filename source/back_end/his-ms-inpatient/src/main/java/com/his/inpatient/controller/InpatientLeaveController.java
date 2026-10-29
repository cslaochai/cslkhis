package com.his.inpatient.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.inpatient.dto.InpatientLeaveDTO;
import com.his.inpatient.service.InpatientLeaveService;
import com.his.inpatient.vo.InpatientLeaveVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 住院患者请假/离院登记（住院业务单据，菜单 320 / 路径 /inpatient-leave）。
 */
@Tag(name = "住院患者请假离院登记")
@RestController
@RequestMapping("/patient/inpatient/leave")
@RequiredArgsConstructor
public class InpatientLeaveController {

    private final InpatientLeaveService inpatientLeaveService;

    /**
     * 请假台账分页：可按患者、状态与病区过滤，只看超期未归时由服务端现算超期（库里不落超期状态）。
     * 用于住院患者请假离院登记页的台账查询卡。
     */
    @PreAuthorize("hasAuthority('ipd:leave:list')")
    @Operation(summary = "请假台账分页（overdueOnly=true 只看超期未归，超期是查询时算的展示态）")
    @PostMapping("/listPage")
    public Result<PageResult<InpatientLeaveVO.Row>> listPage(@Valid @RequestBody InpatientLeaveDTO.PageQuery dto) {
        return Result.success(inpatientLeaveService.listPage(dto));
    }

    /**
     * 单张请假单详情：含承诺书签署证据摘要与当前可用动作标记。
     * 用于详情弹框、编辑弹框回显，以及离院登记与打印前的取数。
     */
    @PreAuthorize("hasAuthority('ipd:leave:list')")
    @Operation(summary = "请假单详情（离院登记/打印数据源，电话已脱敏，含签名证据摘要与动作可用性）")
    @GetMapping("/getById")
    public Result<InpatientLeaveVO.Detail> getById(@RequestParam Long id) {
        return Result.success(inpatientLeaveService.getDetailById(id));
    }

    /**
     * 开单底稿：按住院记录带出患者快照与在途请假单张数，快照由服务端重查、不采信前端传入的姓名科室。
     * 用于新建请假申请时填充表单抬头。
     */
    @PreAuthorize("hasAuthority('ipd:leave:list')")
    @Operation(summary = "开单底稿（按住院带出患者快照与在途请假单张数）")
    @GetMapping("/base")
    public Result<InpatientLeaveVO.Base> base(@RequestParam Long admissionId) {
        return Result.success(inpatientLeaveService.base(admissionId));
    }

    /**
     * 在院患者候选及其在途请假状态，支持关键字过滤与条数上限。
     * 用于请假登记页顶部的在院患者横幅。
     */
    @PreAuthorize("hasAuthority('ipd:leave:list')")
    @Operation(summary = "在院患者候选（横幅数据源，含在途请假单状态）")
    @GetMapping("/inpatients")
    public Result<List<InpatientLeaveVO.Inpatient>> inpatients(@RequestParam(required = false) String keyword,
                                                               @RequestParam(required = false) Integer limit) {
        return Result.success(inpatientLeaveService.inpatients(keyword, limit));
    }

    /**
     * 请假业务统计：待审批、已批准待离院、在院外、超期未归、今日返回。
     * 用于请假离院登记页顶部统计卡。
     */
    @PreAuthorize("hasAuthority('ipd:leave:list')")
    @Operation(summary = "统计卡（待审批/已批准待离院/在院外/超期未归/今日返回）")
    @GetMapping("/stats")
    public Result<InpatientLeaveVO.Stats> stats() {
        return Result.success(inpatientLeaveService.stats());
    }

    /**
     * 填写或修改请假申请单并返回请假单ID文本；仅待审批状态的单可改。
     * 用于登记页的新增与编辑弹框提交。
     */
    @PreAuthorize("hasAuthority('ipd:leave:add')")
    @Operation(summary = "填写/修改申请单（仅待审批可改；一般项目服务端重查快照）")
    @PostMapping("/upsert")
    public Result<String> upsert(@Valid @RequestBody InpatientLeaveDTO.Upsert dto) {
        // 雪花 ID 19 位，裸 Long 出 JSON number 会在前端丢精度，统一字符串出参
        return Result.success("请假单已保存", String.valueOf(inpatientLeaveService.upsert(dto)));
    }

    /**
     * 审批请假申请：批准时以当前登录医师电子签名锁定并落医师意见，拒绝时必须填理由。
     * 用于待审批单的审批弹框。
     */
    @PreAuthorize("hasAuthority('ipd:leave:edit')")
    @Operation(summary = "审批（allow=true 批准并由当前登录医师电子签名锁定；allow=false 拒绝必填理由）")
    @PostMapping("/approve")
    public Result<Void> approve(@Valid @RequestBody InpatientLeaveDTO.Approve dto, HttpServletRequest request) {
        dto.setClientIp(request.getRemoteAddr());
        inpatientLeaveService.approve(dto, dto.getPin());
        return Result.success("已审批", null);
    }

    /**
     * 登记离院：核对患方风险承诺书签署三要素（确认人姓名、与患者关系、手写签名）并写入实际离院时间。
     * 用于已批准单在患者实际外出时的离院登记弹框。
     */
    @PreAuthorize("hasAuthority('ipd:leave:edit')")
    @Operation(summary = "登记离院 = 患方签署风险承诺书三要素（姓名/关系/手写签名）+ 实际离院时间")
    @PostMapping("/confirmLeave")
    public Result<Void> confirmLeave(@Valid @RequestBody InpatientLeaveDTO.Confirm dto) {
        inpatientLeaveService.confirmLeave(dto);
        return Result.success("患方已签署承诺书并登记离院", null);
    }

    /**
     * 返回销假：登记实际返回时间，请假闭环成立。
     * 用于在院外单的患者回院后在销假弹框收尾。
     */
    @PreAuthorize("hasAuthority('ipd:leave:edit')")
    @Operation(summary = "返回销假（登记实际返回时间，闭环成立）")
    @PostMapping("/confirmBack")
    public Result<Void> confirmBack(@Valid @RequestBody InpatientLeaveDTO.Back dto) {
        inpatientLeaveService.confirmBack(dto);
        return Result.success("已销假", null);
    }

    /**
     * 作废请假单：仅待审批与已批准可取消，已离院的单不许取消（人已外出，事实不能抹掉）。
     * 用于台账行上的取消确认。
     */
    @PreAuthorize("hasAuthority('ipd:leave:edit')")
    @Operation(summary = "取消（仅待审批/已批准；已离院的单不许取消）")
    @PostMapping("/cancel")
    public Result<Void> cancel(@Valid @RequestBody InpatientLeaveDTO.Cancel dto) {
        inpatientLeaveService.cancel(dto);
        return Result.success("已取消", null);
    }

    /**
     * 记录超期未归的处置：联系结果与上报对象。
     * 用于台账上对已离院且已超期单的处置弹框。
     */
    @PreAuthorize("hasAuthority('ipd:leave:edit')")
    @Operation(summary = "超期处置记录（仅已离院且超期的单；联系不上必须升级上报）")
    @PostMapping("/recordContact")
    public Result<Void> recordContact(@Valid @RequestBody InpatientLeaveDTO.Contact dto) {
        inpatientLeaveService.recordContact(dto);
        return Result.success("超期处置已记录", null);
    }

    /**
     * 风险承诺书打印留痕：打印一次计数一次，仅已离院或已返回的单可打。
     * 用于详情弹框出纸前登记打印次数。
     */
    @PreAuthorize("hasAuthority('ipd:leave:print')")
    @Operation(summary = "承诺书打印计数（已离院/已返回可打印，打印一次计数一次）")
    @PostMapping("/print")
    public Result<Void> print(@Valid @RequestBody InpatientLeaveDTO.Print dto) {
        inpatientLeaveService.print(dto);
        return Result.success("已记录打印", null);
    }
}

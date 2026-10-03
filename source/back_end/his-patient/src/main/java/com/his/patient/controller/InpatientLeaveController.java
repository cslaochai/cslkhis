package com.his.patient.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.patient.dto.InpatientLeaveDTO;
import com.his.patient.service.InpatientLeaveService;
import com.his.patient.vo.InpatientLeaveVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 住院患者请假/离院登记（住院业务单据，菜单 320 / 路径 /inpatient-leave）。
 *
 * <p>按项目规范 {@code @PreAuthorize} 全部标到方法，类上不挂（类级会静默覆盖未标注的方法）。
 * 新增/修改共用 :add（同一 upsert），审批/离院/销假/取消/超期处置是状态动作用 :edit，
 * 承诺书打印单独收权。护士登记申请、医师审批（批准即电子签名业务类型=10）。
 */
@Tag(name = "住院患者请假离院登记")
@RestController
@RequestMapping("/patient/inpatient/leave")
@RequiredArgsConstructor
public class InpatientLeaveController {

    private final InpatientLeaveService leaveService;

    @PreAuthorize("hasAuthority('ipd:leave:list')")
    @Operation(summary = "请假台账分页（overdueOnly=true 只看超期未归，超期是查询时算的展示态）")
    @PostMapping("/listPage")
    public Result<PageResult<InpatientLeaveVO.Row>> listPage(@RequestBody(required = false) InpatientLeaveDTO.QueryPage dto) {
        return Result.success(leaveService.listPage(dto == null ? new InpatientLeaveDTO.QueryPage() : dto));
    }

    @PreAuthorize("hasAuthority('ipd:leave:list')")
    @Operation(summary = "请假单详情（离院登记/打印数据源，电话已脱敏，含签名证据摘要与动作可用性）")
    @GetMapping("/getById")
    public Result<InpatientLeaveVO.Detail> getById(@RequestParam Long id) {
        return Result.success(leaveService.getDetailById(id));
    }

    @PreAuthorize("hasAuthority('ipd:leave:list')")
    @Operation(summary = "开单底稿（按住院带出患者快照与在途请假单张数）")
    @GetMapping("/base")
    public Result<InpatientLeaveVO.Base> base(@RequestParam Long admissionId) {
        return Result.success(leaveService.base(admissionId));
    }

    @PreAuthorize("hasAuthority('ipd:leave:list')")
    @Operation(summary = "在院患者候选（横幅数据源，含在途请假单状态）")
    @GetMapping("/inpatients")
    public Result<List<InpatientLeaveVO.Inpatient>> inpatients(@RequestParam(required = false) String keyword,
                                                               @RequestParam(required = false) Integer limit) {
        return Result.success(leaveService.inpatients(keyword, limit));
    }

    @PreAuthorize("hasAuthority('ipd:leave:list')")
    @Operation(summary = "统计卡（待审批/已批准待离院/在院外/超期未归/今日返回）")
    @GetMapping("/stats")
    public Result<InpatientLeaveVO.Stats> stats() {
        return Result.success(leaveService.stats());
    }

    @PreAuthorize("hasAuthority('ipd:leave:add')")
    @Operation(summary = "填写/修改申请单（仅待审批可改；一般项目服务端重查快照）")
    @PostMapping("/upsert")
    public Result<String> upsert(@Valid @RequestBody InpatientLeaveDTO.Upsert dto) {
        // 雪花 ID 19 位，裸 Long 出 JSON number 会在前端丢精度，统一字符串出参
        return Result.success("请假单已保存", String.valueOf(leaveService.upsert(dto)));
    }

    @PreAuthorize("hasAuthority('ipd:leave:edit')")
    @Operation(summary = "审批（allow=true 批准并由当前登录医师电子签名锁定；allow=false 拒绝必填理由）")
    @PostMapping("/approve")
    public Result<Void> approve(@Valid @RequestBody InpatientLeaveDTO.Approve dto, HttpServletRequest request) {
        dto.setClientIp(request.getRemoteAddr());
        leaveService.approve(dto);
        return Result.success("已审批", null);
    }

    @PreAuthorize("hasAuthority('ipd:leave:edit')")
    @Operation(summary = "登记离院 = 患方签署风险承诺书三要素（姓名/关系/手写签名）+ 实际离院时间")
    @PostMapping("/confirmLeave")
    public Result<Void> confirmLeave(@Valid @RequestBody InpatientLeaveDTO.Confirm dto) {
        leaveService.confirmLeave(dto);
        return Result.success("患方已签署承诺书并登记离院", null);
    }

    @PreAuthorize("hasAuthority('ipd:leave:edit')")
    @Operation(summary = "返回销假（登记实际返回时间，闭环成立）")
    @PostMapping("/confirmBack")
    public Result<Void> confirmBack(@Valid @RequestBody InpatientLeaveDTO.Back dto) {
        leaveService.confirmBack(dto);
        return Result.success("已销假", null);
    }

    @PreAuthorize("hasAuthority('ipd:leave:edit')")
    @Operation(summary = "取消（仅待审批/已批准；已离院的单不许取消）")
    @PostMapping("/cancel")
    public Result<Void> cancel(@Valid @RequestBody InpatientLeaveDTO.Cancel dto) {
        leaveService.cancel(dto);
        return Result.success("已取消", null);
    }

    @PreAuthorize("hasAuthority('ipd:leave:edit')")
    @Operation(summary = "超期处置记录（仅已离院且超期的单；联系不上必须升级上报）")
    @PostMapping("/recordContact")
    public Result<Void> recordContact(@Valid @RequestBody InpatientLeaveDTO.Contact dto) {
        leaveService.recordContact(dto);
        return Result.success("超期处置已记录", null);
    }

    @PreAuthorize("hasAuthority('ipd:leave:print')")
    @Operation(summary = "承诺书打印计数（已离院/已返回可打印，打印一次计数一次）")
    @PostMapping("/print")
    public Result<Void> print(@Valid @RequestBody InpatientLeaveDTO.Print dto) {
        leaveService.print(dto);
        return Result.success("已记录打印", null);
    }
}

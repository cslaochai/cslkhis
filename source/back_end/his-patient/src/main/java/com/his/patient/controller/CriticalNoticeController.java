package com.his.patient.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.patient.dto.CriticalNoticeDTO;
import com.his.patient.service.CriticalNoticeService;
import com.his.patient.vo.CriticalNoticeVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 病危/病重通知与告知书签收回执
 */
@Tag(name = "病危重通知与签收回执")
@RestController
@RequestMapping("/patient/criticalNotice")
@RequiredArgsConstructor
public class CriticalNoticeController {

    private final CriticalNoticeService criticalNoticeService;

    @PreAuthorize("hasAuthority('ipd:criticalNotice:list')")
    @Operation(summary = "通知台账分页")
    @PostMapping("/listPage")
    public Result<PageResult<CriticalNoticeVO.Row>> listPage(@Valid @RequestBody CriticalNoticeDTO.QueryPage dto) {
        return Result.success(criticalNoticeService.listPage(dto));
    }

    @PreAuthorize("hasAuthority('ipd:criticalNotice:list')")
    @Operation(summary = "通知详情（签收/打印数据源，敏感列已脱敏，含签名证据摘要）")
    @GetMapping("/getById")
    public Result<CriticalNoticeVO.Detail> getById(@RequestParam Long id) {
        return Result.success(criticalNoticeService.getDetailById(id));
    }

    @PreAuthorize("hasAuthority('ipd:criticalNotice:list')")
    @Operation(summary = "开单底稿（按住院带出患者快照）")
    @GetMapping("/base")
    public Result<CriticalNoticeVO.Base> base(@RequestParam Long admissionId) {
        return Result.success(criticalNoticeService.base(admissionId));
    }

    @PreAuthorize("hasAuthority('ipd:criticalNotice:list')")
    @Operation(summary = "在院患者候选（医生站横幅数据源）")
    @GetMapping("/inpatients")
    public Result<List<CriticalNoticeVO.Inpatient>> inpatients(@RequestParam(required = false) String keyword,
                                                               @RequestParam(required = false) Integer limit) {
        return Result.success(criticalNoticeService.inpatients(keyword, limit));
    }

    @PreAuthorize("hasAuthority('ipd:criticalNotice:list')")
    @Operation(summary = "医师候选（告知/见证医师下拉）")
    @GetMapping("/doctorOptions")
    public Result<List<CriticalNoticeVO.DoctorOption>> doctorOptions() {
        return Result.success(criticalNoticeService.doctorOptions());
    }

    @PreAuthorize("hasAuthority('ipd:criticalNotice:list')")
    @Operation(summary = "统计卡（四状态计数）")
    @GetMapping("/stats")
    public Result<CriticalNoticeVO.Stats> stats() {
        return Result.success(criticalNoticeService.stats());
    }

    @PreAuthorize("hasAuthority('ipd:criticalNotice:add')")
    @Operation(summary = "填写/修改草稿（已签发/已签收禁改；一般项目服务端重查快照）")
    @PostMapping("/upsert")
    public Result<String> upsert(@Valid @RequestBody CriticalNoticeDTO.Upsert dto) {
        // 雪花 ID 19 位，裸 Long 出 JSON number 会在前端丢精度，统一字符串出参
        return Result.success("通知单已保存", String.valueOf(criticalNoticeService.upsert(dto)));
    }

    @PreAuthorize("hasAuthority('ipd:criticalNotice:edit')")
    @Operation(summary = "签发（草稿→已签发；当前登录医师电子签名锁定，须患者在院）")
    @PostMapping("/issue")
    public Result<Void> issue(@Valid @RequestBody CriticalNoticeDTO.Issue dto, HttpServletRequest request) {
        dto.setClientIp(request.getRemoteAddr());
        criticalNoticeService.issue(dto);
        return Result.success("已签发并完成医师电子签名", null);
    }

    @PreAuthorize("hasAuthority('ipd:criticalNotice:edit')")
    @Operation(summary = "签收（已签发→已签收；家属手写签名+法定关系，三者缺一不可）")
    @PostMapping("/acknowledge")
    public Result<Void> acknowledge(@Valid @RequestBody CriticalNoticeDTO.Acknowledge dto) {
        criticalNoticeService.acknowledge(dto);
        return Result.success("家属已签收，告知闭环成立", null);
    }

    @PreAuthorize("hasAuthority('ipd:criticalNotice:edit')")
    @Operation(summary = "作废（必填原因；已签收不许作废，已签名须先在签名中心作废签名）")
    @PostMapping("/voidById")
    public Result<Void> voidById(@Valid @RequestBody CriticalNoticeDTO.VoidNotice dto) {
        criticalNoticeService.voidNotice(dto);
        return Result.success("已作废", null);
    }

    @PreAuthorize("hasAuthority('ipd:criticalNotice:print')")
    @Operation(summary = "回执打印计数（两联：病历联+患方联，只有已签收可打印）")
    @PostMapping("/print")
    public Result<Void> print(@Valid @RequestBody CriticalNoticeDTO.Print dto) {
        criticalNoticeService.print(dto);
        return Result.success("已记录打印", null);
    }
}

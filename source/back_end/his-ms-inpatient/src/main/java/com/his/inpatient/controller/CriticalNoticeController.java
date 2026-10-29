package com.his.inpatient.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.inpatient.dto.CriticalNoticeDTO;
import com.his.inpatient.service.CriticalNoticeService;
import com.his.inpatient.vo.CriticalNoticeVO;
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

    /**
     * 病危/病重通知台账分页。
     * 用于病危重通知页查询卡与台账表格。
     */
    @PreAuthorize("hasAuthority('ipd:criticalNotice:list')")
    @Operation(summary = "通知台账分页")
    @PostMapping("/listPage")
    public Result<PageResult<CriticalNoticeVO.Row>> listPage(@Valid @RequestBody CriticalNoticeDTO.PageQuery dto) {
        return Result.success(criticalNoticeService.listPage(dto));
    }

    /**
     * 取单张通知的完整内容，含签收信息与签名证据摘要，敏感列以脱敏文本出参。
     * 用于病危重通知页的详情弹框，以及签收、打印前的核对取数。
     */
    @PreAuthorize("hasAuthority('ipd:criticalNotice:list')")
    @Operation(summary = "通知详情（签收/打印数据源，敏感列已脱敏，含签名证据摘要）")
    @GetMapping("/getById")
    public Result<CriticalNoticeVO.Detail> getById(@RequestParam Long id) {
        return Result.success(criticalNoticeService.getDetailById(id));
    }

    /**
     * 按一次住院带出患者快照作为开单底稿，快照由服务端重查不采信前端传入。
     * 用于通知页选中在院患者后初始化「填写通知」表单。
     */
    @PreAuthorize("hasAuthority('ipd:criticalNotice:list')")
    @Operation(summary = "开单底稿（按住院带出患者快照）")
    @GetMapping("/base")
    public Result<CriticalNoticeVO.Base> base(@RequestParam Long admissionId) {
        return Result.success(criticalNoticeService.base(admissionId));
    }

    /**
     * 列出可开通知的在院患者候选，附带每人历史通知张数。
     * 用于病危重通知页顶部的在院患者横幅。
     */
    @PreAuthorize("hasAuthority('ipd:criticalNotice:list')")
    @Operation(summary = "在院患者候选（医生站横幅数据源）")
    @GetMapping("/inpatients")
    public Result<List<CriticalNoticeVO.Inpatient>> inpatients(@RequestParam(required = false) String keyword,
                                                               @RequestParam(required = false) Integer limit) {
        return Result.success(criticalNoticeService.inpatients(keyword, limit));
    }

    /**
     * 列出可指派的医师候选。
     * 用于通知填写表单里告知医师与见证医师的下拉选择。
     */
    @PreAuthorize("hasAuthority('ipd:criticalNotice:list')")
    @Operation(summary = "医师候选（告知/见证医师下拉）")
    @GetMapping("/doctorOptions")
    public Result<List<CriticalNoticeVO.DoctorOption>> doctorOptions() {
        return Result.success(criticalNoticeService.doctorOptions());
    }

    /**
     * 统计各状态通知单张数。
     * 用于病危重通知页顶部统计卡。
     */
    @PreAuthorize("hasAuthority('ipd:criticalNotice:list')")
    @Operation(summary = "统计卡（四状态计数）")
    @GetMapping("/stats")
    public Result<CriticalNoticeVO.Stats> stats() {
        return Result.success(criticalNoticeService.stats());
    }

    /**
     * 保存通知草稿（新建或修改），已签发与已签收的单据不再接受改动。
     * 用于「填写通知」弹框的保存动作。
     */
    @PreAuthorize("hasAuthority('ipd:criticalNotice:add')")
    @Operation(summary = "填写/修改草稿（已签发/已签收禁改；一般项目服务端重查快照）")
    @PostMapping("/upsert")
    public Result<String> upsert(@Valid @RequestBody CriticalNoticeDTO.Upsert dto) {
        // 雪花 ID 19 位，裸 Long 出 JSON number 会在前端丢精度，统一字符串出参
        return Result.success("通知单已保存", String.valueOf(criticalNoticeService.upsert(dto)));
    }

    /**
     * 签发草稿并以当前登录医师身份落电子签名，要求患者仍在院。
     * 用于台账操作列的签发动作。
     */
    @PreAuthorize("hasAuthority('ipd:criticalNotice:edit')")
    @Operation(summary = "签发（草稿→已签发；当前登录医师电子签名锁定，须患者在院）")
    @PostMapping("/issue")
    public Result<Void> issue(@Valid @RequestBody CriticalNoticeDTO.Issue dto, HttpServletRequest request) {
        dto.setClientIp(request.getRemoteAddr());
        criticalNoticeService.issue(dto, dto.getPin());
        return Result.success("已签发并完成医师电子签名", null);
    }

    /**
     * 记录家属签收：签收人姓名、与患者的法定关系、手写签名三者齐备才成立。
     * 用于「家属签收」弹框的提交。
     */
    @PreAuthorize("hasAuthority('ipd:criticalNotice:edit')")
    @Operation(summary = "签收（已签发→已签收；家属手写签名+法定关系，三者缺一不可）")
    @PostMapping("/acknowledge")
    public Result<Void> acknowledge(@Valid @RequestBody CriticalNoticeDTO.Acknowledge dto) {
        criticalNoticeService.acknowledge(dto);
        return Result.success("家属已签收，告知闭环成立", null);
    }

    /**
     * 作废一张通知并留写作废原因；已签收的单据不许作废。
     * 用于台账操作列的作废动作。
     */
    @PreAuthorize("hasAuthority('ipd:criticalNotice:edit')")
    @Operation(summary = "作废（必填原因；已签收不许作废，已签名须先在签名中心作废签名）")
    @PostMapping("/voidById")
    public Result<Void> voidById(@Valid @RequestBody CriticalNoticeDTO.VoidNotice dto) {
        criticalNoticeService.voidNotice(dto);
        return Result.success("已作废", null);
    }

    /**
     * 登记一次回执打印并累加打印次数，只有已签收的通知可打印。
     * 用于回执输出时记录病历联与患方联的打印事实。
     */
    @PreAuthorize("hasAuthority('ipd:criticalNotice:print')")
    @Operation(summary = "回执打印计数（两联：病历联+患方联，只有已签收可打印）")
    @PostMapping("/print")
    public Result<Void> print(@Valid @RequestBody CriticalNoticeDTO.Print dto) {
        criticalNoticeService.print(dto);
        return Result.success("已记录打印", null);
    }
}

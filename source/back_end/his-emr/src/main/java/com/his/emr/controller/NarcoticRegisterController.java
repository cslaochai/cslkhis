package com.his.emr.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.emr.dto.AmpouleReturnDTO;
import com.his.emr.dto.NarcoticRegisterQueryPageDTO;
import com.his.emr.service.NarcoticControlService;
import com.his.emr.vo.BizNarcoticRegisterVO;
import com.his.emr.vo.NarcoticPrecheckVO;
import com.his.emr.vo.NarcoticRegisterCountVO;
import com.his.emr.vo.NarcoticViolationVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 麻精药品专册控制器（G10）。
 */
@Tag(name = "麻精药品专册")
@RestController
@RequestMapping("/narcotic")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('pharmacy:narcotic:list', 'pharmacy:dispensing:list')")
public class NarcoticRegisterController {

    private final NarcoticControlService narcoticControlService;

    @Operation(summary = "麻精药品专册分页查询")
    @PostMapping("/listPage")
    public Result<PageResult<BizNarcoticRegisterVO>> listPage(@Valid @RequestBody NarcoticRegisterQueryPageDTO query) {
        return Result.success(narcoticControlService.listPage(query));
    }

    @Operation(summary = "专册计数（总登记 / 待回收空安瓿 / 已回收空安瓿）")
    @GetMapping("/statusCount")
    public Result<NarcoticRegisterCountVO> statusCount() {
        return Result.success(narcoticControlService.statusCount());
    }

    @PreAuthorize("hasAuthority('pharmacy:narcotic:edit')")
    @Operation(summary = "空安瓿回收 / 剩余液销毁登记")
    @PostMapping("/ampouleReturn")
    public Result<BizNarcoticRegisterVO> ampouleReturn(@Valid @RequestBody AmpouleReturnDTO dto) {
        return Result.success(narcoticControlService.updateAmpouleReturn(dto));
    }

    /**
     * 处方麻精限量预检：发药前先问一次"这张处方能不能发、为什么不能"。
     * <p>
     * 存在意义是<b>把失败提前</b>：发药接口本身也会拦，但那时药师已经点过一次按钮、
     * 收到一句报错才知道要改。预检让发药窗口在选中处方时就把问题摆出来。
     * 返回空列表 = 可以发。
     */
    @Operation(summary = "处方麻精限量预检（返回违规清单，空=通过）")
    @GetMapping("/checkPrescription")
    public Result<List<NarcoticViolationVO>> checkPrescription(@RequestParam Long prescriptionId,
                                                               @RequestParam(required = false) String overLimitReason) {
        return Result.success(narcoticControlService.checkPrescription(prescriptionId, overLimitReason));
    }

    /**
     * 处方发药前预检（发药窗口主用）：能不能发 / 要不要复核药师 / 每个管制品种限量多少。
     *
     * <p>与 {@code checkPrescription} 并存不是冗余 —— 后者只报"有没有问题"，
     * 处方全合规时返回空列表，窗口据此无法判断要不要选复核人，
     * 只能"点一次、被拒一次"才知道。预检把管制明细清单也带出来。
     */
    @Operation(summary = "处方麻精预检（含管制明细清单与双人复核要求）")
    @GetMapping("/precheck")
    public Result<NarcoticPrecheckVO> precheck(@RequestParam Long prescriptionId,
                                               @RequestParam(required = false) String overLimitReason) {
        return Result.success(narcoticControlService.precheck(prescriptionId, overLimitReason));
    }
}

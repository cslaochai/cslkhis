package com.his.pharmacy.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.pharmacy.dto.AmpouleReturnDTO;
import com.his.pharmacy.dto.NarcoticRegisterPageQueryDTO;
import com.his.pharmacy.service.NarcoticControlService;
import com.his.pharmacy.vo.BizNarcoticRegisterVO;
import com.his.pharmacy.vo.NarcoticPrecheckVO;
import com.his.pharmacy.vo.NarcoticRegisterCountVO;
import com.his.pharmacy.vo.NarcoticViolationVO;
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

    /**
     * 麻精药品专册台账分页：关键字可查患者/处方/登记/药品/批号，按特殊管理分类与空安瓿回收状态过滤。
     * 用于「麻精药品专册」页查询卡。
     */
    @Operation(summary = "麻精药品专册分页查询")
    @PostMapping("/listPage")
    public Result<PageResult<BizNarcoticRegisterVO>> listPage(@Valid @RequestBody NarcoticRegisterPageQueryDTO query) {
        return Result.success(narcoticControlService.listPage(query));
    }

    /**
     * 专册计数：总登记 / 待回收空安瓿 / 已回收空安瓿。
     * 用于麻精专册页顶部计数卡，由后端聚合，翻页不影响数字。
     */
    @Operation(summary = "专册计数（总登记 / 待回收空安瓿 / 已回收空安瓿）")
    @GetMapping("/statusCount")
    public Result<NarcoticRegisterCountVO> statusCount() {
        return Result.success(narcoticControlService.statusCount());
    }

    /**
     * 登记空安瓿回收与剩余液销毁：只补记回收字段，不动发药时生成的业务内容与批号。
     * 用于麻精专册页的回收登记弹窗（专册唯一的受控写入口）。
     */
    @PreAuthorize("hasAuthority('pharmacy:narcotic:edit')")
    @Operation(summary = "空安瓿回收 / 剩余液销毁登记")
    @PostMapping("/ampouleReturn")
    public Result<BizNarcoticRegisterVO> ampouleReturn(@Valid @RequestBody AmpouleReturnDTO dto) {
        return Result.success(narcoticControlService.updateAmpouleReturn(dto));
    }

    /**
     * 处方麻精限量预检：返回超限定量清单，空清单即全部合规。
     * 供发药前把关环节回读「限几日/超几日」明细，作为拦截依据时须与完整预检结论一并看。
     */
    @Operation(summary = "处方麻精限量预检（返回违规清单，空=通过）")
    @GetMapping("/checkPrescription")
    public Result<List<NarcoticViolationVO>> checkPrescription(@RequestParam Long prescriptionId,
                                                               @RequestParam(required = false) String overLimitReason) {
        return Result.success(narcoticControlService.checkPrescription(prescriptionId, overLimitReason));
    }

    /**
     * 发药前完整预检：能否发药、要不要双人复核、要不要登记空安瓿，附各管制档限量与处方实际天数。
     * 用于发药窗口的发药复核弹框，在点「发药」前把拒因一次说清。
     */
    @Operation(summary = "处方麻精预检（含管制明细清单与双人复核要求）")
    @GetMapping("/precheck")
    public Result<NarcoticPrecheckVO> precheck(@RequestParam Long prescriptionId,
                                               @RequestParam(required = false) String overLimitReason) {
        return Result.success(narcoticControlService.precheck(prescriptionId, overLimitReason));
    }
}

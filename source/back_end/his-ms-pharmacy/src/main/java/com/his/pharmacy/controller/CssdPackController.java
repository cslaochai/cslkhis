package com.his.pharmacy.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.pharmacy.dto.CssdDTO;
import com.his.pharmacy.dto.CssdPackApplyDTO;
import com.his.pharmacy.dto.CssdPackConfirmDTO;
import com.his.pharmacy.dto.CssdPackMyPageQueryDTO;
import com.his.pharmacy.dto.CssdPackSignDTO;
import com.his.pharmacy.service.CssdService;
import com.his.pharmacy.vo.CssdPackVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * CSSD 消毒供应追溯控制器。
 *
 * <p>权限按岗位分两段：{@code asset:cssd:list} 看全量台账（供应室/管理员），
 * {@code asset:cssd:apply} 是临床科室的发起与签收（护士站），
 * {@code asset:cssd:edit} 是供应室的回收确认与流转。类上不再挂注解——类级注解会静默覆盖
 * 方法级授权，临床角色没有 list 码会被整类挡成 403。
 */
@Tag(name = "CSSD消毒供应追溯")
@RestController
@RequestMapping("/cssd/pack")
@RequiredArgsConstructor
public class CssdPackController {

    private final CssdService cssdService;

    /**
     * 新建一只器械包并把回收环节写成追溯链首节点，条码留空时按规则自动生成。
     * 用于消毒供应页的回收登记弹框（可先选器械包模板带出灭菌方式与组成清单）。
     */
    @PreAuthorize("hasAuthority('asset:cssd:edit')")
    @Operation(summary = "回收登记（新建器械包并记录回收节点）")
    @PostMapping("/receive")
    public Result<CssdPackVO> receive(@Valid @RequestBody CssdDTO.Receive dto) {
        return Result.success("回收登记成功", cssdService.receive(dto));
    }

    /**
     * 临床科室（护士站）提交回收申请：用完的器械要送回供应室，先落 0-待回收。
     * 供应室在消毒供应页确认回收后才进入清洗流程。
     */
    @PreAuthorize("hasAuthority('asset:cssd:apply')")
    @Operation(summary = "临床发起回收申请")
    @PostMapping("/apply")
    public Result<CssdPackVO> apply(@Valid @RequestBody CssdPackApplyDTO dto) {
        return Result.success("回收申请已提交，等待供应室回收", cssdService.apply(dto));
    }

    /**
     * 供应室确认回收（0-待回收 → 1-已回收），追溯链追加回收节点。
     */
    @PreAuthorize("hasAuthority('asset:cssd:edit')")
    @Operation(summary = "供应室确认回收")
    @PostMapping("/receiveConfirm")
    public Result<CssdPackVO> receiveConfirm(@Valid @RequestBody CssdPackConfirmDTO dto) {
        return Result.success("回收确认成功", cssdService.receiveConfirm(dto));
    }

    /**
     * 把器械包推进到追溯链的下一个环节并追加节点记录，目标环节由当前状态决定、不可跳步。
     * 用于消毒供应页行内「流转」弹框，清洗/打包/灭菌/发放逐环节由供应室人员点选。
     */
    @PreAuthorize("hasAuthority('asset:cssd:edit')")
    @Operation(summary = "流转到下一追溯节点")
    @PostMapping("/advance")
    public Result<CssdPackVO> advance(@Valid @RequestBody CssdDTO.Advance dto) {
        return Result.success("流转成功", cssdService.advance(dto));
    }

    /**
     * 申领科室收到无菌包后签收（6-已发放 → 7-已签收），闭环落到临床侧。
     */
    @PreAuthorize("hasAuthority('asset:cssd:apply')")
    @Operation(summary = "申领科室签收")
    @PostMapping("/sign")
    public Result<CssdPackVO> sign(@Valid @RequestBody CssdPackSignDTO dto) {
        return Result.success("签收成功", cssdService.sign(dto));
    }

    /**
     * 器械包追溯主档分页，可按条码/包名/科室关键字与当前环节状态筛。
     * 用于消毒供应页的器械包列表查询卡。
     */
    @PreAuthorize("hasAuthority('asset:cssd:list')")
    @Operation(summary = "器械包分页查询")
    @PostMapping("/listPage")
    public Result<PageResult<CssdPackVO>> listPage(@Valid @RequestBody CssdDTO.PageQuery dto) {
        var page = cssdService.listPage(dto);
        return Result.success(PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(),
                page.getRecords()));
    }

    /**
     * 当前岗位科室的器械包（待回收 / 待签收 / 已签收），供护士站「我的器械包」用。
     */
    @PreAuthorize("hasAuthority('asset:cssd:apply')")
    @Operation(summary = "本科室器械包分页查询")
    @PostMapping("/myListPage")
    public Result<PageResult<CssdPackVO>> myListPage(@Valid @RequestBody CssdPackMyPageQueryDTO dto) {
        var page = cssdService.myListPage(dto);
        return Result.success(PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(),
                page.getRecords()));
    }

    /**
     * 取单只器械包的档案与其全量追溯链节点（回收至今每一环节的操作人、结果、灭菌批次）。
     * 用于列表行点开的追溯详情弹框，展示这只包从回收到发放的完整链路。
     */
    @PreAuthorize("hasAnyAuthority('asset:cssd:list', 'asset:cssd:apply')")
    @Operation(summary = "器械包详情（含全量追溯链）")
    @GetMapping("/getDetailById")
    public Result<CssdPackVO> getDetailById(@RequestParam Long packId) {
        return Result.success(cssdService.getDetailById(packId));
    }
}

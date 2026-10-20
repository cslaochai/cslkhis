package com.his.pharmacy.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.pharmacy.dto.CssdDTO;
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
 */
@Tag(name = "CSSD消毒供应追溯")
@RestController
@RequestMapping("/cssd/pack")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('asset:cssd:list')")
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
     * 器械包追溯主档分页，可按条码/包名/科室关键字与当前环节状态筛。
     * 用于消毒供应页的器械包列表查询卡。
     */
    @Operation(summary = "器械包分页查询")
    @PostMapping("/listPage")
    public Result<PageResult<CssdPackVO>> listPage(@Valid @RequestBody CssdDTO.PageQuery dto) {
        var page = cssdService.listPage(dto);
        return Result.success(PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(),
                page.getRecords()));
    }

    /**
     * 取单只器械包的档案与其全量追溯链节点（回收至今每一环节的操作人、结果、灭菌批次）。
     * 用于列表行点开的追溯详情弹框，展示这只包从回收到发放的完整链路。
     */
    @Operation(summary = "器械包详情（含全量追溯链）")
    @GetMapping("/getDetailById")
    public Result<CssdPackVO> getDetailById(@RequestParam Long packId) {
        return Result.success(cssdService.getDetailById(packId));
    }
}

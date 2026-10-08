package com.his.operation.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.common.base.Result;
import com.his.operation.dto.AnesthesiaFollowupQueryPageDTO;
import com.his.operation.dto.AnesthesiaFollowupUpsertDTO;
import com.his.operation.service.AnesthesiaFollowupService;
import com.his.operation.vo.AnesthesiaFollowupVO;
import com.his.operation.vo.OperationApplyVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 麻醉术后随访端点
 */
@Tag(name = "麻醉术后随访")
@RestController
@RequestMapping("/patient/inpatient/anesthesiaFollowup")
@RequiredArgsConstructor
public class AnesthesiaFollowupController {

    private final AnesthesiaFollowupService anesthesiaFollowupService;

    @PreAuthorize("hasAuthority('ipd:anesthesia:list')")
    @Operation(summary = "随访分页（麻醉记录/住院/状态/关键字）")
    @GetMapping("/listPage")
    public Result<IPage<AnesthesiaFollowupVO>> listPage(@Valid AnesthesiaFollowupQueryPageDTO query) {
        return Result.success(anesthesiaFollowupService.listPage(query));
    }

    @PreAuthorize("hasAuthority('ipd:anesthesia:list')")
    @Operation(summary = "随访详情（含并发症字典）")
    @GetMapping("/getDetailById")
    public Result<AnesthesiaFollowupVO> getDetailById(@RequestParam Long id) {
        return Result.success(anesthesiaFollowupService.getDetailById(id));
    }

    @PreAuthorize("hasAuthority('ipd:anesthesia:list')")
    @Operation(summary = "某条麻醉记录的全部随访（按轮次升序）")
    @GetMapping("/listByRecord")
    public Result<List<AnesthesiaFollowupVO>> listByRecord(@RequestParam Long recordId) {
        return Result.success(anesthesiaFollowupService.listByRecord(recordId));
    }

    @PreAuthorize("hasAuthority('ipd:anesthesia:list')")
    @Operation(summary = "随访欠账数（工作台角标：已提交麻醉结束超24h且无已完成随访）")
    @GetMapping("/countOverduePending")
    public Result<Long> countOverduePending() {
        return Result.success(anesthesiaFollowupService.countOverduePending());
    }

    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "并发症要点字典（前端渲染勾选框）")
    @GetMapping("/adverseItemList")
    public Result<List<OperationApplyVO.CheckItem>> adverseItemList() {
        return Result.success(anesthesiaFollowupService.adverseItems());
    }

    @PreAuthorize("hasAuthority('ipd:anesthesia:add')")
    @Operation(summary = "新增/修改随访草稿（返回随访单号；轮次服务端定），已完成不可改")
    @PostMapping("/save")
    public Result<String> save(@RequestBody @Valid AnesthesiaFollowupUpsertDTO dto) {
        // 裸 Long 出参会被前端 Number 化丢精度，单号本来就是字符串，直接返回
        return Result.success("随访草稿已保存", anesthesiaFollowupService.save(dto));
    }

    @PreAuthorize("hasAuthority('ipd:anesthesia:edit')")
    @Operation(summary = "完成随访（草稿→已完成；疼痛/恢复必填，并发症经过+处理必填；完成即锁死）")
    @PostMapping("/finish")
    public Result<Void> finish(@RequestParam Long id) {
        anesthesiaFollowupService.finish(id);
        return Result.success("随访已完成（记录已锁定，不可再改）", null);
    }

    @PreAuthorize("hasAuthority('ipd:anesthesia:edit')")
    @Operation(summary = "删除随访（仅草稿可删；已完成是签过名的凭证，删不掉）")
    @DeleteMapping("/deleteById")
    public Result<Void> deleteById(@RequestParam Long id) {
        anesthesiaFollowupService.deleteById(id);
        return Result.success("随访草稿已删除", null);
    }
}

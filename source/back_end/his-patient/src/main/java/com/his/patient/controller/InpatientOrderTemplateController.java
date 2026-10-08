package com.his.patient.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.common.base.Result;
import com.his.patient.dto.InpatientOrderTemplateQueryPageDTO;
import com.his.patient.dto.InpatientOrderTemplateUpsertDTO;
import com.his.patient.service.InpatientOrderTemplateService;
import com.his.patient.vo.InpatientOrderTemplateDetailVO;
import com.his.patient.vo.InpatientOrderTemplateListVO;
import com.his.patient.vo.InpatientOrderTemplateSelectListVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 住院医嘱模板（医生个人模板，sql/103）。
 */
@Tag(name = "住院医嘱模板")
@RestController
@RequestMapping("/patient/inpatient/order/template")
@RequiredArgsConstructor
public class InpatientOrderTemplateController {

    private final InpatientOrderTemplateService inpatientOrderTemplateService;

    @PreAuthorize("hasAuthority('ipd:order:list')")
    @Operation(summary = "模板下拉候选（开立弹窗「套用模板」，只返当前医生自己的）")
    @GetMapping("/selectList")
    public Result<List<InpatientOrderTemplateSelectListVO>> selectList() {
        return Result.success(inpatientOrderTemplateService.selectList());
    }

    @PreAuthorize("hasAuthority('ipd:order:list')")
    @Operation(summary = "模板分页（模板管理弹窗）")
    @GetMapping("/listPage")
    public Result<IPage<InpatientOrderTemplateListVO>> listPage(@Valid InpatientOrderTemplateQueryPageDTO query) {
        return Result.success(inpatientOrderTemplateService.listPage(query));
    }

    @PreAuthorize("hasAuthority('ipd:order:list')")
    @Operation(summary = "模板明细（含明细行，套用与预览共用）")
    @GetMapping("/getById")
    public Result<InpatientOrderTemplateDetailVO> getById(@RequestParam Long id) {
        return Result.success(inpatientOrderTemplateService.getById(id));
    }

    @PreAuthorize("hasAuthority('ipd:order:add')")
    @Operation(summary = "新增/修改模板（一次提交=全量明细），返回模板ID")
    @PostMapping("/upsert")
    public Result<String> upsert(@RequestBody @Valid InpatientOrderTemplateUpsertDTO dto) {
        // 裸 Long 出参是 JSON number，前端一过 Number 就把雪花 ID 尾数改掉，
        // 拿它回查 getById 只会得到「不存在」—— 主键一律字符串出去。
        Long id = inpatientOrderTemplateService.upsert(dto);
        return Result.success("模板已保存", id == null ? null : String.valueOf(id));
    }

    @PreAuthorize("hasAuthority('ipd:order:delete')")
    @Operation(summary = "删除模板（只删模板，不影响已按它开出的医嘱）")
    @DeleteMapping("/deleteById")
    public Result<Void> deleteById(@RequestParam Long id) {
        inpatientOrderTemplateService.deleteById(id);
        return Result.success("模板已删除", null);
    }
}

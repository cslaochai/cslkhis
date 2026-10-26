package com.his.platform.controller;

import com.his.common.base.Result;
import com.his.platform.dto.DeptSubjectMapUpsertDTO;
import com.his.platform.dto.MedSubjectUpsertDTO;
import com.his.platform.service.SysMedSubjectService;
import com.his.platform.vo.DeptSubjectVO;
import com.his.platform.vo.MedSubjectVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 诊疗科目映射控制器：科室 ↔ 国家卫健委《医疗机构诊疗科目名录》。
 *
 * <p>为什么需要：病案首页、卫生统计直报、医保结算清单上报时，"科室"填的是标准诊疗科目代码
 * （如 03.01 呼吸内科专业），不是本院自编的 dept_code。本控制器维护科目主数据与对照关系，
 * 病案首页自身仍存院内 dept_id，上报时才折算。
 */
@Tag(name = "诊疗科目映射")
@RestController
@RequestMapping("/system/medSubject")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class MedSubjectController {

    private final SysMedSubjectService sysMedSubjectService;

    /**
     * 科目树（一级 + 二级），带「已映射科室数」。
     */
    @Operation(summary = "诊疗科目树")
    @GetMapping("/tree")
    public Result<List<MedSubjectVO>> tree() {
        return Result.success(sysMedSubjectService.listTree());
    }

    /**
     * 新增或修改诊疗科目。
     */
    @Operation(summary = "新增或修改诊疗科目")
    @PreAuthorize("hasAuthority('org:medSubject:add')")
    @PostMapping("/subjectUpsert")
    public Result<Void> subjectUpsert(@Valid @RequestBody MedSubjectUpsertDTO upsertDTO) {
        return Result.success(sysMedSubjectService.upsertSubject(upsertDTO), null);
    }

    /**
     * 删除诊疗科目。被科室映射引用或还有二级科目时会被拦下。
     */
    @Operation(summary = "删除诊疗科目")
    @PreAuthorize("hasAuthority('org:medSubject:delete')")
    @DeleteMapping("/deleteSubject")
    public Result<Void> deleteSubject(@RequestParam Long id) {
        sysMedSubjectService.deleteSubject(id);
        return Result.success("删除成功", null);
    }

    /**
     * 取某科室的科目对照（含主科目）。
     */
    @Operation(summary = "获取科室的诊疗科目对照")
    @GetMapping("/deptMap")
    public Result<DeptSubjectVO> deptMap(@RequestParam Long deptId) {
        return Result.success(sysMedSubjectService.getDeptMap(deptId));
    }

    /**
     * 反向查询：某科目被哪些科室当主科目用。
     */
    @Operation(summary = "按科目查科室")
    @GetMapping("/deptMapBySubject")
    public Result<List<DeptSubjectVO>> deptMapBySubject(@RequestParam String subjectCode) {
        return Result.success(sysMedSubjectService.listDeptMapBySubject(subjectCode));
    }

    /**
     * 还没配科目的临床科室（数据治理待办清单）。
     */
    @Operation(summary = "未配科目的临床科室")
    @GetMapping("/unmappedDepts")
    public Result<List<DeptSubjectVO>> unmappedDepts() {
        return Result.success(sysMedSubjectService.listUnmappedDepts());
    }

    /**
     * 保存某科室的科目映射（整单替换）。
     */
    @Operation(summary = "保存科室的诊疗科目映射")
    @PreAuthorize("hasAuthority('org:medSubject:add')")
    @PostMapping("/deptMapUpsert")
    public Result<Void> deptMapUpsert(@Valid @RequestBody DeptSubjectMapUpsertDTO upsertDTO) {
        return Result.success(sysMedSubjectService.saveDeptMap(upsertDTO), null);
    }
}

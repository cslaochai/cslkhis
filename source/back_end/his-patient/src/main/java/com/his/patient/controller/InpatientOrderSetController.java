package com.his.patient.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.common.base.Result;
import com.his.patient.dto.OrderSetQueryPageDTO;
import com.his.patient.dto.OrderSetUpsertDTO;
import com.his.patient.service.InpatientOrderSetService;
import com.his.patient.vo.OrderSetDetailVO;
import com.his.patient.vo.OrderSetListVO;
import com.his.patient.vo.OrderSetSelectListVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 医嘱组套模板（个人 / 科室 / 全院三级共享，sql/142）。
 *
 * <p>约定：查询 GET、写操作 POST、路径驼峰。
 *
 * <p><b>{@code @PreAuthorize} 全部标在方法上，不标类</b>：类级注解会静默覆盖所有没写自己注解的方法，
 * 之前就是这么把医生/药剂师挡在公共接口外面的（G5b）。
 *
 * <p>与个人模板（{@code /patient/inpatient/order/template}）分开是<b>故意的</b>：
 * 个人模板是医生在开立弹窗里顺手「另存为」的私人物件（权限码 ipd:order:*），
 * 组套是要被一个科室乃至全院复用的公共口径（ipd:orderSet:*）——
 * 谁有资格动全院组套、谁只能动自己的，必须在权限码上分得开。
 */
@Tag(name = "医嘱组套模板")
@RestController
@RequestMapping("/patient/inpatient/order/set")
@RequiredArgsConstructor
public class InpatientOrderSetController {

    private final InpatientOrderSetService orderSetService;

    @PreAuthorize("hasAuthority('ipd:orderSet:list')")
    @Operation(summary = "组套下拉候选（开立弹窗「套用组套」：可见的全院 + 本科室 + 自己的）")
    @GetMapping("/selectList")
    public Result<List<OrderSetSelectListVO>> selectList() {
        return Result.success(orderSetService.selectList());
    }

    @PreAuthorize("hasAuthority('ipd:orderSet:list')")
    @Operation(summary = "组套分页（管理页，落在可见集内）")
    @GetMapping("/listPage")
    public Result<IPage<OrderSetListVO>> listPage(@Valid OrderSetQueryPageDTO query) {
        return Result.success(orderSetService.listPage(query));
    }

    @PreAuthorize("hasAuthority('ipd:orderSet:list')")
    @Operation(summary = "组套明细（含明细行，编辑回显与预览共用）")
    @GetMapping("/getDetailById")
    public Result<OrderSetDetailVO> getDetailById(@RequestParam Long id) {
        return Result.success(orderSetService.getDetailById(id));
    }

    @PreAuthorize("hasAuthority('ipd:orderSet:add')")
    @Operation(summary = "新增/修改组套（一次提交=全量明细），返回组套ID")
    @PostMapping("/upsert")
    public Result<String> upsert(@RequestBody @Valid OrderSetUpsertDTO dto) {
        // 裸 Long 出参是 JSON number，前端一过 Number 就把雪花 ID 尾数改掉，
        // 拿它回查只会得到「不存在」—— 主键一律字符串出去。
        Long id = orderSetService.upsert(dto);
        return Result.success("组套已保存", id == null ? null : String.valueOf(id));
    }

    @PreAuthorize("hasAuthority('ipd:orderSet:delete')")
    @Operation(summary = "删除组套（只删模板，不影响已按它开出的医嘱）")
    @DeleteMapping("/deleteById")
    public Result<Void> deleteById(@RequestParam Long id) {
        orderSetService.deleteById(id);
        return Result.success("组套已删除", null);
    }
}

package com.his.inpatient.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.common.base.Result;
import com.his.inpatient.dto.OrderSetPageQueryDTO;
import com.his.inpatient.dto.OrderSetUpsertDTO;
import com.his.inpatient.service.InpatientOrderSetService;
import com.his.inpatient.vo.OrderSetDetailVO;
import com.his.inpatient.vo.OrderSetListVO;
import com.his.inpatient.vo.OrderSetSelectListVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 医嘱组套模板（个人 / 科室 / 全院三级共享，sql/142）。
 */
@Tag(name = "医嘱组套模板")
@RestController
@RequestMapping("/patient/inpatient/order/set")
@RequiredArgsConstructor
public class InpatientOrderSetController {

    private final InpatientOrderSetService inpatientOrderSetService;

    /**
     * 组套下拉候选：只返回当前登录医生可见的全院、本科室与本人组套。
     * 供住院医嘱开立环节「套用组套」回填开立表单时取数。
     */
    @PreAuthorize("hasAuthority('ipd:orderSet:list')")
    @Operation(summary = "组套下拉候选（开立弹窗「套用组套」：可见的全院 + 本科室 + 自己的）")
    @GetMapping("/selectList")
    public Result<List<OrderSetSelectListVO>> selectList() {
        return Result.success(inpatientOrderSetService.selectList());
    }

    /**
     * 组套分页：结果始终收在可见范围内。
     * 用于「医嘱组套（套餐）维护」页查询卡。
     */
    @PreAuthorize("hasAuthority('ipd:orderSet:list')")
    @Operation(summary = "组套分页（管理页，落在可见集内）")
    @GetMapping("/listPage")
    public Result<IPage<OrderSetListVO>> listPage(@Valid OrderSetPageQueryDTO query) {
        return Result.success(inpatientOrderSetService.listPage(query));
    }

    /**
     * 组套明细：连明细行一起返回。
     * 用于「医嘱组套（套餐）维护」页的编辑弹框回显与详情预览。
     */
    @PreAuthorize("hasAuthority('ipd:orderSet:list')")
    @Operation(summary = "组套明细（含明细行，编辑回显与预览共用）")
    @GetMapping("/getDetailById")
    public Result<OrderSetDetailVO> getDetailById(@RequestParam Long id) {
        return Result.success(inpatientOrderSetService.getDetailById(id));
    }

    /**
     * 新增或修改组套：明细按本次提交全量覆盖。
     * 用于「医嘱组套（套餐）维护」页编辑弹框保存。
     *
     * @return 组套ID 的文本形式；新增未落号时为 null
     */
    @PreAuthorize("hasAuthority('ipd:orderSet:add')")
    @Operation(summary = "新增/修改组套（一次提交=全量明细），返回组套ID")
    @PostMapping("/upsert")
    public Result<String> upsert(@RequestBody @Valid OrderSetUpsertDTO dto) {
        // 裸 Long 出参是 JSON number，前端一过 Number 就把雪花 ID 尾数改掉，
        // 拿它回查只会得到「不存在」—— 主键一律字符串出去。
        Long id = inpatientOrderSetService.upsert(dto);
        return Result.success("组套已保存", id == null ? null : String.valueOf(id));
    }

    /**
     * 删除组套模板。
     * 用于「医嘱组套（套餐）维护」页删除操作；已按该组套开出的医嘱不受影响。
     */
    @PreAuthorize("hasAuthority('ipd:orderSet:delete')")
    @Operation(summary = "删除组套（只删模板，不影响已按它开出的医嘱）")
    @DeleteMapping("/deleteById")
    public Result<Void> deleteById(@RequestParam Long id) {
        inpatientOrderSetService.deleteById(id);
        return Result.success("组套已删除", null);
    }
}

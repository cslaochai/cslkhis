package com.his.system.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.system.dto.DoseLimitPageQueryDTO;
import com.his.system.dto.DoseLimitUpsertDTO;
import com.his.system.dto.DrugInteractionPageQueryDTO;
import com.his.system.dto.DrugInteractionUpsertDTO;
import com.his.system.service.DrugKnowledgeService;
import com.his.system.vo.DoseLimitVO;
import com.his.system.vo.DrugInteractionVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 合理用药知识库（药物相互作用 × 剂量上限）维护
 */
@Tag(name = "合理用药知识库")
@RestController
@RequestMapping("/system/drugKnowledge")
@RequiredArgsConstructor
public class DrugKnowledgeController {

    private final DrugKnowledgeService drugKnowledgeService;

    /**
     * 按成分关键字分页查询药物相互作用条目。
     * 用于系统管理「合理用药知识库」页的「药物相互作用」页签表格。
     */
    @Operation(summary = "分页查询药物相互作用")
    @PreAuthorize("hasAuthority('system:drugKnowledge:list')")
    @PostMapping("/interactionListPage")
    public Result<PageResult<DrugInteractionVO>> interactionListPage(@Valid @RequestBody DrugInteractionPageQueryDTO queryDTO) {
        return Result.success(drugKnowledgeService.interactionListPage(queryDTO));
    }

    /**
     * 新增或修改一对成分的相互作用级别与说明，传主键即改、不传即建。
     * 用于该页签的新增/编辑弹框保存，也用于行内「停用/启用」开关——启停走同一个写入口，整行原样回传只改状态。
     */
    @Operation(summary = "新增/修改药物相互作用（返回保存后的整条，成分对顺序以服务端归一化结果为准）")
    @PreAuthorize("hasAuthority('system:drugKnowledge:add')")
    @PostMapping("/interactionUpsert")
    public Result<DrugInteractionVO> interactionUpsert(@Valid @RequestBody DrugInteractionUpsertDTO upsertDTO) {
        return Result.success(drugKnowledgeService.interactionUpsert(upsertDTO));
    }

    /**
     * 删除一条药物相互作用，物理删并释放该成分对的唯一约束占位。
     * 用于该页签行内的删除确认；只想暂时不生效时应改用停用，不要删档。
     */
    @Operation(summary = "删除药物相互作用（物理删）")
    @PreAuthorize("hasAuthority('system:drugKnowledge:delete')")
    @DeleteMapping("/interactionDeleteById")
    public Result<Void> interactionDeleteById(@RequestParam Long id) {
        drugKnowledgeService.interactionDeleteById(id);
        return Result.success();
    }

    /**
     * 按成分关键字分页查询剂量上限条目。
     * 用于系统管理「合理用药知识库」页的「剂量上限」页签表格。
     */
    @Operation(summary = "分页查询剂量上限")
    @PreAuthorize("hasAuthority('system:drugKnowledge:list')")
    @PostMapping("/doseListPage")
    public Result<PageResult<DoseLimitVO>> doseListPage(@Valid @RequestBody DoseLimitPageQueryDTO queryDTO) {
        return Result.success(drugKnowledgeService.doseLimitListPage(queryDTO));
    }

    /**
     * 新增或修改单个药的剂量上限，传主键即改、不传即建。
     * 用于该页签的新增/编辑弹框保存，也用于行内启停开关；保存后审方立刻按新上限比对，无需重启。
     */
    @Operation(summary = "新增/修改剂量上限（返回保存后的整条）")
    @PreAuthorize("hasAuthority('system:drugKnowledge:add')")
    @PostMapping("/doseUpsert")
    public Result<DoseLimitVO> doseUpsert(@Valid @RequestBody DoseLimitUpsertDTO upsertDTO) {
        return Result.success(drugKnowledgeService.doseLimitUpsert(upsertDTO));
    }

    /**
     * 删除一条剂量上限，物理删并释放该药的唯一约束占位。
     * 用于该页签行内的删除确认；只想让上限暂不生效时应改用停用。
     */
    @Operation(summary = "删除剂量上限（物理删）")
    @PreAuthorize("hasAuthority('system:drugKnowledge:delete')")
    @DeleteMapping("/doseDeleteById")
    public Result<Void> doseDeleteById(@RequestParam Long id) {
        drugKnowledgeService.doseLimitDeleteById(id);
        return Result.success();
    }
}

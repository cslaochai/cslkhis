package com.his.operation.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.common.base.Result;
import com.his.operation.dto.AnesthesiaFollowupPageQueryDTO;
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

    /**
     * 麻醉术后随访台账分页：按麻醉记录、住院或随访状态收窄。
     * 用于麻醉工作站「麻醉随访」页签的查询列表。
     */
    @PreAuthorize("hasAuthority('ipd:anesthesia:list')")
    @Operation(summary = "随访分页（麻醉记录/住院/状态/关键字）")
    @GetMapping("/listPage")
    public Result<IPage<AnesthesiaFollowupVO>> listPage(@Valid AnesthesiaFollowupPageQueryDTO query) {
        return Result.success(anesthesiaFollowupService.listPage(query));
    }

    /**
     * 单条随访的全量字段，并发症勾选项字典随出参一起带回。
     * 随访留档回看时取这一份；列表只给简版字段，不含字典。
     */
    @PreAuthorize("hasAuthority('ipd:anesthesia:list')")
    @Operation(summary = "随访详情（含并发症字典）")
    @GetMapping("/getDetailById")
    public Result<AnesthesiaFollowupVO> getDetailById(@RequestParam Long id) {
        return Result.success(anesthesiaFollowupService.getDetailById(id));
    }

    /**
     * 一条麻醉记录名下的全部随访，按轮次升序成组返回。
     * 一次麻醉可有多轮随访（术后即刻 / 24h / 48h / 追加），按记录聚合成随访时间线时取这份。
     */
    @PreAuthorize("hasAuthority('ipd:anesthesia:list')")
    @Operation(summary = "某条麻醉记录的全部随访（按轮次升序）")
    @GetMapping("/listByRecord")
    public Result<List<AnesthesiaFollowupVO>> listByRecord(@RequestParam Long recordId) {
        return Result.success(anesthesiaFollowupService.listByRecord(recordId));
    }

    /**
     * 随访欠账数：麻醉结束超 24 小时且一条已完成随访都没有的麻醉记录数。
     * 用于麻醉工作台的随访超期角标提醒，只出计数不出明细。
     */
    @PreAuthorize("hasAuthority('ipd:anesthesia:list')")
    @Operation(summary = "随访欠账数（工作台角标：已提交麻醉结束超24h且无已完成随访）")
    @GetMapping("/countOverduePending")
    public Result<Long> countOverduePending() {
        return Result.success(anesthesiaFollowupService.countOverduePending());
    }

    /**
     * 术后并发症要点字典：随访表单要勾选的那些项。
     * 供随访登记表单渲染并发症勾选框，码值与后端核查项注册表同源。
     */
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "并发症要点字典（前端渲染勾选框）")
    @GetMapping("/adverseItemList")
    public Result<List<OperationApplyVO.CheckItem>> adverseItemList() {
        return Result.success(anesthesiaFollowupService.adverseItems());
    }

    /**
     * 保存随访草稿（新增与修改同一入口，按 id 分支）。
     * 用于随访登记弹框的「保存草稿」；轮次与随访人由服务端判定，已锁定单据不可再走到这里。
     *
     * @return 随访单号
     */
    @PreAuthorize("hasAuthority('ipd:anesthesia:add')")
    @Operation(summary = "新增/修改随访草稿（返回随访单号；轮次服务端定），已完成不可改")
    @PostMapping("/save")
    public Result<String> save(@RequestBody @Valid AnesthesiaFollowupUpsertDTO dto) {
        // 裸 Long 出参会被前端 Number 化丢精度，单号本来就是字符串，直接返回
        return Result.success("随访草稿已保存", anesthesiaFollowupService.save(dto));
    }

    /**
     * 把随访草稿定稿为已完成并当场锁死。
     * 用于随访列表行上的「完成随访」确认动作：疼痛评分与恢复情况、并发症经过与处理在这一刻收口，之后只能另起一轮。
     */
    @PreAuthorize("hasAuthority('ipd:anesthesia:edit')")
    @Operation(summary = "完成随访（草稿→已完成；疼痛/恢复必填，并发症经过+处理必填；完成即锁死）")
    @PostMapping("/finish")
    public Result<Void> finish(@RequestParam Long id) {
        anesthesiaFollowupService.finish(id);
        return Result.success("随访已完成（记录已锁定，不可再改）", null);
    }

    /**
     * 删掉一份随访草稿。
     * 用于清理误建的草稿；已定稿的随访是留档凭证，删不掉。
     */
    @PreAuthorize("hasAuthority('ipd:anesthesia:edit')")
    @Operation(summary = "删除随访（仅草稿可删；已完成是签过名的凭证，删不掉）")
    @DeleteMapping("/deleteById")
    public Result<Void> deleteById(@RequestParam Long id) {
        anesthesiaFollowupService.deleteById(id);
        return Result.success("随访草稿已删除", null);
    }
}

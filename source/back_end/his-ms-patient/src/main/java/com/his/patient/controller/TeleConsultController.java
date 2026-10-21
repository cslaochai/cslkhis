package com.his.patient.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.patient.dto.*;
import com.his.patient.service.TeleConsultService;
import com.his.patient.vo.OnlineConsultVO;
import com.his.patient.vo.TeleConsultStatVO;
import com.his.patient.vo.TeleConsultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 远程会诊与线上问诊控制器：跨机构会诊申请安排，以及互联网复诊问诊单接诊回复的接口入口。
 */
@Tag(name = "互联网医院与远程会诊")
@RestController
@RequestMapping("/patient/teleconsult")
@RequiredArgsConstructor
public class TeleConsultController {

    private final TeleConsultService teleConsultService;

    // 远程会诊

    /**
     * 远程会诊单分页。
     * 用于「远程会诊」页查询卡。
     */
    @PreAuthorize("hasAuthority('ipd:teleconsult:list')")
    @Operation(summary = "远程会诊分页")
    @PostMapping("/teleListPage")
    public Result<PageResult<TeleConsultVO>> teleListPage(@Valid @RequestBody TeleConsultPageQueryDTO dto) {
        return Result.success(teleConsultService.teleListPage(dto));
    }

    /**
     * 单张远程会诊单详情。
     * 用于会诊详情弹框查看，也是修改弹框的回显取数口。
     */
    @PreAuthorize("hasAuthority('ipd:teleconsult:list')")
    @Operation(summary = "远程会诊详情")
    @GetMapping("/teleGetDetailById")
    public Result<TeleConsultVO> teleGetDetailById(@RequestParam Long id) {
        return Result.success(teleConsultService.teleGetDetailById(id));
    }

    /**
     * 提交或修改远程会诊申请：仅待安排状态可改。
     * 用于「远程会诊」页申请与会诊修改弹框提交。
     */
    @PreAuthorize("hasAuthority('ipd:teleconsult:add')")
    @Operation(summary = "远程会诊申请 / 修改（仅待安排可改）")
    @PostMapping("/teleUpsert")
    public Result<TeleConsultVO> teleUpsert(@Valid @RequestBody TeleConsultUpsertDTO dto) {
        return Result.success(dto.getId() == null ? "会诊申请已提交" : "修改成功", teleConsultService.teleUpsert(dto));
    }

    /**
     * 安排会诊：待安排推进到已安排，定会诊时间与接入方式。
     * 用于「远程会诊」页的安排弹框。
     */
    @PreAuthorize("hasAuthority('ipd:teleconsult:edit')")
    @Operation(summary = "安排会诊（待安排→已安排，定时间与接入方式）")
    @PostMapping("/teleArrange")
    public Result<TeleConsultVO> teleArrange(@Valid @RequestBody TeleArrangeDTO dto) {
        return Result.success("已安排", teleConsultService.teleArrange(dto));
    }

    /**
     * 完成会诊：已安排推进到已完成，会诊意见必填。
     * 用于「远程会诊」页的完成弹框回填外院专家意见。
     */
    @PreAuthorize("hasAuthority('ipd:teleconsult:edit')")
    @Operation(summary = "完成会诊（已安排→已完成，会诊意见必填）")
    @PostMapping("/teleComplete")
    public Result<TeleConsultVO> teleComplete(@Valid @RequestBody TeleActionDTO dto) {
        return Result.success("会诊已完成", teleConsultService.teleComplete(dto));
    }

    /**
     * 取消会诊：原因必填，取消即终态。
     * 用于「远程会诊」页的取消弹框。
     */
    @PreAuthorize("hasAuthority('ipd:teleconsult:edit')")
    @Operation(summary = "取消会诊（原因必填，终态）")
    @PostMapping("/teleCancel")
    public Result<TeleConsultVO> teleCancel(@Valid @RequestBody TeleActionDTO dto) {
        return Result.success("已取消", teleConsultService.teleCancel(dto));
    }

    /**
     * 删除远程会诊单：仅待安排可删。
     * 用于「远程会诊」页行内删除。
     */
    @PreAuthorize("hasAuthority('ipd:teleconsult:add')")
    @Operation(summary = "删除远程会诊单（软删；仅待安排）")
    @DeleteMapping("/teleDeleteById")
    public Result<Boolean> teleDeleteById(@RequestParam Long id) {
        boolean ok = teleConsultService.teleDeleteById(id);
        return Result.success(ok ? "已删除" : "删除失败", null);
    }

    // 线上问诊

    /**
     * 线上问诊单分页。
     * 用于「线上问诊」页查询卡。
     */
    @PreAuthorize("hasAuthority('ipd:teleconsult:list')")
    @Operation(summary = "线上问诊分页")
    @PostMapping("/onlineListPage")
    public Result<PageResult<OnlineConsultVO>> onlineListPage(@Valid @RequestBody OnlinePageQueryDTO dto) {
        return Result.success(teleConsultService.onlineListPage(dto));
    }

    /**
     * 单条线上问诊详情。
     * 用于「线上问诊」页详情弹框。
     */
    @PreAuthorize("hasAuthority('ipd:teleconsult:list')")
    @Operation(summary = "线上问诊详情")
    @GetMapping("/onlineGetDetailById")
    public Result<OnlineConsultVO> onlineGetDetailById(@RequestParam Long id) {
        return Result.success(teleConsultService.onlineGetDetailById(id));
    }

    /**
     * 发起线上问诊：互联网复诊或咨询。
     * 用于「线上问诊」页新建问诊弹框提交。
     */
    @PreAuthorize("hasAuthority('ipd:teleconsult:add')")
    @Operation(summary = "发起线上问诊（互联网复诊/咨询）")
    @PostMapping("/onlineApply")
    public Result<OnlineConsultVO> onlineApply(@Valid @RequestBody OnlineApplyDTO dto) {
        return Result.success("问诊已发起", teleConsultService.onlineApply(dto));
    }

    /**
     * 接诊：待接诊推进到接诊中，接诊人取当前登录人。
     * 用于「线上问诊」页行内接诊。
     */
    @PreAuthorize("hasAuthority('ipd:teleconsult:edit')")
    @Operation(summary = "接诊（待接诊→接诊中，接诊人=当前登录人）")
    @PostMapping("/onlineAccept")
    public Result<OnlineConsultVO> onlineAccept(@RequestParam Long id) {
        return Result.success("已接诊", teleConsultService.onlineAccept(id));
    }

    /**
     * 回复并结束问诊：接诊中推进到已完成，回复内容必填。
     * 用于「线上问诊」页的回复弹框。
     */
    @PreAuthorize("hasAuthority('ipd:teleconsult:edit')")
    @Operation(summary = "回复并结束（接诊中→已完成，回复必填）")
    @PostMapping("/onlineReply")
    public Result<OnlineConsultVO> onlineReply(@Valid @RequestBody OnlineReplyDTO dto) {
        return Result.success("已回复并结束", teleConsultService.onlineReply(dto));
    }

    /**
     * 退诊：原因必填，退诊即终态。
     * 用于「线上问诊」页的退诊弹框。
     */
    @PreAuthorize("hasAuthority('ipd:teleconsult:edit')")
    @Operation(summary = "退诊（原因必填，终态）")
    @PostMapping("/onlineReject")
    public Result<OnlineConsultVO> onlineReject(@Valid @RequestBody TeleActionDTO dto) {
        return Result.success("已退诊", teleConsultService.onlineReject(dto));
    }

    /**
     * 删除问诊单：仅待接诊可删。
     * 用于「线上问诊」页行内删除。
     */
    @PreAuthorize("hasAuthority('ipd:teleconsult:add')")
    @Operation(summary = "删除问诊单（软删；仅待接诊）")
    @DeleteMapping("/onlineDeleteById")
    public Result<Boolean> onlineDeleteById(@RequestParam Long id) {
        boolean ok = teleConsultService.onlineDeleteById(id);
        return Result.success(ok ? "已删除" : "删除失败", null);
    }

    // 统计

    /**
     * 远程会诊与线上问诊的状态分布统计。
     * 用于「远程会诊」与「线上问诊」两页顶部统计卡。
     */
    @PreAuthorize("hasAuthority('ipd:teleconsult:list')")
    @Operation(summary = "统计（远程会诊与线上问诊的状态分布）")
    @GetMapping("/stat")
    public Result<TeleConsultStatVO> stat() {
        return Result.success(teleConsultService.stat());
    }
}

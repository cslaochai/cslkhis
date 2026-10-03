package com.his.patient.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.patient.dto.OnlineApplyDTO;
import com.his.patient.dto.OnlineQueryPageDTO;
import com.his.patient.dto.OnlineReplyDTO;
import com.his.patient.dto.TeleActionDTO;
import com.his.patient.dto.TeleArrangeDTO;
import com.his.patient.dto.TeleConsultQueryPageDTO;
import com.his.patient.dto.TeleConsultUpsertDTO;
import com.his.patient.service.TeleConsultService;
import com.his.patient.vo.OnlineConsultVO;
import com.his.patient.vo.TeleConsultStatVO;
import com.his.patient.vo.TeleConsultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 互联网医院 / 远程会诊。
 *
 * <p>两条线共一个控制器：远程会诊（院际/跨院专家）与线上问诊（互联网复诊）。
 * 按项目规范 @PreAuthorize 全部标到方法；按钮可用性由后端 VO 的 can* 字段给。
 */
@Tag(name = "互联网医院与远程会诊")
@RestController
@RequestMapping("/patient/teleconsult")
@RequiredArgsConstructor
public class TeleConsultController {

    private final TeleConsultService teleConsultService;

    // 远程会诊

    @PreAuthorize("hasAuthority('ipd:teleconsult:list')")
    @Operation(summary = "远程会诊分页")
    @PostMapping("/teleListPage")
    public Result<PageResult<TeleConsultVO>> teleListPage(@RequestBody TeleConsultQueryPageDTO dto) {
        return Result.success(teleConsultService.teleListPage(dto));
    }

    @PreAuthorize("hasAuthority('ipd:teleconsult:list')")
    @Operation(summary = "远程会诊详情")
    @GetMapping("/teleGetDetailById")
    public Result<TeleConsultVO> teleGetDetailById(@RequestParam Long id) {
        return Result.success(teleConsultService.teleGetDetailById(id));
    }

    @PreAuthorize("hasAuthority('ipd:teleconsult:add')")
    @Operation(summary = "远程会诊申请 / 修改（仅待安排可改）")
    @PostMapping("/teleUpsert")
    public Result<TeleConsultVO> teleUpsert(@Valid @RequestBody TeleConsultUpsertDTO dto) {
        return Result.success(dto.getId() == null ? "会诊申请已提交" : "修改成功", teleConsultService.teleUpsert(dto));
    }

    @PreAuthorize("hasAuthority('ipd:teleconsult:edit')")
    @Operation(summary = "安排会诊（待安排→已安排，定时间与接入方式）")
    @PostMapping("/teleArrange")
    public Result<TeleConsultVO> teleArrange(@Valid @RequestBody TeleArrangeDTO dto) {
        return Result.success("已安排", teleConsultService.teleArrange(dto));
    }

    @PreAuthorize("hasAuthority('ipd:teleconsult:edit')")
    @Operation(summary = "完成会诊（已安排→已完成，会诊意见必填）")
    @PostMapping("/teleComplete")
    public Result<TeleConsultVO> teleComplete(@Valid @RequestBody TeleActionDTO dto) {
        return Result.success("会诊已完成", teleConsultService.teleComplete(dto));
    }

    @PreAuthorize("hasAuthority('ipd:teleconsult:edit')")
    @Operation(summary = "取消会诊（原因必填，终态）")
    @PostMapping("/teleCancel")
    public Result<TeleConsultVO> teleCancel(@Valid @RequestBody TeleActionDTO dto) {
        return Result.success("已取消", teleConsultService.teleCancel(dto));
    }

    @PreAuthorize("hasAuthority('ipd:teleconsult:add')")
    @Operation(summary = "删除远程会诊单（软删；仅待安排）")
    @DeleteMapping("/teleDeleteById")
    public Result<Boolean> teleDeleteById(@RequestParam Long id) {
        boolean ok = teleConsultService.teleDeleteById(id);
        return Result.success(ok ? "已删除" : "删除失败", null);
    }

    // 线上问诊

    @PreAuthorize("hasAuthority('ipd:teleconsult:list')")
    @Operation(summary = "线上问诊分页")
    @PostMapping("/onlineListPage")
    public Result<PageResult<OnlineConsultVO>> onlineListPage(@RequestBody OnlineQueryPageDTO dto) {
        return Result.success(teleConsultService.onlineListPage(dto));
    }

    @PreAuthorize("hasAuthority('ipd:teleconsult:list')")
    @Operation(summary = "线上问诊详情")
    @GetMapping("/onlineGetDetailById")
    public Result<OnlineConsultVO> onlineGetDetailById(@RequestParam Long id) {
        return Result.success(teleConsultService.onlineGetDetailById(id));
    }

    @PreAuthorize("hasAuthority('ipd:teleconsult:add')")
    @Operation(summary = "发起线上问诊（互联网复诊/咨询）")
    @PostMapping("/onlineApply")
    public Result<OnlineConsultVO> onlineApply(@Valid @RequestBody OnlineApplyDTO dto) {
        return Result.success("问诊已发起", teleConsultService.onlineApply(dto));
    }

    @PreAuthorize("hasAuthority('ipd:teleconsult:edit')")
    @Operation(summary = "接诊（待接诊→接诊中，接诊人=当前登录人）")
    @PostMapping("/onlineAccept")
    public Result<OnlineConsultVO> onlineAccept(@RequestParam Long id) {
        return Result.success("已接诊", teleConsultService.onlineAccept(id));
    }

    @PreAuthorize("hasAuthority('ipd:teleconsult:edit')")
    @Operation(summary = "回复并结束（接诊中→已完成，回复必填）")
    @PostMapping("/onlineReply")
    public Result<OnlineConsultVO> onlineReply(@Valid @RequestBody OnlineReplyDTO dto) {
        return Result.success("已回复并结束", teleConsultService.onlineReply(dto));
    }

    @PreAuthorize("hasAuthority('ipd:teleconsult:edit')")
    @Operation(summary = "退诊（原因必填，终态）")
    @PostMapping("/onlineReject")
    public Result<OnlineConsultVO> onlineReject(@Valid @RequestBody TeleActionDTO dto) {
        return Result.success("已退诊", teleConsultService.onlineReject(dto));
    }

    @PreAuthorize("hasAuthority('ipd:teleconsult:add')")
    @Operation(summary = "删除问诊单（软删；仅待接诊）")
    @DeleteMapping("/onlineDeleteById")
    public Result<Boolean> onlineDeleteById(@RequestParam Long id) {
        boolean ok = teleConsultService.onlineDeleteById(id);
        return Result.success(ok ? "已删除" : "删除失败", null);
    }

    // 统计

    @PreAuthorize("hasAuthority('ipd:teleconsult:list')")
    @Operation(summary = "统计（远程会诊与线上问诊的状态分布）")
    @GetMapping("/stat")
    public Result<TeleConsultStatVO> stat() {
        return Result.success(teleConsultService.stat());
    }
}

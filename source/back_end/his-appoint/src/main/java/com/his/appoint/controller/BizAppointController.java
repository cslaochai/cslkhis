package com.his.appoint.controller;

import com.his.appoint.dto.*;
import com.his.appoint.service.BizAppointService;
import com.his.appoint.vo.AppointStatusCountVO;
import com.his.appoint.vo.BizAppointInfoListVO;
import com.his.appoint.vo.RevisitFeePreviewVO;
import com.his.appoint.vo.RevisitRecordSelectVO;
import com.his.common.base.PageResult;
import com.his.common.base.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 挂号管理控制器
 */
@Tag(name = "挂号管理")
@RestController
@RequestMapping("/appoint")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('opd:appointments:list', 'opd:doctorWorkstation:list', 'opd:todayVisits:list')")
public class BizAppointController {

    private final BizAppointService bizAppointService;

    @Operation(summary = "分页查询挂号记录")
    @GetMapping("/listPage")
    public Result<PageResult<BizAppointInfoListVO>> listPage(@Valid AppointQueryDTO queryDTO) {
        PageResult<BizAppointInfoListVO> result = bizAppointService.listPage(queryDTO);
        return Result.success(result);
    }

    @Operation(summary = "挂号状态统计：六格状态卡一次取全（口径与 listPage 一致）")
    @GetMapping("/statusCount")
    public Result<AppointStatusCountVO> statusCount(@Valid AppointQueryDTO queryDTO) {
        return Result.success(bizAppointService.statusCount(queryDTO));
    }

    @Operation(summary = "预约看板：一次取整段区间的全部挂号（不分页）")
    @PostMapping("/boardList")
    public Result<List<BizAppointInfoListVO>> boardList(@Valid @RequestBody AppointBoardQueryDTO queryDTO) {
        return Result.success(bizAppointService.boardList(queryDTO));
    }

    @PreAuthorize("hasAuthority('opd:appointments:add')")
    @Operation(summary = "患者挂号或编辑挂号（新增/修改合一）")
    @PostMapping("/appointUpsert")
    public Result<BizAppointInfoListVO> appointUpsert(@Valid @RequestBody AppointUpsertDTO upsertDTO) {
        return Result.success(upsertDTO.getId() == null ? "挂号成功" : "修改成功", bizAppointService.appointUpsert(upsertDTO));
    }

    /**
     * 医生站建复诊号（只放来源 1-当日回诊、2-医嘱复诊预约）。
     *
     * <p>不复用 {@code /appointUpsert}：那个接口要求 {@code opd:appointments:add}，而 sql/120 起
     * 医生角色（10013）已经拿不到挂号页面的授权 —— 挂在它上面等于医生站的「建复诊」按钮
     * <b>对医生永远 403</b>（前端按 {@code opd:doctorWorkstation:add} 显示按钮，两边口径不一致，
     * 现象是点了才报错）。这里按医生站自己的码收口。
     *
     * <p>来源 3 由患者小程序发起、来源 4 由随访任务发起，都不该从医生站写，
     * 否则「谁发起的」这条事实会失真，收费策略也就能被绕过。
     */
    @PreAuthorize("hasAuthority('opd:doctorWorkstation:add')")
    @Operation(summary = "医生站建复诊号（当日回诊 / 医嘱复诊预约）")
    @PostMapping("/revisitUpsert")
    public Result<BizAppointInfoListVO> revisitUpsert(@Valid @RequestBody AppointUpsertDTO upsertDTO) {
        return Result.success("复诊号已创建", bizAppointService.revisitUpsert(upsertDTO));
    }

    /**
     * 复诊费用预估（窗口/医生站/随访共用；患者自助走 {@code /miniapp/revisit/feePreview}）。
     *
     * <p>用 {@code isAuthenticated()} 而不是按钮码：这一个判定被挂号收费、医生站、病房随访三处
     * 页面复用，挂任何一个码都会让另外两个 403（同「通用参照数据不配权限码」的口径）。
     * 只读、不涉敏，越权由 {@code patientScopeViolated} 收口（员工放行、患者只碰自己绑定的就诊人）。
     */
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "复诊费用预估：提交挂号前问一次这张号收多少钱")
    @PostMapping("/revisitFeePreview")
    public Result<RevisitFeePreviewVO> revisitFeePreview(@RequestBody @Valid RevisitFeePreviewDTO previewDTO) {
        return Result.success(bizAppointService.revisitFeePreviewScoped(previewDTO));
    }

    /**
     * 复诊「原病历」候选列表（窗口/医生站共用）。
     *
     * <p>不开在 {@code /emr/getByPatientId} 上：那个接口挂在 EmrController 的<b>类级</b>
     * {@code hasAnyAuthority('opd:doctorWorkstation:list', ...)} 底下，收费员和前台导诊碰不到，
     * 而「挂复诊要不要选原病历」是挂号窗口每天的动作。这里只出下拉要的几个字段，
     * 权限按「通用参照数据」口径写 {@code isAuthenticated()}，越权仍由 patientScopeViolated 收口。
     */
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "复诊原病历候选列表（按就诊日倒序）")
    @GetMapping("/revisitRecordSelectList")
    public Result<List<RevisitRecordSelectVO>> revisitRecordSelectList(@RequestParam Long patientId) {
        return Result.success(bizAppointService.revisitRecordSelectListScoped(patientId));
    }

    @PreAuthorize("hasAuthority('opd:appointments:delete')")
    @Operation(summary = "退号")
    @PostMapping("/cancelRegist")
    public Result<Void> cancelRegist(@RequestBody @Valid AppointCancelDTO appointCancelDTO) {
        bizAppointService.cancelRegist(appointCancelDTO);
        return Result.success();
    }

    @Operation(summary = "获取挂号详情")
    @GetMapping("/getDetail")
    public Result<BizAppointInfoListVO> getDetail(@Valid AppointQueryDTO appointQueryDTO) {
        return Result.success(bizAppointService.getDetail(appointQueryDTO));
    }

    @PreAuthorize("hasAuthority('opd:appointments:edit')")
    @Operation(summary = "更新挂号状态")
    @PostMapping("/updateStatus")
    public Result<Void> updateStatus(@Valid @RequestBody AppointStatusUpsertDTO appointStatusDTO) {
        boolean success = bizAppointService.updateStatus(appointStatusDTO.getRegistId(), appointStatusDTO.getStatus());
        return success ? Result.success() : Result.error("更新状态失败");
    }

}

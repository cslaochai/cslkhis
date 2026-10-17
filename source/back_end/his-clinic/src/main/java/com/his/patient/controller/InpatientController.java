package com.his.patient.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.common.base.Result;
import com.his.patient.dto.*;
import com.his.patient.dto.InpatientAdmitDTO;
import com.his.patient.service.InpatientService;
import com.his.patient.vo.*;
import com.his.patient.vo.BedVO;
import com.his.patient.vo.WardVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 住院管理（第 1 期：入出院闭环 + 病案首页）
 */
@Tag(name = "住院管理")
@RestController
@RequestMapping("/patient/inpatient")
@RequiredArgsConstructor
public class InpatientController {

    /**
     * 住院参照数据的读权限持有者：入出院/会诊/手术/转科/输血/摆药 六类页面各自的 list 码任一即可。
     */
    private static final String READERS = "'ipd:inpatient:list', 'ipd:consultation:list', 'ipd:surgery:list', "
            + "'ipd:transfer:list', 'medtech:transfusion:list', 'pharmacy:wardDispense:list'";

    private static final String READ = "hasAnyAuthority(" + READERS + ")";

    /**
     * 在院患者列表额外放开给住院结算页：财务岗只有 ipd:settlement:list，
     * 让它为了列出于院患者去借入出院管理的码，等于把医护岗的入口一并授出去。
     */
    private static final String READ_OR_SETTLEMENT = "hasAnyAuthority(" + READERS + ", 'ipd:settlement:list')";

    private final InpatientService inpatientService;

    @PreAuthorize(READ_OR_SETTLEMENT)
    @Operation(summary = "住院列表分页（在院/已出院）")
    @GetMapping("/listPage")
    public Result<IPage<InpatientVO>> listPage(@Valid InpatientQueryPageDTO query) {
        return Result.success(inpatientService.listPage(query));
    }

    @Operation(summary = "本科室住院列表分页（医生站/护士站左栏，科室由服务端按登录态强制过滤）")
    @GetMapping("/listMyDeptPage")
    @PreAuthorize("isAuthenticated()")
    public Result<IPage<InpatientVO>> listMyDeptPage(@Valid InpatientQueryPageDTO query) {
        return Result.success(inpatientService.listMyDeptPage(query));
    }

    @PreAuthorize(READ)
    @Operation(summary = "住院详情（入院信息+病案首页+诊断明细+手术明细）")
    @GetMapping("/detail")
    public Result<InpatientDetailVO> detail(@RequestParam Long admissionId) {
        return Result.success(inpatientService.detail(admissionId));
    }

    @PreAuthorize(READ)
    @Operation(summary = "住院统计卡片")
    @GetMapping("/stats")
    public Result<InpatientStatsVO> stats() {
        return Result.success(inpatientService.stats());
    }

    @PreAuthorize(READ)
    @Operation(summary = "病区列表（床位数实时取自床位表）")
    @GetMapping("/ward/list")
    public Result<List<WardVO>> listWards() {
        return Result.success(inpatientService.listWards());
    }

    @PreAuthorize(READ)
    @Operation(summary = "床位列表（床位图/选床）")
    @GetMapping("/bed/list")
    public Result<List<BedVO>> listBeds(@RequestParam(required = false) Long wardId,
                                        @RequestParam(required = false) Long deptId,
                                        @RequestParam(required = false) Integer bedStatus) {
        return Result.success(inpatientService.listBeds(wardId, deptId, bedStatus));
    }

    /**
     * 病区床位图：一床一卡（含空床），科室边界由服务端按登录态收口。
     * <p>方法级注解必须写 —— 本类没有类级兜底，漏一行就退化成「任何登录用户都能拉全院床位图」，
     * 而读侧的科室收口只在这条链路上做，接口本身不再有任何闸门。
     */
    @Operation(summary = "病区床位图（住院患者总览，按登录岗位科室收口）")
    @GetMapping("/bedMap")
    @PreAuthorize("hasAnyAuthority('ipd:nurse:list', 'ipd:order:list', 'ipd:inpatient:list')")
    public Result<BedMapVO> bedMap(@Valid BedMapQueryDTO query) {
        return Result.success(inpatientService.bedMap(query));
    }

    @PreAuthorize("hasAuthority('ipd:inpatient:edit')")
    @Operation(summary = "入院登记（分床并占用床位）")
    @PostMapping("/admit")
    public Result<String> admit(@Valid @RequestBody InpatientAdmitDTO dto) {
        // 雪花ID 超过 JS 的 2^53，直接返回 Long 会在浏览器端被静默截断（2100945558440022018 → ...0022000），
        // 前端拿这个 ID 回头查详情/换床/出院必然「入院记录不存在」。统一以字符串出参。
        return Result.success("入院登记成功", String.valueOf(inpatientService.admit(dto)));
    }

    @PreAuthorize("hasAuthority('ipd:inpatient:edit')")
    @Operation(summary = "换床（限同一科室内部）")
    @PostMapping("/transfer")
    public Result<Void> transfer(@RequestBody @Valid InpatientTransferDTO dto) {
        inpatientService.transfer(dto);
        return Result.success("换床成功", null);
    }

    @PreAuthorize("hasAuthority('ipd:inpatient:edit')")
    @Operation(summary = "出院办理（释放床位并回写病案首页）")
    @PostMapping("/discharge")
    public Result<Void> discharge(@RequestBody @Valid InpatientDischargeDTO dto) {
        inpatientService.discharge(dto);
        return Result.success("出院办理成功", null);
    }

    @PreAuthorize("hasAuthority('ipd:inpatient:add')")
    @Operation(summary = "保存病案首页（含诊断/手术明细，整表替换）")
    @PostMapping("/summary/save")
    public Result<InpatientDetailVO> saveSummary(@RequestBody @Valid InpatientSummaryUpsertDTO dto) {
        return Result.success("病案首页已保存", inpatientService.saveSummary(dto));
    }
}

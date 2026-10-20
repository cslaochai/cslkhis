package com.his.patient.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.common.base.Result;
import com.his.patient.dto.*;
import com.his.patient.dto.InpatientAdmitDTO;
import com.his.patient.service.AttendingRelationService;
import com.his.patient.service.ChargeNurseRelationService;
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
    private final AttendingRelationService attendingRelationService;
    private final ChargeNurseRelationService chargeNurseRelationService;

    /**
     * 住院患者分页：按在院/已出院状态与科室、病区、主管医师、关键字过滤。
     * 用于入出院管理页查询卡；住院结算、会诊与 VTE 监测也复用同一口径取在院名单。
     */
    @PreAuthorize(READ_OR_SETTLEMENT)
    @Operation(summary = "住院列表分页（在院/已出院）")
    @GetMapping("/listPage")
    public Result<IPage<InpatientVO>> listPage(@Valid InpatientPageQueryDTO query) {
        return Result.success(inpatientService.listPage(query));
    }

    /**
     * 本科室在院患者分页：科室由服务端按登录岗位强制收口，不接受前端传科室。
     * 用于医生站与护士站左栏的在院患者列表。
     */
    @Operation(summary = "本科室住院列表分页（医生站/护士站左栏，科室由服务端按登录态强制过滤）")
    @GetMapping("/listMyDeptPage")
    @PreAuthorize("isAuthenticated()")
    public Result<IPage<InpatientVO>> listMyDeptPage(@Valid InpatientPageQueryDTO query) {
        return Result.success(inpatientService.listMyDeptPage(query));
    }

    /**
     * 住院全量详情：入院信息、病案首页、诊断明细与手术明细一次取齐。
     * 用于入出院管理页的住院详情弹框，也作为病案首页编辑的回显数据源。
     */
    @PreAuthorize(READ)
    @Operation(summary = "住院详情（入院信息+病案首页+诊断明细+手术明细）")
    @GetMapping("/detail")
    public Result<InpatientDetailVO> detail(@RequestParam Long admissionId) {
        return Result.success(inpatientService.detail(admissionId));
    }

    /**
     * 住院规模统计：在院、今日入出院、待收治住院证与床位占用及使用率。
     * 用于入出院管理页顶部统计卡片。
     */
    @PreAuthorize(READ)
    @Operation(summary = "住院统计卡片")
    @GetMapping("/stats")
    public Result<InpatientStatsVO> stats() {
        return Result.success(inpatientService.stats());
    }

    /**
     * 病区清单及其实时床位数。
     * 用于入出院管理页与床位中心的病区筛选下拉，也供病区维度的护理统计按病区取数。
     */
    @PreAuthorize(READ)
    @Operation(summary = "病区列表（床位数实时取自床位表）")
    @GetMapping("/ward/list")
    public Result<List<WardVO>> listWards() {
        return Result.success(inpatientService.listWards());
    }

    /**
     * 床位清单：可按病区、科室与床位状态过滤，用于挑出可用床位。
     * 用于入院登记分床、科内换床与转科申请里选目标床位。
     */
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

    /**
     * 入院登记：按门诊住院证建立住院记录、分床并占用所选床位，返回入院ID文本。
     * 用于入出院管理页「入院登记」弹框的收治提交。
     */
    @PreAuthorize("hasAuthority('ipd:inpatient:edit')")
    @Operation(summary = "入院登记（分床并占用床位）")
    @PostMapping("/admit")
    public Result<String> admit(@Valid @RequestBody InpatientAdmitDTO dto) {
        // 雪花ID 超过 JS 的 2^53，直接返回 Long 会在浏览器端被静默截断（2100945558440022018 → ...0022000），
        // 前端拿这个 ID 回头查详情/换床/出院必然「入院记录不存在」。统一以字符串出参。
        return Result.success("入院登记成功", String.valueOf(inpatientService.admit(dto)));
    }

    /**
     * 换床：把在院患者从当前床位移到同科室的空床，两侧床位占用与病区统计同步更新。
     * 用于入出院管理页的「换床」弹框；跨科移动属转科流程，不经本接口。
     */
    @PreAuthorize("hasAuthority('ipd:inpatient:edit')")
    @Operation(summary = "换床（限同一科室内部）")
    @PostMapping("/transfer")
    public Result<Void> transfer(@RequestBody @Valid InpatientTransferDTO dto) {
        inpatientService.transfer(dto);
        return Result.success("换床成功", null);
    }

    /**
     * 出院办理：核对结算与出院方式一致性后结束住院、释放床位并登记出院。
     * 用于入出院管理页的「出院办理」弹框。
     */
    @PreAuthorize("hasAuthority('ipd:inpatient:edit')")
    @Operation(summary = "出院办理（释放床位并回写病案首页）")
    @PostMapping("/discharge")
    public Result<Void> discharge(@RequestBody @Valid InpatientDischargeDTO dto) {
        inpatientService.discharge(dto);
        return Result.success("出院办理成功", null);
    }

    /**
     * 保存病案首页：连同诊断与手术明细整表覆盖，返回保存后的住院详情。
     * 用于入出院管理页病案首页编辑弹框的提交。
     */
    @PreAuthorize("hasAuthority('ipd:inpatient:add')")
    @Operation(summary = "保存病案首页（含诊断/手术明细，整表替换）")
    @PostMapping("/summary/save")
    public Result<InpatientDetailVO> saveSummary(@RequestBody @Valid InpatientSummaryUpsertDTO dto) {
        return Result.success("病案首页已保存", inpatientService.saveSummary(dto));
    }

    // ==================== 主管医生管床关系（biz_attending_relation） ====================

    /**
     * 某次住院的管床关系列表（默认只列有效，可传 status=0 看已结束的历史交接）。
     * 用于住院详情「主管医生」区的现管医生与交接留痕。
     */
    @PreAuthorize(READ)
    @Operation(summary = "管床关系列表（主管/主诊组长/协作）")
    @GetMapping("/attending/list")
    public Result<List<AttendingRelationVO>> attendingList(@RequestParam Long admissionId,
                                                            @RequestParam(required = false) Integer status) {
        return Result.success(attendingRelationService.listByAdmission(admissionId, status));
    }

    /**
     * 建立/转交管床关系：主管、主诊组长这类唯一负责关系，重新指派时旧记录自动置失效留痕，
     * 不删行。仅在院可操作，已出院关系作为病案凭证不再改动。
     */
    @PreAuthorize("hasAuthority('ipd:inpatient:edit')")
    @Operation(summary = "建立/转交管床关系（主管/主诊组长/协作）")
    @PostMapping("/attending/bind")
    public Result<String> attendingBind(@RequestBody @Valid AttendingBindDTO dto) {
        return Result.success("管床关系已保存", String.valueOf(attendingRelationService.bind(dto)));
    }

    /**
     * 结束一段管床关系（置失效并写失效时间，保留行留痕）。
     */
    @PreAuthorize("hasAuthority('ipd:inpatient:edit')")
    @Operation(summary = "结束管床关系（置失效留痕）")
    @PostMapping("/attending/end")
    public Result<Void> attendingEnd(@RequestParam Long id) {
        attendingRelationService.endById(id);
        return Result.success("管床关系已结束", null);
    }

    /**
     * 误建关系的物理删除（唯一键不含删除标志，软删会占住键位导致无法重建）。
     */
    @PreAuthorize("hasAuthority('ipd:inpatient:edit')")
    @Operation(summary = "删除误建管床关系（物理删）")
    @DeleteMapping("/attending/delete")
    public Result<Void> attendingDelete(@RequestParam Long id) {
        attendingRelationService.deleteById(id);
        return Result.success("已删除", null);
    }

    // ==================== 责任护士关系（biz_charge_nurse_relation） ====================

    /**
     * 某次住院的责任护士关系列表（默认只列有效，status=0 看历史交接）。
     */
    @PreAuthorize(READ)
    @Operation(summary = "责任护士关系列表")
    @GetMapping("/nurse/list")
    public Result<List<ChargeNurseRelationVO>> nurseList(@RequestParam Long admissionId,
                                                          @RequestParam(required = false) Integer status) {
        return Result.success(chargeNurseRelationService.listByAdmission(admissionId, status));
    }

    /**
     * 建立/转交责任护士：一次住院一名有效主责，重新指派时旧记录置失效留痕。
     */
    @PreAuthorize("hasAuthority('ipd:inpatient:edit')")
    @Operation(summary = "建立/转交责任护士")
    @PostMapping("/nurse/bind")
    public Result<String> nurseBind(@RequestBody @Valid ChargeNurseBindDTO dto) {
        return Result.success("责任护士已保存", String.valueOf(chargeNurseRelationService.bind(dto)));
    }

    /**
     * 结束责任护士关系（置失效留痕）。
     */
    @PreAuthorize("hasAuthority('ipd:inpatient:edit')")
    @Operation(summary = "结束责任护士关系（置失效留痕）")
    @PostMapping("/nurse/end")
    public Result<Void> nurseEnd(@RequestParam Long id) {
        chargeNurseRelationService.endById(id);
        return Result.success("责任护士关系已结束", null);
    }

    /**
     * 删除误建责任护士关系（物理删）。
     */
    @PreAuthorize("hasAuthority('ipd:inpatient:edit')")
    @Operation(summary = "删除误建责任护士关系（物理删）")
    @DeleteMapping("/nurse/delete")
    public Result<Void> nurseDelete(@RequestParam Long id) {
        chargeNurseRelationService.deleteById(id);
        return Result.success("已删除", null);
    }
}

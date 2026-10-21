package com.his.charge.controller;


import com.his.charge.dto.*;
import com.his.charge.service.YbChronicService;
import com.his.charge.vo.ChronicCatalogVO;
import com.his.charge.vo.ChronicRegListVO;
import com.his.charge.vo.ChronicRegSummaryVO;
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
 * 门诊慢特病控制器：病种目录维护与患者备案台账的接口入口，备案是患者能否按门特结算的资格依据。
 * 菜单 1011，sql/163。
 */
@Tag(name = "慢特病人员备案")
@RestController
@RequestMapping("/charge/ybChronic")
@RequiredArgsConstructor
public class YbChronicController {

    private final YbChronicService ybChronicService;

    /**
     * 病种目录分页：按病种类别与启停状态筛国家门特病种。
     * 用于「门诊慢特病病种目录」页查询卡，也是备案台账页筛病种时的取数来源。
     */
    @PreAuthorize("hasAuthority('finance:insuranceChronic:list')")
    @Operation(summary = "病种目录分页")
    @GetMapping("/catalogListPage")
    public Result<PageResult<ChronicCatalogVO>> catalogListPage(@Valid ChronicCatalogPageQueryDTO queryDTO) {
        return Result.success(ybChronicService.catalogListPage(queryDTO));
    }

    /**
     * 启用中的病种全量列表，供表单下拉一次取齐。
     * 用于备案新增/修改弹框选病种；参照数据跨岗位复用，只要求登录不配权限码。
     */
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "启用中的病种目录（备案表单下拉，参照数据不配权限码）")
    @GetMapping("/catalogSelectList")
    public Result<List<ChronicCatalogVO>> catalogSelectList() {
        return Result.success(ybChronicService.selectCatalogList());
    }

    /**
     * 病种目录新增或修改：病种编码唯一，只启停不删除，保证历史备案能追溯回病种。
     * 用于病种目录页的编辑弹框保存。
     */
    @PreAuthorize("hasAuthority('finance:insuranceChronic:add')")
    @Operation(summary = "病种目录新增/修改（编码唯一，只启停不删）")
    @PostMapping("/catalogUpsert")
    public Result<ChronicCatalogVO> catalogUpsert(@Valid @RequestBody ChronicCatalogUpsertDTO dto) {
        return Result.success(ybChronicService.catalogUpsert(dto));
    }

    /**
     * 病种目录启停：停用后不再出现在备案下拉里，已备案的关系不受影响。
     * 用于病种目录页行内的启停开关。
     */
    @PreAuthorize("hasAuthority('finance:insuranceChronic:add')")
    @Operation(summary = "病种目录启停")
    @PostMapping("/changeCatalogStatus")
    public Result<Void> changeCatalogStatus(@RequestParam Long id, @RequestParam Integer status) {
        ybChronicService.changeCatalogStatus(id, status);
        return Result.success(null);
    }

    /**
     * 备案台账分页：展示态含已过期备案，便于人事与医保经办续备。
     * 用于「慢特病人员备案」页查询卡的主表格。
     */
    @PreAuthorize("hasAuthority('finance:insuranceChronic:list')")
    @Operation(summary = "备案台账分页（展示态含已过期）")
    @GetMapping("/regListPage")
    public Result<PageResult<ChronicRegListVO>> regListPage(@Valid ChronicRegPageQueryDTO queryDTO) {
        return Result.success(ybChronicService.regListPage(queryDTO));
    }

    /**
     * 单条备案的取数，含病种、有效期与经办信息。
     * 用于备案详情弹框只读回显，以及备案编辑弹框打开时的表单回显。
     */
    @PreAuthorize("hasAuthority('finance:insuranceChronic:list')")
    @Operation(summary = "备案详情")
    @GetMapping("/regGetById")
    public Result<ChronicRegListVO> regGetById(@RequestParam Long id) {
        return Result.success(ybChronicService.regGetById(id));
    }

    /**
     * 某患者当前在用的门特资格列表（按有效期现算，过期的不在内）。
     * 供开方与收费结算环节判断这次就诊能否按门特走统筹报销，属资格核对而非资金动作。
     */
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "患者在用门特资格（医生站/收费判断能否走门特）")
    @GetMapping("/regActiveOfPatient")
    public Result<List<ChronicRegListVO>> regActiveOfPatient(@RequestParam Long patientId) {
        return Result.success(ybChronicService.regActiveOfPatient(patientId));
    }

    /**
     * 备案状态汇总计数：有效、待续备、已注销、已驳回四档一次给全。
     * 用于备案台账页顶部的汇总卡片，数字由后端聚合，页面不自造。
     */
    @PreAuthorize("hasAuthority('finance:insuranceChronic:list')")
    @Operation(summary = "备案汇总（有效/待续备/已注销/已驳回）")
    @GetMapping("/regSummary")
    public Result<ChronicRegSummaryVO> regSummary() {
        return Result.success(ybChronicService.regSummary());
    }

    /**
     * 备案新增或修改：经办人缺省取当前登录人，只有有效备案能改，同患者同病种的有效期不许重叠。
     * 用于备案台账页的新增与编辑弹框登记患者的门特资格。
     */
    @PreAuthorize("hasAuthority('finance:insuranceChronic:add')")
    @Operation(summary = "备案新增/修改（经办人默认当前登录人，仅有效可改）")
    @PostMapping("/regUpsert")
    public Result<ChronicRegListVO> regUpsert(@Valid @RequestBody ChronicRegUpsertDTO dto) {
        return Result.success(ybChronicService.regUpsert(dto));
    }

    /**
     * 备案注销：登记终态且不可逆，注销后该患者不再具备这条门特资格。
     * 用于备案台账页对有效备案的注销操作，需填注销原因。
     */
    @PreAuthorize("hasAuthority('finance:insuranceChronic:cancel')")
    @Operation(summary = "备案注销（终态不可逆）")
    @PostMapping("/regCancel")
    public Result<Void> regCancel(@Valid @RequestBody ChronicRegTerminalDTO dto) {
        ybChronicService.regCancel(dto);
        return Result.success(null);
    }

    /**
     * 备案驳回：把一条已登记的备案判为不合格，同为不可逆终态，补材料后另起一条新备案。
     * 用于备案台账页经办人员纠正录错或材料不符的备案，与注销共用同一处理权限码。
     */
    @PreAuthorize("hasAuthority('finance:insuranceChronic:cancel')")
    @Operation(summary = "备案驳回（终态不可逆，补材料后另起新单）")
    @PostMapping("/regReject")
    public Result<Void> regReject(@Valid @RequestBody ChronicRegTerminalDTO dto) {
        ybChronicService.regReject(dto);
        return Result.success(null);
    }
}

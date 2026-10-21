package com.his.charge.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.common.constant.SystemConfigKeyConst;
import com.his.common.enums.DipVersionEnum;
import com.his.charge.config.DipSchemeConfig;
import com.his.charge.dto.DipSimDTO;
import com.his.charge.dto.MedpayConfigUpsertDTO;
import com.his.charge.service.DipSimService;
import com.his.charge.vo.DipSimVO;
import com.his.charge.vo.DipVersionVO;
import com.his.charge.vo.MedpayConfigVO;
import com.his.spi.service.SystemConfigFacadeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * DIP 分组模拟控制器（院内简化模拟器；正式方案接入后整体替换，与 DRG 模拟器同构）。
 */
@Tag(name = "DIP 分组模拟")
@RestController
@RequestMapping("/report/dip")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('report:dip:list')")
public class DipSimController {

    private final DipSimService dipSimService;

    private final DipSchemeConfig dipSchemeConfig;

    private final SystemConfigFacadeService sysConfigService;

    @Operation(summary = "医保付费主参数（主付费方式 / DIP 病种库版本 / DIP 点值）")
    @GetMapping("/config")
    public Result<MedpayConfigVO> medpayConfig() {
        MedpayConfigVO vo = new MedpayConfigVO();
        vo.setMode(sysConfigService.getConfigValue(SystemConfigKeyConst.MEDPAY_MODE, "DRG"));
        vo.setDipVersion(sysConfigService.getConfigValue(SystemConfigKeyConst.DIP_DEFAULT_VERSION,
                DipVersionEnum.DIP_2_0.getCode()));
        vo.setPointValue(sysConfigService.getConfigValue(SystemConfigKeyConst.DIP_POINT_VALUE, null));
        return Result.success(vo);
    }

    @PreAuthorize("hasAuthority('system:config:add')")
    @Operation(summary = "保存医保付费主参数，并刷新 DIP 版本缓存")
    @PostMapping("/config")
    public Result<Void> saveMedpayConfig(@Valid @RequestBody MedpayConfigUpsertDTO dto) {
        String mode = dto.getMode() == null ? "" : dto.getMode().trim();
        if (!"DRG".equalsIgnoreCase(mode) && !"DIP".equalsIgnoreCase(mode)) {
            return Result.error("主付费方式仅支持 DRG / DIP");
        }
        String dipVersion = DipVersionEnum.requireValid(dto.getDipVersion());
        sysConfigService.setConfigValue(SystemConfigKeyConst.MEDPAY_MODE, "医保付费主方式", mode.toUpperCase());
        sysConfigService.setConfigValue(SystemConfigKeyConst.DIP_DEFAULT_VERSION, "DIP 病种库默认版本", dipVersion);
        String pointValue = dto.getPointValue() == null ? "" : dto.getPointValue().trim();
        sysConfigService.setConfigValue(SystemConfigKeyConst.DIP_POINT_VALUE, "DIP 点值（元/分值）", pointValue);
        dipSchemeConfig.updateDefaultVersion(dipVersion);
        return Result.success("医保付费参数已保存", null);
    }

    @Operation(summary = "单条模拟（首页主诊断为空时补传 icdCode，手术操作为空时补传 operCode）")
    @PostMapping("/simulate")
    public Result<DipSimVO.SimResult> simulate(@Valid @RequestBody DipSimDTO.Simulate dto) {
        return Result.success("模拟完成", dipSimService.simulate(dto));
    }

    @Operation(summary = "批量模拟（不传 ids 默认最近 50 条）")
    @PostMapping("/simulateBatch")
    public Result<DipSimVO.SummaryListVO> simulateBatch(@Valid @RequestBody DipSimDTO.SimulateBatch dto) {
        return Result.success("批量模拟完成", dipSimService.simulateBatch(dto));
    }

    @Operation(summary = "模拟结果分页")
    @PostMapping("/result/listPage")
    public Result<PageResult<DipSimVO.ResultRow>> resultPage(@Valid @RequestBody DipSimDTO.ResultQuery dto) {
        var page = dipSimService.resultPage(dto);
        return Result.success(PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(),
                page.getRecords()));
    }

    @Operation(summary = "可模拟首页列表 + 入组/盈亏统计")
    @GetMapping("/summary/list")
    public Result<DipSimVO.SummaryListVO> summaryList(@RequestParam(required = false) Integer limit,
                                                      @RequestParam(required = false) String version) {
        return Result.success(dipSimService.summaryList(limit, version, null));
    }

    @Operation(summary = "病种目录（biz_dip_disease）")
    @GetMapping("/disease/list")
    public Result<List<DipSimVO.DiseaseRow>> diseaseList(@RequestParam(required = false) String version) {
        return Result.success(dipSimService.diseaseList(version));
    }

    @PreAuthorize("hasAnyAuthority('system:config:add')")
    @Operation(summary = "当前 DIP 结算默认病种库版本")
    @GetMapping("/version")
    public Result<DipVersionVO> dipVersion() {
        DipVersionVO vo = new DipVersionVO();
        vo.setDbVersion(dipSchemeConfig.getCurrentVersion());
        vo.setRedisKey(DipSchemeConfig.REDIS_KEY);
        return Result.success(vo);
    }

    @PreAuthorize("hasAuthority('system:config:add')")
    @Operation(summary = "设置 DIP 结算默认病种库版本")
    @PostMapping("/version")
    public Result<Void> setDipVersion(@RequestParam String version) {
        dipSchemeConfig.updateDefaultVersion(version);
        return Result.success("已切换 DIP 结算默认病种库版本为 " + version, null);
    }
}

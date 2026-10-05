package com.his.patient.controller;

import com.his.common.base.Result;
import com.his.patient.dto.PatientContactQueryDTO;
import com.his.patient.dto.PatientContactUpsertDTO;
import com.his.patient.service.PatientHealthProfileService;
import com.his.patient.vo.PatientContactVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 患者联系人控制器
 *
 * <p>一个患者可有多个联系人（患者联系方式）。建档时若填写了紧急联系人，
 * 由患者主档保存流程自动落成一条「主要联系人」（isPrimary=1）；本控制器负责后续的
 * 查询、新增、修改与删除。
 *
 * <p>⚠ {@code relationship} 是 {&#64;code 患者关系字典} 的**码值**，出参另带
 * {@code relationshipText} 文案。此前 DTO 把它声明成 String 并注明「如：父亲、配偶」，
 * 于是「配偶」被 MySQL 隐式转成 0 静默落库 —— 现在类型上就写不进这种值了。
 */
@Tag(name = "患者联系人")
@RestController
@RequestMapping("/patient/contact")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('patient:profile:list')")
public class PatientContactController {

    private final PatientHealthProfileService healthProfileService;

    @Operation(summary = "查询患者的联系人列表")
    @PostMapping("/list")
    public Result<List<PatientContactVO>> list(@Valid @RequestBody PatientContactQueryDTO queryDTO) {
        return Result.success(healthProfileService.getProfile(queryDTO.getPatientId()).getContacts());
    }

    @Operation(summary = "根据ID查询联系人")
    @GetMapping("/getById")
    public Result<PatientContactVO> getById(@RequestParam Long contactId) {
        return Result.success(healthProfileService.getContact(contactId));
    }

    @PreAuthorize("hasAuthority('patient:profile:add')")
    @Operation(summary = "新增或修改联系人")
    @PostMapping("/contactUpsert")
    public Result<PatientContactVO> contactUpsert(@RequestBody @Valid PatientContactUpsertDTO contactUpsertDTO) {
        return Result.success(healthProfileService.saveContact(contactUpsertDTO));
    }

    @PreAuthorize("hasAuthority('patient:profile:delete')")
    @Operation(summary = "根据ID删除联系人")
    @DeleteMapping("/deleteById")
    public Result<Void> deleteById(@RequestParam Long contactId) {
        healthProfileService.deleteContact(contactId);
        return Result.success();
    }
}

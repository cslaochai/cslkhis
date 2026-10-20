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
 * 患者联系人控制器：紧急联系人的读取、登记与删除入口，是健康档案六组分写之一。
 */
@Tag(name = "患者联系人")
@RestController
@RequestMapping("/patient/contact")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('patient:profile:list')")
public class PatientContactController {

    private final PatientHealthProfileService patientHealthProfileService;

    /**
     * 按患者取联系人列表。
     * 供只需读联系人这一组的取数场景；档案页展示走健康档案聚合接口，不经过这里。
     */
    @Operation(summary = "查询患者的联系人列表")
    @PostMapping("/list")
    public Result<List<PatientContactVO>> list(@Valid @RequestBody PatientContactQueryDTO queryDTO) {
        return Result.success(patientHealthProfileService.getProfile(queryDTO.getPatientId()).getContacts());
    }

    /**
     * 按 id 取单个联系人，联系电话与地址为明文。
     * 供联系人修改弹框回填表单前核对原始值。
     */
    @Operation(summary = "根据ID查询联系人")
    @GetMapping("/getById")
    public Result<PatientContactVO> getById(@RequestParam Long contactId) {
        return Result.success(patientHealthProfileService.getContact(contactId));
    }

    /**
     * 新增或修改一名联系人（与患者关系按码值传、姓名、电话、是否主要联系人）。
     * 用于健康档案页联系人分组的新增/修改弹框保存。
     */
    @PreAuthorize("hasAuthority('patient:profile:add')")
    @Operation(summary = "新增或修改联系人")
    @PostMapping("/contactUpsert")
    public Result<PatientContactVO> contactUpsert(@RequestBody @Valid PatientContactUpsertDTO contactUpsertDTO) {
        return Result.success(patientHealthProfileService.saveContact(contactUpsertDTO));
    }

    /**
     * 按 id 删除一名联系人。
     * 用于健康档案页联系人分组行操作「删除」的二次确认之后。
     */
    @PreAuthorize("hasAuthority('patient:profile:delete')")
    @Operation(summary = "根据ID删除联系人")
    @DeleteMapping("/deleteById")
    public Result<Void> deleteById(@RequestParam Long contactId) {
        patientHealthProfileService.deleteContact(contactId);
        return Result.success();
    }
}

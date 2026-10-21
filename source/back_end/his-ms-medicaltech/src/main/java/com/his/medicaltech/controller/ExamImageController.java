package com.his.medicaltech.controller;

import com.his.common.base.Result;
import com.his.medicaltech.dto.ExamImageMockImportDTO;
import com.his.medicaltech.dto.ExamImageUploadDTO;
import com.his.medicaltech.service.ExamImageService;
import com.his.medicaltech.vo.ExamImageVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 简化 PACS：检查/检验影像帧（sql/137）
 */
@Tag(name = "检查影像（简化 PACS）")
@RestController
@RequestMapping("/medicaltech/examImage")
@RequiredArgsConstructor
public class ExamImageController {

    private final ExamImageService examImageService;

    /**
     * 按业务类型与申请单取名下影像帧清单。
     * 用于医技检查工作站与放射诊断各页面的影像面板取数。
     */
    @Operation(summary = "按申请单查询影像帧列表")
    @GetMapping("/listByApplyId")
    @PreAuthorize("isAuthenticated()")
    public Result<List<ExamImageVO>> listByApplyId(@RequestParam Integer bizType, @RequestParam Long applyId) {
        return Result.success(examImageService.listByApply(bizType, applyId));
    }

    /**
     * 接收人工上传的影像帧文件落盘并挂到申请单归档。
     * 用于医技影像面板的手工补录上传动作。
     */
    @Operation(summary = "上传影像帧（multipart：file + bizType/applyId/modality）")
    @PostMapping("/upload")
    @PreAuthorize("hasAuthority('medtech:inspectionWorkstation:imageAdd')")
    public Result<ExamImageVO> upload(@RequestPart("file") MultipartFile file,
                                      @Valid ExamImageUploadDTO uploadDTO) {
        return Result.success("上传成功", examImageService.upload(file, uploadDTO));
    }

    /**
     * 服务端生成测试影像帧，模拟真设备接入后的导入效果。
     * 用于联调学习阶段影像面板的「模拟 DICOM 导入」入口，不接真设备。
     */
    @Operation(summary = "模拟 DICOM 导入（后端生成测试帧，学习阶段不接真设备）")
    @PostMapping("/mockImport")
    @PreAuthorize("hasAuthority('medtech:inspectionWorkstation:imageAdd')")
    public Result<List<ExamImageVO>> mockImport(@RequestBody @Valid ExamImageMockImportDTO importDTO) {
        return Result.success("导入成功", examImageService.mockImport(importDTO));
    }

    /**
     * 移除误传或作废检查的影像帧，物理删文件记录并留操作原因。
     * 用于医技影像面板的删帧动作。
     */
    @Operation(summary = "删除影像帧（物理删 + 审计留痕）")
    @DeleteMapping("/deleteById")
    @PreAuthorize("hasAuthority('medtech:inspectionWorkstation:imageDelete')")
    public Result<Void> deleteById(@RequestParam Long id,
                                   @RequestParam(required = false) String reason) {
        boolean ok = examImageService.deleteById(id, reason);
        return ok ? Result.success("已删除（操作已留痕）", null) : Result.error("删除失败");
    }
}

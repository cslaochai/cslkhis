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
 *
 * <p>权限口径：看图跟着「能不能进这张报告所在的岗位」走，不单独设码 ——
 * 影像清单的读口子挂在检查工作站页面码（401）下，医生站与患者端是各自报告接口顺带带出 images，
 * 不新增入口；只有<b>写</b>（上传/导入）与<b>删</b>需要单独按钮码，
 * 因为「能不能把人家的片子删掉」是真正的权限边界。
 */
@Tag(name = "检查影像（简化 PACS）")
@RestController
@RequestMapping("/medicaltech/examImage")
@RequiredArgsConstructor
public class ExamImageController {

    private final ExamImageService examImageService;

    @Operation(summary = "按申请单查询影像帧列表")
    @GetMapping("/listByApplyId")
    @PreAuthorize("isAuthenticated()")
    public Result<List<ExamImageVO>> listByApplyId(@RequestParam Integer bizType, @RequestParam Long applyId) {
        return Result.success(examImageService.listByApply(bizType, applyId));
    }

    @Operation(summary = "上传影像帧（multipart：file + bizType/applyId/modality）")
    @PostMapping("/upload")
    @PreAuthorize("hasAuthority('medtech:inspectionWorkstation:imageAdd')")
    public Result<ExamImageVO> upload(@RequestPart("file") MultipartFile file,
                                      @Valid ExamImageUploadDTO uploadDTO) {
        return Result.success("上传成功", examImageService.upload(file, uploadDTO));
    }

    @Operation(summary = "模拟 DICOM 导入（后端生成测试帧，学习阶段不接真设备）")
    @PostMapping("/mockImport")
    @PreAuthorize("hasAuthority('medtech:inspectionWorkstation:imageAdd')")
    public Result<List<ExamImageVO>> mockImport(@RequestBody @Valid ExamImageMockImportDTO importDTO) {
        return Result.success("导入成功", examImageService.mockImport(importDTO));
    }

    @Operation(summary = "删除影像帧（物理删 + 审计留痕）")
    @DeleteMapping("/deleteById")
    @PreAuthorize("hasAuthority('medtech:inspectionWorkstation:imageDelete')")
    public Result<Void> deleteById(@RequestParam Long id,
                                   @RequestParam(required = false) String reason) {
        boolean ok = examImageService.deleteById(id, reason);
        return ok ? Result.success("已删除（操作已留痕）", null) : Result.error("删除失败");
    }
}

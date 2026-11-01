package com.his.clinical.trigger;

import com.his.clinical.service.MedicalRecordArchiveService;
import com.his.common.support.SystemOperatorContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 门诊病历定时自动归档触发器（emr-arch 系统动作）。
 *
 * <p>病历签名完成（电子签名已签或纸质签名已核验）后不需要病案管理岗逐份手点归档：
 * 每天零点后由系统把前一日及更早、证据链完整的待归档病案推进为已归档。
 * 缺签名证据的病案留在待归档，继续由超期提醒催办与人工处理。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MedicalRecordArchiveAutoTrigger {

    private final MedicalRecordArchiveService medicalRecordArchiveService;

    @Scheduled(cron = "0 10 0 * * ?")
    public void scheduledAutoArchive() {
        try {
            SystemOperatorContext.bindSystemTask();
            medicalRecordArchiveService.autoArchiveSigned();
        } catch (Exception e) {
            // 定时任务失败只记日志：自动归档是兜底路径，不能影响任何业务线程
            log.error("[病历归档] 自动归档定时任务失败：{}", e.getMessage(), e);
        } finally {
            SystemOperatorContext.clear();
        }
    }
}

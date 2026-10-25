package com.his.clinical.service;

import com.his.clinical.event.ClinicSubmittedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 结诊事务提交后的异步收尾监听器。
 *
 * <p>原来规则质控 + 两个 PDF 生成同步堵在结诊事务里，医生点结诊要等 iText 加载中文字体、
 * 规则引擎全量跑一遍、两次写盘才返回。它们都是事后产物（质控是事后提醒、PDF 是事后打印用），
 * 不该堵在医生关键路径上。
 *
 * <p>触发条件：
 * <ul>
 *   <li>{@link TransactionPhase#AFTER_COMMIT}：主事务提交成功才跑，回滚了不跑；</li>
 *   <li>{@link Async}：在独立线程池跑，不占 Tomcat 请求线程。</li>
 * </ul>
 * 失败只记日志，不回滚结诊（结诊已提交成功）。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ClinicAfterSubmitListener {

    private final EmrService emrService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onClinicSubmitted(ClinicSubmittedEvent event) {
        try {
            emrService.asyncAfterSubmit(event.getRecordId());
        } catch (Exception ex) {
            log.error("[结诊收尾] 异步收尾失败 recordId={}", event.getRecordId(), ex);
        }
    }
}

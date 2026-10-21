package com.his.platform.spi;

import com.his.common.support.FieldSpec;
import com.his.platform.service.FieldChangeRecorder;
import com.his.spi.service.FieldChangeFacadeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * {@link FieldChangeFacadeService} 窄端口实现：字段级变更前后快照 diff 落库（sql/159），
 * 委托平台内部 {@link FieldChangeRecorder}（recordAction 等内部方法不外泄）。
 */
@Service
@RequiredArgsConstructor
public class FieldChangeFacadeServiceImpl implements FieldChangeFacadeService {

    private final FieldChangeRecorder fieldChangeRecorder;

    @Override
    public String record(String bizType, Object bizId, String bizNo, String bizName,
                         Object oldObj, Object newObj, List<FieldSpec> specs) {
        return fieldChangeRecorder.record(bizType, bizId, bizNo, bizName, oldObj, newObj, specs);
    }
}

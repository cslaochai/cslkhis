package com.his.emr.support;

import com.his.system.entity.SysDictData;
import com.his.system.service.DictCacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 门诊治疗站字典文案回填单点。
 *
 * <p>文案只从字典数据取，取不到渲染「未知(n)」而不回落成任何合法值：
 * 回落会把脏状态伪装成正常状态，页面上一切看起来都对，只有账是错的。
 */
@Component
@RequiredArgsConstructor
public class TreatmentDictText {

    public static final String DICT_ITEM_TYPE = "his_treatment_item_type";
    public static final String DICT_APPLY_STATUS = "his_treatment_apply_status";
    public static final String DICT_RECORD_STATUS = "his_treatment_record_status";
    public static final String DICT_EXEC_STATUS = "his_treatment_exec_status";
    public static final String DICT_CHARGE_STATUS = "his_treatment_charge_status";

    private final DictCacheService dictCacheService;

    public String text(String dictType, Object value) {
        if (value == null) {
            return null;
        }
        String v = String.valueOf(value);
        List<SysDictData> list = dictCacheService.getDictDataByType(dictType);
        if (list == null || list.isEmpty()) {
            return "未知(" + v + ")";
        }
        return list.stream()
                .filter(d -> d != null && v.equals(String.valueOf(d.getDictValue())))
                .map(SysDictData::getDictLabel)
                .findFirst()
                .orElse("未知(" + v + ")");
    }
}

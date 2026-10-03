package com.his.patient.support;

import com.his.system.entity.SysDictData;
import com.his.system.service.DictCacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 患者域字典文案回填单点（口径同医技 SubDictText：取不到渲染「未知(n)」，绝不回落成看似合法的值）。
 */
@Component
@RequiredArgsConstructor
public class DictText {

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

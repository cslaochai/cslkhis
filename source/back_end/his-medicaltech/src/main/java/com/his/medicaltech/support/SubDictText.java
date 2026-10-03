package com.his.medicaltech.support;

import com.his.system.entity.SysDictData;
import com.his.system.service.DictCacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 医技亚专业字典文案回填单点。
 *
 * <p>为什么不在 VO 里写死中文：状态/类型文案一旦两处维护（后端枚举 + 前端字典），
 * 必然漂移，而且漂移的表现是"页面显示 A、导出显示 B"这种查不出原因的错。
 * 所以文案一律从字典数据取，取不到就渲染「未知(n)」——
 * **绝不回落成某个看起来合法的值**（回落会把脏数据伪装成正常数据）。
 */
@Component
@RequiredArgsConstructor
public class SubDictText {

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

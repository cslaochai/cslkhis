package com.his.spi.service;

import com.his.spi.vo.ItemCatalogVO;

import java.util.List;

/**
 * 一般检查项目目录端口（实现落在 his-ms-clinical）：{@code sys_treatment_item}（诊疗项目）的读取。
 *
 * <p>体检的「一般检查」（身高体重腰围血压、内/外/眼/耳鼻喉/口腔/妇科检查等）本质是诊查类项目，
 * 与诊疗项目同一张价表，不另建第四套目录。</p>
 */
public interface TreatmentItemCatalogFacadeService {

    /**
     * 列出一般检查（诊疗）项目目录。
     *
     * @param keyword 关键字（匹配项目编码或名称；为空 = 不过滤）
     * @return 启用状态的项目目录项
     */
    List<ItemCatalogVO> listItems(String keyword);
}

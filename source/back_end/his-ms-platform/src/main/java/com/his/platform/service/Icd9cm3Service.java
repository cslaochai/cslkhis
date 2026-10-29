package com.his.platform.service;

import com.his.platform.vo.SysIcd9cm3SelectListVO;

import java.util.List;

/**
 * 手术操作编码服务：ICD-9-CM-3 术式候选的检索收口。
 */
public interface Icd9cm3Service {

    /**
     * 按编码或名称片段检索启用中的术式，前缀命中排在包含命中之前。
     *
     * @param keyword 编码或名称片段（空白时按维护顺序返回前若干条）
     * @param limit   返回条数上限
     */
    List<SysIcd9cm3SelectListVO> selectOptions(String keyword, int limit);
}

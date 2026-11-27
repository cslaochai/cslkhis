package com.his.spi.service;

import com.his.spi.vo.ItemCatalogVO;

import java.util.List;

/**
 * 医技项目目录端口（实现落在 his-ms-medicaltech）：检查项目与检验项目两张字典的读取。
 *
 * <p>只读端口，实现只依赖本模块 mapper（叶子），不牵编排服务。</p>
 */
public interface MedicalItemCatalogFacadeService {

    /**
     * 列出检查 / 检验项目目录。
     *
     * @param itemSource 项目来源（1-检验项目 2-检查项目；为空 = 两张表都返回）
     * @param keyword    关键字（匹配项目编码或名称；为空 = 不过滤）
     * @return 启用状态的项目目录项；两类都为空时返回空列表
     */
    List<ItemCatalogVO> listItems(Integer itemSource, String keyword);
}

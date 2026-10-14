package com.mss.service;

import com.mss.common.base.PageVO;
import com.mss.dto.CatalogQueryDTO;
import com.mss.dto.MappingMapDTO;
import com.mss.dto.MappingQueryDTO;
import com.mss.entity.MssCatalog;
import com.mss.entity.MssItemMapping;
import com.mss.vo.ActionVO;
import com.mss.vo.CatalogStatsVO;

/**
 * 医保三大目录 + 院内项目对照
 */
public interface CatalogService {

    PageVO<MssCatalog> catalogPage(CatalogQueryDTO query);

    CatalogStatsVO catalogStats();

    PageVO<MssItemMapping> mappingPage(MappingQueryDTO query);

    ActionVO map(MappingMapDTO dto);

    ActionVO unmap(Long id);

    ActionVO autoMatch();
}

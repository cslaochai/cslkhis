package com.his.charge.service;




import com.his.charge.dto.YbCatalogQueryPageDTO;
import com.his.charge.dto.YbCatalogUpsertDTO;
import com.his.charge.vo.BizYbCatalogVO;
import com.his.charge.vo.YbImportResultVO;
import com.his.common.base.PageResult;
import java.util.List;

/**
 * 国家医保目录服务（模拟目录库的维护口径：只启停不删除）。
 */
public interface YbCatalogService {

    /**
     * 分页查询（编码/名称模糊，类型/状态筛选；对照候选弹窗复用本接口）
     */
    PageResult<BizYbCatalogVO> listPage(YbCatalogQueryPageDTO queryDTO);

    /**
     * 新增/修改（yb_code 全局唯一；id 空=新增）
     */
    BizYbCatalogVO upsert(YbCatalogUpsertDTO dto);

    /**
     * 批量导入（按 yb_code 幂等：存在即更新，不存在则新增）
     */
    YbImportResultVO importBatch(List<YbCatalogUpsertDTO> items);

    /**
     * 启停（停用后不可新对照，已对照关系不受影响）
     */
    void changeStatus(Long id, Integer status);
}

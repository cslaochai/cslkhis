package com.his.system.service;

import com.his.common.base.PageResult;
import com.his.system.dto.SysDrugPageQueryDTO;
import com.his.system.dto.SysDrugSelectDTO;
import com.his.system.dto.SysDrugUpsertDTO;
import com.his.system.vo.SysDrugSelectListVO;
import com.his.system.vo.SysDrugVO;

import java.util.List;

/**
 * 药品档案服务：全院共用药品目录（编码、名称、药品类型、特殊药品标志、启停）的读写收口。
 */
public interface SysDrugService {

    /**
     * 药品档案分页：按名称模糊、药品类型、特殊药品标志筛选，药品编码升序。
     * 供药品目录台账取数；含停用与未启用的档案，不因状态被过滤掉。
     */
    PageResult<SysDrugVO> listPage(SysDrugPageQueryDTO queryDTO);

    /**
     * 药品下拉候选（不分页）：只给启用中的药品，可按药品类型收窄。
     * 用于门诊开方与住院医嘱选药，也用于药库入库、采购单、发药追溯这类要挑药的环节。
     *
     * @param queryDTO 药品类型留空 = 西药/中成药/中药饮片全类型一起给
     */
    List<SysDrugSelectListVO> selectList(SysDrugSelectDTO queryDTO);

    /**
     * 单条药品档案详情：按主键取全量字段。
     * 供药品维护入口取回完整原值再保存，避免拿下拉或列表的残缺字段整对象覆盖回去。
     */
    SysDrugVO getInfo(Long drugId);

    /**
     * 新增/修改一条药品档案：带主键走修改，不带则新建。
     * 用于药房建档与目录维护写入；改动的名称与类型会立刻影响各选药环节的可见项。
     */
    void upsert(SysDrugUpsertDTO upsertDTO);

    /**
     * 删除一条药品档案。
     * 用于药品目录维护里的下架动作；只想让某药暂时不出现在选药下拉里应当改状态停用，而不是删档案。
     */
    void delete(Long drugId);
}

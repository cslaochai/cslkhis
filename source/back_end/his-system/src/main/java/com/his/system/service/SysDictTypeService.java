package com.his.system.service;

import com.his.system.dto.SysDictTypeUpsertDTO;
import com.his.system.vo.SysDictTypeVO;

import java.util.List;

/**
 * 字典类型服务：字典分类（类型编码与名称）的读写收口，划出可被维护、可被业务引用的字典范围。
 */
public interface SysDictTypeService {

    /**
     * 全部字典类型清单：一次给出所有分类，不分页。
     * 用于「字典管理」页左侧的类型列表，选中某个类型后才去取它的字典项。
     */
    List<SysDictTypeVO> selectList();

    /**
     * 单个字典类型详情：按主键取全量字段。
     * 供字典类型的修改入口取回完整原值再保存，避免拿列表残缺字段整对象覆盖回去。
     */
    SysDictTypeVO getInfo(Long typeId);

    /**
     * 新增/修改一个字典类型：带主键走修改，不带则新建一个分类。
     * 用于「字典管理」页的字典类型弹框保存；新建的分类随后才能挂字典项供业务翻译。
     */
    void upsert(SysDictTypeUpsertDTO upsertDTO);

    /**
     * 删除一个字典类型。
     * 用于「字典管理」页类型行的删除，删后该编码不再是一个可维护的字典分类。
     */
    void delete(Long typeId);
}

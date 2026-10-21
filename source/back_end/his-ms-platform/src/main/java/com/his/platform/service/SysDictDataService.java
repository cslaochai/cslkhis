package com.his.platform.service;

import com.his.platform.dto.DictDataQueryDTO;
import com.his.platform.dto.SysDictDataUpsertDTO;
import com.his.platform.vo.DictTypeGroupVO;
import com.his.platform.vo.SysDictDataVO;

import java.util.List;

/**
 * 字典数据服务：操作员后台维护的各字典类型取值项的读写收口，字典侧码值→文案的事实来源。
 */
public interface SysDictDataService {

    /**
     * 按字典类型取该类型下的字典项清单。
     * 用于「字典管理」页选中左侧类型后右侧的取值表格，也是业务下拉与编码翻译的取数入口。
     *
     * @return 含停用项的全量清单；只展示启用项与否由取数侧按状态自行筛
     */
    List<SysDictDataVO> selectList(DictDataQueryDTO queryDTO);

    /**
     * 一次取多个字典类型并按类型分组返回。
     * 用于页面挂载时把本页要用到的几类编码一次拉齐，避免一类一个请求。
     *
     * @return 每个请求类型一组；单次最多 8 个类型，超出报错
     */
    List<DictTypeGroupVO> selectGroup(DictDataQueryDTO queryDTO);

    /**
     * 单条字典数据详情：按主键取全量字段。
     * 供字典数据的修改入口取回完整原值再保存，避免拿列表残缺字段整对象覆盖回去。
     */
    SysDictDataVO getInfo(Long dataId);

    /**
     * 新增/修改一条字典数据：带主键走修改，不带则在所属类型下新建一项。
     * 用于「字典管理」页的字典数据弹框保存；改完即影响全仓按该类型翻译出来的文案。
     */
    void upsert(SysDictDataUpsertDTO upsertDTO);

    /**
     * 删除一条字典数据。
     * 用于「字典管理」页字典数据行的删除，删后该编码在业务侧不再能翻译成名称。
     */
    void delete(Long dataId);
}

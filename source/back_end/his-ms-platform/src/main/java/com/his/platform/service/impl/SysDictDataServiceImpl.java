package com.his.platform.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.common.exception.BusinessException;
import com.his.common.support.FieldSpec;
import com.his.platform.dto.DictDataQueryDTO;
import com.his.platform.dto.SysDictDataUpsertDTO;
import com.his.platform.entity.SysDictData;
import com.his.platform.mapper.SysDictDataMapper;
import com.his.platform.service.DictCacheService;
import com.his.platform.service.FieldChangeRecorder;
import com.his.platform.service.SysDictDataService;
import com.his.platform.vo.DictTypeGroupVO;
import com.his.platform.vo.SysDictDataVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SysDictDataServiceImpl extends ServiceImpl<SysDictDataMapper, SysDictData> implements SysDictDataService {

    /**
     * 对象类型：数据字典（与字典类型同一类，便于"这套字典最近谁改了什么"一次筛出来）
     */
    private static final String SYS_DICT = SysDictTypeServiceImpl.SYS_DICT;

    /**
     * 字典项参与留痕的字段。
     *
     * <p>dictValue 是码值本体：改动它等于把历史单据里的旧码值翻译错位，是最该留痕的一项。
     * 状态决定下拉里还出不出现，排序影响操作员按顺序找到项，一并记。
     */
    private static final List<FieldSpec> DICT_DATA_FIELDS = FieldSpec.list(
            FieldSpec.of("dictLabel", "字典标签"),
            FieldSpec.of("dictValue", "字典值"),
            FieldSpec.of("dictSort", "排序"),
            FieldSpec.of("status", "状态")
    );

    private final SysDictDataMapper sysDictDataMapper;
    private final DictCacheService dictCacheService;
    private final FieldChangeRecorder fieldChangeRecorder;

    @Override
    public List<SysDictDataVO> selectList(DictDataQueryDTO queryDTO) {
        return toVOList(dictCacheService.getDictDataByTypeRaw(queryDTO.getDictTypes()));
    }

    @Override
    public List<DictTypeGroupVO> selectGroup(DictDataQueryDTO queryDTO) {
        List<DictTypeGroupVO> result = new ArrayList<>();
        String dictTypes = queryDTO.getDictTypes();
        if (dictTypes == null || dictTypes.isEmpty()) {
            return result;
        }

        String[] dictTypeList = dictTypes.split(",");
        if (dictTypeList.length > 8) {
            throw new BusinessException("数据字典每次最多只能查询8个！");
        }
        for (String type : dictTypeList) {
            String trimType = type.trim();
            if (!trimType.isEmpty()) {
                DictTypeGroupVO group = new DictTypeGroupVO();
                group.setDictType(trimType);
                group.setDataList(toVOList(dictCacheService.getDictDataByTypeRaw(trimType)));
                result.add(group);
            }
        }
        return result;
    }

    @Override
    public SysDictDataVO getInfo(Long dataId) {
        SysDictData dictData = sysDictDataMapper.selectById(dataId);
        SysDictDataVO vo = new SysDictDataVO();
        BeanUtils.copyProperties(dictData, vo);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void upsert(SysDictDataUpsertDTO upsertDTO) {
        SysDictData dictData = new SysDictData();
        BeanUtils.copyProperties(upsertDTO, dictData);
        SysDictData before = upsertDTO.getId() == null ? null : sysDictDataMapper.selectById(upsertDTO.getId());
        if (upsertDTO.getId() == null) {
            sysDictDataMapper.insert(dictData);
        } else {
            sysDictDataMapper.updateById(dictData);
        }
        dictCacheService.refreshDictCache(dictData.getDictType());
        SysDictData after = dictData.getId() == null ? null : sysDictDataMapper.selectById(dictData.getId());
        if (after != null) {
            // bizNo 记字典类型编码：按它就能把那套字典的全部变更串起来
            fieldChangeRecorder.record(SYS_DICT, after.getId(), after.getDictType(), after.getDictLabel(),
                    before, after, DICT_DATA_FIELDS, upsertDTO.getReason());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long dataId) {
        SysDictData dictData = sysDictDataMapper.selectById(dataId);
        if (dictData != null) {
            sysDictDataMapper.deleteById(dataId);
            dictCacheService.refreshDictCache(dictData.getDictType());
            // 码值行删了只能留动作：删掉的那条旧值已经没有对象可以承载
            fieldChangeRecorder.recordAction(SYS_DICT, dataId, dictData.getDictType(), dictData.getDictLabel(),
                    "删除字典项（标签=" + dictData.getDictLabel() + "，值=" + dictData.getDictValue() + "）");
        }
    }

    private List<SysDictDataVO> toVOList(List<SysDictData> dataList) {
        return dataList.stream().map(d -> {
            SysDictDataVO vo = new SysDictDataVO();
            BeanUtils.copyProperties(d, vo);
            return vo;
        }).collect(Collectors.toList());
    }
}

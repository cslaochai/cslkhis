package com.his.platform.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.common.support.FieldSpec;
import com.his.platform.dto.SysDictTypeUpsertDTO;
import com.his.platform.entity.SysDictType;
import com.his.platform.mapper.SysDictTypeMapper;
import com.his.platform.service.FieldChangeRecorder;
import com.his.platform.service.SysDictTypeService;
import com.his.platform.vo.SysDictTypeVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SysDictTypeServiceImpl extends ServiceImpl<SysDictTypeMapper, SysDictType> implements SysDictTypeService {

    /**
     * 对象类型：数据字典（类型与字典项共用一类，bizNo 区分到类型编码）
     */
    public static final String SYS_DICT = "SYS_DICT";

    /**
     * 字典类型参与留痕的字段。
     *
     * <p>字典名称/启停状态都会直接改变全院页面的下拉与码值翻译：类型停用后该套字典整列空白，
     * 这是"整页像崩了"的典型故障，必须能回溯到是谁在什么时候关的、为什么关。
     * dictType（编码）改不了（页面禁用 + 后端按 id 更新），故不声明。
     */
    private static final List<FieldSpec> DICT_TYPE_FIELDS = FieldSpec.list(
            FieldSpec.of("dictName", "字典名称"),
            FieldSpec.of("status", "状态")
    );

    private final SysDictTypeMapper sysDictTypeMapper;
    private final FieldChangeRecorder fieldChangeRecorder;

    @Override
    public List<SysDictTypeVO> selectList() {
        return sysDictTypeMapper.selectList(null).stream().map(t -> {
            SysDictTypeVO vo = new SysDictTypeVO();
            BeanUtils.copyProperties(t, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public SysDictTypeVO getInfo(Long typeId) {
        SysDictType dictType = sysDictTypeMapper.selectById(typeId);
        SysDictTypeVO vo = new SysDictTypeVO();
        BeanUtils.copyProperties(dictType, vo);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void upsert(SysDictTypeUpsertDTO upsertDTO) {
        SysDictType dictType = new SysDictType();
        BeanUtils.copyProperties(upsertDTO, dictType);
        SysDictType before = upsertDTO.getId() == null ? null : sysDictTypeMapper.selectById(upsertDTO.getId());
        if (upsertDTO.getId() == null) {
            sysDictTypeMapper.insert(dictType);
        } else {
            sysDictTypeMapper.updateById(dictType);
        }
        // 拿落库后的真实值比对（不拿 DTO 比）：updateById 只写非 null 字段，DTO 里的空值不会落库
        SysDictType after = dictType.getId() == null ? null : sysDictTypeMapper.selectById(dictType.getId());
        if (after != null) {
            fieldChangeRecorder.record(SYS_DICT, after.getId(), after.getDictType(), after.getDictName(),
                    before, after, DICT_TYPE_FIELDS, upsertDTO.getReason());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long typeId) {
        SysDictType dictType = sysDictTypeMapper.selectById(typeId);
        sysDictTypeMapper.deleteById(typeId);
        if (dictType != null) {
            // 类型删了对象就没了，只能靠这条动作留痕回答"谁删了这套字典"
            fieldChangeRecorder.recordAction(SYS_DICT, typeId, dictType.getDictType(), dictType.getDictName(),
                    "删除字典类型");
        }
    }
}

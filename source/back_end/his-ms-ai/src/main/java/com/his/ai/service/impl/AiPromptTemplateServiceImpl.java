package com.his.ai.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.ai.dto.AiPromptTemplatePageQueryDTO;
import com.his.ai.dto.AiPromptTemplateUpsertDTO;
import com.his.ai.entity.SysAiPromptTemplate;
import com.his.ai.mapper.SysAiPromptTemplateMapper;
import com.his.ai.service.AiPromptTemplateService;
import com.his.ai.support.PromptTemplate;
import com.his.ai.vo.AiPromptTemplateVO;
import com.his.common.base.PageResult;
import com.his.common.enums.YesOrNoEnum;
import com.his.common.exception.BusinessException;
import com.his.common.util.TextUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AiPromptTemplateServiceImpl extends ServiceImpl<SysAiPromptTemplateMapper, SysAiPromptTemplate> implements AiPromptTemplateService {

    private final SysAiPromptTemplateMapper promptTemplateMapper;
    private final PromptTemplate promptTemplate;

    @Override
    public PageResult<AiPromptTemplateVO> listPage(AiPromptTemplatePageQueryDTO query) {
        Page<SysAiPromptTemplate> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<SysAiPromptTemplate> wrapper = new LambdaQueryWrapper<SysAiPromptTemplate>()
                .eq(SysAiPromptTemplate::getIsActive, YesOrNoEnum.YES.getCode())
                .orderByAsc(SysAiPromptTemplate::getTemplateCode);
        if (TextUtil.hasText(query.getKeyword())) {
            String kw = TextUtil.trim(query.getKeyword());
            wrapper.and(w -> w.like(SysAiPromptTemplate::getTemplateCode, kw)
                    .or().like(SysAiPromptTemplate::getTemplateName, kw));
        }
        Page<SysAiPromptTemplate> result = promptTemplateMapper.selectPage(page, wrapper);
        List<AiPromptTemplateVO> records = result.getRecords().stream().map(this::toVO).toList();
        return PageResult.of(result.getTotal(), result.getCurrent(), result.getSize(), result.getPages(), records);
    }

    @Override
    public List<AiPromptTemplateVO> history(String code) {
        List<SysAiPromptTemplate> list = promptTemplateMapper.selectList(
                new LambdaQueryWrapper<SysAiPromptTemplate>()
                        .eq(SysAiPromptTemplate::getTemplateCode, code)
                        .orderByDesc(SysAiPromptTemplate::getIsActive)
                        .orderByDesc(SysAiPromptTemplate::getId));
        return list.stream().map(this::toVO).toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String upsert(AiPromptTemplateUpsertDTO dto) {
        String code = dto.getTemplateCode();
        // 把当前激活的设为停用
        promptTemplateMapper.update(null,
                new LambdaUpdateWrapper<SysAiPromptTemplate>()
                        .eq(SysAiPromptTemplate::getTemplateCode, code)
                        .eq(SysAiPromptTemplate::getIsActive, YesOrNoEnum.YES.getCode())
                        .set(SysAiPromptTemplate::getIsActive, YesOrNoEnum.NO.getCode()));
        // 新版本号：取最新一行递增，解析失败回落 1.0
        SysAiPromptTemplate latest = promptTemplateMapper.selectOne(
                new LambdaQueryWrapper<SysAiPromptTemplate>()
                        .eq(SysAiPromptTemplate::getTemplateCode, code)
                        .orderByDesc(SysAiPromptTemplate::getId)
                        .last("LIMIT 1"));
        String newVersion = nextVersion(latest);
        SysAiPromptTemplate entity = new SysAiPromptTemplate();
        entity.setTemplateCode(code);
        entity.setTemplateName(latest != null ? latest.getTemplateName() : code);
        entity.setVersion(newVersion);
        entity.setContent(dto.getContent());
        entity.setIsActive(YesOrNoEnum.YES.getCode());
        entity.setRemark(dto.getRemark());
        promptTemplateMapper.insert(entity);
        promptTemplate.evictCache(code);
        return "v" + newVersion;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String activate(Long id) {
        SysAiPromptTemplate row = promptTemplateMapper.selectById(id);
        if (row == null) {
            throw new BusinessException("版本不存在");
        }
        String code = row.getTemplateCode();
        // 全部设为停用
        promptTemplateMapper.update(null,
                new LambdaUpdateWrapper<SysAiPromptTemplate>()
                        .eq(SysAiPromptTemplate::getTemplateCode, code)
                        .set(SysAiPromptTemplate::getIsActive, YesOrNoEnum.NO.getCode()));
        // 目标版本设为生效
        row.setIsActive(YesOrNoEnum.YES.getCode());
        promptTemplateMapper.updateById(row);
        promptTemplate.evictCache(code);
        return "已切回 v" + row.getVersion();
    }

    private String nextVersion(SysAiPromptTemplate latest) {
        String newVersion = "1.0";
        if (latest != null && latest.getVersion() != null) {
            try {
                double v = Double.parseDouble(latest.getVersion());
                newVersion = String.format("%.1f", v + 0.1);
            } catch (NumberFormatException ignored) {
            }
        }
        return newVersion;
    }

    private AiPromptTemplateVO toVO(SysAiPromptTemplate entity) {
        AiPromptTemplateVO vo = new AiPromptTemplateVO();
        vo.setId(entity.getId());
        vo.setTemplateCode(entity.getTemplateCode());
        vo.setTemplateName(entity.getTemplateName());
        vo.setVersion(entity.getVersion());
        vo.setContent(entity.getContent());
        vo.setIsActive(entity.getIsActive());
        vo.setRemark(entity.getRemark());
        vo.setCreateTime(entity.getCreateTime());
        vo.setUpdateTime(entity.getUpdateTime());
        return vo;
    }
}

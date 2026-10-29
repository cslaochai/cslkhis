package com.his.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.common.enums.EnableStatusEnum;
import com.his.common.util.TextUtil;
import com.his.platform.entity.SysIcd9cm3;
import com.his.platform.mapper.SysIcd9cm3Mapper;
import com.his.platform.service.Icd9cm3Service;
import com.his.platform.vo.SysIcd9cm3SelectListVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class Icd9cm3ServiceImpl extends ServiceImpl<SysIcd9cm3Mapper, SysIcd9cm3> implements Icd9cm3Service {

    /** 精确/前缀命中池上限：先把「以关键词开头」的捞进来，避免宽池截断挤掉正确编码。 */
    private static final int PREFIX_POOL_LIMIT = 100;

    /** 包含命中池上限：兜住关键词不在名称开头的情况，同时防单字输入全表拉取。 */
    private static final int CONTAINS_POOL_LIMIT = 200;

    @Override
    public List<SysIcd9cm3SelectListVO> selectOptions(String keyword, int limit) {
        int size = limit <= 0 ? 50 : limit;
        if (!TextUtil.hasText(keyword)) {
            return toVoList(list(new LambdaQueryWrapper<SysIcd9cm3>()
                    .eq(SysIcd9cm3::getStatus, EnableStatusEnum.ENABLED.getCode())
                    .orderByAsc(SysIcd9cm3::getSortOrder)
                    .last("LIMIT " + size)));
        }

        String kw = keyword.trim();
        Map<String, SysIcd9cm3> pool = new LinkedHashMap<>();
        for (SysIcd9cm3 row : list(new LambdaQueryWrapper<SysIcd9cm3>()
                .eq(SysIcd9cm3::getStatus, EnableStatusEnum.ENABLED.getCode())
                .and(w -> w.likeRight(SysIcd9cm3::getOpCode, kw)
                        .or().likeRight(SysIcd9cm3::getOpName, kw))
                .orderByAsc(SysIcd9cm3::getSortOrder)
                .last("LIMIT " + PREFIX_POOL_LIMIT))) {
            pool.put(row.getOpCode(), row);
        }
        if (pool.size() < size) {
            for (SysIcd9cm3 row : list(new LambdaQueryWrapper<SysIcd9cm3>()
                    .eq(SysIcd9cm3::getStatus, EnableStatusEnum.ENABLED.getCode())
                    .and(w -> w.like(SysIcd9cm3::getOpCode, kw)
                            .or().like(SysIcd9cm3::getOpName, kw))
                    .orderByAsc(SysIcd9cm3::getSortOrder)
                    .last("LIMIT " + CONTAINS_POOL_LIMIT))) {
                pool.putIfAbsent(row.getOpCode(), row);
            }
        }

        List<SysIcd9cm3> rows = new ArrayList<>(pool.values());
        return toVoList(rows.size() > size ? rows.subList(0, size) : rows);
    }

    private List<SysIcd9cm3SelectListVO> toVoList(List<SysIcd9cm3> rows) {
        List<SysIcd9cm3SelectListVO> result = new ArrayList<>(rows.size());
        for (SysIcd9cm3 row : rows) {
            SysIcd9cm3SelectListVO vo = new SysIcd9cm3SelectListVO();
            vo.setOpCode(row.getOpCode());
            vo.setOpName(row.getOpName());
            vo.setCodeStd(row.getCodeStd());
            result.add(vo);
        }
        return result;
    }
}

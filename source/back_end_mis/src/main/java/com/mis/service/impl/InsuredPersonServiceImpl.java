package com.mis.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mis.common.base.PageResult;
import com.mis.dto.PersonQueryDTO;
import com.mis.dto.PersonSaveDTO;
import com.mis.entity.JinbInsuredPerson;
import com.mis.mapper.JinbInsuredPersonMapper;
import com.mis.service.InsuredPersonService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;

@Service
public class InsuredPersonServiceImpl implements InsuredPersonService {

    private final JinbInsuredPersonMapper personMapper;

    public InsuredPersonServiceImpl(JinbInsuredPersonMapper personMapper) {
        this.personMapper = personMapper;
    }

    @Override
    public JinbInsuredPerson query(PersonQueryDTO dto) {
        LambdaQueryWrapper<JinbInsuredPerson> wrapper = new LambdaQueryWrapper<>();
        if (dto != null) {
            if (StringUtils.hasText(dto.getIdCard())) {
                wrapper.eq(JinbInsuredPerson::getIdCard, dto.getIdCard());
            } else if (StringUtils.hasText(dto.getInsuranceNo())) {
                wrapper.eq(JinbInsuredPerson::getInsuranceNo, dto.getInsuranceNo());
            } else if (StringUtils.hasText(dto.getName())) {
                wrapper.eq(JinbInsuredPerson::getName, dto.getName());
            }
        }
        wrapper.last("limit 1");
        return personMapper.selectOne(wrapper);
    }

    @Override
    public PageResult<JinbInsuredPerson> page(String keyword, int pageNum, int pageSize) {
        LambdaQueryWrapper<JinbInsuredPerson> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(JinbInsuredPerson::getName, keyword)
                    .or().like(JinbInsuredPerson::getIdCard, keyword)
                    .or().like(JinbInsuredPerson::getInsuranceNo, keyword));
        }
        wrapper.orderByDesc(JinbInsuredPerson::getId);
        Page<JinbInsuredPerson> page = personMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        return PageResult.of(page.getTotal(), pageNum, pageSize, page.getPages(), page.getRecords());
    }

    @Override
    public JinbInsuredPerson save(PersonSaveDTO dto) {
        JinbInsuredPerson exist = personMapper.selectOne(new LambdaQueryWrapper<JinbInsuredPerson>()
                .eq(JinbInsuredPerson::getIdCard, dto.getIdCard())
                .last("limit 1"));
        if (exist != null) {
            BeanUtils.copyProperties(dto, exist);
            personMapper.updateById(exist);
            return exist;
        }
        JinbInsuredPerson person = new JinbInsuredPerson();
        BeanUtils.copyProperties(dto, person);
        if (person.getPersonStatus() == null) {
            person.setPersonStatus(1);
        }
        if (person.getAccountBalance() == null) {
            person.setAccountBalance(BigDecimal.ZERO);
        }
        personMapper.insert(person);
        return person;
    }
}

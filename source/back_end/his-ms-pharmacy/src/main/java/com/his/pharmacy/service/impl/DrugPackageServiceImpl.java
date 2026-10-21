package com.his.pharmacy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.common.entity.CurrentUser;
import com.his.common.exception.BusinessException;
import com.his.common.utils.UserUtils;
import com.his.pharmacy.dto.BizDrugPackageUpsertDTO;
import com.his.pharmacy.entity.BizDrugPackage;
import com.his.pharmacy.entity.BizDrugPackageDetail;
import com.his.pharmacy.mapper.BizDrugPackageDetailMapper;
import com.his.pharmacy.mapper.BizDrugPackageMapper;
import com.his.pharmacy.service.DrugPackageService;
import com.his.pharmacy.vo.BizDrugPackageDetailVO;
import com.his.pharmacy.vo.BizDrugPackageVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DrugPackageServiceImpl extends ServiceImpl<BizDrugPackageMapper, BizDrugPackage> implements DrugPackageService {

    private final BizDrugPackageMapper bizDrugPackageMapper;
    private final BizDrugPackageDetailMapper bizDrugPackageDetailMapper;

    @Override
    public List<BizDrugPackageVO> listDrugPackages() {
        Long doctorId = currentDoctorIdOrNull();
        if (doctorId == null) {
            return new ArrayList<>();
        }
        List<BizDrugPackage> list = bizDrugPackageMapper.selectList(
                new LambdaQueryWrapper<BizDrugPackage>()
                        .eq(BizDrugPackage::getDoctorId, doctorId)
                        .orderByDesc(BizDrugPackage::getCreateTime)
        );
        if (list == null) {
            return new ArrayList<>();
        }
        return list.stream().map(this::toPackageVO).collect(Collectors.toList());
    }

    @Override
    public BizDrugPackageVO getDrugPackageDetail(Long packageId) {
        BizDrugPackage pkg = bizDrugPackageMapper.selectById(packageId);
        if (pkg != null) {
            pkg.setDetails(bizDrugPackageDetailMapper.selectList(
                    new LambdaQueryWrapper<BizDrugPackageDetail>()
                            .eq(BizDrugPackageDetail::getPackageId, packageId)
            ));
        }
        return toPackageVO(pkg);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveDrugPackage(BizDrugPackageUpsertDTO upsertDTO) {
        Long doctorId = requireCurrentDoctorId();
        BizDrugPackage pkg = new BizDrugPackage();
        BeanUtils.copyProperties(upsertDTO, pkg);
        pkg.setDoctorId(doctorId);
        if (upsertDTO.getDetails() != null) {
            pkg.setDetails(upsertDTO.getDetails().stream().map(dto -> {
                BizDrugPackageDetail detail = new BizDrugPackageDetail();
                BeanUtils.copyProperties(dto, detail);
                return detail;
            }).collect(Collectors.toList()));
        }
        bizDrugPackageMapper.insert(pkg);
        if (pkg.getDetails() != null) {
            for (BizDrugPackageDetail d : pkg.getDetails()) {
                d.setPackageId(pkg.getId());
                bizDrugPackageDetailMapper.insert(d);
            }
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteDrugPackage(Long id) {
        bizDrugPackageDetailMapper.delete(
                new LambdaQueryWrapper<BizDrugPackageDetail>()
                        .eq(BizDrugPackageDetail::getPackageId, id)
        );
        return bizDrugPackageMapper.deleteById(id) > 0;
    }

    private Long currentDoctorIdOrNull() {
        CurrentUser user = UserUtils.getCurrentUser();
        return user != null ? user.getEmployeeId() : null;
    }

    private Long requireCurrentDoctorId() {
        Long doctorId = currentDoctorIdOrNull();
        if (doctorId == null) {
            throw new BusinessException("未获取到用户信息");
        }
        return doctorId;
    }

    private BizDrugPackageVO toPackageVO(BizDrugPackage pkg) {
        if (pkg == null) {
            return null;
        }
        BizDrugPackageVO vo = new BizDrugPackageVO();
        BeanUtils.copyProperties(pkg, vo);
        if (pkg.getDetails() != null) {
            vo.setDetails(pkg.getDetails().stream().map(detail -> {
                BizDrugPackageDetailVO detailVO = new BizDrugPackageDetailVO();
                BeanUtils.copyProperties(detail, detailVO);
                return detailVO;
            }).collect(Collectors.toList()));
        }
        return vo;
    }
}

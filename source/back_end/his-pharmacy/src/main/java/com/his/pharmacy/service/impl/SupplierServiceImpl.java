package com.his.pharmacy.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.common.base.PageResult;
import com.his.common.exception.BusinessException;
import com.his.pharmacy.dto.SupplierQueryPageDTO;
import com.his.pharmacy.dto.SupplierUpsertDTO;
import com.his.pharmacy.entity.SysSupplier;
import com.his.pharmacy.mapper.SysSupplierMapper;
import com.his.pharmacy.service.SupplierService;
import com.his.pharmacy.vo.SysSupplierSelectListVO;
import com.his.pharmacy.vo.SysSupplierVO;
import com.his.system.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 供应商服务实现
 */
@Service
@RequiredArgsConstructor
public class SupplierServiceImpl extends ServiceImpl<SysSupplierMapper, SysSupplier> implements SupplierService {

    private final SysSupplierMapper supplierMapper;

    @Override
    public PageResult<SysSupplierVO> page(SupplierQueryPageDTO queryDTO) {
        Page<SysSupplierVO> page = supplierMapper.selectSupplierPage(
                new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize()),
                queryDTO.getKeyword(), queryDTO.getStatus());
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), page.getRecords());
    }

    @Override
    public List<SysSupplierSelectListVO> selectList() {
        List<SysSupplier> list = this.lambdaQuery()
                .eq(SysSupplier::getStatus, 1)
                .orderByAsc(SysSupplier::getSupplierCode)
                .list();
        return list.stream().map(s -> {
            SysSupplierSelectListVO vo = new SysSupplierSelectListVO();
            BeanUtils.copyProperties(s, vo);
            return vo;
        }).toList();
    }

    @Override
    public SysSupplierVO getDetailById(Long supplierId) {
        SysSupplierVO vo = supplierMapper.selectSupplierById(supplierId);
        if (vo == null) {
            throw new BusinessException("供应商不存在或已删除");
        }
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long upsert(SupplierUpsertDTO dto) {
        String code = dto.getSupplierCode() == null ? null : dto.getSupplierCode().trim();
        if (supplierMapper.countByCode(code, dto.getSupplierId()) > 0) {
            throw new BusinessException("供应商编码已存在：" + code);
        }

        Integer rating = dto.getRating() == null ? 3 : dto.getRating();
        Integer status = dto.getStatus() == null ? 1 : dto.getStatus();

        String operator = UserUtils.getCurrentEmployeeName();

        if (dto.getSupplierId() == null) {
            SysSupplier entity = new SysSupplier();
            BeanUtils.copyProperties(dto, entity);
            entity.setSupplierId(null);
            entity.setSupplierCode(code);
            entity.setSupplierName(dto.getSupplierName().trim());
            entity.setRating(rating);
            entity.setStatus(status);
            entity.setCreateBy(operator);
            entity.setUpdateBy(operator);
            if (!this.save(entity)) {
                throw new BusinessException("新增供应商失败");
            }
            return entity.getSupplierId();
        }

        // 显式走 Mapper 的 selectById：Service 里 this.getById(Long) 会被解析成返回 VO 的重载方法
        SysSupplier exist = this.getBaseMapper().selectById(dto.getSupplierId());
        if (exist == null) {
            throw new BusinessException("供应商不存在或已删除");
        }
        // 用 UpdateWrapper 显式 set：MP 的 updateById 会跳过 null 字段，导致"清空地址/证照号"这类操作静默失效
        boolean ok = this.lambdaUpdate()
                .eq(SysSupplier::getSupplierId, dto.getSupplierId())
                .set(SysSupplier::getSupplierCode, code)
                .set(SysSupplier::getSupplierName, dto.getSupplierName().trim())
                .set(SysSupplier::getContactPerson, dto.getContactPerson())
                .set(SysSupplier::getPhone, dto.getPhone())
                .set(SysSupplier::getAddress, dto.getAddress())
                .set(SysSupplier::getLicenseNo, dto.getLicenseNo())
                .set(SysSupplier::getLicenseExpiry, dto.getLicenseExpiry())
                .set(SysSupplier::getRating, rating)
                .set(SysSupplier::getStatus, status)
                .set(SysSupplier::getRemark, dto.getRemark())
                .set(SysSupplier::getUpdateBy, operator)
                .update();
        if (!ok) {
            throw new BusinessException("修改供应商失败");
        }
        return dto.getSupplierId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(Long supplierId) {
        SysSupplier exist = this.getBaseMapper().selectById(supplierId);
        if (exist == null) {
            throw new BusinessException("供应商不存在或已删除");
        }
        if (supplierMapper.countOrderRef(supplierId) > 0) {
            throw new BusinessException("该供应商已被采购订单引用，不能删除；如需停供请把状态改为「停用」");
        }
        if (!this.removeById(supplierId)) {
            throw new BusinessException("删除供应商失败");
        }
    }
}

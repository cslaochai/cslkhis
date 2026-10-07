package com.his.operation.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.common.exception.BusinessException;
import com.his.common.util.TextUtil;
import com.his.operation.dto.OperationRoomUpsertDTO;
import com.his.operation.entity.SysOperationRoom;
import com.his.operation.mapper.SysOperationRoomMapper;
import com.his.operation.service.OperationRoomService;
import com.his.operation.vo.OperationRoomVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * 手术间主数据服务实现。
 *
 * <p>删除是物理删（{@code purgeById}）：{@code uk_room_code}/{@code uk_room_name} 不含
 * del_flag，软删行继续占键，"删了再建同一个间"必然 500（L12 铁律）。
 * 手术间没有留档价值——历史手术单快照的是 operation_room 文本列，删主数据改不了历史。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OperationRoomServiceImpl extends ServiceImpl<SysOperationRoomMapper, SysOperationRoom> implements OperationRoomService {

    private static final int STATUS_ENABLED = 1;

    private final SysOperationRoomMapper sysOperationRoomMapper;

    // C-非 DTO 字段校验：既校验必填又做 trim 归一化，返回值参与编码/名称唯一性比对与落库，
    // 若下沉为纯 @NotBlank 会丢失 trim，保留（@Valid 已挡 null，此处负责归一化）
    private static String trimRequired(String value, String message) {
        if (!TextUtil.hasText(value)) {
            throw new BusinessException(message);
        }
        return value.trim();
    }

    @Override
    public List<OperationRoomVO> listAll() {
        return sysOperationRoomMapper.selectList(new LambdaQueryWrapper<SysOperationRoom>()
                        .orderByAsc(SysOperationRoom::getSortOrder)
                        .orderByAsc(SysOperationRoom::getRoomCode))
                .stream().map(this::toVO).toList();
    }

    @Override
    public List<OperationRoomVO> selectEnabled() {
        return sysOperationRoomMapper.selectList(new LambdaQueryWrapper<SysOperationRoom>()
                        .eq(SysOperationRoom::getStatus, STATUS_ENABLED)
                        .orderByAsc(SysOperationRoom::getSortOrder)
                        .orderByAsc(SysOperationRoom::getRoomCode))
                .stream().map(this::toVO).toList();
    }

    @Override
    public String upsert(OperationRoomUpsertDTO dto) {
        // C-非 DTO 字段校验：dto 整体是否为空由 @RequestBody 绑定保证，Bean Validation 不覆盖 null 请求体，保留
        if (dto == null) {
            throw new BusinessException("请求体不能为空");
        }
        String code = trimRequired(dto.getRoomCode(), "手术间编码不能为空");
        String name = trimRequired(dto.getRoomName(), "手术间名称不能为空");
        Integer status = dto.getStatus() == null ? STATUS_ENABLED : dto.getStatus();
        if (!Objects.equals(STATUS_ENABLED, status) && !Objects.equals(0, status)) {
            throw new BusinessException("状态只允许 1-启用 0-停用，当前=" + status);
        }

        Long selfId = dto.getId();
        long dupCode = sysOperationRoomMapper.selectCount(new LambdaQueryWrapper<SysOperationRoom>()
                .eq(SysOperationRoom::getRoomCode, code)
                .ne(selfId != null, SysOperationRoom::getId, selfId));
        if (dupCode > 0) {
            throw new BusinessException("手术间编码「" + code + "」已存在（编码全局唯一，撞了排台总表会出现两列同名台）");
        }
        long dupName = sysOperationRoomMapper.selectCount(new LambdaQueryWrapper<SysOperationRoom>()
                .eq(SysOperationRoom::getRoomName, name)
                .ne(selfId != null, SysOperationRoom::getId, selfId));
        if (dupName > 0) {
            throw new BusinessException("手术间名称「" + name + "」已存在（申请单快照的是名称，重名会分不出排到哪一间）");
        }

        SysOperationRoom entity;
        if (selfId == null) {
            entity = new SysOperationRoom();
            entity.setRoomCode(code);
        } else {
            entity = sysOperationRoomMapper.selectById(selfId);
            if (entity == null) {
                throw new BusinessException("手术间不存在");
            }
            entity.setRoomCode(code);
        }
        entity.setRoomName(name);
        entity.setLocation(dto.getLocation());
        entity.setSortOrder(dto.getSortOrder() == null || dto.getSortOrder() <= 0 ? 1 : dto.getSortOrder());
        entity.setStatus(status);
        if (dto.getRemark() != null) {
            entity.setRemark(dto.getRemark());
        }

        if (selfId == null) {
            sysOperationRoomMapper.insert(entity);
            log.info("新增手术间 {}（{}）", entity.getRoomCode(), entity.getRoomName());
        } else {
            sysOperationRoomMapper.updateById(entity);
            log.info("修改手术间 {}（{}）", entity.getRoomCode(), entity.getRoomName());
        }
        return String.valueOf(entity.getId());
    }

    @Override
    public void deleteById(Long roomId) {
        // C-非 DTO 入参：校验对象是 @RequestParam 标量参数，Bean Validation 不覆盖，保留
        if (roomId == null) {
            throw new BusinessException("手术间ID不能为空");
        }
        SysOperationRoom entity = sysOperationRoomMapper.selectById(roomId);
        if (entity == null) {
            throw new BusinessException("手术间不存在");
        }
        sysOperationRoomMapper.purgeById(roomId);
        log.info("物理删除手术间 {}（{}），操作不影响历史申请单的文本快照", entity.getRoomCode(), entity.getRoomName());
    }

    private OperationRoomVO toVO(SysOperationRoom entity) {
        OperationRoomVO vo = new OperationRoomVO();
        vo.setId(entity.getId());
        vo.setRoomCode(entity.getRoomCode());
        vo.setRoomName(entity.getRoomName());
        vo.setLocation(entity.getLocation());
        vo.setSortOrder(entity.getSortOrder());
        vo.setStatus(entity.getStatus());
        vo.setStatusText(Objects.equals(STATUS_ENABLED, entity.getStatus()) ? "启用" : "停用");
        vo.setRemark(entity.getRemark());
        return vo;
    }
}

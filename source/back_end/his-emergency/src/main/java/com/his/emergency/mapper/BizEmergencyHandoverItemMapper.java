package com.his.emergency.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.emergency.entity.BizEmergencyHandoverItem;
import org.apache.ibatis.annotations.Mapper;

/**
 * 急诊交班明细 Mapper。
 *
 * <p>本表设计上只增不改不删（撤销一张已提交的交班单等于销毁责任记录），
 * 所以不需要为唯一键 {@code uk_handover_item} 补物理删方法 ——
 * 真要做"作废重交"时必须写显式 {@code @Delete}，不能用 {@code remove()}（软删的行仍占键）。
 */
@Mapper
public interface BizEmergencyHandoverItemMapper extends BaseMapper<BizEmergencyHandoverItem> {
}

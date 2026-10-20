package com.his.medicaltech.service;


import com.his.common.base.PageResult;
import com.his.medicaltech.dto.ExamApptDTO;
import com.his.medicaltech.entity.BizExamDevice;
import com.his.medicaltech.vo.ExamApptVO;

import java.util.List;

/**
 * 检查设备档位服务：可预约设备的档位台账与其可开展项目配置的收口。
 */
public interface ExamDeviceService extends com.baomidou.mybatisplus.extension.service.IService<BizExamDevice> {

    /**
     * 设备档位分页台账：档位参数、日均号源数与已挂项目数。
     * 用于「检查设备与号源」页的设备列表卡。
     */
    PageResult<ExamApptVO.DeviceVO> listPage(ExamApptDTO.DeviceQuery q);

    /**
     * 设备下拉，传入检查项目时只返回能开展该项目的设备。
     * 用于预约受理弹框选设备，以及预约台账按设备筛选。
     */
    List<ExamApptVO.DeviceSelectListVO> selectList(Integer deviceType, Long itemId);

    /**
     * 单台设备的档位详情，含已配置的可开展项目。
     * 供设备档位维护环节按设备 ID 取当前配置后再改。
     */
    ExamApptVO.DeviceVO getDetail(Long deviceId);

    /**
     * 设备已挂的可开展项目清单（含项目自身时长与档位换算分钟）。
     * 用于设备项目配置弹框展示当前已配内容。
     */
    List<ExamApptVO.DeviceItemVO> itemList(Long deviceId);

    /**
     * 检查项目候选，按关键词限量返回。
     * 用于给设备配可开展项目时挑选项目。
     */
    List<ExamApptVO.ItemSelectListVO> itemCandidates(String keyword, Integer limit);

    /**
     * 全院设备台账里可挂接的设备候选（只读引用，档位不拥有设备档案）。
     * 用于新增设备档位时挑选实物设备。
     */
    List<ExamApptVO.EquipmentSelectListVO> equipmentOptions();

    /**
     * 新增或修改设备档位：开单时段、单号时长、并行数与可提前预约天数等预约参数。
     * 用于设备档位弹框保存；设备档案本身不在此改。
     */
    ExamApptVO.DeviceVO upsert(ExamApptDTO.DeviceUpsert dto);

    /**
     * 删除设备档位，名下仍有未完成预约时拒绝。
     * 用于号源页清理录错或已停用的档位。
     */
    void deleteById(Long deviceId);

    /**
     * 覆盖式保存设备可开展项目，返回落库条数。
     * 用于设备项目配置弹框一次提交整张项目清单。
     */
    int saveItems(ExamApptDTO.DeviceItemSave dto);
}

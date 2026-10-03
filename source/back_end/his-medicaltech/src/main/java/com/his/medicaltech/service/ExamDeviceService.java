package com.his.medicaltech.service;


import com.his.medicaltech.entity.BizExamDevice;
import com.his.common.base.PageResult;
import com.his.medicaltech.dto.ExamApptDTO;
import com.his.medicaltech.vo.ExamApptVO;
import java.util.List;

public interface ExamDeviceService extends com.baomidou.mybatisplus.extension.service.IService<BizExamDevice> {

    PageResult<ExamApptVO.DeviceVO> listPage(ExamApptDTO.DeviceQuery q);

    List<ExamApptVO.DeviceSelectListVO> selectList(Integer deviceType, Long itemId);

    ExamApptVO.DeviceVO getDetail(Long deviceId);

    List<ExamApptVO.DeviceItemVO> itemList(Long deviceId);

    List<ExamApptVO.ItemSelectListVO> itemCandidates(String keyword, Integer limit);

    List<ExamApptVO.EquipmentSelectListVO> equipmentOptions();

    ExamApptVO.DeviceVO upsert(ExamApptDTO.DeviceUpsert dto);

    void deleteById(Long deviceId);

    int saveItems(ExamApptDTO.DeviceItemSave dto);
}

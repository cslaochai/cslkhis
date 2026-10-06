package com.his.medicaltech.service;

import com.his.common.base.PageResult;
import com.his.medicaltech.dto.PathologyDTO;
import com.his.medicaltech.entity.BizPathologyBlock;
import com.his.medicaltech.entity.BizPathologyOrder;
import com.his.medicaltech.vo.PathologyVO;

import java.util.List;

public interface PathologyService extends com.baomidou.mybatisplus.extension.service.IService<BizPathologyOrder> {

    PageResult<PathologyVO.ListVO> pageVO(PathologyDTO.Query q);

    PathologyVO.StatsVO stats();

    PathologyVO.DetailVO getDetail(Long orderId);

    List<PathologyVO.BlockVO> listBlocks(Long orderId);

    PathologyVO.DetailVO upsertOrder(PathologyDTO.OrderUpsert dto);

    BizPathologyOrder createFromEndoscopy(PathologyDTO.FromEndoscopy dto);

    void receive(PathologyDTO.Receive dto);

    void process(PathologyDTO.Process dto);

    BizPathologyBlock addBlock(PathologyDTO.BlockUpsert dto);

    Long blockAction(PathologyDTO.BlockAction dto);

    void report(PathologyDTO.Report dto);

    void audit(PathologyDTO.Audit dto);

    void publish(Long orderId);

    void cancel(PathologyDTO.Cancel dto);

    String orderNoById(Long id);

    boolean existsBySource(String sourceRecordNo);
}

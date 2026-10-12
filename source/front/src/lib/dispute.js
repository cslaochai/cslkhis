// 纠纷/投诉页面共用工具：字典由页面一次拉取后以 prop 传给各弹框；标签颜色口径列表与详情共用。
import {getDictDataMapList} from '@/api/system';
import {DICT_TYPE} from '@/lib/dict-cache';

/**
 * 拉取纠纷页 8 张字典，返回 { caseType, source, status, level, dealType, duty, relation, seal }。
 * 字典：8 张超过单次 5 个 type 的上限 → 分两批取（一次塞 8 个会整个返回空且零报错）
 */
export const fetchDisputeDicts = async () => {
  const a = await getDictDataMapList([
    DICT_TYPE.DISPUTE_CASE_TYPE, DICT_TYPE.DISPUTE_SOURCE, DICT_TYPE.DISPUTE_STATUS,
    DICT_TYPE.DISPUTE_LEVEL, DICT_TYPE.DISPUTE_DEAL_TYPE,
  ].join(','));
  const b = await getDictDataMapList([
    DICT_TYPE.DISPUTE_DUTY, DICT_TYPE.DISPUTE_RELATION, DICT_TYPE.DISPUTE_SEAL_STATUS,
  ].join(','));
  const da = a?.data || {};
  const db = b?.data || {};
  return {
    caseType: da[DICT_TYPE.DISPUTE_CASE_TYPE] || [],
    source: da[DICT_TYPE.DISPUTE_SOURCE] || [],
    status: da[DICT_TYPE.DISPUTE_STATUS] || [],
    level: da[DICT_TYPE.DISPUTE_LEVEL] || [],
    dealType: da[DICT_TYPE.DISPUTE_DEAL_TYPE] || [],
    duty: db[DICT_TYPE.DISPUTE_DUTY] || [],
    relation: db[DICT_TYPE.DISPUTE_RELATION] || [],
    seal: db[DICT_TYPE.DISPUTE_SEAL_STATUS] || [],
  };
};

export const statusTagType = (v) => {
  switch (Number(v)) {
    case 1:
      return 'warning';
    case 2:
      return 'primary';
    case 3:
      return 'danger';
    case 4:
      return 'success';
    case 5:
      return 'info';
    default:
      return 'info';
  }
};

export const levelTagType = (v) => {
  switch (Number(v)) {
    case 1:
      return 'info';
    case 2:
      return 'warning';
    case 3:
      return 'danger';
    default:
      return 'info';
  }
};

export const sealTagType = (v) => {
  switch (Number(v)) {
    case 1:
      return 'success';
    case 2:
      return 'danger';
    default:
      return 'info';
  }
};

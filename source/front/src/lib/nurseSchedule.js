// 文案唯一来源仍是后端 PublishStatusEnum（出参 publishStatusText），这里只补
// 「能不能走这一步」的判定，码值常量与 isPublishEffective 一律从 publishStatus.js 取。
import {PUBLISH_DRAFT, PUBLISH_PENDING, isPublishEffective} from './publishStatus'

/**
 * 护理排班（菜单 32x）按钮权限码与发布态流转判定 —— 页面与其拆出的弹框组件共用同一份，
 * 避免权限串与「哪一步能点」的门槛在多份文件里漂移。
 */
export const PERM_ADD = 'nursing:schedule:add';
export const PERM_EDIT = 'nursing:schedule:edit';
export const PERM_DELETE = 'nursing:schedule:delete';
/** 审核发布（G-15）：独立于排班权限，排班人不能自己把自己排的班发出去 */
export const PERM_AUDIT = 'nursing:schedule:audit';

/**
 * 发布态流转的可操作性判定（G-15）—— 矩阵格弹框与台账表格共用这一份，
 * 两边各写一套 if 一定会漂成「台账能点、弹框不能点」。
 * 一律要求有 id：没落库的新格子没有任何态可流转。
 * @param {{id?: string, publishStatus?: number}} row 矩阵 Cell 或台账 Row
 */
export const canSubmitPublish = (row) => !!row?.id && Number(row.publishStatus) === PUBLISH_DRAFT;
export const canAuditPublish = (row) => !!row?.id && Number(row.publishStatus) === PUBLISH_PENDING;
export const canAdjustPublish = (row) => !!row?.id && isPublishEffective(Number(row.publishStatus));
/** 撤回：任意态都能回草稿（含草稿本身，与后端 recall 的状态机同口径） */
export const canRecallPublish = (row) => !!row?.id;

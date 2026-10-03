package com.his.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.his.common.base.PageResult;
import com.his.system.dto.SysUserPasswordUpsertDTO;
import com.his.system.dto.SysUserQueryPageDTO;
import com.his.system.dto.SysUserUpsertDTO;
import com.his.system.entity.SysUser;
import com.his.system.vo.RoleNameVO;
import com.his.system.vo.SysUserListVO;
import com.his.system.vo.UserDetailVO;

import java.util.List;

/**
 * 用户服务接口
 */
public interface SysUserService extends IService<SysUser> {

    /**
     * 用户管理分页（出参已是 VO）
     */
    PageResult<SysUserListVO> queryUserPage(SysUserQueryPageDTO queryDTO);

    /**
     * 新增或修改用户：管理员与患者/其他类型账号的闸门、字段级留痕都在这里。
     */
    void upsertUser(SysUserUpsertDTO upsertDTO);

    /**
     * 删除用户（含闸门与删除留痕）
     */
    void removeUser(Long userId);

    /**
     * 管理端重置密码（含闸门与动作留痕，不记录口令本身）
     */
    void resetUserPassword(SysUserPasswordUpsertDTO resetDTO);

    /**
     * 分页查询用户列表
     */
    PageResult<SysUser> selectUserPage(String userName, Long deptId, Integer status, int pageNum, int pageSize);

    /**
     * 根据用户名查询用户
     */
    SysUser selectByUserName(String userName);

    /**
     * 新增用户
     */
    boolean addUser(SysUserUpsertDTO upsertDTO);

    /**
     * 修改用户
     */
    boolean updateUser(SysUserUpsertDTO upsertDTO);

    /**
     * 删除用户
     */
    boolean deleteUser(Long userId);

    /**
     * 重置密码
     */
    boolean resetPassword(Long userId);

    /**
     * 获取用户详情（包含角色、部门和员工信息）
     */
    UserDetailVO getUserDetail(Long userId);

    /**
     * 本人档案（只读展示）：手机号/身份证/邮箱由后端脱敏后出参，不接受 userId 入参。
     */
    UserDetailVO getSelfProfile();

    /**
     * 修改密码（验证旧密码）
     */
    boolean changePassword(Long userId, String oldPassword, String newPassword);

    /**
     * 按角色编码取「编码-名称」对照，编码为空返回空集合
     */
    List<RoleNameVO> selectRoleNames(List<String> roleCodes);
}

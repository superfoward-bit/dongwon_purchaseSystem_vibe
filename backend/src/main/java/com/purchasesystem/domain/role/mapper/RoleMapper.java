package com.purchasesystem.domain.role.mapper;

import com.purchasesystem.domain.role.CmRole;
import com.purchasesystem.domain.role.CmRoleFunc;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RoleMapper {

    List<CmRole> findRoles(@Param("keyword") String keyword);

    int countByRoleId(@Param("roleId") String roleId);

    int insertRole(CmRole role);

    int updateRole(CmRole role);

    int deleteRole(@Param("id") Long id, @Param("modId") String modId);

    /** 역할의 메뉴권한 목록 */
    List<CmRoleFunc> findRoleFuncs(@Param("roleId") String roleId);

    int deleteRoleFuncs(@Param("roleId") String roleId);

    int insertRoleFunc(CmRoleFunc func);
}

package com.purchasesystem.domain.role;

import com.purchasesystem.common.exception.BusinessException;
import com.purchasesystem.domain.role.mapper.RoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleMapper roleMapper;

    public List<CmRole> getRoles(String keyword) {
        return roleMapper.findRoles(keyword);
    }

    @Transactional
    public void createRole(CmRole role, String userId) {
        if (roleMapper.countByRoleId(role.getRoleId()) > 0) {
            throw new BusinessException("이미 존재하는 역할ID입니다: " + role.getRoleId());
        }
        role.setRegId(userId);
        roleMapper.insertRole(role);
    }

    @Transactional
    public void updateRole(CmRole role, String userId) {
        role.setModId(userId);
        if (roleMapper.updateRole(role) == 0) {
            throw new BusinessException("수정 대상이 없습니다.");
        }
    }

    @Transactional
    public void deleteRole(Long id, String userId) {
        roleMapper.deleteRole(id, userId);
    }

    public List<CmRoleFunc> getRoleFuncs(String roleId) {
        return roleMapper.findRoleFuncs(roleId);
    }

    /** 역할 메뉴권한 일괄 저장 (전체 삭제 후 재등록) */
    @Transactional
    public void saveRoleFuncs(String roleId, List<CmRoleFunc> funcs) {
        roleMapper.deleteRoleFuncs(roleId);
        if (funcs != null) {
            for (CmRoleFunc f : funcs) {
                if (f.getAuthCd() == null || f.getAuthCd().isBlank()) continue;
                f.setRoleId(roleId);
                roleMapper.insertRoleFunc(f);
            }
        }
    }
}

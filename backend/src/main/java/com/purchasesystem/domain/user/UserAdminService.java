package com.purchasesystem.domain.user;

import com.purchasesystem.common.exception.BusinessException;
import com.purchasesystem.domain.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserAdminService {

    private static final String DEFAULT_PWD = "1234";

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public List<CmUser> getUsers(String keyword) {
        return userMapper.findUsers(keyword);
    }

    @Transactional
    public void createUser(CmUser user, String actorId) {
        if (userMapper.countByLoginId(user.getLoginId()) > 0) {
            throw new BusinessException("이미 사용 중인 로그인ID입니다: " + user.getLoginId());
        }
        if (userMapper.countByUsrId(user.getCompCd(), user.getUsrId()) > 0) {
            throw new BusinessException("이미 존재하는 사용자ID입니다: " + user.getUsrId());
        }
        user.setPwdHash(passwordEncoder.encode(DEFAULT_PWD));   // 초기 비밀번호 1234
        userMapper.insertUser(user);
        saveRoles(user.getCompCd(), user.getUsrId(), user.getRoles());
    }

    @Transactional
    public void updateUser(CmUser user, String actorId) {
        user.setModId(actorId);
        if (userMapper.updateUser(user) == 0) {
            throw new BusinessException("수정 대상이 없습니다.");
        }
        if (user.getRoles() != null) {
            saveRoles(user.getCompCd(), user.getUsrId(), user.getRoles());
        }
    }

    @Transactional
    public void deleteUser(Long id, String actorId) {
        userMapper.deleteUser(id, actorId);
    }

    @Transactional
    public void resetPassword(Long id, String actorId) {
        userMapper.resetPassword(id, passwordEncoder.encode(DEFAULT_PWD), actorId);
    }

    public List<String> getUserRoles(String compCd, String usrId) {
        return userMapper.findRoleIds(compCd, usrId);
    }

    @Transactional
    public void saveRoles(String compCd, String usrId, List<String> roles) {
        userMapper.deleteUserRoles(compCd, usrId);
        if (roles != null) {
            for (String roleId : roles) {
                if (roleId == null || roleId.isBlank()) continue;
                userMapper.insertUserRole(compCd, usrId, roleId);
            }
        }
    }
}

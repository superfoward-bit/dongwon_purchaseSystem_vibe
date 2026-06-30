package com.purchasesystem.domain.user.mapper;

import com.purchasesystem.domain.user.CmUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserMapper {

    CmUser findByLoginId(@Param("loginId") String loginId);

    int countByLoginId(@Param("loginId") String loginId);

    int insertUser(CmUser user);

    int updateLastLogin(@Param("loginId") String loginId);

    /** 사용자의 역할 ID 목록 */
    List<String> findRoleIds(@Param("compCd") String compCd, @Param("usrId") String usrId);

    int insertUserRole(@Param("compCd") String compCd, @Param("usrId") String usrId,
                       @Param("roleId") String roleId);

    // ---- 사용자 관리 ----
    List<CmUser> findUsers(@Param("keyword") String keyword);

    int countByUsrId(@Param("compCd") String compCd, @Param("usrId") String usrId);

    int updateUser(CmUser user);

    int deleteUser(@Param("id") Long id, @Param("modId") String modId);

    int resetPassword(@Param("id") Long id, @Param("pwdHash") String pwdHash, @Param("modId") String modId);

    int deleteUserRoles(@Param("compCd") String compCd, @Param("usrId") String usrId);
}

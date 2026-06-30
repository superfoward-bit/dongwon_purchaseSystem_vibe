package com.purchasesystem.domain.user;

import lombok.Data;

import java.util.List;

/**
 * 사용자 (CM_USER).
 */
@Data
public class CmUser {
    private Long id;
    private String compCd;
    private String usrId;
    private String usrNm;
    private String loginId;
    private String pwdHash;
    private String deptCd;
    private String posCd;
    private String email;
    private String mobile;
    private String purcGrpCd;
    private String langCd;
    private String usrTyp;
    private String vdCd;
    private Integer pwdFailCnt;
    private String useYn;
    private String delYn;
    private String modId;
    private List<String> roles;   // 전송용(영속 아님): 부여 역할 ID 목록
}

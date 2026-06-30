package com.purchasesystem.security.jwt;

/**
 * 인증된 사용자 정보 (SecurityContext에 보관).
 */
public record LoginUser(String loginId, String compCd, String usrId, String usrNm, String vdCd) {
}

package com.purchasesystem;

/**
 * SonarCloud 연동 테스트용 데모 클래스.
 * 일부러 코드 냄새(code smell)를 넣어 자동 분석이 잡는지 확인한다.
 * 확인 후 삭제 예정.
 */
public class SonarDemo {

    // 하드코딩된 비밀번호 -> Security Hotspot 검출 대상
    private static final String PASSWORD = "admin1234";

    public int demo(int value) {
        int unused = 42; // 사용되지 않는 지역변수 -> Code Smell

        try {
            int result = value / 0; // 0으로 나누기 -> Bug
            return result;
        } catch (Exception e) {
            // 비어 있는 catch 블록 -> Code Smell
        }

        return value;
    }
}

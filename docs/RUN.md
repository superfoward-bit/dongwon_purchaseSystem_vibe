# 실행 · 테스트 가이드 (purchasesystem)

## 0. 구성 요약
- **DB**: Oracle (Docker 컨테이너 `purchase-oracle`) — 호스트포트 **1522**, 서비스 FREEPDB1, 계정 PURCHASE/purchase
- **백엔드**: Spring Boot, 포트 **8090** (jar 실행)
- **프론트**: Vue/Vite, 포트 **5173** (`/api` → 8090 프록시)
- **로그인**: admin / admin1234 (관리자), mgr1 / 1234 (결재 테스트), vendor1 / 1234 (협력사 포털)

> ⚠️ 호스트 Oracle XE가 8080을 점유하므로 백엔드는 **8090**을 씁니다. Oracle XE는 끄지 마세요.

---

## 1. Oracle 기동 (PowerShell 또는 터미널)
```powershell
# Docker Desktop이 꺼져 있으면 먼저 실행
Start-Process "C:\Program Files\Docker\Docker\Docker Desktop.exe"
# 데몬이 뜰 때까지 1~2분 대기 후
docker start purchase-oracle
```
DB 준비 확인 (성공하면 1 출력):
```bash
echo "SELECT 1 FROM DUAL;" | docker exec -i purchase-oracle sqlplus -S PURCHASE/purchase@localhost:1521/FREEPDB1
```
> 머신을 재부팅하면 컨테이너가 멈춰 있을 수 있습니다. `docker start purchase-oracle` 후 DB ready까지 30초~수분 대기.

---

## 2. 백엔드 기동
빌드(코드 변경 시에만):
```powershell
$env:JAVA_HOME="C:\Program Files\Microsoft\jdk-21.0.11.10-hotspot"
& C:\purchasesystem\backend\gradlew.bat -p C:\purchasesystem\backend build -x test
```
실행:
```powershell
& "C:\Program Files\Microsoft\jdk-21.0.11.10-hotspot\bin\java.exe" -jar C:\purchasesystem\backend\build\libs\purchase-backend-0.0.1-SNAPSHOT.jar
```
기동 확인 (400/401 이면 정상 기동):
```bash
curl -s -o /dev/null -w "%{http_code}" http://localhost:8090/api/auth/login -X POST -H "Content-Type: application/json" -d "{}"
```
> 기동에 ~20초 걸립니다. `Started PurchaseApplication` 로그가 뜨면 완료.
> 기존 java가 떠있으면 포트충돌 → 먼저 종료: PowerShell `Get-NetTCPConnection -LocalPort 8090 -State Listen | %{ Stop-Process -Id $_.OwningProcess -Force }`

---

## 3. 프론트엔드 기동
```powershell
cd C:\purchasesystem\frontend
npm run dev
```
브라우저에서 **http://localhost:5173** 접속.

---

## 4. 로그인
- 관리자: **admin / admin1234**
- 결재 테스트(2차 결재·대결): mgr1 / 1234
- 협력사 포털: vendor1 / 1234 (VD-00001)

---

## 5. 핵심 흐름 테스트 시나리오 (관리자 로그인 후)

### A. 구매 백본 (PR → 발주 → 입고 → 정산)
1. **구매요청**: 구매(PRO) > 구매요청 > 등록 → 품목(🔍), 수량/단가, (선택)예산 지정 → 저장 → 상신(결재선 admin) → 결재함에서 승인
2. **발주**: 구매 > 발주 > 등록 → 협력사 선택(결제조건 자동), 품목·과세구분, 입고창고/요청·확정납기 → 저장 → 상신 → 승인(여신한도 초과 시 차단됨)
3. **입고/검수**: 구매 > 입고 > 발주 선택 → 합격/불합격 수량 + **LOT/제조일/유통기한** + 입고창고 → 저장 → 상신 → 승인 ⇒ **재고 자동입고**
4. **재고 확인**: 재고관리 > 재고현황 — 방금 입고한 LOT/유통기한/현재고 확인. 수불부에 입고 이력
5. **정산**: 구매 > 정산/마감 > 협력사·마감월 → 대상조회 → 생성(갭정산·과세구분 VAT·지급예정일) → 상신 → 마감확정
6. **세금계산서/전표/지급**: 인터페이스연계(IFC) > 전자세금계산서·회계전표, 구매 > 지급관리 — 정산에서 생성 → 발행/전기/지급

### B. 재고·MRP 사이클
1. **출고**: 재고관리 > 출고 > 등록 → 창고·품목·LOT·수량 → 출고확정 ⇒ 재고 차감 (수불부 OUT)
2. **재고실사**: 재고관리 > 재고실사 > 등록 → 창고 선택 → "장부재고 불러오기" → 실사수량 입력 → 확정 ⇒ 차이만큼 조정(ADJ)
3. **MRP**: 기준정보 > 품목 에서 발주정책(안전재고/발주점/발주량) 설정 → 구매 > MRP/자동발주 > **MRP 실행** → 발주제안 확인 → 선택 → **PR 자동생성**

### C. 소싱
- 견적(RFx): 구매 > 견적 등록 → 공고 → 견적입력(대행) → 비교 → 선정 → 발주생성 / 또는 **유찰처리** → **재공고**
- 역경매: 구매 > 역경매 → 입찰 → 낙찰 → 발주생성

### D. 협력사·품질
- 협력사: 기준정보 > 협력사 — 신규등록 심사, 거래중지/재개(사유), 여신한도, 평가
- 품질: 품질관리 > 입고검사(규격 상·하한+측정값→자동 합부), 부적합/개선(CAPA 효과검증), 클레임(보상방식·실회수)

### E. 통제
- 예산: 기준정보 > 예산관리 — 집행률, PR 상신 시 예산초과 차단 확인
- 결재: 전자결재 > 결재함/상신함 — 합의·참조·전결·위임

---

## 6. 종료
```powershell
# 백엔드 종료
Get-NetTCPConnection -LocalPort 8090 -State Listen | %{ Stop-Process -Id $_.OwningProcess -Force }
# 프론트: npm 실행 터미널에서 Ctrl+C
# DB: 컨테이너는 켜둬도 됨 (멈추려면) docker stop purchase-oracle
```

---

## 7. 트러블슈팅
| 증상 | 원인 / 해결 |
|---|---|
| 로그인이 401 HTML 페이지로 옴 | 백엔드(8090)가 죽고 Oracle XE가 8080 가로챔 → 백엔드 재기동, 8090 사용 확인 |
| 백엔드 기동 직후 즉시 종료 + "Connection refused 1522" | Oracle 컨테이너 미기동 → `docker start purchase-oracle` 후 DB ready 대기 |
| API가 500 (cold start) | 재기동 직후 첫 호출은 간헐 500 → 재시도 |
| 화면 빈 메뉴 | 백엔드/프론트 중 하나 미기동 → 둘 다 확인 |
| 한글 깨짐(콘솔) | 표시 문제일 뿐, DB·화면은 정상(UTF-8) |

> 참고시스템(C:\smartx91, D:\smartx91)은 읽기 전용 참고용이며 절대 수정하지 않음.

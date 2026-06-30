# 설계 문서

## 기능
| 문서 | 내용 | 상태 |
|------|------|------|
| `01-feature-matrix.md` | DIPS+DFS 합집합 전체 기능 목록(+감사로그·상태전이) | ✅ |

## DB 설계
| 문서 | 내용 | 상태 |
|------|------|------|
| `03-db-design.md` | 설계표준·전체 테이블 인벤토리(~85)·CM_/LOG_ 기반 | ✅ |
| `03-db-design-status-transition.md` | 문서 상태전이(전진+되돌리기) 공통 모델 | ✅ |
| `03-db-design-part2-procurement.md` | PR/RX/AU/PO/GR 구매 트랜잭션 | ✅ |
| `03-db-design-part3-pricing-settlement.md` | CT/IP/TP/CL/PF 단가·정산(갭정산) | ✅ |
| `03-db-design-part4-vendor-item.md` | VD/IT 협력사·품목 마스터 | ✅ |
| `03-db-design-part5-quality-approval-edoc-if.md` | QL/AP/ED/IF 품질·결재·전자계약·인터페이스 | ✅ |

## 예정
| 문서 | 내용 | 상태 |
|------|------|------|
| `db/ddl/*.sql` | 물리 DDL(Oracle CREATE TABLE) | 예정 |
| `04-screen-list.md` | 화면 목록·메뉴 구조 | 예정 |
| `05-interface.md` | SAP/INDIGO 연계 설계(계승 vs REST) | 예정 |

> 참고 원본(읽기 전용): DIPS `C:\smartx91\workspace\smartx9.1-dwhf2018`, DFS `D:\smartx91\workspace\smartx9.1-dwfb2020`

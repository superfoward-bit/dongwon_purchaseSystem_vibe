package com.purchasesystem.domain.tax;

import lombok.Data;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** 전자세금계산서 (TX_TAXBILL). */
@Data
public class TxTaxbill {
    private Long id;
    private String compCd;
    private String billNo;
    private Long closeId;
    private String closeNo;
    private String vdCd;
    private String vdNm;
    private String billYmd;
    private String billTyp;
    private BigDecimal supplyAmt;
    private BigDecimal vatAmt;
    private BigDecimal totAmt;
    private String billSts;
    private String billStsNm;
    private String ntsStatus;
    private String ntsApprovalNo;
    private String writeYmd;
    private String issueYmd;
    private String supBizNo;
    private String supNm;
    private String supCeo;
    private String supAddr;
    private String buyBizNo;
    private String buyNm;
    private String buyCeo;
    private String buyAddr;
    private String billKind;
    private String origBillNo;
    private String modifyRsn;
    private String remark;
    private String regId;
    private String regDt;

    private List<TxTaxbillItem> items = new ArrayList<>();
}

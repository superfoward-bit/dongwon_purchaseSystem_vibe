package com.purchasesystem.domain.stock.mapper;

import com.purchasesystem.domain.stock.StIssue;
import com.purchasesystem.domain.stock.StIssueItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface IssueMapper {
    List<StIssue> findList(@Param("keyword") String keyword, @Param("sts") String sts);
    StIssue findById(@Param("id") Long id);
    List<StIssueItem> findItems(@Param("issueId") Long issueId);
    void insert(StIssue i);
    void updateHeader(StIssue i);
    void updateStatus(@Param("id") Long id, @Param("sts") String sts, @Param("modId") String modId);
    void deleteItems(@Param("issueId") Long issueId);
    void insertItem(StIssueItem it);
}

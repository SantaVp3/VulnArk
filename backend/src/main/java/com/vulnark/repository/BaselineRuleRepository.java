package com.vulnark.repository;

import com.vulnark.entity.BaselineRule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BaselineRuleRepository extends JpaRepository<BaselineRule, Long> {
    
    Optional<BaselineRule> findByRuleId(String ruleId);
    
    List<BaselineRule> findByEnabledTrue();
    
    List<BaselineRule> findByPlatformAndEnabledTrue(BaselineRule.Platform platform);
    
    @Query("SELECT r FROM BaselineRule r WHERE (r.platform = :platform OR r.platform = 'BOTH') AND r.enabled = true")
    List<BaselineRule> findByPlatformOrBothAndEnabledTrue(@Param("platform") BaselineRule.Platform platform);
    
    List<BaselineRule> findByStandardAndEnabledTrue(String standard);
    
    @Query("SELECT r FROM BaselineRule r WHERE r.standard = :standard AND r.version = :version AND r.enabled = true")
    List<BaselineRule> findByStandardAndVersionAndEnabledTrue(@Param("standard") String standard, @Param("version") String version);
    
    List<BaselineRule> findByCategoryAndEnabledTrue(String category);
    
    List<BaselineRule> findBySeverityAndEnabledTrue(BaselineRule.Severity severity);
    
    @Query("SELECT r FROM BaselineRule r WHERE r.name LIKE %:keyword% OR r.description LIKE %:keyword% OR r.category LIKE %:keyword%")
    Page<BaselineRule> findByKeyword(@Param("keyword") String keyword, Pageable pageable);
    
    @Query("SELECT DISTINCT r.standard FROM BaselineRule r WHERE r.enabled = true")
    List<String> findDistinctStandards();
    
    @Query("SELECT DISTINCT r.category FROM BaselineRule r WHERE r.enabled = true")
    List<String> findDistinctCategories();
    
    @Query("SELECT DISTINCT r.version FROM BaselineRule r WHERE r.standard = :standard AND r.enabled = true")
    List<String> findDistinctVersionsByStandard(@Param("standard") String standard);
    
    @Query("SELECT COUNT(r) FROM BaselineRule r WHERE r.enabled = true")
    long countEnabledRules();
    
    @Query("SELECT COUNT(r) FROM BaselineRule r WHERE r.platform = :platform AND r.enabled = true")
    long countByPlatformAndEnabledTrue(@Param("platform") BaselineRule.Platform platform);
    
    @Query("SELECT COUNT(r) FROM BaselineRule r WHERE r.severity = :severity AND r.enabled = true")
    long countBySeverityAndEnabledTrue(@Param("severity") BaselineRule.Severity severity);
}

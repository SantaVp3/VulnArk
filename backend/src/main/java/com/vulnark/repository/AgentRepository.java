package com.vulnark.repository;

import com.vulnark.entity.Agent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AgentRepository extends JpaRepository<Agent, Long> {
    
    Optional<Agent> findByAgentId(String agentId);
    
    List<Agent> findByStatus(Agent.Status status);
    
    @Query("SELECT a FROM Agent a WHERE a.lastHeartbeat < :threshold")
    List<Agent> findOfflineAgents(@Param("threshold") LocalDateTime threshold);
    
    @Query("SELECT a FROM Agent a WHERE a.platform = :platform")
    List<Agent> findByPlatform(@Param("platform") Agent.Platform platform);
    
    @Query("SELECT a FROM Agent a WHERE a.hostname LIKE %:keyword% OR a.name LIKE %:keyword% OR a.ipAddress LIKE %:keyword%")
    Page<Agent> findByKeyword(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT a FROM Agent a WHERE " +
           "(:keyword IS NULL OR a.hostname LIKE %:keyword% OR a.name LIKE %:keyword% OR a.ipAddress LIKE %:keyword%) AND " +
           "(:status IS NULL OR a.status = :status) AND " +
           "(:platform IS NULL OR a.platform = :platform)")
    Page<Agent> findByConditions(@Param("keyword") String keyword,
                                @Param("status") Agent.Status status,
                                @Param("platform") Agent.Platform platform,
                                Pageable pageable);
    
    @Query("SELECT COUNT(a) FROM Agent a WHERE a.status = :status")
    long countByStatus(@Param("status") Agent.Status status);

    @Query("SELECT COUNT(a) FROM Agent a WHERE a.platform = :platform")
    long countByPlatform(@Param("platform") Agent.Platform platform);

    boolean existsByAgentId(String agentId);

    boolean existsByHostnameAndIpAddress(String hostname, String ipAddress);
}

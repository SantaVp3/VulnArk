package com.vulnark.repository;

import com.vulnark.entity.Agent;
import com.vulnark.entity.BaselineResult;
import com.vulnark.entity.BaselineTask;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BaselineResultRepository extends JpaRepository<BaselineResult, Long> {
    
    List<BaselineResult> findByTask(BaselineTask task);
    
    List<BaselineResult> findByAgent(Agent agent);
    
    List<BaselineResult> findByTaskAndStatus(BaselineTask task, BaselineResult.Status status);
    
    @Query("SELECT r FROM BaselineResult r WHERE r.agent.agentId = :agentId")
    List<BaselineResult> findByAgentId(@Param("agentId") String agentId);
    
    @Query("SELECT r FROM BaselineResult r WHERE r.task.taskId = :taskId")
    List<BaselineResult> findByTaskId(@Param("taskId") String taskId);
    
    @Query("SELECT r FROM BaselineResult r WHERE r.severity = :severity AND r.status = :status")
    Page<BaselineResult> findBySeverityAndStatus(@Param("severity") BaselineResult.Severity severity, 
                                                 @Param("status") BaselineResult.Status status, 
                                                 Pageable pageable);
    
    @Query("SELECT r FROM BaselineResult r WHERE r.category = :category")
    List<BaselineResult> findByCategory(@Param("category") String category);
    
    @Query("SELECT COUNT(r) FROM BaselineResult r WHERE r.task = :task AND r.status = :status")
    long countByTaskAndStatus(@Param("task") BaselineTask task, @Param("status") BaselineResult.Status status);
    
    @Query("SELECT COUNT(r) FROM BaselineResult r WHERE r.agent = :agent AND r.status = :status")
    long countByAgentAndStatus(@Param("agent") Agent agent, @Param("status") BaselineResult.Status status);
    
    @Query("SELECT COUNT(r) FROM BaselineResult r WHERE r.severity = :severity AND r.status = 'FAIL'")
    long countFailedBySeverity(@Param("severity") BaselineResult.Severity severity);
    
    @Query("SELECT r.category, COUNT(r) FROM BaselineResult r WHERE r.task = :task GROUP BY r.category")
    List<Object[]> countByCategory(@Param("task") BaselineTask task);
    
    @Query("SELECT r FROM BaselineResult r WHERE r.checkTime BETWEEN :startTime AND :endTime")
    List<BaselineResult> findByCheckTimeBetween(@Param("startTime") LocalDateTime startTime, 
                                               @Param("endTime") LocalDateTime endTime);
}

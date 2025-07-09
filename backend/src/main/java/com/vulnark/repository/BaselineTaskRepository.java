package com.vulnark.repository;

import com.vulnark.entity.Agent;
import com.vulnark.entity.BaselineTask;
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
public interface BaselineTaskRepository extends JpaRepository<BaselineTask, Long> {
    
    Optional<BaselineTask> findByTaskId(String taskId);
    
    List<BaselineTask> findByAgent(Agent agent);
    
    List<BaselineTask> findByAgentAndStatus(Agent agent, BaselineTask.Status status);
    
    @Query("SELECT t FROM BaselineTask t WHERE t.agent.agentId = :agentId AND t.status = :status")
    List<BaselineTask> findByAgentIdAndStatus(@Param("agentId") String agentId, @Param("status") BaselineTask.Status status);
    
    @Query("SELECT t FROM BaselineTask t WHERE t.agent.agentId = :agentId AND t.status IN :statuses ORDER BY t.createdTime DESC")
    List<BaselineTask> findPendingTasksByAgentId(@Param("agentId") String agentId, @Param("statuses") List<BaselineTask.Status> statuses);
    
    @Query("SELECT t FROM BaselineTask t WHERE t.status = :status AND t.scheduledTime <= :currentTime")
    List<BaselineTask> findScheduledTasks(@Param("status") BaselineTask.Status status, @Param("currentTime") LocalDateTime currentTime);
    
    @Query("SELECT t FROM BaselineTask t WHERE t.taskType = :taskType")
    Page<BaselineTask> findByTaskType(@Param("taskType") BaselineTask.TaskType taskType, Pageable pageable);
    
    @Query("SELECT COUNT(t) FROM BaselineTask t WHERE t.status = :status")
    long countByStatus(@Param("status") BaselineTask.Status status);
    
    @Query("SELECT COUNT(t) FROM BaselineTask t WHERE t.agent = :agent AND t.status = :status")
    long countByAgentAndStatus(@Param("agent") Agent agent, @Param("status") BaselineTask.Status status);
}

package com.vulnark.service;

import com.vulnark.entity.ScanTask;
import com.vulnark.entity.ScanResult;
import com.vulnark.entity.Asset;
import com.vulnark.entity.User;
import com.vulnark.repository.ScanTaskRepository;
import com.vulnark.repository.ScanResultRepository;
import com.vulnark.repository.AssetRepository;
import com.vulnark.repository.UserRepository;
// import com.vulnark.service.scanner.ScannerEngine;
// import com.vulnark.service.scanner.ScannerEngineFactory;
import com.vulnark.dto.ScanTaskRequest;
import com.vulnark.dto.ScanTaskResponse;
import com.vulnark.exception.BusinessException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * 扫描服务
 */
@Service
@Transactional
public class ScanningService {

    private static final Logger logger = LoggerFactory.getLogger(ScanningService.class);

    @Autowired
    private ScanTaskRepository scanTaskRepository;

    @Autowired
    private ScanResultRepository scanResultRepository;

    @Autowired
    private AssetRepository assetRepository;

    @Autowired
    private UserRepository userRepository;

    // @Autowired
    // private ScannerEngineFactory scannerEngineFactory;

    /**
     * 创建扫描任务
     */
    public ScanTaskResponse createScanTask(ScanTaskRequest request, Long userId) {
        try {
            // 验证用户
            User user = userRepository.findByIdAndDeletedFalse(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));

            // 创建扫描任务
            ScanTask scanTask = new ScanTask();
            scanTask.setName(request.getName());
            scanTask.setDescription(request.getDescription());
            scanTask.setScanType(request.getScanType());
            scanTask.setScanEngineType(request.getScanEngine());
            scanTask.setScanTemplate(request.getScanTemplate());
            scanTask.setCreatedBy(user);
            scanTask.setScanParameters(request.getScanParameters());
            scanTask.setScheduledStartTime(request.getScheduledStartTime());
            scanTask.setScanConfigId(request.getScanConfigId());

            // 设置项目关联
            if (request.getProjectId() != null) {
                // 这里可以添加项目验证逻辑
                // Project project = projectRepository.findById(request.getProjectId())...
            }

            // 保存任务
            scanTask = scanTaskRepository.save(scanTask);

            logger.info("创建扫描任务成功: {}", scanTask.getId());
            return convertToResponse(scanTask);

        } catch (Exception e) {
            logger.error("创建扫描任务失败: {}", e.getMessage(), e);
            throw new BusinessException("创建扫描任务失败: " + e.getMessage());
        }
    }

    /**
     * 更新扫描任务
     */
    public ScanTaskResponse updateScanTask(Long taskId, ScanTaskRequest request, Long userId) {
        try {
            ScanTask scanTask = scanTaskRepository.findByIdAndDeletedFalse(taskId)
                .orElseThrow(() -> new BusinessException("扫描任务不存在"));

            // 检查任务状态，只有未开始的任务才能修改
            if (!scanTask.canStart()) {
                throw new BusinessException("任务已开始执行，无法修改");
            }

            // 更新任务信息
            scanTask.setName(request.getName());
            scanTask.setDescription(request.getDescription());
            scanTask.setScanType(request.getScanType());
            scanTask.setScanEngineType(request.getScanEngine());
            scanTask.setScanTemplate(request.getScanTemplate());
            scanTask.setScanParameters(request.getScanParameters());
            scanTask.setScheduledStartTime(request.getScheduledStartTime());

            scanTask = scanTaskRepository.save(scanTask);

            logger.info("更新扫描任务成功: {}", taskId);
            return convertToResponse(scanTask);

        } catch (Exception e) {
            logger.error("更新扫描任务失败: {}", e.getMessage(), e);
            throw new BusinessException("更新扫描任务失败: " + e.getMessage());
        }
    }

    /**
     * 删除扫描任务
     */
    public void deleteScanTask(Long taskId, Long userId) {
        try {
            ScanTask scanTask = scanTaskRepository.findByIdAndDeletedFalse(taskId)
                .orElseThrow(() -> new BusinessException("扫描任务不存在"));

            // 检查任务状态，正在运行的任务需要先停止
            if (scanTask.isRunning()) {
                throw new BusinessException("任务正在运行，请先停止任务");
            }

            // 软删除任务
            scanTask.setDeleted(true);
            scanTaskRepository.save(scanTask);

            // 同时删除相关的扫描结果
            scanResultRepository.deleteByScanTaskId(taskId, LocalDateTime.now());

            logger.info("删除扫描任务成功: {}", taskId);

        } catch (Exception e) {
            logger.error("删除扫描任务失败: {}", e.getMessage(), e);
            throw new BusinessException("删除扫描任务失败: " + e.getMessage());
        }
    }

    /**
     * 启动扫描任务
     */
    @Async
    public CompletableFuture<Void> startScanTask(Long taskId, Long userId) {
        try {
            ScanTask scanTask = scanTaskRepository.findByIdAndDeletedFalse(taskId)
                .orElseThrow(() -> new BusinessException("扫描任务不存在"));

            // 检查任务状态
            if (!scanTask.canStart()) {
                throw new BusinessException("任务状态不允许启动");
            }

            // 标记任务开始
            scanTask.markAsStarted();
            scanTaskRepository.save(scanTask);

            logger.info("开始执行扫描任务: {}", taskId);

            // TODO: 实际的扫描引擎集成将在Phase 2实现
            // 现在只是模拟扫描过程
            simulateScan(scanTask);

            return CompletableFuture.completedFuture(null);

        } catch (Exception e) {
            logger.error("启动扫描任务失败: {}", e.getMessage(), e);
            // 标记任务失败
            updateTaskStatus(taskId, ScanTask.TaskStatus.FAILED, e.getMessage());
            throw new BusinessException("启动扫描任务失败: " + e.getMessage());
        }
    }

    /**
     * 停止扫描任务
     */
    public void stopScanTask(Long taskId, Long userId) {
        try {
            ScanTask scanTask = scanTaskRepository.findByIdAndDeletedFalse(taskId)
                .orElseThrow(() -> new BusinessException("扫描任务不存在"));

            if (!scanTask.canCancel()) {
                throw new BusinessException("任务状态不允许停止");
            }

            // TODO: 停止扫描引擎的实际实现将在Phase 2添加

            // 更新任务状态
            scanTask.markAsCancelled();
            scanTaskRepository.save(scanTask);

            logger.info("停止扫描任务成功: {}", taskId);

        } catch (Exception e) {
            logger.error("停止扫描任务失败: {}", e.getMessage(), e);
            throw new BusinessException("停止扫描任务失败: " + e.getMessage());
        }
    }

    /**
     * 获取扫描任务详情
     */
    public ScanTaskResponse getScanTask(Long taskId) {
        ScanTask scanTask = scanTaskRepository.findByIdAndDeletedFalse(taskId)
            .orElseThrow(() -> new BusinessException("扫描任务不存在"));
        return convertToResponse(scanTask);
    }

    /**
     * 获取扫描任务列表
     */
    public Page<ScanTaskResponse> getScanTasks(Pageable pageable) {
        Page<ScanTask> tasks = scanTaskRepository.findByDeletedFalse(pageable);
        return tasks.map(this::convertToResponse);
    }

    /**
     * 根据条件查询扫描任务
     */
    public Page<ScanTaskResponse> searchScanTasks(String name, ScanTask.TaskStatus status, 
                                                 ScanTask.ScanType scanType, ScanTask.ScanEngine scanEngine,
                                                 Long projectId, Long createdById, Pageable pageable) {
        Page<ScanTask> tasks = scanTaskRepository.findByConditions(
            name, status, scanType, scanEngine, projectId, createdById, pageable);
        return tasks.map(this::convertToResponse);
    }

    /**
     * 获取扫描结果
     */
    public Page<ScanResult> getScanResults(Long taskId, Pageable pageable) {
        return scanResultRepository.findByScanTaskIdAndDeletedFalse(taskId, pageable);
    }

    /**
     * 模拟扫描过程 (Phase 1 - 基础实现)
     */
    private void simulateScan(ScanTask scanTask) {
        try {
            // 模拟扫描进度
            for (int progress = 0; progress <= 100; progress += 20) {
                scanTask.updateProgress(progress);
                scanTaskRepository.save(scanTask);

                // 模拟扫描耗时
                Thread.sleep(2000); // 2秒

                // 检查任务是否被取消
                ScanTask currentTask = scanTaskRepository.findByIdAndDeletedFalse(scanTask.getId()).orElse(null);
                if (currentTask == null || currentTask.getStatus() == ScanTask.TaskStatus.CANCELLED) {
                    logger.info("扫描任务被取消: {}", scanTask.getId());
                    return;
                }
            }

            // 生成模拟扫描结果
            generateMockScanResults(scanTask);

            // 标记任务完成
            scanTask.markAsCompleted();
            scanTaskRepository.save(scanTask);

            logger.info("模拟扫描完成: {}", scanTask.getId());

        } catch (Exception e) {
            logger.error("模拟扫描失败: {}", e.getMessage(), e);
            scanTask.markAsFailed(e.getMessage());
            scanTaskRepository.save(scanTask);
        }
    }

    /**
     * 生成模拟扫描结果
     */
    private void generateMockScanResults(ScanTask scanTask) {
        try {
            // 创建一些模拟的扫描结果
            String[] mockHosts = {"192.168.1.1", "192.168.1.2", "192.168.1.3"};
            String[] mockVulns = {
                "SQL注入漏洞", "跨站脚本攻击(XSS)", "文件上传漏洞",
                "弱密码", "目录遍历", "信息泄露"
            };
            ScanResult.Severity[] severities = {
                ScanResult.Severity.HIGH, ScanResult.Severity.MEDIUM,
                ScanResult.Severity.LOW, ScanResult.Severity.INFO
            };

            int totalResults = 0;
            int highCount = 0, mediumCount = 0, lowCount = 0, infoCount = 0;

            for (String host : mockHosts) {
                for (int i = 0; i < 2; i++) { // 每个主机2个漏洞
                    ScanResult result = new ScanResult();
                    result.setScanTask(scanTask);
                    result.setTargetHost(host);
                    result.setTargetPort(80 + i);
                    result.setVulnerabilityName(mockVulns[totalResults % mockVulns.length]);
                    result.setSeverity(severities[totalResults % severities.length]);
                    result.setVulnerabilityDescription("这是一个模拟的漏洞描述");
                    result.setSolution("建议修复方案");
                    result.setCvssScore(5.0 + (totalResults % 5));

                    // 尝试关联资产
                    Optional<Asset> asset = assetRepository.findByIpAddressAndDeletedFalse(host);
                    asset.ifPresent(result::setAsset);

                    scanResultRepository.save(result);

                    // 统计数量
                    totalResults++;
                    switch (result.getSeverity()) {
                        case CRITICAL:
                        case HIGH:
                            highCount++;
                            break;
                        case MEDIUM:
                            mediumCount++;
                            break;
                        case LOW:
                            lowCount++;
                            break;
                        case INFO:
                            infoCount++;
                            break;
                    }
                }
            }

            // 更新任务统计
            scanTask.setTotalVulnerabilityCount(totalResults);
            scanTask.setHighRiskCount(highCount);
            scanTask.setMediumRiskCount(mediumCount);
            scanTask.setLowRiskCount(lowCount);
            scanTask.setInfoRiskCount(infoCount);

            logger.info("生成模拟扫描结果完成，任务ID: {}, 结果数量: {}", scanTask.getId(), totalResults);

        } catch (Exception e) {
            logger.error("生成模拟扫描结果失败: {}", e.getMessage(), e);
        }
    }



    /**
     * 更新任务状态
     */
    private void updateTaskStatus(Long taskId, ScanTask.TaskStatus status, String errorMessage) {
        try {
            Optional<ScanTask> taskOpt = scanTaskRepository.findByIdAndDeletedFalse(taskId);
            if (taskOpt.isPresent()) {
                ScanTask task = taskOpt.get();
                task.setStatus(status);
                if (errorMessage != null) {
                    task.setErrorMessage(errorMessage);
                }
                scanTaskRepository.save(task);
            }
        } catch (Exception e) {
            logger.error("更新任务状态失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 转换为响应对象
     */
    private ScanTaskResponse convertToResponse(ScanTask scanTask) {
        ScanTaskResponse response = new ScanTaskResponse();
        response.setId(scanTask.getId());
        response.setName(scanTask.getName());
        response.setDescription(scanTask.getDescription());
        response.setScanType(scanTask.getScanType());
        response.setScanEngine(scanTask.getScanEngineType());
        response.setScanTemplate(scanTask.getScanTemplate());
        response.setStatus(scanTask.getStatus());
        response.setProgress(scanTask.getProgress());
        response.setTotalVulnerabilityCount(scanTask.getTotalVulnerabilityCount());
        response.setHighRiskCount(scanTask.getHighRiskCount());
        response.setMediumRiskCount(scanTask.getMediumRiskCount());
        response.setLowRiskCount(scanTask.getLowRiskCount());
        response.setInfoRiskCount(scanTask.getInfoRiskCount());
        response.setScheduledStartTime(scanTask.getScheduledStartTime());
        response.setActualStartTime(scanTask.getActualStartTime());
        response.setCompletedTime(scanTask.getCompletedTime());
        response.setCreatedTime(scanTask.getCreatedTime());
        response.setUpdatedTime(scanTask.getUpdatedTime());
        response.setErrorMessage(scanTask.getErrorMessage());
        response.setExternalTaskId(scanTask.getExternalTaskId());
        response.setScanParameters(scanTask.getScanParameters());

        if (scanTask.getCreatedBy() != null) {
            response.setCreatedById(scanTask.getCreatedBy().getId());
            response.setCreatedByName(scanTask.getCreatedBy().getUsername());
        }

        if (scanTask.getProject() != null) {
            response.setProjectId(scanTask.getProject().getId());
            response.setProjectName(scanTask.getProject().getName());
        }

        // 计算实际执行时间
        if (scanTask.getActualStartTime() != null && scanTask.getCompletedTime() != null) {
            Long duration = scanTask.getDurationMinutes();
            response.setActualDuration(duration != null ? duration.intValue() : null);
        }

        return response;
    }
}

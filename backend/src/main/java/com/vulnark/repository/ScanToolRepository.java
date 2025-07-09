package com.vulnark.repository;

import com.vulnark.entity.ScanTool;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 扫描工具仓库接口
 */
@Repository
public interface ScanToolRepository extends JpaRepository<ScanTool, Long> {
    
    /**
     * 根据工具名称查找
     */
    Optional<ScanTool> findByName(String name);
    
    /**
     * 查找所有已安装的工具
     */
    @Query("SELECT st FROM ScanTool st WHERE st.status = 'INSTALLED' OR st.status = 'OUTDATED'")
    List<ScanTool> findAllInstalled();
    
    /**
     * 查找需要更新的工具
     */
    @Query("SELECT st FROM ScanTool st WHERE st.autoUpdate = true AND st.currentVersion != st.latestVersion")
    List<ScanTool> findNeedUpdate();
    
    /**
     * 根据状态查找工具
     */
    List<ScanTool> findByStatus(ScanTool.ToolStatus status);
    
    /**
     * 查找启用自动更新的工具
     */
    List<ScanTool> findByAutoUpdateTrue();
    
    /**
     * 统计各状态的工具数量
     */
    @Query("SELECT st.status, COUNT(st) FROM ScanTool st GROUP BY st.status")
    List<Object[]> countByStatus();
}

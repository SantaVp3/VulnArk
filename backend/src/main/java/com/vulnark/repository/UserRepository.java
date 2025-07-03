package com.vulnark.repository;

import com.vulnark.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // 基础查询
    @Query("SELECT u FROM User u WHERE u.username = ?1 AND u.deleted = false")
    Optional<User> findByUsername(String username);

    @Query("SELECT u FROM User u WHERE u.email = ?1 AND u.deleted = false")
    Optional<User> findByEmail(String email);

    @Query("SELECT u FROM User u WHERE (u.username = ?1 OR u.email = ?1) AND u.deleted = false")
    Optional<User> findByUsernameOrEmail(String usernameOrEmail);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    // 查找未删除的用户
    List<User> findByDeletedFalse();
    Page<User> findByDeletedFalse(Pageable pageable);
    Optional<User> findByIdAndDeletedFalse(Long id);

    // 根据角色查找用户
    List<User> findByRoleAndDeletedFalse(User.Role role);

    // 根据状态查找用户
    List<User> findByStatusAndDeletedFalse(User.Status status);

    // 根据部门查找用户
    List<User> findByDepartmentAndDeletedFalse(String department);

    // 复合查询：根据多个条件查找用户
    @Query("SELECT u FROM User u WHERE u.deleted = false " +
           "AND (:username IS NULL OR u.username LIKE %:username%) " +
           "AND (:email IS NULL OR u.email LIKE %:email%) " +
           "AND (:fullName IS NULL OR u.fullName LIKE %:fullName%) " +
           "AND (:role IS NULL OR u.role = :role) " +
           "AND (:status IS NULL OR u.status = :status) " +
           "AND (:department IS NULL OR u.department LIKE %:department%) " +
           "AND (:position IS NULL OR u.position LIKE %:position%)")
    Page<User> findByConditions(
            @Param("username") String username,
            @Param("email") String email,
            @Param("fullName") String fullName,
            @Param("role") User.Role role,
            @Param("status") User.Status status,
            @Param("department") String department,
            @Param("position") String position,
            Pageable pageable);

    // 全文搜索
    @Query("SELECT u FROM User u WHERE u.deleted = false " +
           "AND (u.username LIKE %:keyword% " +
           "OR u.email LIKE %:keyword% " +
           "OR u.fullName LIKE %:keyword% " +
           "OR u.department LIKE %:keyword% " +
           "OR u.position LIKE %:keyword%)")
    Page<User> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    // 统计查询
    @Query("SELECT COUNT(u) FROM User u WHERE u.deleted = false")
    long countByDeletedFalse();

    @Query("SELECT COUNT(u) FROM User u WHERE u.deleted = false AND u.role = :role")
    long countByRoleAndDeletedFalse(@Param("role") User.Role role);

    @Query("SELECT COUNT(u) FROM User u WHERE u.deleted = false AND u.status = :status")
    long countByStatusAndDeletedFalse(@Param("status") User.Status status);

    // 获取用户统计信息
    @Query("SELECT u.role, COUNT(u) FROM User u WHERE u.deleted = false GROUP BY u.role")
    List<Object[]> getUserRoleStatistics();

    @Query("SELECT u.status, COUNT(u) FROM User u WHERE u.deleted = false GROUP BY u.status")
    List<Object[]> getUserStatusStatistics();

    @Query("SELECT u.department, COUNT(u) FROM User u WHERE u.deleted = false AND u.department IS NOT NULL GROUP BY u.department")
    List<Object[]> getUserDepartmentStatistics();

    // 仪表盘相关查询
    // 兼容旧的查询方法（保留以防有其他地方使用）
    @Query("SELECT COUNT(u) FROM User u WHERE u.deleted = false AND u.status = 'ACTIVE'")
    long countByEnabledTrueAndDeletedFalse();

    @Query("SELECT COUNT(u) FROM User u WHERE u.deleted = false AND u.status != 'ACTIVE'")
    long countByEnabledFalseAndDeletedFalse();

    // 按角色统计用户数量（字符串参数）
    @Query("SELECT COUNT(u) FROM User u WHERE u.deleted = false AND u.role = :role")
    long countByRoleStringAndDeletedFalse(@Param("role") String role);
}

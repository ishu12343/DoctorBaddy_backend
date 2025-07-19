package com.healthcare.repository;

import com.healthcare.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    List<Notification> findByUserIdAndIsReadFalseOrderByCreatedAtDesc(Long userId);
    
    @Query("SELECT n FROM Notification n " +
           "WHERE n.user.id = :userId " +
           "AND n.type = :type " +
           "ORDER BY n.createdAt DESC")
    List<Notification> findByUserIdAndType(
        @Param("userId") Long userId,
        @Param("type") String type
    );
    
    @Query("SELECT n FROM Notification n " +
           "WHERE n.user.id = :userId " +
           "AND n.isRead = false " +
           "AND n.type = :type " +
           "ORDER BY n.createdAt DESC")
    List<Notification> findUnreadByUserIdAndType(
        @Param("userId") Long userId,
        @Param("type") String type
    );
    
    @Query("SELECT COUNT(n) FROM Notification n " +
           "WHERE n.user.id = :userId " +
           "AND n.isRead = false")
    long countUnreadByUserId(@Param("userId") Long userId);
    
    @Modifying
    @Transactional
    @Query("UPDATE Notification n SET n.isRead = true, n.readAt = CURRENT_TIMESTAMP " +
           "WHERE n.id = :id")
    void markAsRead(@Param("id") Long id);
    
    @Modifying
    @Transactional
    @Query("UPDATE Notification n SET n.isRead = true, n.readAt = CURRENT_TIMESTAMP " +
           "WHERE n.user.id = :userId")
    void markAllAsRead(@Param("userId") Long userId);
    
    @Query("SELECT n FROM Notification n " +
           "WHERE n.user.id = :userId " +
           "AND n.createdAt >= :afterDate " +
           "ORDER BY n.createdAt DESC")
    List<Notification> findRecentByUserId(
        @Param("userId") Long userId,
        @Param("afterDate") LocalDateTime afterDate
    );
    
    @Query("SELECT n FROM Notification n " +
           "WHERE n.user.id = :userId " +
           "AND n.priority = :priority " +
           "ORDER BY n.createdAt DESC")
    List<Notification> findByPriority(
        @Param("userId") Long userId,
        @Param("priority") String priority
    );
    
    @Query("SELECT n FROM Notification n " +
           "WHERE n.user.id = :userId " +
           "AND n.referenceId = :referenceId " +
           "ORDER BY n.createdAt DESC")
    List<Notification> findByReferenceId(
        @Param("userId") Long userId,
        @Param("referenceId") String referenceId
    );
    
    @Modifying
    @Transactional
    @Query("DELETE FROM Notification n WHERE n.user.id = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM Notification n WHERE n.user.id = :userId AND n.isRead = true")
    void deleteReadByUserId(@Param("userId") Long userId);
}

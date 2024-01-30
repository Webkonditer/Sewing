package ru.vilas.sewing.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.vilas.sewing.model.Category;
import ru.vilas.sewing.model.OperationData;
import ru.vilas.sewing.model.Task;
import ru.vilas.sewing.model.User;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OperationDataRepository extends JpaRepository<OperationData, Long> {
    List<OperationData> findByTask(Task task);

     @Query("SELECT COALESCE(SUM(od.completedOperations), 0) FROM OperationData od " +
            "WHERE od.seamstress.id = :seamstressId " +
            "AND DATE(od.date) = :operationDate " +
            "AND od.task.id = :taskId")
    Integer countCompletedOperationsBySeamstressAndDate(
            @Param("seamstressId") Long seamstressId,
            @Param("operationDate") LocalDate operationDate,
            @Param("taskId") Long taskId);

    boolean existsByTaskAndSeamstressAndDate(
            Task task, User seamstress, LocalDate date);

    void deleteByTaskAndSeamstressAndDate(Task task, User seamstress, LocalDate date);


    @Modifying
    @Query("UPDATE OperationData od SET od.completedOperations = :completedOperations, od.hoursWorked = :newHoursWorked " +
            "WHERE od.task = :task AND od.seamstress = :seamstress AND od.date = :date")
    void updateCompletedOperationsAndHoursWorkedByTaskAndSeamstressAndDate(
            @Param("task") Task task,
            @Param("seamstress") User seamstress,
            @Param("date") LocalDate date,
            @Param("completedOperations") int completedOperations,
            @Param("newHoursWorked") Duration newHoursWorked);

    @Query("SELECT COALESCE(SUM(od.completedOperations * t.costPerPiece), 0) " +
            "FROM OperationData od " +
            "JOIN od.task t " +
            "WHERE od.seamstress.id = :seamstressId " +
            "AND od.date BETWEEN :startDate AND :endDate")
    BigDecimal calculateEarningsForSeamstressInPeriod(
            @Param("seamstressId") Long seamstressId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT COALESCE(SUM(od.completedOperations * t.costPerPiece), 0) " +
            "FROM OperationData od " +
            "JOIN od.task t " +
            "WHERE od.seamstress.id = :seamstressId " +
            "AND od.date BETWEEN :startDate AND :endDate " +
            "GROUP BY od.date " +
            "ORDER BY od.date")
    List<BigDecimal> getEarningsByDateRange(@Param("seamstressId") Long seamstressId,
                                            @Param("startDate") LocalDate startDate,
                                            @Param("endDate") LocalDate endDate);
    @Query("SELECT COALESCE(SUM(od.completedOperations * t.costPerPiece), 0) " +
            "FROM OperationData od " +
            "JOIN od.task t " +
            "WHERE od.seamstress.id = :seamstressId " +
            "AND od.date = :currentDate")
    BigDecimal getEarningsByDate(@Param("seamstressId") Long seamstressId,
                                 @Param("currentDate") LocalDate currentDate);


    @Query("SELECT o FROM OperationData o " +
            "WHERE o.date BETWEEN :startDate AND :endDate " +
            "AND o.seamstress.id = :seamstressId " +
            "AND (:category IS NULL OR o.category = :category)")
    List<OperationData> findBetweenDatesAndBySeamstressAndCategory(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("seamstressId") Long seamstressId,
            @Param("category") Category category);

    @Query("SELECT o FROM OperationData o " +
            "WHERE o.date BETWEEN :startDate AND :endDate " +
            "AND o.seamstress.id = :seamstressId " +
            "AND (:categories IS NULL OR o.category IN :categories)")
    List<OperationData> findBetweenDatesAndBySeamstressAndCategories(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("seamstressId") Long seamstressId,
            @Param("categories") List<Category> categories);

    @Query("SELECT o FROM OperationData o " +
            "WHERE o.seamstress.id = :seamstressId " +
            "AND o.date = :operationDate " +
            "AND o.task.id = :taskId")
    List<OperationData> findFirstBySeamstressIdAndDateAndTaskId(
            @Param("seamstressId") Long seamstressId,
            @Param("operationDate") LocalDate operationDate,
            @Param("taskId") Long taskId
    );


    List<OperationData> findByCategoryIdAndSeamstressIdAndDateBetween(Long categoryId, Long seamstressId, LocalDate startDate, LocalDate endDate);
}


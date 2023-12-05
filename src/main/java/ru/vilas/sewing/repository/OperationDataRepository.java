package ru.vilas.sewing.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.vilas.sewing.model.OperationData;
import ru.vilas.sewing.model.Task;
import ru.vilas.sewing.model.User;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

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


//    void updateCompletedOperationsByTaskAndSeamstressAndDate(
//            Task task, User seamstress, LocalDate date, int completedOperations);

    @Modifying
    @Query("UPDATE OperationData od SET od.completedOperations = :completedOperations " +
            "WHERE od.task = :task AND od.seamstress = :seamstress AND od.date = :date")
    void updateCompletedOperationsByTaskAndSeamstressAndDate(
            @Param("task") Task task,
            @Param("seamstress") User seamstress,
            @Param("date") LocalDate date,
            @Param("completedOperations") int completedOperations);
}


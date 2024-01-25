package ru.vilas.sewing.service.admin;

import org.springframework.stereotype.Service;
import ru.vilas.sewing.model.Category;
import ru.vilas.sewing.model.OperationData;
import ru.vilas.sewing.model.User;
import ru.vilas.sewing.repository.OperationDataRepository;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
public class AdminOperationServiceImpl implements AdminOperationService {

    private final  OperationDataRepository operationDataRepository;

    public AdminOperationServiceImpl(OperationDataRepository operationDataRepository) {
        this.operationDataRepository = operationDataRepository;
    }

    @Override
    public List<OperationData> findByCategoryIdAndSeamstressIdAndDateBetween(Long categoryId, Long seamstressId, LocalDate startDate, LocalDate endDate) {
        // Фильтр по категории
        Predicate<OperationData> categoryFilter = (categoryId != null && categoryId != 0) ?
                operationData -> operationData.getCategory().getId().equals(categoryId) :
                operationData -> true;

        // Фильтр по швеи
        Predicate<OperationData> seamstressFilter = (seamstressId != null && seamstressId != 0) ?
                operationData -> operationData.getSeamstress().getId().equals(seamstressId) :
                operationData -> true;

        Predicate<OperationData> userRoleFilter = operationData -> operationData.getSeamstress().getRoles().stream()
                .anyMatch(role -> role.getName().equals("ROLE_USER"));

        // Фильтр по датам
        Predicate<OperationData> dateFilter = operationData ->
                (startDate == null || operationData.getDate().isAfter(startDate) || operationData.getDate().isEqual(startDate)) &&
                        (endDate == null || operationData.getDate().isBefore(endDate) || operationData.getDate().isEqual(endDate));

        return operationDataRepository.findAll().stream()
                .filter(categoryFilter
                        .and(seamstressFilter)
                        .and(userRoleFilter)
                        .and(dateFilter))
                .sorted(Comparator.comparing(OperationData::getDate).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public void deleteOperation(Long id) {
        operationDataRepository.deleteById(id);
    }

    @Override
    public void saveOperationData(OperationData operationData) {
        operationDataRepository.save(operationData);
    }

    @Override
    public OperationData getOperationById(Long id) {
        return operationDataRepository.findById(id).orElse(null);
    }

    @Override
    public void editOperation(Long id, OperationData updatedOperation) {
        OperationData existingOperation = operationDataRepository.findById(id).orElse(null);

        if (existingOperation != null) {
            //  обновление данных
            existingOperation.setDate(updatedOperation.getDate());
            existingOperation.setSeamstress(updatedOperation.getSeamstress());
            existingOperation.setTaskType(updatedOperation.getTaskType());
            existingOperation.setCategory(updatedOperation.getCategory());
            existingOperation.setTask(updatedOperation.getTask());
            existingOperation.setCostPerPiece(updatedOperation.getCostPerPiece());
            existingOperation.setHourlyRate(updatedOperation.getHourlyRate());
            existingOperation.setCompletedOperations(updatedOperation.getCompletedOperations());
            existingOperation.setHoursWorked(updatedOperation.getHoursWorked());

            operationDataRepository.save(existingOperation);
        }
    }

}

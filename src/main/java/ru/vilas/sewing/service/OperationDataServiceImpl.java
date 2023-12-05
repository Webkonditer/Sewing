package ru.vilas.sewing.service;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.vilas.sewing.dto.OperationDto;
import ru.vilas.sewing.repository.OperationDataRepository;
import ru.vilas.sewing.model.OperationData;
import ru.vilas.sewing.model.Task;
import ru.vilas.sewing.repository.TaskRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class OperationDataServiceImpl implements OperationDataService {
    private final OperationDataRepository operationDataRepository;
    private final TaskRepository taskRepository;

    @Autowired
    public OperationDataServiceImpl(OperationDataRepository operationDataRepository, TaskRepository taskRepository) {
        this.operationDataRepository = operationDataRepository;
        this.taskRepository = taskRepository;
    }

    @Override
    public void saveOperationData(OperationData operationData) {
        operationDataRepository.save(operationData);
    }

    @Override
    public OperationData getOperationDataById(Long id) {
        return operationDataRepository.findById(id).orElse(null);
    }

    @Override
    public List<OperationData> getOperationDataByTask(Task task) {
        return operationDataRepository.findByTask(task);
    }

    @Override
    public OperationDto convertToOperationDto(Task task, Long seamstressId) {

        OperationDto operationDto = new OperationDto();
        operationDto.setId(task.getId());
        operationDto.setName(task.getName());
        operationDto.setEquipment(task.getEquipment());
        operationDto.setTimeInSeconds(task.getTimeInSeconds());
        operationDto.setCostPerPiece(task.getCostPerPiece());
        operationDto.setNormPerShift(task.getNormPerShift());
        operationDto.setSeamstressId(seamstressId);
        operationDto.setOperations(operationDataRepository
                .countCompletedOperationsBySeamstressAndDate(seamstressId, LocalDate.now(), task.getId()) );
        System.out.println(seamstressId);
        System.out.println(LocalDate.now());
        System.out.println(task.getId());
        System.out.println("!!!!!!!!!!!! " + operationDataRepository
                .countCompletedOperationsBySeamstressAndDate(seamstressId, LocalDate.now(), task.getId()) );
        return operationDto;
    }
}

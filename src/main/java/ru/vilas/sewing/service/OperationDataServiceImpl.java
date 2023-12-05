package ru.vilas.sewing.service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.vilas.sewing.dto.InOperationDto;
import ru.vilas.sewing.dto.OperationDto;
import ru.vilas.sewing.dto.SeamstressDto;
import ru.vilas.sewing.model.Role;
import ru.vilas.sewing.model.User;
import ru.vilas.sewing.repository.OperationDataRepository;
import ru.vilas.sewing.model.OperationData;
import ru.vilas.sewing.model.Task;
import ru.vilas.sewing.repository.TaskRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class OperationDataServiceImpl implements OperationDataService {
    private final OperationDataRepository operationDataRepository;
    private final TaskRepository taskRepository;
    private final CustomUserDetailsService customUserDetailsService;

    @Autowired
    public OperationDataServiceImpl(OperationDataRepository operationDataRepository, TaskRepository taskRepository, CustomUserDetailsService customUserDetailsService) {
        this.operationDataRepository = operationDataRepository;
        this.taskRepository = taskRepository;
        this.customUserDetailsService = customUserDetailsService;
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

        return operationDto;
    }

    @Override
    @Transactional
    public void saveOrUpdateOperations(List<InOperationDto> inoperationDtos) {

        User user = customUserDetailsService.getCurrentUser();

        for (InOperationDto inoperationDto : inoperationDtos) {

            Task task = taskRepository.getReferenceById(inoperationDto.getTaskId());

            if (inoperationDto.getOperations() == 0) {
                if (operationDataRepository.existsByTaskAndSeamstressAndDate(task, user, LocalDate.now())){
                    operationDataRepository.deleteByTaskAndSeamstressAndDate(task, user, LocalDate.now());
                }
                continue;
            }

            if (operationDataRepository.existsByTaskAndSeamstressAndDate(task, user, LocalDate.now())){
                operationDataRepository.updateCompletedOperationsByTaskAndSeamstressAndDate(
                        task, user, LocalDate.now(), inoperationDto.getOperations());
                continue;
            }

            OperationData operationData = new OperationData();
            operationData.setTask(task);
            operationData.setSeamstress(user);
            operationData.setDate(LocalDate.now());
            operationData.setCompletedOperations(inoperationDto.getOperations());

            operationDataRepository.save(operationData);
        }
    }

    @Override
    public BigDecimal getEarningsForSeamstressInPeriod(Long seamstressId, LocalDate startDate, LocalDate endDate){
        return operationDataRepository.calculateEarningsForSeamstressInPeriod(seamstressId, startDate, endDate);
    }

    @Override
    public List<SeamstressDto> getSeamstressDtosList(LocalDate startDate, LocalDate endDate){
        List<User> users = customUserDetailsService.getAllUsers();
        List<SeamstressDto> seamstressDtos = new ArrayList<>();
        for (User user: users) {
            if (user.getRoles().stream().anyMatch(role -> !role.getName().equals("ROLE_USER"))) {
                continue;
            }
            SeamstressDto seamstressDto = new SeamstressDto();
            seamstressDto.setSeamstressId(user.getId());
            seamstressDto.setSeamstressName(user.getName());
            seamstressDto.setEarnings(getEarningsForSeamstressInPeriod(user.getId(), startDate, endDate));
            seamstressDtos.add(seamstressDto);
        }

        return seamstressDtos;
    }

}

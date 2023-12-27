package ru.vilas.sewing.service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
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
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static ru.vilas.sewing.dto.TaskTypes.HOURLY;

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
        operationDto.setTaskType(task.getTaskType());
        operationDto.setName(task.getName());
        operationDto.setEquipment(task.getEquipment());
        operationDto.setTimeInSeconds(task.getTimeInSeconds());
        operationDto.setCostPerPiece(task.getCostPerPiece());
        operationDto.setNormPerShift(task.getNormPerShift());
        operationDto.setSeamstressId(seamstressId);
        operationDto.setOperations(operationDataRepository
                .countCompletedOperationsBySeamstressAndDate(seamstressId, LocalDate.now(), task.getId()) );

        if (task.getTaskType().equals(HOURLY)){
            List<OperationData> operations = operationDataRepository
                    .findFirstBySeamstressIdAndDateAndTaskId(seamstressId, LocalDate.now(), task.getId());
            if (!operations.isEmpty()) {
                Duration duration = operations.get(0).getHoursWorked();
                operationDto.setHours((int)duration.toHours());
                operationDto.setMinutes((int)duration.toMinutes() % 60);
            }
        }

        return operationDto;
    }

    @Override
    @Transactional
    public void saveOrUpdateOperations(List<InOperationDto> inoperationDtos) {

        //Получаем текущую швею
        User user = customUserDetailsService.getCurrentUser();

        for (InOperationDto inoperationDto : inoperationDtos) {

            // Получаем задачи
            Task task = taskRepository.getReferenceById(inoperationDto.getTaskId());

            // Проверяем, не обнулил ли пользователь количество опереций по задаче.
            if (inoperationDto.getOperations() == 0 && !task.getTaskType().equals(HOURLY)) {
                if (operationDataRepository.existsByTaskAndSeamstressAndDate(task, user, LocalDate.now())){
                    operationDataRepository.deleteByTaskAndSeamstressAndDate(task, user, LocalDate.now());
                }
                continue;
            }

            // Проверяем, не обнулил ли пользователь количество времени по задаче.
            if (inoperationDto.getHours() == 0 && inoperationDto.getMinutes() == 0 && task.getTaskType().equals(HOURLY)) {
                if (operationDataRepository.existsByTaskAndSeamstressAndDate(task, user, LocalDate.now())){
                    operationDataRepository.deleteByTaskAndSeamstressAndDate(task, user, LocalDate.now());
                }
                continue;
            }

            // Если запись за текщие сутки существует, обновляем ее.
            if (operationDataRepository.existsByTaskAndSeamstressAndDate(task, user, LocalDate.now())){
                operationDataRepository.updateCompletedOperationsAndHoursWorkedByTaskAndSeamstressAndDate(
                        task, user, LocalDate.now(), inoperationDto.getOperations(),
                        Duration.ofHours(inoperationDto.getHours()).plusMinutes(inoperationDto.getMinutes()));
                continue;
            }

            OperationData operationData = new OperationData();

            operationData.setCategory(task.getCategory());
            operationData.setTaskType(task.getTaskType());
            operationData.setTask(task);
            operationData.setSeamstress(user);
            if (!task.getTaskType().equals(HOURLY)) {
                operationData.setCostPerPiece(task.getCostPerPiece());
                operationData.setCompletedOperations(inoperationDto.getOperations());
            }
            if (task.getTaskType().equals(HOURLY)) {
                operationData.setHourlyRate(user.getHourlyRate());
                operationData.setHoursWorked(Duration.ofHours(inoperationDto.getHours()).plusMinutes(inoperationDto.getMinutes()));
            }
            operationData.setSalary(user.getSalary() != null ? user.getSalary() : BigDecimal.valueOf(0));
            operationData.setDate(LocalDate.now());


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
            List<BigDecimal> earnings = getEarningsByDateRange(user.getId(), startDate, endDate);
            seamstressDto.setEarnings(earnings);
            seamstressDto.setAmountOfEarnings(earnings.stream().reduce(BigDecimal.ZERO, BigDecimal::add));
            seamstressDtos.add(seamstressDto);
        }

        return seamstressDtos;
    }

    @Override
    public List<String> getDatesInPeriod(LocalDate startDate, LocalDate endDate) {
        List<String> result = new ArrayList<>();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy E"); // Формат даты и дня недели

        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            String formattedDate = currentDate.format(formatter);
            result.add(formattedDate);

            currentDate = currentDate.plusDays(1);
        }

        return result;
    }

    private List<BigDecimal> getEarningsByDateRange(Long seamstressId, LocalDate startDate, LocalDate endDate) {
        List<BigDecimal> earnings = new ArrayList<>();
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            BigDecimal earning = operationDataRepository.getEarningsByDate(seamstressId, currentDate);
            earnings.add(earning);

            currentDate = currentDate.plusDays(1);
        }

        return earnings;
    }



}

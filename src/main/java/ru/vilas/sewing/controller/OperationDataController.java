package ru.vilas.sewing.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.vilas.sewing.dto.InOperationDto;
import ru.vilas.sewing.dto.OperationDto;
import ru.vilas.sewing.model.Category;
import ru.vilas.sewing.model.OperationData;
import ru.vilas.sewing.model.Task;
import ru.vilas.sewing.service.CategoryService;
import ru.vilas.sewing.service.CustomUserDetailsService;
import ru.vilas.sewing.service.OperationDataService;
import ru.vilas.sewing.service.TaskService;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/operations")
public class OperationDataController {

    private final OperationDataService operationDataService;
    private final CategoryService categoryService;
    private final TaskService taskService;
    private final CustomUserDetailsService сustomUserDetailsService;

    @Autowired
    public OperationDataController(OperationDataService operationDataService, CategoryService categoryService, TaskService taskService, CustomUserDetailsService сustomUserDetailsService) {
        this.operationDataService = operationDataService;
        this.categoryService = categoryService;
        this.taskService = taskService;
        this.сustomUserDetailsService = сustomUserDetailsService;
    }

//    @PostMapping("/save")
//    public String saveOperationData(@ModelAttribute OperationData operationData) {
//        operationDataService.saveOperationData(operationData);
//        return "redirect:/tasks/" + operationData.getTask().getId();
//    }

    @GetMapping("/{operationDataId}")
    public String getOperationDataDetails(@PathVariable Long operationDataId, Model model) {
        OperationData operationData = operationDataService.getOperationDataById(operationDataId);
        model.addAttribute("operationData", operationData);
        return "operationDataDetails";
    }

    @GetMapping("/category/{categoryId}")
    public String getTasksForCategory(@PathVariable Long categoryId, Model model, Authentication authentication) {

        Object principal =  authentication.getPrincipal();
        String userName = null;
        if (principal instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) principal;
            userName = userDetails.getUsername();
        }

        // Получаем идентификатор пользователя
        Long currentUserId = сustomUserDetailsService.getUserIdByUsername(userName);

        Category category = categoryService.getCategoryById(categoryId);
        List<Task> tasks = taskService.getTasksByCategory(category);


        List<OperationDto> operationDtos = tasks.stream()
                .map(task -> operationDataService.convertToOperationDto(task, currentUserId))
                .collect(Collectors.toList());

        model.addAttribute("tasks", operationDtos);

        return "tasks";
    }

    @PostMapping("/save")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void saveOperations(@RequestBody List<InOperationDto> inOperationDtos) {
        operationDataService.saveOrUpdateOperations(inOperationDtos);
    }
}

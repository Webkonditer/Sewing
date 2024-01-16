package ru.vilas.sewing.controller.admin;

import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.vilas.sewing.model.Category;
import ru.vilas.sewing.model.OperationData;
import ru.vilas.sewing.model.Task;
import ru.vilas.sewing.model.User;
import ru.vilas.sewing.service.CategoryService;
import ru.vilas.sewing.service.admin.AdminCategoryService;
import ru.vilas.sewing.service.admin.AdminOperationService;
import ru.vilas.sewing.service.admin.AdminTaskService;
import ru.vilas.sewing.service.admin.SeamstressService;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/operations")
public class AdminOperationController {

    private final AdminOperationService operationService;

    private final CategoryService categoryService;

    private final SeamstressService userService;

    private final AdminCategoryService adminCategoryService;

    private final AdminTaskService adminTaskService;

    private final SeamstressService seamstressService;

    public AdminOperationController(AdminOperationService operationService, CategoryService categoryService, SeamstressService userService, AdminCategoryService adminCategoryService, AdminTaskService adminTaskService, SeamstressService seamstressService) {
        this.operationService = operationService;
        this.categoryService = categoryService;
        this.userService = userService;
        this.adminCategoryService = adminCategoryService;
        this.adminTaskService = adminTaskService;
        this.seamstressService = seamstressService;
    }

    @GetMapping
    public String showOperationsPage(Model model,
                @RequestParam(name = "categoryId", required = false) Long categoryId,
                @RequestParam(name = "seamstressId", required = false) Long seamstressId,
                @RequestParam(name = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                @RequestParam(name = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        // Если параметры не переданы, устанавливаем значения по умолчанию
        if (endDate == null) {
            endDate = LocalDate.now();
        }
        if (startDate == null) {
            startDate = LocalDate.now();
        }

        List<OperationData> operations = operationService.findByCategoryIdAndSeamstressIdAndDateBetween(categoryId, seamstressId, startDate, endDate);
        List<Category> categories = categoryService.getAllCategories();
        List<User> seamstresses = userService.getAllSeamstresses();

        model.addAttribute("categories", categories);
        model.addAttribute("seamstresses", seamstresses);
        model.addAttribute("operations", operations);
        model.addAttribute("selectedCategoryId", categoryId);
        model.addAttribute("selectedSeamstressId", seamstressId);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);

        return "admin/operationsList";
    }

    @GetMapping("/delete/{id}")
    public String deleteOperation(@PathVariable Long id) {
        operationService.deleteOperation(id);
        return "redirect:/admin/operations";
    }

    // Добавление задачи
    @GetMapping("/new")
    public String showOperationForm(Model model) {
        List<User> seamstresses = seamstressService.getAllSeamstresses();
        List<Category> categories = adminCategoryService.getAllCategories()
                .stream()
                .filter(Category::isActive) // Фильтрация по активным категориям
                .collect(Collectors.toList());
        List<Task> tasks = adminTaskService.getAllTasks();

        model.addAttribute("seamstresses", seamstresses);
        model.addAttribute("categories", categories);
        model.addAttribute("tasks", tasks);
        model.addAttribute("operationData", new OperationData());
        return "admin/addOperation";
    }

    @PostMapping("/new")
    public String addNewOperation(@ModelAttribute("operationData") @Valid OperationData operationData,
                                  BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            // Если есть ошибки валидации, возвращаем пользователя на форму с сообщениями об ошибках
            return "admin/addOperation";
        }

        // Сохраняем выполненную задачу в базе данных
        operationService.saveOperationData(operationData);

        // Перенаправляем пользователя на страницу с выполненными задачами
        return "redirect:/admin/operations";
    }

    @GetMapping("/edit/{id}")
    public String editOperation(@PathVariable Long id, Model model) {
        OperationData operationData = operationService.getOperationById(id);
        List<User> seamstresses = seamstressService.getAllSeamstresses();
        List<Task> tasks = adminTaskService.getAllTasks();
        List<Category> categories = adminCategoryService.getAllCategories()
                .stream()
                .filter(Category::isActive) // Фильтрация по активным категориям
                .collect(Collectors.toList());

        model.addAttribute("operationData", operationData);
        model.addAttribute("seamstresses", seamstresses);
        model.addAttribute("tasks", tasks);
        model.addAttribute("categories", categories);

        return "admin/editOperation";
    }

    @PostMapping("/edit/{id}")
    public String editOperation(@PathVariable Long id, @ModelAttribute OperationData operationData, BindingResult bindingResult, Model model) {
        // Валидация данных
        if (bindingResult.hasErrors()) {
            model.addAttribute("validationErrors", bindingResult.getAllErrors());
            List<User> seamstresses = seamstressService.getAllSeamstresses();
            List<Task> tasks = adminTaskService.getAllTasks();
            List<Category> categories = adminCategoryService.getAllCategories()
                    .stream()
                    .filter(Category::isActive) // Фильтрация по активным категориям
                    .collect(Collectors.toList());
            model.addAttribute("operationData", operationData);
            model.addAttribute("seamstresses", seamstresses);
            model.addAttribute("tasks", tasks);
            model.addAttribute("categories", categories);

            return "admin/editOperation";
        }

        // Внесение изменений
        operationService.editOperation(id, operationData);

        return "redirect:/admin/operations"; // Перенаправление на страницу с выполненными задачами
    }

}

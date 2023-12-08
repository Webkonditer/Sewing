package ru.vilas.sewing.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.vilas.sewing.service.admin.AdminCategoryService;

@Controller
@RequestMapping("/admin/tasks")
public class AdminTaskController {

    private final AdminTaskService taskService;

    private AdminCategoryService categoryService;

    @GetMapping
    public String getAllTasks(Model model) {
        List<Task> tasks = taskService.getAllTasks();
        model.addAttribute("tasks", tasks);
        return "admin/taskList";
    }

    // Добавление задачи
    @GetMapping("/new")
    public String showTaskForm(Model model) {
        List<Category> categories = categoryService.getAllCategories();
        model.addAttribute("categories", categories);
        model.addAttribute("task", new Task());
        return "admin/taskForm";
    }

    @PostMapping("/new")
    public String addTask(@ModelAttribute Task task) {
        taskService.saveTask(task);
        return "redirect:/admin/tasks";
    }

    // Редактирование задачи
    @GetMapping("/edit/{taskId}")
    public String showEditForm(@PathVariable Long taskId, Model model) {
        Task task = taskService.getTaskById(taskId);
        List<Category> categories = categoryService.getAllCategories();
        model.addAttribute("categories", categories);
        model.addAttribute("task", task);
        return "admin/taskForm";
    }

    @PostMapping("/edit/{taskId}")
    public String updateTask(@PathVariable Long taskId, @ModelAttribute Task task) {
        task.setId(taskId);
        taskService.saveTask(task);
        return "redirect:/admin/tasks";
    }

    // Удаление задачи
    @GetMapping("/delete/{taskId}")
    public String deleteTask(@PathVariable Long taskId) {
        taskService.deleteTask(taskId);
        return "redirect:/admin/tasks";
    }
}

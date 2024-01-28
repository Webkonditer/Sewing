package ru.vilas.sewing.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.vilas.sewing.model.Category;
import ru.vilas.sewing.service.admin.AdminCategoryService;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/categories")
public class AdminCategoryController {

    private final AdminCategoryService adminCategoryService;

    public AdminCategoryController(AdminCategoryService adminCategoryService) {
        this.adminCategoryService = adminCategoryService;
    }


    @GetMapping
    public String showCategories(Model model) {
        List<Category> categories = adminCategoryService.getAllCategories()
                .stream()
                .filter(Category::isActive) // Фильтрация по активным категориям
                .collect(Collectors.toList());
        model.addAttribute("categories", categories);

        return "admin/categoryList";
    }

    // Добавление новой категории
    @GetMapping("/new")
    public String showNewCategoryForm(Model model) {
        model.addAttribute("category", new Category());
        return "admin/addCategory";
    }

    @PostMapping("/new")
    public String addCategory(@ModelAttribute Category category) {
        adminCategoryService.saveCategory(category);
        return "redirect:/admin/categories";
    }

    // Редактирование существующей категории
    @GetMapping("/edit/{id}")
    public String showEditCategoryForm(@PathVariable Long id, Model model) {
        Category category = adminCategoryService.getCategoryById(id);
        model.addAttribute("category", category);
        return "admin/editCategory";
    }

    @PostMapping("/edit")
    public String updateCategory(@ModelAttribute Category category) {
        adminCategoryService.saveCategory(category);
        return "redirect:/admin/categories";
    }

    // Удаление категории
    @GetMapping("/delete/{id}")
    public String deleteCategory(@PathVariable Long id) {
        adminCategoryService.deleteCategory(id);
        return "redirect:/admin/categories";
    }
}

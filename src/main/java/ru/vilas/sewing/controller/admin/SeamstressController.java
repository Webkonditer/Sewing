package ru.vilas.sewing.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.vilas.sewing.config.UsernameExistsException;
import ru.vilas.sewing.model.User;
import ru.vilas.sewing.service.admin.SeamstressService;

import java.util.List;

@Controller
@RequestMapping("/admin/seamstresses")
public class SeamstressController {

    private final SeamstressService seamstressService;

    public SeamstressController(SeamstressService seamstressService) {
        this.seamstressService = seamstressService;
    }


    @GetMapping
    public String getSeamstresses(Model model) {
        List<User> seamstresses = seamstressService.getAllSeamstresses();
        model.addAttribute("seamstresses", seamstresses);
        return "admin/seamstresses";
    }

    @GetMapping("/new")
    public String showAddSeamstressForm(Model model) {
        model.addAttribute("seamstressDto", new User());
        return "admin/addSeamstress";
    }

    @PostMapping("/new")
    public String addSeamstress(@ModelAttribute User user, Model model) {
        try {
            seamstressService.saveSeamstress(user);
            return "redirect:/admin/seamstresses";
        } catch (UsernameExistsException e) {
            // Если пользователь с таким логином уже существует
            model.addAttribute("user", user);
            model.addAttribute("error", "Пользователь с таким логином уже существует");
            return "admin/addSeamstress"; // Возвращает на страницу добавления с сообщением об ошибке
        }
    }

    @GetMapping("/delete/{id}")
    public String deleteSeamstress(@PathVariable Long id) {
        seamstressService.deleteSeamstress(id);
        return "redirect:/admin/seamstresses";
    }
}


package ru.vilas.sewing.controller.admin;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.vilas.sewing.dto.SeamstressDto;
import ru.vilas.sewing.service.OperationDataService;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class EarningsController {

    private final OperationDataService operationDataService;

    public EarningsController(OperationDataService operationDataService) {
        this.operationDataService = operationDataService;
    }

    @GetMapping
    public String getEarningsReport(
            @RequestParam(name = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(name = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Model model) {

        // Если параметры не переданы, устанавливаем значения по умолчанию
        if (startDate == null) {
            startDate = LocalDate.now().minusMonths(1); //  1 месяц назад
        }

        if (endDate == null) {
            endDate = LocalDate.now();
        }

        List<SeamstressDto> seamstressDtos = operationDataService.getSeamstressDtosList(startDate, endDate);

        model.addAttribute("seamstressDtos", seamstressDtos);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);

        return "admin/earningsReport";
    }
}

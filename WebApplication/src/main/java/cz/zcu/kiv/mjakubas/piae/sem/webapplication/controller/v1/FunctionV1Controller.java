package cz.zcu.kiv.mjakubas.piae.sem.webapplication.controller.v1;

import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Allocation;
import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Function;
import cz.zcu.kiv.mjakubas.piae.sem.core.service.v1.AllocationService;
import cz.zcu.kiv.mjakubas.piae.sem.core.service.v1.EmployeeService;
import cz.zcu.kiv.mjakubas.piae.sem.core.service.v1.FunctionService;
import cz.zcu.kiv.mjakubas.piae.sem.core.service.v1.WorkplaceService;
import cz.zcu.kiv.mjakubas.piae.sem.core.vo.EmployeeVO;
import cz.zcu.kiv.mjakubas.piae.sem.core.vo.FunctionVO;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * Contains all sites for working with functions.
 */
@Controller
@RequestMapping("/f")
@AllArgsConstructor
@PreAuthorize("hasAuthority(T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).SECRETARIAT)")
public class FunctionV1Controller {

    private final FunctionService functionService;
    private final EmployeeService employeeService;
    private final WorkplaceService workplaceService;

    private final AllocationService allocationService;

    private static final String EMPLOYEES = "employees";

    private static final String RESTRICTIONS = "restrictions";

    @GetMapping()
    @PreAuthorize("hasAnyAuthority(" +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).SECRETARIAT, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).FUNCTION_ADMIN, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).ADMIN)")
    public String getFunctions(Model model) {
        var functions = functionService.getFunctions();
        List<Allocation> firstAllocations = functionService.prepareFirst(functions);

        model.addAttribute("functions", functions);
        model.addAttribute("firstAllocations", firstAllocations);
        return "views/functions";
    }

    @GetMapping("/create")
    @PreAuthorize("hasAnyAuthority(" +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).SECRETARIAT, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).FUNCTION_ADMIN, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).ADMIN)")
    public String createFunction(Model model) {
        model.addAttribute("functionVO", new FunctionVO());

        var functions = functionService.getFunctions();
        var employees = employeeService.getEmployees();
        var workplaces = workplaceService.getWorkplaces();

        model.addAttribute(EMPLOYEES, employees);
        model.addAttribute("workplaces", workplaces);
        model.addAttribute(RESTRICTIONS, functions);

        return "forms/function/create_function_form";
    }

    @PostMapping("/create")
    @PreAuthorize("hasAnyAuthority(" +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).SECRETARIAT, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).FUNCTION_ADMIN, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).ADMIN)")
    public String createFunction(@ModelAttribute FunctionVO functionVO, BindingResult bindingResult, Model model) {

        functionService.createFunction(functionVO);

        return "redirect:/f?create=success";
    }

    @GetMapping("/{id}/edit")
    @PreAuthorize("hasAnyAuthority(" +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).SECRETARIAT, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).FUNCTION_ADMIN, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).ADMIN) " +
            " or @securityService.isFunctionManager(#id) or @securityService.isWorkplaceManager(#id)")
    public String editFunction(Model model, @PathVariable long id,
                              @RequestParam(required = false) Boolean manage) {
        Function data = functionService.getFunction(id);

        model.addAttribute("functionVO",
                new FunctionVO(
                        data.getId(),
                        data.getName(),
                        data.getShortcut(),
                        data.getDateFrom(),
                        data.getDateUntil(),
                        data.getProbability(),
                        data.getDefaultTime(),
                        data.getFunctionManager().getId(),
                        data.getFunctionManager().getLastName(),
                        data.getFunctionWorkplace().getId(),
                        data.getDescription()));

        model.addAttribute(EMPLOYEES, employeeService.getEmployees());
        model.addAttribute("workplaces", workplaceService.getWorkplaces());
        model.addAttribute(RESTRICTIONS, functionService.getFunctions());
        model.addAttribute("manage", manage);

        return "forms/function/edit_function_form";
    }

    @PostMapping("/{id}/edit")
    @PreAuthorize("hasAnyAuthority(" +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).SECRETARIAT, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).FUNCTION_ADMIN, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).ADMIN) " +
            " or @securityService.isFunctionManager(#id) or @securityService.isWorkplaceManager(#id)")
    public String editFunction(Model model, @PathVariable long id, @ModelAttribute FunctionVO functionVO,
                              BindingResult errors, @RequestParam(required = false) Boolean manage) {
        System.out.println(functionVO);

        functionService.editFunction(functionVO, id);

//        if (Boolean.TRUE.equals(manage))
//            return String.format("redirect:/f/%s/manage?edit=success", id);

        return "redirect:/f/{id}/detail?edit=success";
    }

    @GetMapping("/{id}/delete")
    @PreAuthorize("hasAnyAuthority(" +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).SECRETARIAT, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).FUNCTION_ADMIN, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).ADMIN) " +
            " or @securityService.isFunctionManager(#id) or @securityService.isWorkplaceManager(#id)")
    public String editFunction(Model model, @PathVariable long id) {
        return "redirect:/f?delete=success";
    }

    @GetMapping("/{id}/detail")
    @PreAuthorize("hasAnyAuthority(" +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).SECRETARIAT, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).FUNCTION_ADMIN, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).ADMIN) " +
            " or @securityService.isFunctionManager(#id) or @securityService.isWorkplaceManager(#id)")
    public String detailFunction(Model model, @PathVariable long id,
                               @RequestParam(required = false) Boolean manage) {
        Function data = functionService.getFunction(id);
        var allocations = allocationService.getFunctionAllocations(id).getAllocations();

        model.addAttribute("allocations", allocations);
        model.addAttribute("function",
                new FunctionVO()
                        .name(data.getName())
                        .shortcut(data.getShortcut())
                        .functionManagerId(data.getFunctionManager().getId())
                        .functionManagerName(data.getFunctionManager().getLastName())
                        .probability(data.getProbability())
                        .defaultTime(data.getDefaultTime())
                        .dateFrom(data.getDateFrom())
                        .dateUntil(data.getDateUntil())
                        .description(data.getDescription()));

        model.addAttribute(EMPLOYEES, employeeService.getEmployees());
        model.addAttribute("workplaces", workplaceService.getWorkplaces());
        model.addAttribute(RESTRICTIONS, functionService.getFunctions());
        model.addAttribute("manage", manage);

        return "details/function_detail";
    }


    @GetMapping("/{id}/employee/add")
    @PreAuthorize("hasAnyAuthority(" +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).SECRETARIAT, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).FUNCTION_ADMIN, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).ADMIN)")
    public String addSubordinate(Model model, @PathVariable long id, @ModelAttribute EmployeeVO userVO) {
        model.addAttribute("userVO", new EmployeeVO());

        var restrictions = functionService.getFunctionEmployees(id);

        model.addAttribute(RESTRICTIONS, restrictions);
        model.addAttribute(EMPLOYEES, employeeService.getEmployees());

        return "forms/function/create_function_employee_form";
    }

    @PostMapping("/{id}/employee/add")
    @PreAuthorize("hasAnyAuthority(" +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).SECRETARIAT, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).FUNCTION_ADMIN, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).ADMIN)")
    public String addSubordinate(Model model, @PathVariable long id, @ModelAttribute EmployeeVO userVO,
                                 BindingResult errors) {
        model.addAttribute("userVO", userVO);
        var function = functionService.getFunction(id);

        functionService.assignEmployee(userVO, function.getId());

        return "redirect:/f?add_employee=success";
    }

    @PreAuthorize("hasAnyAuthority(" +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).SECRETARIAT, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).FUNCTION_ADMIN, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).ADMIN) " +
            " or @securityService.isFunctionManager(#id) or @securityService.isWorkplaceManager(#id)")
    @GetMapping("/{id}/manage")
    public String manageFunction(Model model, @PathVariable long id) {
        var payload = allocationService.getFunctionAllocations(id);
        model.addAttribute("allocations", payload.getAllocations());
        model.addAttribute("function", payload.getAssignmentsFunctions());
        model.addAttribute(EMPLOYEES, payload.getAllocationsEmployees());
        return "views/function_management";
    }
}

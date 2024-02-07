package cz.zcu.kiv.mjakubas.piae.sem.webapplication.controller.v1;

import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Allocation;
import cz.zcu.kiv.mjakubas.piae.sem.core.domain.AllocationCell;
import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Function;
import cz.zcu.kiv.mjakubas.piae.sem.core.exceptions.SecurityException;
import cz.zcu.kiv.mjakubas.piae.sem.core.service.MyUtils;
import cz.zcu.kiv.mjakubas.piae.sem.core.service.v1.AllocationService;
import cz.zcu.kiv.mjakubas.piae.sem.core.service.v1.EmployeeService;
import cz.zcu.kiv.mjakubas.piae.sem.core.service.v1.FunctionService;
import cz.zcu.kiv.mjakubas.piae.sem.core.service.v1.WorkplaceService;
import cz.zcu.kiv.mjakubas.piae.sem.core.vo.AllocationVO;
import cz.zcu.kiv.mjakubas.piae.sem.core.vo.EmployeeVO;
import cz.zcu.kiv.mjakubas.piae.sem.core.vo.FunctionVO;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Collections;
import java.util.LinkedList;
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
    private final MyUtils utils;

    private static final String EMPLOYEES = "employees";
    private static final String RESTRICTIONS = "restrictions";
    private static final String FUNCTION = "function";
    private static final String FUNCTIONS_REDIRECT = "redirect:/f";
    private static final String FUNCTION_DETAIL_REDIRECT = "redirect:/f/%s/detail";
    private static final String PERMISSION_ERROR = "permissionError";
    private static final String GENEREAL_ERROR = "generalError";

    @GetMapping()
    @PreAuthorize("hasAnyAuthority(" +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).SECRETARIAT, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).USER, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).FUNCTION_ADMIN, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).ADMIN)")
    public String getFunctions(Model model,
                               RedirectAttributes redirectAttributes) {
        try {
            var functions = functionService.getFunctions();
            List<Allocation> firstAllocations = functionService.prepareFirst(functions);

            model.addAttribute("functions", functions);
            model.addAttribute("firstAllocations", firstAllocations);
            model.addAttribute("canCreateFunction", functionService.canCreateFunction());
            return "views/functions";
        } catch (SecurityException e) {
            redirectAttributes.addFlashAttribute(PERMISSION_ERROR, true);
            return "redirect:/index";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(GENEREAL_ERROR, true);
            return "redirect:/index";
        }
    }

    @GetMapping("/create")
    @PreAuthorize("hasAnyAuthority(" +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).SECRETARIAT, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).FUNCTION_ADMIN, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).ADMIN)")
    public String createFunction(Model model) {
        var function =  new FunctionVO();
        function.setName("Nová funkce");

        model.addAttribute(FUNCTION, function);
        var employees = employeeService.getEmployees();

        model.addAttribute(EMPLOYEES, employees);
        model.addAttribute("workplaces", workplaceService.getWorkplaces());

        return "forms/function/create_function_form";
    }

    @PostMapping("/create")
    @PreAuthorize("hasAnyAuthority(" +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).SECRETARIAT, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).FUNCTION_ADMIN, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).ADMIN)")
    public String createFunction(@ModelAttribute FunctionVO functionVO,
                                 RedirectAttributes redirectAttributes) {
        try {
            long id = functionService.createFunction(functionVO);
            redirectAttributes.addAttribute("id", id);
            return "redirect:/f/{id}/detail?create=success";
        } catch (SecurityException e) {
            redirectAttributes.addFlashAttribute(PERMISSION_ERROR, true);
            return FUNCTIONS_REDIRECT;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(GENEREAL_ERROR, true);
            return FUNCTIONS_REDIRECT;
        }
    }

    @GetMapping("/{id}/detail")
    @PreAuthorize("hasAnyAuthority(" +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).SECRETARIAT, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).USER, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).FUNCTION_ADMIN, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).ADMIN) " +
            " or @securityService.isFunctionManager(#id) or @securityService.isWorkplaceManager(#id)")
    public String detailFunction(Model model, @PathVariable long id,
                                 RedirectAttributes redirectAttributes) {
        try {
            prepareFunctionForDetail(model, id);
            return "details/function_detail";
        } catch (SecurityException e) {
            redirectAttributes.addFlashAttribute(PERMISSION_ERROR, true);
            return FUNCTIONS_REDIRECT;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(GENEREAL_ERROR, true);
            return FUNCTIONS_REDIRECT;
        }
    }

    @PostMapping("/{id}/edit")
    @PreAuthorize("hasAnyAuthority(" +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).SECRETARIAT, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).FUNCTION_ADMIN, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).ADMIN) " +
            " or @securityService.isFunctionManager(#id) or @securityService.isWorkplaceManager(#id)")
    public String editFunction(@PathVariable long id, @ModelAttribute FunctionVO functionVO,
                               RedirectAttributes redirectAttributes) {
        try {
            functionService.editFunction(functionVO, id);
            return "redirect:/f/{id}/detail?edit=success";
        } catch (SecurityException e) {
            redirectAttributes.addFlashAttribute(PERMISSION_ERROR, true);
            return String.format(FUNCTION_DETAIL_REDIRECT, id);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(GENEREAL_ERROR, true);
            return String.format(FUNCTION_DETAIL_REDIRECT, id);
        }
    }

    @PostMapping("/{id}/delete")
    @PreAuthorize("hasAnyAuthority(" +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).SECRETARIAT, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).FUNCTION_ADMIN, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).ADMIN) " +
            " or @securityService.isFunctionManager(#id) or @securityService.isWorkplaceManager(#id)")
    public String deleteFunction(@PathVariable long id, RedirectAttributes redirectAttributes) {
        try {
            functionService.removeFunction(id);
            return "redirect:/f?delete=success";
        } catch (SecurityException e) {
            redirectAttributes.addFlashAttribute(PERMISSION_ERROR, true);
            return String.format(FUNCTION_DETAIL_REDIRECT, id);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(GENEREAL_ERROR, true);
            return String.format(FUNCTION_DETAIL_REDIRECT, id);
        }
    }

    @PostMapping("/{id}/employee/add")
    @PreAuthorize("hasAnyAuthority(" +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).SECRETARIAT, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).FUNCTION_ADMIN, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).ADMIN)")
    public String addSubordinate(Model model, @PathVariable long id, @ModelAttribute EmployeeVO userVO) {
        model.addAttribute("userVO", userVO);
        var function = functionService.getFunction(id);

        functionService.assignEmployee(userVO, function.getId());

        return "redirect:/f?add_employee=success";
    }

    private AllocationVO newFunctionAllocation(Function function) {
        AllocationVO newAllocation = new AllocationVO();
        newAllocation.setFunctionId(function.getId());
        newAllocation.setWorkerId(1);
        newAllocation.setRole("nový");
        newAllocation.setIsCertain(1.0F);
        newAllocation.setAllocationScope(1);
        newAllocation.setDateFrom(utils.convertToLocalDateTime(function.getDateFrom()));
        newAllocation.setDateUntil(utils.convertToLocalDateTime(function.getDateUntil()));
        newAllocation.setDescription("description");

        return newAllocation;
    }

    private List<AllocationVO> mapAllocations(List<Allocation> allocations) {
        List<AllocationVO> allocationsVO = new LinkedList<>();
        for (Allocation allocation : allocations) {
            AllocationVO allocationVO = new AllocationVO();
            allocationVO.setId(allocation.getId());
            allocationVO.setFunctionId(allocation.getFunction().getId());
            allocationVO.setWorkerId(allocation.getWorker().getId());
            allocationVO.setRole(allocation.getRole());
            allocationVO.setDateFrom(utils.convertToLocalDateTime(allocation.getDateFrom()));
            allocationVO.setDateUntil(utils.convertToLocalDateTime(allocation.getDateUntil()));
            allocationVO.setAllocationScope(allocation.getTime());
            allocationVO.setIsCertain(allocation.getIsCertain());
            allocationVO.setDescription(allocation.getDescription());
            allocationsVO.add(allocationVO);
        }

        return allocationsVO;
    }

    private void prepareFunctionForDetail(Model model, long id) {
        Function function = functionService.getFunction(id);
        var allocations = allocationService.getFunctionAllocations(id).getAllocations();
        model.addAttribute("allocations", mapAllocations(allocations));

        FunctionVO functionVO = new FunctionVO()
                .name(function.getName())
                .shortcut(function.getShortcut())
                .functionManagerId(function.getFunctionManager().getId())
                .functionManagerName(function.getFunctionManager().getLastName())
                .probability(function.getProbability())
                .defaultTime(function.getDefaultTime())
                .dateFrom(function.getDateFrom())
                .dateUntil(function.getDateUntil())
                .firstYear(
                        allocations.isEmpty() ? utils.convertToLocalDateTime(function.getDateFrom()).getYear() :
                                utils.convertToLocalDateTime(allocations.get(0).getDateFrom()).getYear())
                .description(function.getDescription());

        model.addAttribute(FUNCTION, functionVO);
        model.addAttribute("newAllocation", newFunctionAllocation(function));

        List<List<AllocationCell>> allList = functionService.prepareFunctionsCells(allocations);

        List<AllocationCell> certainList =
                !allList.isEmpty() ? new java.util.ArrayList<>(Collections.nCopies(allList.get(0).size(), new AllocationCell())) : new LinkedList<>();
        List<AllocationCell> uncertainList =
                !allList.isEmpty() ? new java.util.ArrayList<>(Collections.nCopies(allList.get(0).size(), new AllocationCell())) : new LinkedList<>();
        if (!allList.isEmpty()) {
            functionService.prepareFunctionsCells(allList, certainList, uncertainList);
        }

        if (certainList.isEmpty()) {
            certainList = Collections.nCopies(12, new AllocationCell(0, 1));
        }

        if (uncertainList.isEmpty()) {
            uncertainList = Collections.nCopies(12, new AllocationCell(0, 1));
        }

        model.addAttribute("certainAllocations", certainList);
        model.addAttribute("uncertainAllocations", uncertainList);
        model.addAttribute("monthlyAllocations", allList);


        model.addAttribute(EMPLOYEES, employeeService.getEmployees());
        model.addAttribute("workplaces", workplaceService.getWorkplaces());
        model.addAttribute("canEdit", functionService.canEditFunction(function.getId()));
        model.addAttribute("canDelete", functionService.canDeleteFunction(function.getId()));
        model.addAttribute("canCreateAllocation", functionService.canCreateAllocation(function.getId()));
        model.addAttribute("canEditAllocation", functionService.canEditAllocation(function.getId()));
        model.addAttribute("canDeleteAllocation", functionService.canDeleteAllocation(function.getId()));
    }
}

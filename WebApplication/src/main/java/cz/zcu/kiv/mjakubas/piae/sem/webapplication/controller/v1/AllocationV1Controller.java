package cz.zcu.kiv.mjakubas.piae.sem.webapplication.controller.v1;

import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Employee;
import cz.zcu.kiv.mjakubas.piae.sem.core.service.v1.AllocationService;
import cz.zcu.kiv.mjakubas.piae.sem.core.service.v1.EmployeeService;
import cz.zcu.kiv.mjakubas.piae.sem.core.service.v1.ProjectService;
import cz.zcu.kiv.mjakubas.piae.sem.core.vo.AllocationVO;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.net.http.HttpRequest;

/**
 * Contains all sites for working with allocations.
 */
@Controller
@RequestMapping("/a")
@AllArgsConstructor
@PreAuthorize("hasAuthority(T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).ADMIN)")
public class AllocationV1Controller {

    private final AllocationService allocationService;
    private final ProjectService projectService;
    private final EmployeeService employeeService;

    private static final String EMPLOYEES = "employees";

    @PreAuthorize("@securityService.isProjectManager(#projectId) or @securityService.isWorkplaceManager(#projectId)")
    @GetMapping("/of/{projectId}/manage/for/{employeeId}/add")
    public String createAllocation(Model model, @PathVariable long employeeId, @PathVariable long projectId) {
        var project = projectService.getProject(projectId);
        var employee = employeeService.getEmployee(employeeId);

        model.addAttribute(EMPLOYEES, employeeService.getEmployees());

        model.addAttribute("minDate", project.getDateFrom());
        model.addAttribute("maxDate", project.getDateUntil());

        var rules = allocationService.getProjectAllocationsRules(projectId, employeeId);
        model.addAttribute("rules", rules);

        AllocationVO allocationVO = new AllocationVO();
        allocationVO.setProjectId(project.getId());
        allocationVO.setEmployeeId(employee.getId());
        model.addAttribute("assignmentVO", allocationVO);
        return "forms/allocation/create_assignment_form";
    }

    @PreAuthorize("@securityService.isProjectManager(#projectId) or @securityService.isWorkplaceManager(#projectId)")
    @PostMapping("/of/{projectId}/manage/for/{employeeId}/add")
    public String createAllocation(Model model, @PathVariable long employeeId, @PathVariable long projectId,
                                   @ModelAttribute AllocationVO allocationVO, BindingResult errors) {

        allocationService.createAllocation(allocationVO);
        return String.format("redirect:/p/%s/manage", projectId);
    }

    @PreAuthorize("@securityService.isAtLeastProjectManager() or @securityService.isWorkplaceManager(#id)")
    @GetMapping("{id}/edit")
    public String editAllocation(Model model, @PathVariable long id) {
        var allocation = allocationService.getAllocation(id);
        var project = projectService.getProject(allocation.getProject().getId());
        var employee = employeeService.getEmployee(allocation.getWorker().getId());

        model.addAttribute(EMPLOYEES, employeeService.getEmployees());

        model.addAttribute("minDate", project.getDateFrom());
        model.addAttribute("maxDate", project.getDateUntil());

        var rules = allocationService.getProjectAllocationsRules(project.getId(), employee.getId());
        model.addAttribute("rules", rules);

        AllocationVO allocationVO = new AllocationVO();
        allocationVO.setProjectId(project.getId());
        allocationVO.setEmployeeId(employee.getId());
        allocationVO.setAllocationScope(allocation.getAllocationScope());
        allocationVO.setDescription(allocation.getDescription());
        allocationVO.setDateFrom(allocation.getDateFrom());
        allocationVO.setDateUntil(allocation.getDateUntil());
        model.addAttribute("assignmentVO", allocationVO);

        return "forms/allocation/edit_assignment_form";
    }

    @PreAuthorize("@securityService.isAtLeastProjectManager() or @securityService.isWorkplaceManager(#id)")
    @PostMapping("{id}/edit")
    public String editAllocation(Model model, @PathVariable long id,
                                 @ModelAttribute AllocationVO allocationVO) {
        var projectId = allocationVO.getProjectId();

        allocationService.updateAllocation(allocationVO, id);
        return String.format("redirect:/p/%s/manage", projectId);
    }

    @PreAuthorize("@securityService.isAtLeastProjectManager()")
    @GetMapping("/workload")
    public String viewWorkload(Model model) {
        var employees = employeeService.getEmployees();
        for (Employee e : employees) {
            var allocations = allocationService.getEmployeeAllocations(e.getId());
            e.getIntervals().addAll(allocationService.processAllocations(allocations));
        }

        model.addAttribute(EMPLOYEES, employees);
        return "views/workload";
    }

    @PreAuthorize("@securityService.isUserView(#id)")
    @GetMapping("/of/employee/{id}")
    public String viewEmployeeAllocations(Model model, @PathVariable long id) {
        var payload = allocationService.getEmployeeAllocationsPayload(id);
        model.addAttribute("allocations", payload.getAllocations());
        model.addAttribute("projects", payload.getAssignmentsProjects());

        return "views/allocations/employee_allocations";
    }

    @PreAuthorize("@securityService.isSuperiorView(#id)")
    @GetMapping("/of/superior/{id}")
    public String viewSubordinateAllocations(Model model, @PathVariable long id) {
        var payload = allocationService.getSubordinatesAllocations(id);
        model.addAttribute("allocations", payload.getAllocations());
        model.addAttribute("projects", payload.getAssignmentsProjects());
        model.addAttribute(EMPLOYEES, payload.getAllocationsEmployees());

        return "views/allocations/superior_allocations";
    }
}

package cz.zcu.kiv.mjakubas.piae.sem.webapplication.controller.v1;

import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Employee;
import cz.zcu.kiv.mjakubas.piae.sem.core.service.v1.AllocationService;
import cz.zcu.kiv.mjakubas.piae.sem.core.service.v1.CourseService;
import cz.zcu.kiv.mjakubas.piae.sem.core.service.v1.EmployeeService;
import cz.zcu.kiv.mjakubas.piae.sem.core.service.v1.FunctionService;
import cz.zcu.kiv.mjakubas.piae.sem.core.service.v1.ProjectService;
import cz.zcu.kiv.mjakubas.piae.sem.core.vo.AllocationVO;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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
    private final CourseService courseService;
    private final FunctionService functionService;
    private final EmployeeService employeeService;

    private static final String EMPLOYEES = "employees";

    @PreAuthorize("@securityService.isProjectManager(#projectId) or @securityService.isWorkplaceManager(#projectId)")
    @GetMapping("/create/{projectId}")
    public String createAllocationForProject(Model model, @ModelAttribute AllocationVO allocationVO, @PathVariable long projectId) {
        var project = projectService.getProject(projectId);
        var employee = employeeService.getEmployee(allocationVO.getEmployeeId());

        var rules = allocationService.getProjectAllocationsRules(projectId, 1);
        model.addAttribute("rules", rules);

        long allocationId = allocationService.createAllocation(allocationVO);
        return String.format("redirect:/p/%s/detail?edit=success", projectId);
    }

    @PreAuthorize("@securityService.isCourseManager(#courseId) or @securityService.isWorkplaceManager(#courseId)")
    @GetMapping("/create/{courseId}")
    public String createAllocationForCourse(Model model, @ModelAttribute AllocationVO allocationVO, @PathVariable long courseId) {
        var course = courseService.getCourse(courseId);
        var employee = employeeService.getEmployee(allocationVO.getEmployeeId());

        var rules = allocationService.getCourseAllocationsRules(courseId, 1);
        model.addAttribute("rules", rules);

        long allocationId = allocationService.createAllocation(allocationVO);
        return String.format("redirect:/c/%s/detail?edit=success", courseId);
    }

    @PreAuthorize("@securityService.isFunctionManager(#functionId) or @securityService.isWorkplaceManager(#functionId)")
    @GetMapping("/create/{functionId}")
    public String createAllocationForFunction(Model model, @ModelAttribute AllocationVO allocationVO, @PathVariable long functionId) {
        var function = functionService.getFunction(functionId);
        var employee = employeeService.getEmployee(allocationVO.getEmployeeId());

        var rules = allocationService.getFunctionAllocationsRules(functionId, 1);
        model.addAttribute("rules", rules);

        long allocationId = allocationService.createAllocation(allocationVO);
        return String.format("redirect:/f/%s/detail?edit=success", functionId);
    }

    @PreAuthorize("@securityService.isAtLeastProjectManager() or @securityService.isWorkplaceManager(#id)")
    @PostMapping("{id}/edit")
    public String editAllocation(Model model, @PathVariable long id,
                                 @ModelAttribute AllocationVO allocationVO) {
        allocationService.updateAllocation(allocationVO, id);
        if (allocationVO.getProjectId() > 0) {
            return String.format("redirect:/p/%s/detail?edit=success", allocationVO.getProjectId());
        } else if (allocationVO.getCourseId() > 0) {
            return String.format("redirect:/c/%s/detail?edit=success", allocationVO.getCourseId());
        } else {
            return String.format("redirect:/f/%s/detail?edit=success", allocationVO.getFunctionId());
        }
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

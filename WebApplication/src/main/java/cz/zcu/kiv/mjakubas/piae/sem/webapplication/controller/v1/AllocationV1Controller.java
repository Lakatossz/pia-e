package cz.zcu.kiv.mjakubas.piae.sem.webapplication.controller.v1;

import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Allocation;
import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Course;
import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Employee;
import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Function;
import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Project;
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

    @PreAuthorize("@securityService.isProjectManager(#projectId)")
    @PostMapping("/create/project/{projectId}")
    public String createAllocationForProject(Model model, @ModelAttribute AllocationVO allocationVO, @PathVariable long projectId) {

        var project = projectService.getProject(projectId);
        allocationVO.setDateFrom(project.getDateFrom());
        allocationVO.setDateUntil(project.getDateUntil());
        allocationVO.setIsActive(true);

        System.out.println("allocationVO: " + allocationVO);

        var employee = employeeService.getEmployee(allocationVO.getEmployeeId());

        var rules = allocationService.getProjectAllocationsRules(projectId, 1);
        model.addAttribute("rules", rules);

        long allocationId = allocationService.createAllocation(allocationVO);
        return String.format("redirect:/p/%s/detail?edit=success", projectId);
    }

    @PreAuthorize("@securityService.isCourseManager(#courseId)")
    @PostMapping("/create/course/{courseId}")
    public String createAllocationForCourse(Model model, @ModelAttribute AllocationVO allocationVO, @PathVariable long courseId) {
        System.out.println("allocationVO: " + allocationVO);

        var course = courseService.getCourse(courseId);

        allocationVO.setDateFrom(course.getDateFrom());
        allocationVO.setDateUntil(course.getDateUntil());
        allocationVO.setIsActive(true);

        var employee = employeeService.getEmployee(allocationVO.getEmployeeId());

        var rules = allocationService.getCourseAllocationsRules(courseId, 1);
        model.addAttribute("rules", rules);

        long allocationId = allocationService.createAllocation(allocationVO);
        return String.format("redirect:/c/%s/detail?edit=success", courseId);
    }

    @PreAuthorize("@securityService.isFunctionManager(#functionId)")
    @PostMapping("/create/function/{functionId}")
    public String createAllocationForFunction(Model model, @ModelAttribute AllocationVO allocationVO, @PathVariable long functionId) {
        var function = functionService.getFunction(functionId);
        var employee = employeeService.getEmployee(allocationVO.getEmployeeId());

        var rules = allocationService.getFunctionAllocationsRules(functionId, 1);
        model.addAttribute("rules", rules);

        long allocationId = allocationService.createAllocation(allocationVO);
        return String.format("redirect:/f/%s/detail?edit=success", functionId);
    }

    @PreAuthorize("@securityService.isProjectManager(#projectId)")
    @PostMapping("/{allocationId}/edit/p/{projectId}")
    public String editProjectAllocation(Model model,
                                        @ModelAttribute AllocationVO allocationVO,
                                        @PathVariable long allocationId,
                                        @PathVariable long projectId) {

        Project project = projectService.getProject(projectId);
        allocationVO.setDateFrom(project.getDateFrom());
        allocationVO.setDateUntil(project.getDateUntil());
        allocationVO.setId(allocationId);

        System.out.println(allocationVO);

        allocationService.updateAllocation(allocationVO, allocationId);

        return String.format("redirect:/p/%s/detail?edit=success", allocationVO.getProjectId());
    }

    @PreAuthorize("@securityService.isCourseManager(#courseId)")
    @PostMapping("/{allocationId}/edit/c/{courseId}")
    public String editCourseAllocation(Model model,
                                       @ModelAttribute AllocationVO allocationVO,
                                       @PathVariable long allocationId,
                                       @PathVariable long courseId) {

        Course course = courseService.getCourse(courseId);
        allocationVO.setDateFrom(course.getDateFrom());
        allocationVO.setDateUntil(course.getDateUntil());
        allocationVO.setId(allocationId);

        System.out.println(allocationVO);

        allocationService.updateAllocation(allocationVO, allocationId);

        return String.format("redirect:/c/%s/detail?edit=success", allocationVO.getCourseId());
    }

    @PreAuthorize("@securityService.isFunctionManager(#functionId)")
    @PostMapping("/{allocationId}/edit/f/{functionId}")
    public String editFunctionAllocation(Model model,
                                         @ModelAttribute AllocationVO allocationVO,
                                         @PathVariable long allocationId,
                                         @PathVariable long functionId) {

        Function function = functionService.getFunction(functionId);
        allocationVO.setDateFrom(function.getDateFrom());
        allocationVO.setDateUntil(function.getDateUntil());
        allocationVO.setId(allocationId);

        System.out.println(allocationVO);

        allocationService.updateAllocation(allocationVO, allocationId);

        return String.format("redirect:/f/%s/detail?edit=success", allocationVO.getFunctionId());
    }

    @PreAuthorize("@securityService.isFunctionManager(#projectId)")
    @PostMapping("/{allocationId}/delete/p/{projectId}")
    public String deleteProjectAllocation(Model model, @PathVariable long projectId, @PathVariable long allocationId) {

        System.out.println("Tady jsem");

        allocationService.getAllocation(allocationId);

        return String.format("redirect:/p/%s/detail?edit=success", projectId);
    }


    @PreAuthorize("@securityService.isCourseManager(#courseId)")
    @PostMapping("/{allocationId}/delete/c/{courseId}")
    public String deleteCourseAllocation(Model model, @PathVariable long courseId, @PathVariable long allocationId) {
        return String.format("redirect:/c/%s/detail?edit=success", courseId);
    }


    @PreAuthorize("@securityService.isFunctionManager(#functionId)")
    @PostMapping("/{allocationId}/delete/c/{functionId}")
    public String deleteFunctionAllocation(Model model, @PathVariable long functionId, @PathVariable long allocationId) {

        System.out.println("Tady jsem");

        return String.format("redirect:/f/%s/detail?edit=success", functionId);
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

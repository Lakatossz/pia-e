package cz.zcu.kiv.mjakubas.piae.sem.webapplication.controller.v1;

import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Activity;
import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Allocation;
import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Course;
import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Employee;
import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Function;
import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Project;
import cz.zcu.kiv.mjakubas.piae.sem.core.domain.TermState;
import cz.zcu.kiv.mjakubas.piae.sem.core.service.MyUtils;
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

import java.time.LocalDate;

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
    private final MyUtils utils;

    private static final String EMPLOYEES = "employees";

    private static final String PROJECT_DETAIL_REDIRECT_SUCCESS = "redirect:/p/%s/detail?edit=success";
    private static final String COURSE_DETAIL_REDIRECT_SUCCESS = "redirect:/c/%s/detail?edit=success";
    private static final String FUNCTION_DETAIL_REDIRECT_SUCCESS = "redirect:/f/%s/detail?edit=success";

    @PreAuthorize("hasAnyAuthority(" +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).SECRETARIAT, " +
            "@securityService.isProjectManager(#projectId), " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).ADMIN)")
    @PostMapping("/create/project/{projectId}")
    public String createAllocationForProject(Model model,
                                             @ModelAttribute AllocationVO allocationVO,
                                             @PathVariable long projectId) {
        Project project = projectService.getProject(projectId);

        System.out.println(allocationVO);

//        addAditionalActivityValues(allocationVO, 0, employeeId);
        setAllocationDates(allocationVO, project);

        allocationVO.setTerm(TermState.N.getValue());

        var rules = allocationService.getProjectAllocationsRules(projectId, allocationVO.getWorkerId());
        model.addAttribute("rules", rules);

        allocationService.createAllocation(allocationVO);
        return String.format(PROJECT_DETAIL_REDIRECT_SUCCESS, projectId);
    }

    @PreAuthorize("hasAnyAuthority(" +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).SECRETARIAT, " +
            "@securityService.isCourseManager(#courseId), " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).ADMIN)")
    @PostMapping("/create/course/{courseId}/{year}")
    public String createAllocationForCourse(Model model,
                                            @ModelAttribute AllocationVO allocationVO,
                                            @PathVariable long courseId,
                                            @PathVariable long year) {
        Course course = courseService.getCourse(courseId);

        System.out.println("year: " + year);

        System.out.println(allocationVO);

        setAllocationDates(allocationVO, course);
        courseService.addCoursesValues(allocationVO, (int) year);

        var rules = allocationService.getCourseAllocationsRules(courseId, 1);
        model.addAttribute("rules", rules);

        allocationService.createAllocation(allocationVO);
        return String.format(COURSE_DETAIL_REDIRECT_SUCCESS, courseId);
    }

    @PreAuthorize("hasAnyAuthority(" +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).SECRETARIAT, " +
            "@securityService.isFunctionManager(#functionId), " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).ADMIN)")
    @PostMapping("/create/function/{functionId}")
    public String createAllocationForFunction(Model model,
                                              @ModelAttribute AllocationVO allocationVO,
                                              @PathVariable long functionId) {
        var function = functionService.getFunction(functionId);

        System.out.println(allocationVO);

//        addAditionalActivityValues(allocationVO, 0, employeeId);
        setAllocationDates(allocationVO, function);
        allocationVO.setTerm(TermState.N.getValue());

        var rules = allocationService.getFunctionAllocationsRules(functionId, 1);
        model.addAttribute("rules", rules);

        allocationService.createAllocation(allocationVO);
        return String.format(FUNCTION_DETAIL_REDIRECT_SUCCESS, functionId);
    }

    @PreAuthorize("hasAnyAuthority(" +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).SECRETARIAT, " +
            "@securityService.isProjectManager(#projectId), " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).ADMIN)")
    @PostMapping("/{allocationId}/edit/p/{projectId}/{employeeId}")
    public String editProjectAllocation(Model model,
                                        @ModelAttribute AllocationVO allocationVO,
                                        @PathVariable long allocationId,
                                        @PathVariable long projectId,
                                        @PathVariable long employeeId) {
        addAditionalActivityValues(allocationVO, allocationId, employeeId);
        allocationService.updateAllocation(allocationVO, allocationId);
        return String.format(PROJECT_DETAIL_REDIRECT_SUCCESS, allocationVO.getProjectId());
    }

    @PreAuthorize("hasAnyAuthority(" +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).SECRETARIAT, " +
            "@securityService.isCourseManager(#courseId), " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).ADMIN)")
    @PostMapping("/{allocationId}/edit/c/{courseId}/{employeeId}")
    public String editCourseAllocation(Model model,
                                       @ModelAttribute AllocationVO allocationVO,
                                       @PathVariable long allocationId,
                                       @PathVariable long courseId,
                                       @PathVariable long employeeId) {

        Course course = courseService.getCourse(courseId);
        Allocation allocation = allocationService.getAllocation(allocationId);
        addAditionalActivityValues(allocationVO, allocationId, employeeId);
//        if (!term.equals(allocation.getTerm().getValue()))
//            addCoursesValues(allocationVO, course);

        System.out.println(allocationVO);

        allocationService.updateAllocation(allocationVO, allocationId);

        return String.format(COURSE_DETAIL_REDIRECT_SUCCESS, allocationVO.getCourseId());
    }

    @PreAuthorize("hasAnyAuthority(" +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).SECRETARIAT, " +
            "@securityService.isFunctionManager(#functionId), " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).ADMIN)")
    @PostMapping("/{allocationId}/edit/f/{functionId}/{employeeId}")
    public String editFunctionAllocation(Model model,
                                         @ModelAttribute AllocationVO allocationVO,
                                         @PathVariable long allocationId,
                                         @PathVariable long functionId,
                                         @PathVariable long employeeId) {
        System.out.println(allocationVO);

        Function function = functionService.getFunction(functionId);
        addAditionalActivityValues(allocationVO, allocationId, employeeId);

        System.out.println(employeeId);

        System.out.println(allocationVO);

        allocationService.updateAllocation(allocationVO, allocationId);

        return String.format(FUNCTION_DETAIL_REDIRECT_SUCCESS, allocationVO.getFunctionId());
    }

    @PreAuthorize("hasAnyAuthority(" +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).SECRETARIAT, " +
            "@securityService.isProjectManager(#projectId), " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).ADMIN)")
    @PostMapping("/{allocationId}/delete/p/{projectId}")
    public String deleteProjectAllocation(Model model, @PathVariable long projectId, @PathVariable long allocationId) {
        allocationService.removeAllocation(allocationId);
        return String.format(PROJECT_DETAIL_REDIRECT_SUCCESS, projectId);
    }

    @PreAuthorize("hasAnyAuthority(" +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).SECRETARIAT, " +
            "@securityService.isCourseManager(#courseId), " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).ADMIN)")
    @PostMapping("/{allocationId}/delete/c/{courseId}")
    public String deleteCourseAllocation(Model model, @PathVariable long courseId, @PathVariable long allocationId) {
        allocationService.removeAllocation(allocationId);
        return String.format(COURSE_DETAIL_REDIRECT_SUCCESS, courseId);
    }


    @PreAuthorize("hasAnyAuthority(" +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).SECRETARIAT, " +
            "@securityService.isFunctionManager(#functionId), " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).ADMIN)")
    @PostMapping("/{allocationId}/delete/f/{functionId}")
    public String deleteFunctionAllocation(Model model, @PathVariable long functionId, @PathVariable long allocationId) {
        allocationService.removeAllocation(allocationId);
        return String.format(FUNCTION_DETAIL_REDIRECT_SUCCESS, functionId);
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

    private void setAllocationDates(AllocationVO allocationVO, Activity activity) {
        allocationVO.setDateFrom(utils.convertToLocalDateTime(activity.getDateFrom()));
        allocationVO.setDateUntil(utils.convertToLocalDateTime(activity.getDateUntil()));
        allocationVO.setIsActive(true);
    }

    private void addAditionalActivityValues(AllocationVO allocationVO,
                                            long allocationId,
                                            long employeeId) {
        allocationVO.setId(allocationId);
        allocationVO.setWorkerId(employeeId);
    }
}

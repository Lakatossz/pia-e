package cz.zcu.kiv.mjakubas.piae.sem.webapplication.controller.v1;

import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Activity;
import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Allocation;
import cz.zcu.kiv.mjakubas.piae.sem.core.domain.AllocationCell;
import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Course;
import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Employee;
import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Function;
import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Project;
import cz.zcu.kiv.mjakubas.piae.sem.core.domain.ProjectState;
import cz.zcu.kiv.mjakubas.piae.sem.core.domain.TermState;
import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Workplace;
import cz.zcu.kiv.mjakubas.piae.sem.core.exceptions.CollisionException;
import cz.zcu.kiv.mjakubas.piae.sem.core.exceptions.SecurityException;
import cz.zcu.kiv.mjakubas.piae.sem.core.service.MyUtils;
import cz.zcu.kiv.mjakubas.piae.sem.core.service.v1.AllocationService;
import cz.zcu.kiv.mjakubas.piae.sem.core.service.v1.CourseService;
import cz.zcu.kiv.mjakubas.piae.sem.core.service.v1.EmployeeService;
import cz.zcu.kiv.mjakubas.piae.sem.core.service.v1.FunctionService;
import cz.zcu.kiv.mjakubas.piae.sem.core.service.v1.ProjectService;
import cz.zcu.kiv.mjakubas.piae.sem.core.vo.AllocationVO;
import cz.zcu.kiv.mjakubas.piae.sem.core.vo.ProjectVO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

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
    private static final String PROJECT_DETAIL_REDIRECT = "redirect:/p/%s/detail";
    private static final String PROJECT_DETAIL_REDIRECT_SUCCESS = "redirect:/p/%s/detail?edit=success";
    private static final String COURSE_DETAIL_REDIRECT = "redirect:/c/%s/detail";
    private static final String COURSE_DETAIL_REDIRECT_SUCCESS = "redirect:/c/%s/detail?edit=success";
    private static final String FUNCTION_DETAIL_REDIRECT = "redirect:/f/%s/detail";
    private static final String FUNCTION_DETAIL_REDIRECT_SUCCESS = "redirect:/f/%s/detail?edit=success";
    private static final String PERMISSION_ERROR = "permissionError";
    private static final String COLLISION_ERROR = "collisionError";
    private static final String GENEREAL_ERROR = "generalError";

    @PreAuthorize("hasAnyAuthority(" +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).SECRETARIAT, " +
            "@securityService.isProjectManager(#projectId), " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).ADMIN)")
    @PostMapping("/create/project/{projectId}")
    public String createAllocationForProject(RedirectAttributes redirectAttributes,
                                             Model model,
                                             @ModelAttribute AllocationVO allocationVO,
                                             @PathVariable long projectId) {
        Project project = projectService.getProject(projectId);
        setAllocationDates(allocationVO, project);
        allocationVO.setTerm(TermState.N.getValue());

        try {
            allocationService.createAllocation(allocationVO);
            return String.format(PROJECT_DETAIL_REDIRECT_SUCCESS, projectId);
        } catch (CollisionException e) {
            redirectAttributes.addFlashAttribute(COLLISION_ERROR, true);
            return String.format(PROJECT_DETAIL_REDIRECT, projectId);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(GENEREAL_ERROR, true);
            return String.format(PROJECT_DETAIL_REDIRECT, projectId);
        }
    }

    @PreAuthorize("hasAnyAuthority(" +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).SECRETARIAT, " +
            "@securityService.isCourseManager(#courseId), " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).ADMIN)")
    @PostMapping("/create/course/{courseId}/{year}")
    public String createAllocationForCourse(RedirectAttributes redirectAttributes,
                                            Model model,
                                            @ModelAttribute AllocationVO allocationVO,
                                            @PathVariable long courseId,
                                            @PathVariable long year) {
        Course course = courseService.getCourse(courseId);
        setAllocationDates(allocationVO, course);
        courseService.addCoursesValues(allocationVO, (int) year);

        try {
            allocationService.createAllocation(allocationVO);
            return String.format(COURSE_DETAIL_REDIRECT_SUCCESS, courseId);
        } catch (CollisionException e) {
            redirectAttributes.addFlashAttribute(COLLISION_ERROR, true);
            return String.format(COURSE_DETAIL_REDIRECT, courseId);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(GENEREAL_ERROR, true);
            return String.format(COURSE_DETAIL_REDIRECT, courseId);
        }
    }

    @PreAuthorize("hasAnyAuthority(" +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).SECRETARIAT, " +
            "@securityService.isFunctionManager(#functionId), " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).ADMIN)")
    @PostMapping("/create/function/{functionId}")
    public String createAllocationForFunction(RedirectAttributes redirectAttributes,
                                              Model model,
                                              @ModelAttribute AllocationVO allocationVO,
                                              @PathVariable long functionId) {
        var function = functionService.getFunction(functionId);
        setAllocationDates(allocationVO, function);
        allocationVO.setTerm(TermState.N.getValue());

        try {
            allocationService.createAllocation(allocationVO);
            return String.format(FUNCTION_DETAIL_REDIRECT_SUCCESS, functionId);
        } catch (CollisionException e) {
            redirectAttributes.addFlashAttribute(COLLISION_ERROR, true);
            return String.format(FUNCTION_DETAIL_REDIRECT, functionId);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(GENEREAL_ERROR, true);
            return String.format(FUNCTION_DETAIL_REDIRECT, functionId);
        }
    }

    @PreAuthorize("hasAnyAuthority(" +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).SECRETARIAT, " +
            "@securityService.isProjectManager(#projectId), " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).ADMIN)")
    @PostMapping("/{allocationId}/edit/p/{projectId}/{employeeId}")
    public String editProjectAllocation(RedirectAttributes redirectAttributes,
                                        @ModelAttribute AllocationVO allocationVO,
                                        @PathVariable long allocationId,
                                        @PathVariable long projectId,
                                        @PathVariable long employeeId) {
        try {
            addAditionalActivityValues(allocationVO, allocationId, employeeId);
            allocationService.updateAllocation(allocationVO, allocationId);
            return String.format(PROJECT_DETAIL_REDIRECT_SUCCESS, projectId);
        } catch (CollisionException e) {
            redirectAttributes.addFlashAttribute(COLLISION_ERROR, true);
            return String.format(PROJECT_DETAIL_REDIRECT, projectId);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(GENEREAL_ERROR, true);
            return String.format(PROJECT_DETAIL_REDIRECT, projectId);
        }
    }

    @PreAuthorize("hasAnyAuthority(" +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).SECRETARIAT, " +
            "@securityService.isCourseManager(#courseId), " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).ADMIN)")
    @PostMapping("/{allocationId}/edit/c/{courseId}/{employeeId}/{term}")
    public String editCourseAllocation(RedirectAttributes redirectAttributes,
                                       @ModelAttribute AllocationVO allocationVO,
                                       @PathVariable long allocationId,
                                       @PathVariable long courseId,
                                       @PathVariable long employeeId,
                                       @PathVariable String term) {
        allocationVO.setTerm(term);
        addAditionalActivityValues(allocationVO, allocationId, employeeId);

        try {
            allocationService.updateAllocation(allocationVO, allocationId);
            return String.format(COURSE_DETAIL_REDIRECT_SUCCESS, courseId);
        } catch (CollisionException e) {
            redirectAttributes.addFlashAttribute(COLLISION_ERROR, true);
            return String.format(COURSE_DETAIL_REDIRECT, courseId);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(GENEREAL_ERROR, true);
            return String.format(COURSE_DETAIL_REDIRECT, courseId);
        }
    }

    @PreAuthorize("hasAnyAuthority(" +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).SECRETARIAT, " +
            "@securityService.isFunctionManager(#functionId), " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).ADMIN)")
    @PostMapping("/{allocationId}/edit/f/{functionId}/{employeeId}")
    public String editFunctionAllocation(RedirectAttributes redirectAttributes,
                                         @ModelAttribute AllocationVO allocationVO,
                                         @PathVariable long allocationId,
                                         @PathVariable long functionId,
                                         @PathVariable long employeeId) {
        addAditionalActivityValues(allocationVO, allocationId, employeeId);

        try {
            allocationService.updateAllocation(allocationVO, allocationId);
            return String.format(FUNCTION_DETAIL_REDIRECT_SUCCESS, functionId);
        } catch (CollisionException e) {
            redirectAttributes.addFlashAttribute(COLLISION_ERROR, true);
            return String.format(FUNCTION_DETAIL_REDIRECT, functionId);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(GENEREAL_ERROR, true);
            return String.format(FUNCTION_DETAIL_REDIRECT, functionId);
        }
    }

    @PreAuthorize("hasAnyAuthority(" +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).SECRETARIAT, " +
            "@securityService.isProjectManager(#projectId), " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).ADMIN)")
    @PostMapping("/{allocationId}/delete/p/{projectId}")
    public String deleteProjectAllocation(RedirectAttributes redirectAttributes,
                                          @PathVariable long projectId,
                                          @PathVariable long allocationId) {
        try {
            allocationService.removeAllocation(allocationId);
            return String.format(PROJECT_DETAIL_REDIRECT_SUCCESS, projectId);
        } catch (SecurityException e) {
            redirectAttributes.addFlashAttribute(PERMISSION_ERROR, true);
            return String.format(PROJECT_DETAIL_REDIRECT, projectId);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(GENEREAL_ERROR, true);
            return String.format(PROJECT_DETAIL_REDIRECT, projectId);
        }
    }

    @PreAuthorize("hasAnyAuthority(" +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).SECRETARIAT, " +
            "@securityService.isCourseManager(#courseId), " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).ADMIN)")
    @PostMapping("/{allocationId}/delete/c/{courseId}")
    public String deleteCourseAllocation(RedirectAttributes redirectAttributes,
                                         @PathVariable long courseId,
                                         @PathVariable long allocationId) {
        try {
            allocationService.removeAllocation(allocationId);
            return String.format(COURSE_DETAIL_REDIRECT_SUCCESS, courseId);
        } catch (SecurityException e) {
            redirectAttributes.addFlashAttribute(PERMISSION_ERROR, true);
            return String.format(COURSE_DETAIL_REDIRECT, courseId);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(GENEREAL_ERROR, true);
            return String.format(COURSE_DETAIL_REDIRECT, courseId);
        }
    }


    @PreAuthorize("hasAnyAuthority(" +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).SECRETARIAT, " +
            "@securityService.isFunctionManager(#functionId), " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).ADMIN)")
    @PostMapping("/{allocationId}/delete/f/{functionId}")
    public String deleteFunctionAllocation(RedirectAttributes redirectAttributes,
                                           @PathVariable long functionId,
                                           @PathVariable long allocationId) {
        try {
            allocationService.removeAllocation(allocationId);
            return String.format(FUNCTION_DETAIL_REDIRECT_SUCCESS, functionId);
        } catch (SecurityException e) {
            redirectAttributes.addFlashAttribute(PERMISSION_ERROR, true);
            return String.format(FUNCTION_DETAIL_REDIRECT, functionId);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(GENEREAL_ERROR, true);
            return String.format(FUNCTION_DETAIL_REDIRECT, functionId);
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
        if (allocationVO.getCourseId() == 0) {
            allocationVO.setTerm("N");
        }
    }
}

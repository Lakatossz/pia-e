package cz.zcu.kiv.mjakubas.piae.sem.webapplication.controller.v1;

import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Allocation;
import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Course;
import cz.zcu.kiv.mjakubas.piae.sem.core.domain.TermState;
import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Workplace;
import cz.zcu.kiv.mjakubas.piae.sem.core.service.MyUtils;
import cz.zcu.kiv.mjakubas.piae.sem.core.service.v1.AllocationService;
import cz.zcu.kiv.mjakubas.piae.sem.core.service.v1.CourseService;
import cz.zcu.kiv.mjakubas.piae.sem.core.service.v1.EmployeeService;
import cz.zcu.kiv.mjakubas.piae.sem.core.service.v1.WorkplaceService;
import cz.zcu.kiv.mjakubas.piae.sem.core.vo.AllocationVO;
import cz.zcu.kiv.mjakubas.piae.sem.core.vo.CourseVO;
import cz.zcu.kiv.mjakubas.piae.sem.core.vo.EmployeeVO;
import cz.zcu.kiv.mjakubas.piae.sem.core.vo.WorkplaceVO;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * Contains all sites for working with courses.
 */
@Controller
@RequestMapping("/c")
@AllArgsConstructor
@PreAuthorize("hasAuthority(T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).SECRETARIAT)")
public class CourseV1Controller {

    private final CourseService courseService;
    private final EmployeeService employeeService;
    private final WorkplaceService workplaceService;
    private final AllocationService allocationService;
    private final MyUtils utils;

    private static final String EMPLOYEES = "employees";
    private static final String RESTRICTIONS = "restrictions";
    private static final String COURSE = "course";
    private static final String WORKPLACES = "workplaces";

    @GetMapping()
    @PreAuthorize("hasAnyAuthority(" +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).SECRETARIAT, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).COURSE_SCHEDULER, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).COURSE_SUPERVISOR, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).ADMIN)")
    public String getCourses(Model model) {
        var courses = courseService.getCourses();
        List<Allocation> firstAllocations = courseService.getFirstAllocationsForEachCourse(courses);

        model.addAttribute("courses", courses);
        model.addAttribute("firstAllocations", firstAllocations);
        return "views/courses";
    }

    @GetMapping("/create")
    @PreAuthorize("hasAnyAuthority(" +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).SECRETARIAT, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).COURSE_SCHEDULER, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).COURSE_SUPERVISOR, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).ADMIN)")
    public String createCourse(Model model) {
        CourseVO newCourse = new CourseVO().name("Nový předmět");
        model.addAttribute(COURSE, newCourse);

        var courses = courseService.getCourses();
        var employees = employeeService.getEmployees();

        model.addAttribute(EMPLOYEES, employees);
        model.addAttribute(WORKPLACES, workplaceService.getWorkplaces());
        model.addAttribute(RESTRICTIONS, courses);

        return "forms/course/create_course_form";
    }

    @PostMapping("/create")
    @PreAuthorize("hasAnyAuthority(" +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).SECRETARIAT, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).COURSE_SCHEDULER, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).COURSE_SUPERVISOR, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).ADMIN)")
    public String createCourse(@ModelAttribute CourseVO courseVO,
                               BindingResult bindingResult, Model model,
                               RedirectAttributes redirectAttributes) {

        System.out.println(courseVO);

        long id = courseService.createCourse(courseVO);

        redirectAttributes.addAttribute("id", id);
        return "redirect:/c/{id}/detail?create=success";
    }

    @GetMapping("/{id}/edit")
    @PreAuthorize("hasAnyAuthority(" +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).SECRETARIAT, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).COURSE_SCHEDULER, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).COURSE_SUPERVISOR, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).ADMIN) " +
            " or @securityService.isCourseManager(#id) or @securityService.isWorkplaceManager(#id)")
    public String editCourse(Model model, @PathVariable long id,
                              @RequestParam(required = false) Boolean manage) {
        Course course = courseService.getCourse(id);

        CourseVO courseVO = new CourseVO()
                .id(course.getId())
                .name(course.getName())
                .shortcut(course.getShortcut())
                .courseManagerId(course.getCourseManager().getId())
                .courseManagerName(course.getCourseManager().getLastName())
                .courseWorkplace(course.getCourseWorkplace().getId())
                .dateFrom(course.getDateFrom())
                .dateUntil(course.getDateUntil())
                .introduced(course.getIntroduced())
                .term(course.getTerm())
                .lectureRequired(course.getLectureRequired())
                .exerciseRequired(course.getExerciseRequired());

        model.addAttribute(COURSE, courseVO);

        model.addAttribute(EMPLOYEES, employeeService.getEmployees());
        model.addAttribute(WORKPLACES, workplaceService.getWorkplaces());
        model.addAttribute(RESTRICTIONS, courseService.getCourses());
        model.addAttribute("manage", manage);

        return "forms/course/edit_course_form";
    }

    @PostMapping("/{id}/edit")
    @PreAuthorize("hasAnyAuthority(" +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).SECRETARIAT, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).COURSE_SCHEDULER, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).COURSE_SUPERVISOR, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).ADMIN) " +
            " or @securityService.isCourseManager(#id) or @securityService.isWorkplaceManager(#id)")
    public String editCourse(Model model, @PathVariable long id, @ModelAttribute CourseVO courseVO,
                              BindingResult errors, @RequestParam(required = false) Boolean manage) {

        System.out.println(courseVO);

        System.out.println("courseVO.getCourseWorkplace(): " + courseVO.getCourseWorkplace());

        courseService.editCourse(courseVO, id);

        return "redirect:/c/{id}/detail?edit=success";
    }

    @PostMapping("/{id}/delete")
    @PreAuthorize("hasAnyAuthority(" +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).SECRETARIAT, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).COURSE_SCHEDULER, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).COURSE_SUPERVISOR, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).ADMIN) " +
            " or @securityService.isCourseManager(#id) or @securityService.isWorkplaceManager(#id)")
    public String deleteCourse(Model model, @PathVariable long id) {
        courseService.removeCourse(id);
        return "redirect:/c?delete=success";
    }

    @GetMapping("/{id}/detail")
    @PreAuthorize("hasAnyAuthority(" +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).SECRETARIAT, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).COURSE_SCHEDULER, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).COURSE_SUPERVISOR, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).ADMIN) " +
            " or @securityService.isCourseManager(#id) or @securityService.isWorkplaceManager(#id)")
    public String detailCourse(Model model, @PathVariable long id,
                               @RequestParam(required = false) Boolean manage) {
        Course course = courseService.getCourse(id);

        var allocations = allocationService.getCourseAllocations(id).getAllocations();
        model.addAttribute("allocations", mapAllocations(allocations));

        List<List<AllocationVO>> allocationsByEars = new ArrayList<>();
        for (List<Allocation> allList : course.getAllocationsByYears()) {
            allocationsByEars.add(mapAllocations(allList));
        }

        CourseVO courseVO = new CourseVO()
                .name(course.getName())
                .shortcut(course.getShortcut())
                .courseManagerId(course.getCourseManager().getId())
                .courseManagerName(course.getCourseManager().getLastName())
                .courseWorkplace(course.getCourseWorkplace().getId())
                .dateFrom(course.getDateFrom())
                .dateUntil(course.getDateUntil())
                .introduced(new Date())
                .term(course.getTerm())
                .lectureRequired(course.getLectureRequired())
                .exerciseRequired(course.getExerciseRequired())
                .allocationsByYears(allocationsByEars)
                .years(course.getYears());

        model.addAttribute(COURSE, courseVO);

        model.addAttribute("terms", TermState.values());
        model.addAttribute("newAllocation", newCourseAllocation(course));
        model.addAttribute("allocationsByYears", allocationsByEars);

        var employees = employeeService.getEmployees();
        model.addAttribute(EMPLOYEES, employees);
        model.addAttribute(WORKPLACES, workplaceService.getWorkplaces());
        model.addAttribute("manage", manage);

        return "details/course_detail";
    }

    @GetMapping("/{id}/employee/add")
    @PreAuthorize("hasAnyAuthority(" +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).SECRETARIAT, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).COURSE_SCHEDULER, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).COURSE_SUPERVISOR, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).ADMIN)")
    public String addSubordinate(Model model, @PathVariable long id, @ModelAttribute EmployeeVO userVO) {
        var restrictions = courseService.getCourseEmployees(id);

        model.addAttribute("userVO", new EmployeeVO());
        model.addAttribute(RESTRICTIONS, restrictions);
        model.addAttribute(EMPLOYEES, employeeService.getEmployees());

        return "forms/course/create_course_employee_form";
    }

    @PostMapping("/{id}/employee/add")
    @PreAuthorize("hasAnyAuthority(" +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).SECRETARIAT, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).COURSE_SCHEDULER, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).COURSE_SUPERVISOR, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).ADMIN)")
    public String addSubordinate(Model model, @PathVariable long id, @ModelAttribute EmployeeVO userVO,
                                 BindingResult errors) {
        model.addAttribute("userVO", userVO);
        var course = courseService.getCourse(id);

        courseService.assignEmployee(userVO, course.getId());

        return "redirect:/c?addemployee=success";
    }

    @GetMapping("/{id}/manage")
    @PreAuthorize("hasAnyAuthority(" +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).SECRETARIAT, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).COURSE_SCHEDULER, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).COURSE_SUPERVISOR, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).ADMIN) " +
            " or @securityService.isCourseManager(#id) or @securityService.isWorkplaceManager(#id)")
    public String manageCourse(Model model, @PathVariable long id) {
        var payload = allocationService.getCourseAllocations(id);
        model.addAttribute("allocations", payload.getAllocations());
        model.addAttribute("courses", payload.getAssignmentsCourses());
        model.addAttribute(EMPLOYEES, payload.getAllocationsEmployees());
        return "views/course_management";
    }

    private AllocationVO newCourseAllocation(Course course) {
        AllocationVO newAllocation = new AllocationVO();
        newAllocation.setCourseId(course.getId());
        newAllocation.setRole("nový");
        newAllocation.setIsCertain(1.0F);
        newAllocation.setAllocationScope(1.0F);
        newAllocation.setDateFrom(utils.convertToLocalDateTime(course.getDateFrom()));
        newAllocation.setDateUntil(utils.convertToLocalDateTime(course.getDateUntil()));
        newAllocation.setDescription("description");
        return newAllocation;
    }

    private List<AllocationVO> mapAllocations(List<Allocation> allocations) {
        List<AllocationVO> allocationsVO = new LinkedList<>();
        for (Allocation allocation : allocations) {
            AllocationVO allocationVO = new AllocationVO();
            allocationVO.setId(allocation.getId());
            allocationVO.setCourseId(allocation.getCourse().getId());
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
}

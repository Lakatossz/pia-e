package cz.zcu.kiv.mjakubas.piae.sem.webapplication.controller.v1;

import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Allocation;
import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Course;
import cz.zcu.kiv.mjakubas.piae.sem.core.service.v1.AllocationService;
import cz.zcu.kiv.mjakubas.piae.sem.core.service.v1.CourseService;
import cz.zcu.kiv.mjakubas.piae.sem.core.service.v1.EmployeeService;
import cz.zcu.kiv.mjakubas.piae.sem.core.service.v1.WorkplaceService;
import cz.zcu.kiv.mjakubas.piae.sem.core.vo.AllocationVO;
import cz.zcu.kiv.mjakubas.piae.sem.core.vo.CourseVO;
import cz.zcu.kiv.mjakubas.piae.sem.core.vo.EmployeeVO;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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

    private static final String EMPLOYEES = "employees";
    private static final String RESTRICTIONS = "restrictions";

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
        CourseVO newCourse = new CourseVO().name("Nový projekt");
        model.addAttribute("course", newCourse);

        var courses = courseService.getCourses();
        var employees = employeeService.getEmployees();
        var workplaces = workplaceService.getWorkplaces();

        model.addAttribute(EMPLOYEES, employees);
        model.addAttribute("workplaces", workplaces);
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

        model.addAttribute("courseVO",
                new CourseVO()
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
                        .exerciseRequired(course.getExerciseRequired()));

        model.addAttribute(EMPLOYEES, employeeService.getEmployees());
        model.addAttribute("workplaces", workplaceService.getWorkplaces());
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
        courseService.editCourse(courseVO, id);

        if (Boolean.TRUE.equals(manage))
            return String.format("redirect:/c/%s/manage?edit=success", id);

        return "redirect:/c/{id}/detail?edit=success";
    }

    @GetMapping("/{id}/delete")
    @PreAuthorize("hasAnyAuthority(" +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).SECRETARIAT, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).COURSE_SCHEDULER, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).COURSE_SUPERVISOR, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).ADMIN) " +
            " or @securityService.isCourseManager(#id) or @securityService.isWorkplaceManager(#id)")
    public String editCourse(Model model, @PathVariable long id) {
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
        model.addAttribute("allocations", allocations);

        List<List<AllocationVO>> allocationsByEars = new ArrayList<>();
        for (List<Allocation> allList : course.getAllocationsByYears()) {
            List<AllocationVO> list = new ArrayList<>();
            for (Allocation all : allList) {
                var allVO = new AllocationVO();
                allVO.setId(all.getId());
                allVO.setCourseId(all.getCourse().getId());
                allVO.setEmployeeId(all.getWorker().getId());
                allVO.setAllocationScope(all.getAllocationScope());
                allVO.setDateFrom(all.getDateFrom());
                allVO.setDateUntil(all.getDateUntil());
                allVO.setRole(all.getRole());
                allVO.setDescription(all.getDescription());
                allVO.setIsCertain(all.getIsCertain());
                list.add(allVO);
            }
            allocationsByEars.add(list);
        }

        model.addAttribute("course",
                new CourseVO()
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
                        .years(course.getYears()));

        Calendar cal = Calendar.getInstance();

        AllocationVO newAllocation = new AllocationVO();
        newAllocation.setCourseId(course.getId());
        newAllocation.setRole("nový");
        newAllocation.setIsCertain(1.0F);
        newAllocation.setAllocationScope(1.0F);
        cal.set(2023, Calendar.JANUARY, 1);
        newAllocation.setDateFrom(cal.getTime());
        cal.set(2023, Calendar.DECEMBER, 31);
        newAllocation.setDateUntil(cal.getTime());
        newAllocation.setDescription("description");

        model.addAttribute("newAllocation", newAllocation);

        var employees = employeeService.getEmployees();

//        employees.forEach(employeeService::prepareProjectsCells); // TODO potrebuju to?
//        employees.forEach(employeeService::prepareTotalCells);

        model.addAttribute("allocationsByYears", allocationsByEars);

        model.addAttribute(EMPLOYEES, employees);
        model.addAttribute("workplaces", workplaceService.getWorkplaces());
        model.addAttribute(RESTRICTIONS, courseService.getCourses());
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
}

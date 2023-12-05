package cz.zcu.kiv.mjakubas.piae.sem.webapplication.controller.v1;

import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Allocation;
import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Course;
import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Employee;
import cz.zcu.kiv.mjakubas.piae.sem.core.domain.payload.AllocationPayload;
import cz.zcu.kiv.mjakubas.piae.sem.core.service.v1.AllocationService;
import cz.zcu.kiv.mjakubas.piae.sem.core.service.v1.CourseService;
import cz.zcu.kiv.mjakubas.piae.sem.core.service.v1.EmployeeService;
import cz.zcu.kiv.mjakubas.piae.sem.core.service.v1.WorkplaceService;
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

import java.util.ArrayList;
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
    public String getCourses(Model model) {
        var courses = courseService.getCourses();
        List<Allocation> firstAllocations = new ArrayList<>();
        courses.forEach(course -> {
            List<Allocation> allocationsWithoutFirst = course.getCourseAllocations();
            if (!allocationsWithoutFirst.isEmpty()) {
                allocationsWithoutFirst.remove(0);
                List<Allocation> temp = allocationService.getCourseAllocations(course.getId()).getAllocations();
                if (!temp.isEmpty()) {
                    Allocation allocation = temp.get(0);
                    firstAllocations.add(allocation);
                } else
                    firstAllocations.add(new Allocation().time(-1));
            } else
                firstAllocations.add(new Allocation().time(-1));
            course.setCourseAllocations(allocationsWithoutFirst);
        });

        model.addAttribute("courses", courses);
        model.addAttribute("firstAllocations", firstAllocations);
        return "views/courses";
    }

    @GetMapping("/create")
    public String createCourse(Model model) {
        model.addAttribute("courseVO", new CourseVO());

        var courses = courseService.getCourses();
        var employees = employeeService.getEmployees();
        var workplaces = workplaceService.getWorkplaces();

        model.addAttribute(EMPLOYEES, employees);
        model.addAttribute("workplaces", workplaces);
        model.addAttribute(RESTRICTIONS, courses);

        return "forms/course/create_course_form";
    }

    @PostMapping("/create")
    public String createCourse(@ModelAttribute CourseVO courseVO, BindingResult bindingResult, Model model) {
        courseService.createCourse(courseVO);

        return "redirect:/c?create=success";
    }

    @PreAuthorize("hasAuthority(T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).SECRETARIAT)" +
            " or @securityService.isCourseManager(#id) or @securityService.isWorkplaceManager(#id)")
    @GetMapping("/{id}/edit")
    public String editCourse(Model model, @PathVariable long id,
                              @RequestParam(required = false) Boolean manage) {
        Course data = courseService.getCourse(id);

        model.addAttribute("courseVO",
                new CourseVO()
                        .name(data.getName())
                        .shortcut(data.getShortcut())
                        .courseManagerId(data.getCourseManager().getId())
                        .courseManagerName(data.getCourseManager().getLastName())
                        .courseWorkplace(data.getCourseWorkplace().getId())
                        .dateFrom(data.getDateFrom())
                        .dateUntil(data.getDateUntil())
                        .introduced(data.getIntroduced())
                        .lectureRequired(data.getLectureRequired())
                        .exerciseRequired(data.getExerciseRequired()));

        model.addAttribute(EMPLOYEES, employeeService.getEmployees());
        model.addAttribute("workplaces", workplaceService.getWorkplaces());
        model.addAttribute(RESTRICTIONS, courseService.getCourses());
        model.addAttribute("manage", manage);

        return "forms/course/edit_course_form";
    }

    @PreAuthorize("hasAuthority(T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).SECRETARIAT) or @securityService.isProjectManager(#id) or @securityService.isWorkplaceManager(#id)")
    @PostMapping("/{id}/edit")
    public String editCourse(Model model, @PathVariable long id, @ModelAttribute CourseVO courseVO,
                              BindingResult errors, @RequestParam(required = false) Boolean manage) {
        courseService.editCourse(courseVO, id);

        if (Boolean.TRUE.equals(manage))
            return String.format("redirect:/c/%s/manage?edit=success", id);

        return "redirect:/c?edit=success";
    }

    @GetMapping("/{id}/delete")
    public String editCourse(Model model, @PathVariable long id) {
        return "redirect:/c?delete=success";
    }

    @PreAuthorize("hasAuthority(T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).SECRETARIAT)" +
            " or @securityService.isCourseManager(#id) or @securityService.isWorkplaceManager(#id)")
    @GetMapping("/{id}/detail")
    public String detailCourse(Model model, @PathVariable long id,
                               @RequestParam(required = false) Boolean manage) {
        Course data = courseService.getCourse(id);
        var allocations = allocationService.getCourseAllocations(id).getAllocations();

        model.addAttribute("allocations", allocations);
        model.addAttribute("course",
                new CourseVO()
                        .name(data.getName())
                        .shortcut(data.getShortcut())
                        .courseManagerId(data.getCourseManager().getId())
                        .courseManagerName(data.getCourseManager().getLastName())
                        .courseWorkplace(data.getCourseWorkplace().getId())
                        .dateFrom(data.getDateFrom())
                        .dateUntil(data.getDateUntil())
                        .introduced(data.getIntroduced())
                        .lectureRequired(data.getLectureRequired())
                        .exerciseRequired(data.getExerciseRequired()));

        model.addAttribute(EMPLOYEES, employeeService.getEmployees());
        model.addAttribute("workplaces", workplaceService.getWorkplaces());
        model.addAttribute(RESTRICTIONS, courseService.getCourses());
        model.addAttribute("manage", manage);

        return "details/course_detail";
    }

    @GetMapping("/{id}/employee/add")
    public String addSubordinate(Model model, @PathVariable long id, @ModelAttribute EmployeeVO userVO) {
        var restrictions = courseService.getCourseEmployees(id);

        model.addAttribute("userVO", new EmployeeVO());
        model.addAttribute(RESTRICTIONS, restrictions);
        model.addAttribute(EMPLOYEES, employeeService.getEmployees());

        return "forms/course/create_course_employee_form";
    }

    @PostMapping("/{id}/employee/add")
    public String addSubordinate(Model model, @PathVariable long id, @ModelAttribute EmployeeVO userVO,
                                 BindingResult errors) {
        model.addAttribute("userVO", userVO);
        var course = courseService.getCourse(id);

        courseService.assignEmployee(userVO, course.getId());

        return "redirect:/c?addemployee=success";
    }

    @PreAuthorize("@securityService.isCourseManager(#id) or @securityService.isWorkplaceManager(#id)")
    @GetMapping("/{id}/manage")
    public String manageCourse(Model model, @PathVariable long id) {
        var payload = allocationService.getCourseAllocations(id);
        model.addAttribute("allocations", payload.getAllocations());
        model.addAttribute("courses", payload.getAssignmentsCourses());
        model.addAttribute(EMPLOYEES, payload.getAllocationsEmployees());
        return "views/course_management";
    }
}

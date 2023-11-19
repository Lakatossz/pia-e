package cz.zcu.kiv.mjakubas.piae.sem.webapplication.controller.v1;

import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Course;
import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Project;
import cz.zcu.kiv.mjakubas.piae.sem.core.service.v1.AllocationService;
import cz.zcu.kiv.mjakubas.piae.sem.core.service.v1.CourseService;
import cz.zcu.kiv.mjakubas.piae.sem.core.service.v1.EmployeeService;
import cz.zcu.kiv.mjakubas.piae.sem.core.service.v1.WorkplaceService;
import cz.zcu.kiv.mjakubas.piae.sem.core.vo.CourseVO;
import cz.zcu.kiv.mjakubas.piae.sem.core.vo.EmployeeVO;
import cz.zcu.kiv.mjakubas.piae.sem.core.vo.ProjectVO;
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

    @GetMapping()
    public String getCourses(Model model) {
        var courses = courseService.getCourses();

        model.addAttribute("courses", courses);
        return "views/courses";
    }

    @GetMapping("/create")
    public String createCourse(Model model) {
        model.addAttribute("courseVO", new CourseVO());

        var courses = courseService.getCourses();
        var employees = employeeService.getEmployees();
        var workplaces = workplaceService.getWorkplaces();

        model.addAttribute("employees", employees);
        model.addAttribute("workplaces", workplaces);
        model.addAttribute("restrictions", courses);

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
                              @RequestParam(required = false) boolean manage) {
        Course data = courseService.getCourse(id);

        model.addAttribute("courseVO",
                new CourseVO()
                        .name(data.getName())
                        .courseManager(data.getCourseManager().getId())
                        .courseWorkplace(data.getCourseWorkplace().getId())
                        .dateFrom(data.getDateFrom())
                        .dateUntil(data.getDateUntil()));

        model.addAttribute("employees", employeeService.getEmployees());
        model.addAttribute("workplaces", workplaceService.getWorkplaces());

        model.addAttribute("restrictions", courseService.getCourses());

        model.addAttribute("manage", manage);

        return "forms/course/edit_course_form";
    }

    @PreAuthorize("hasAuthority(T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).SECRETARIAT) or @securityService.isProjectManager(#id) or @securityService.isWorkplaceManager(#id)")
    @PostMapping("/{id}/edit")
    public String editCourse(Model model, @PathVariable long id, @ModelAttribute CourseVO courseVO,
                              BindingResult errors, @RequestParam(required = false) boolean manage) {
        Course data = courseService.getCourse(id);

        courseService.editCourse(courseVO, id);

        if (manage)
            return String.format("redirect:/c/%s/manage?edit=success", id);

        return "redirect:/c?edit=success";
    }

    @GetMapping("/{id}/delete")
    public String editCourse(Model model, @PathVariable long id) {
        return "redirect:/c?delete=success";
    }

    @GetMapping("/{id}/employee/add")
    public String addSubordinate(Model model, @PathVariable long id, @ModelAttribute EmployeeVO userVO) {
        model.addAttribute("userVO", new EmployeeVO());

        var course = courseService.getCourse(id);

        var restrictions = courseService.getCourseEmployees(id);

        model.addAttribute("restrictions", restrictions);
        model.addAttribute("employees", employeeService.getEmployees());

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
        model.addAttribute("employees", payload.getAllocationsEmployees());
        return "views/course_management";
    }
}

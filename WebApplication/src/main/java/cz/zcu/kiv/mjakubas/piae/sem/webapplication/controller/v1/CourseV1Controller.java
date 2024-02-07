package cz.zcu.kiv.mjakubas.piae.sem.webapplication.controller.v1;

import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Allocation;
import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Course;
import cz.zcu.kiv.mjakubas.piae.sem.core.domain.TermState;
import cz.zcu.kiv.mjakubas.piae.sem.core.exceptions.SecurityException;
import cz.zcu.kiv.mjakubas.piae.sem.core.service.MyUtils;
import cz.zcu.kiv.mjakubas.piae.sem.core.service.v1.CourseService;
import cz.zcu.kiv.mjakubas.piae.sem.core.service.v1.EmployeeService;
import cz.zcu.kiv.mjakubas.piae.sem.core.service.v1.WorkplaceService;
import cz.zcu.kiv.mjakubas.piae.sem.core.vo.AllocationVO;
import cz.zcu.kiv.mjakubas.piae.sem.core.vo.CourseVO;
import cz.zcu.kiv.mjakubas.piae.sem.core.vo.EmployeeVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * Contains all sites for working with courses.
 */
@Controller
@RequestMapping("/c")
@PreAuthorize("hasAuthority(T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).SECRETARIAT)")
public class CourseV1Controller {

    @Autowired
    private CourseService courseService;

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private WorkplaceService workplaceService;

    @Autowired
    private MyUtils utils;

    private static final String EMPLOYEES = "employees";
    private static final String RESTRICTIONS = "restrictions";
    private static final String COURSE = "course";
    private static final String WORKPLACES = "workplaces";
    private static final String COURSES_REDIRECT = "redirect:/c";
    private static final String COURSE_DETAIL_REDIRECT = "redirect:/c/%s/detail";
    private static final String PERMISSION_ERROR = "permissionError";
    private static final String GENEREAL_ERROR = "generalError";

    @GetMapping()
    @PreAuthorize("hasAnyAuthority(" +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).SECRETARIAT, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).USER, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).COURSE_SCHEDULER, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).COURSE_SUPERVISOR, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).ADMIN)")
    public String getCourses(Model model,
                             RedirectAttributes redirectAttributes) {
        try {
            var courses = courseService.getCourses();
            List<Allocation> firstAllocations = courseService.getFirstAllocationsForEachCourse(courses);

            model.addAttribute("courses", courses);
            model.addAttribute("firstAllocations", firstAllocations);
            model.addAttribute("canCreateCourse", courseService.canCreateCourse());
            model.addAttribute("canCreateCourse", courseService.canCreateCourse());
            return "views/courses";
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
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).COURSE_SCHEDULER, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).COURSE_SUPERVISOR, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).ADMIN)")
    public String createCourse(Model model,
                               RedirectAttributes redirectAttributes) {
        try {
            CourseVO newCourse = new CourseVO().name("Nový předmět");
            model.addAttribute(COURSE, newCourse);

            var courses = courseService.getCourses();
            var employees = employeeService.getEmployees();

            model.addAttribute(EMPLOYEES, employees);
            model.addAttribute(WORKPLACES, workplaceService.getWorkplaces());
            model.addAttribute(RESTRICTIONS, courses);

            return "forms/course/create_course_form";
        } catch (SecurityException e) {
            redirectAttributes.addFlashAttribute(PERMISSION_ERROR, true);
            return COURSES_REDIRECT;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(GENEREAL_ERROR, true);
            return COURSES_REDIRECT;
        }
    }

    @PostMapping("/create")
    @PreAuthorize("hasAnyAuthority(" +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).SECRETARIAT, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).COURSE_SCHEDULER, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).COURSE_SUPERVISOR, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).ADMIN)")
    public String createCourse(@ModelAttribute CourseVO courseVO,
                               RedirectAttributes redirectAttributes) {

        try {
            long id = courseService.createCourse(courseVO);
            redirectAttributes.addAttribute("id", id);
            return "redirect:/c/{id}/detail?create=success";
        } catch (SecurityException e) {
            redirectAttributes.addFlashAttribute(PERMISSION_ERROR, true);
            return COURSES_REDIRECT;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(GENEREAL_ERROR, true);
            return COURSES_REDIRECT;
        }
    }

    @GetMapping("/{id}/detail")
    @PreAuthorize("hasAnyAuthority(" +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).SECRETARIAT, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).USER, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).COURSE_SCHEDULER, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).COURSE_SUPERVISOR, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).ADMIN) " +
            " or @securityService.isCourseManager(#id) or @securityService.isWorkplaceManager(#id)")
    public String detailCourse(Model model, @PathVariable long id,
                               RedirectAttributes redirectAttributes) {
        try {
            prepareForCourseDetail(model, id);
            return "details/course_detail";
        } catch (SecurityException e) {
            redirectAttributes.addFlashAttribute(PERMISSION_ERROR, true);
            return COURSES_REDIRECT;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(GENEREAL_ERROR, true);
            return COURSES_REDIRECT;
        }
    }

    @PostMapping("/{id}/edit")
    @PreAuthorize("hasAnyAuthority(" +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).SECRETARIAT, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).COURSE_SCHEDULER, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).COURSE_SUPERVISOR, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).ADMIN) " +
            " or @securityService.isCourseManager(#id) or @securityService.isWorkplaceManager(#id)")
    public String editCourse(@PathVariable long id,
                             @ModelAttribute CourseVO courseVO,
                             RedirectAttributes redirectAttributes) {
        try {
            courseService.editCourse(courseVO, id);
            return "redirect:/c/{id}/detail?edit=success";
        } catch (SecurityException e) {
            redirectAttributes.addFlashAttribute(PERMISSION_ERROR, true);
            return String.format(COURSE_DETAIL_REDIRECT, id);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(GENEREAL_ERROR, true);
            return String.format(COURSE_DETAIL_REDIRECT, id);
        }
    }

    @PostMapping("/{id}/delete")
    @PreAuthorize("hasAnyAuthority(" +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).SECRETARIAT, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).COURSE_SCHEDULER, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).COURSE_SUPERVISOR, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).ADMIN) " +
            " or @securityService.isCourseManager(#id) or @securityService.isWorkplaceManager(#id)")
    public String deleteCourse(@PathVariable long id,
                               RedirectAttributes redirectAttributes) {
        try {
            courseService.removeCourse(id);
            return "redirect:/c?delete=success";
        } catch (SecurityException e) {
            redirectAttributes.addFlashAttribute(PERMISSION_ERROR, true);
            return String.format(COURSE_DETAIL_REDIRECT, id);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(GENEREAL_ERROR, true);
            return String.format(COURSE_DETAIL_REDIRECT, id);
        }
    }

    @PostMapping("/{id}/employee/add")
    @PreAuthorize("hasAnyAuthority(" +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).SECRETARIAT, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).COURSE_SCHEDULER, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).COURSE_SUPERVISOR, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).ADMIN)")
    public String addSubordinate(Model model, @PathVariable long id, @ModelAttribute EmployeeVO userVO) {
        model.addAttribute("userVO", userVO);
        var course = courseService.getCourse(id);

        courseService.assignEmployee(userVO, course.getId());

        return "redirect:/c?addemployee=success";
    }

    private AllocationVO newCourseAllocation(Course course) {
        AllocationVO newAllocation = new AllocationVO();
        newAllocation.setCourseId(course.getId());
        newAllocation.setWorkerId(1);
        newAllocation.setRole("nový");
        newAllocation.setIsCertain(1.0F);
        newAllocation.setAllocationScope(1);
        newAllocation.setTerm("Z");
        newAllocation.setDateFrom(utils.convertToLocalDateTime(course.getDateFrom()).withMonth(9));
        newAllocation.setDateUntil(utils.convertToLocalDateTime(course.getDateUntil()).withMonth(2));
        newAllocation.setDescription("description");
        return newAllocation;
    }

    private List<List<AllocationVO>> mapAllocations(List<List<Allocation>> allocations) {
        List<List<AllocationVO>> listOfLists = new LinkedList<>();
        for (List<Allocation> list : allocations) {
            List<AllocationVO> allocationsVO = new LinkedList<>();
            for (Allocation allocation : list) {
                AllocationVO allocationVO = new AllocationVO();
                allocationVO.setId(allocation.getId());
                allocationVO.setCourseId(allocation.getCourse().getId());
                allocationVO.setWorkerId(allocation.getWorker().getId());
                allocationVO.setRole(allocation.getRole());
                allocationVO.setTerm(allocation.getTerm() != null ? allocation.getTerm().getValue() : TermState.N.getValue());
                allocationVO.setDateFrom(utils.convertToLocalDateTime(allocation.getDateFrom()));
                allocationVO.setDateUntil(utils.convertToLocalDateTime(allocation.getDateUntil()));
                allocationVO.setAllocationScope(allocation.getTime());
                allocationVO.setIsCertain(allocation.getIsCertain());
                allocationVO.setDescription(allocation.getDescription());
                allocationsVO.add(allocationVO);
            }
            listOfLists.add(allocationsVO);
        }

        return listOfLists;
    }

    private void prepareForCourseDetail(Model model, long id) {
        Course course = courseService.getCourse(id);

        CourseVO courseVO = new CourseVO()
                .name(course.getName())
                .shortcut(course.getShortcut())
                .courseManagerId(course.getCourseManager().getId())
                .courseManagerName(course.getCourseManager().getLastName())
                .courseWorkplace(course.getCourseWorkplace().getId())
                .dateFrom(course.getDateFrom())
                .dateUntil(course.getDateUntil())
                .introduced(new Date())
                .lectureLength(course.getLectureLength())
                .exerciseLength(course.getExerciseLength())
                .allocationsByYears(course.getAllocationsByYears())
                .numberOfStudents(course.getNumberOfStudents())
                .years(course.getYears());

        model.addAttribute(COURSE, courseVO);

        model.addAttribute("terms", TermState.values());
        model.addAttribute("newAllocation", newCourseAllocation(course));

        List<List<AllocationVO>> list = mapAllocations(course.getAllocationsByYears());

        model.addAttribute("allocationsByYears", list);

        var employees = employeeService.getEmployees();
        model.addAttribute(EMPLOYEES, employees);
        model.addAttribute(WORKPLACES, workplaceService.getWorkplaces());
        model.addAttribute("canEdit", courseService.canEditCourse(course.getId()));
        model.addAttribute("canDelete", courseService.canDeleteCourse(course.getId()));
        model.addAttribute("canCreateAllocation", courseService.canCreateAllocation(course.getId()));
        model.addAttribute("canEditAllocation", courseService.canDeleteAllocation(course.getId()));
        model.addAttribute("canDeleteAllocation", courseService.canDeleteAllocation(course.getId()));
    }
}

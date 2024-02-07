package cz.zcu.kiv.mjakubas.piae.sem.webapplication.controller.v1;

import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Allocation;
import cz.zcu.kiv.mjakubas.piae.sem.core.domain.AllocationCell;
import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Project;
import cz.zcu.kiv.mjakubas.piae.sem.core.domain.ProjectState;
import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Workplace;
import cz.zcu.kiv.mjakubas.piae.sem.core.exceptions.SecurityException;
import cz.zcu.kiv.mjakubas.piae.sem.core.service.MyUtils;
import cz.zcu.kiv.mjakubas.piae.sem.core.service.v1.AllocationService;
import cz.zcu.kiv.mjakubas.piae.sem.core.service.v1.EmployeeService;
import cz.zcu.kiv.mjakubas.piae.sem.core.service.v1.ProjectService;
import cz.zcu.kiv.mjakubas.piae.sem.core.service.v1.WorkplaceService;
import cz.zcu.kiv.mjakubas.piae.sem.core.vo.AllocationVO;
import cz.zcu.kiv.mjakubas.piae.sem.core.vo.EmployeeVO;
import cz.zcu.kiv.mjakubas.piae.sem.core.vo.ProjectVO;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * Contains all sites for working with projects.
 */
@Controller
@RequestMapping("/p")
@AllArgsConstructor
@PreAuthorize("hasAuthority(T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).SECRETARIAT)")
public class ProjectV1Controller {

    private final ProjectService projectService;
    private final EmployeeService employeeService;
    private final WorkplaceService workplaceService;
    private final AllocationService allocationService;
    private final MyUtils utils;

    private static final String EMPLOYEES = "employees";
    private static final String RESTRICTIONS = "restrictions";
    private static final String PROJECT = "project";
    private static final String PROJECTS_REDIRECT = "redirect:/p";
    private static final String PROJECT_DETAIL_REDIRECT = "redirect:/p/%s/detail";
    private static final String PERMISSION_ERROR = "permissionError";
    private static final String GENEREAL_ERROR = "generalError";

    @GetMapping()
    @PreAuthorize("hasAnyAuthority(" +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).SECRETARIAT, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).USER, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).PROJECT_ADMIN, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).ADMIN)")
    public String getProjects(Model model,
                              RedirectAttributes redirectAttributes) {
        try {
            var projects = projectService.getProjects();
            projects.forEach(project ->
                    project.getEmployees().addAll(projectService.getProjectEmployees(project.getId())));
            List<Allocation> firstAllocations = projectService.prepareFirst(projects);

            model.addAttribute("projects", projects);
            model.addAttribute("firstAllocations", firstAllocations);
            model.addAttribute("canCreateProject", projectService.canCreateProject());
            return "views/projects";
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
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).PROJECT_ADMIN, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).ADMIN)")
    public String createProject(Model model) {
        model.addAttribute(PROJECT, newProject());

        var projects = projectService.getProjects();
        var employees = employeeService.getEmployees();
        var workplaces = workplaceService.getWorkplaces();

        model.addAttribute("states", ProjectState.values());

        model.addAttribute(EMPLOYEES, employees);
        model.addAttribute("workplaces", workplaces);
        model.addAttribute(RESTRICTIONS, projects);

        return "forms/project/create_project_form";
    }

    @PostMapping("/create")
    @PreAuthorize("hasAnyAuthority(" +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).SECRETARIAT, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).PROJECT_ADMIN, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).ADMIN)")
    public String createProject(@ModelAttribute ProjectVO projectVO,
                                RedirectAttributes redirectAttributes) {
        try {
            long id = projectService.createProject(projectVO);
            redirectAttributes.addAttribute("id", id);
            return "redirect:/p/{id}/detail?create=success";
        } catch (SecurityException e) {
            redirectAttributes.addFlashAttribute(PERMISSION_ERROR, true);
            return PROJECTS_REDIRECT;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(GENEREAL_ERROR, true);
            return PROJECTS_REDIRECT;
        }
    }

    @PreAuthorize("hasAnyAuthority(" +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).SECRETARIAT, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).USER, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).PROJECT_ADMIN, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).ADMIN) " +
            " or @securityService.isProjectManager(#id) or @securityService.isWorkplaceManager(#id)")
    @GetMapping("/{id}/detail")
    public String detailProject(Model model, @PathVariable long id,
                                RedirectAttributes redirectAttributes) {
        try {
            prepareForProjectDetail(model, id);
            return "details/project_detail";
        } catch (SecurityException e) {
            redirectAttributes.addFlashAttribute(PERMISSION_ERROR, true);
            return PROJECTS_REDIRECT;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(GENEREAL_ERROR, true);
            return PROJECTS_REDIRECT;
        }
    }

    @PreAuthorize("hasAnyAuthority(" +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).SECRETARIAT, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).PROJECT_ADMIN, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).ADMIN) " +
            " or @securityService.isProjectManager(#id) or @securityService.isWorkplaceManager(#id)")
    @PostMapping("/{id}/edit")
    public String editProject(@PathVariable long id, @ModelAttribute ProjectVO projectVO,
                              RedirectAttributes redirectAttributes) {
        try {
            projectService.editProject(projectVO, id);
            return "redirect:/p/{id}/detail?edit=success";
        } catch (SecurityException e) {
            redirectAttributes.addFlashAttribute(PERMISSION_ERROR, true);
            return String.format(PROJECT_DETAIL_REDIRECT, id);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(GENEREAL_ERROR, true);
            return PROJECTS_REDIRECT;
        }
    }

    @PostMapping("/{id}/delete")
    @PreAuthorize("hasAnyAuthority(" +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).SECRETARIAT, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).PROJECT_ADMIN, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).ADMIN) " +
            " or @securityService.isProjectManager(#id) or @securityService.isWorkplaceManager(#id)")
    public String deleteProject(RedirectAttributes redirectAttributes, @PathVariable long id) {
        try {
            projectService.removeProject(id);
            return "redirect:/p?delete=success";
        } catch (SecurityException e) {
            redirectAttributes.addFlashAttribute(PERMISSION_ERROR, true);
            return String.format(PROJECT_DETAIL_REDIRECT, id);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(GENEREAL_ERROR, true);
            return String.format(PROJECT_DETAIL_REDIRECT, id);
        }
    }

    @PostMapping("/{id}/employee/add")
    @PreAuthorize("hasAnyAuthority(" +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).SECRETARIAT, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).PROJECT_ADMIN, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).ADMIN)")
    public String addSubordinate(Model model, @PathVariable long id, @ModelAttribute EmployeeVO userVO) {
        model.addAttribute("userVO", userVO);
        var project = projectService.getProject(id);

        /* it is what it is... */
        String str = userVO.getOrionLogin();
        String orion = str.substring(str.indexOf("(") + 1, str.indexOf(")"));
        userVO.setOrionLogin(orion);

        projectService.assignEmployee(userVO, project.getId());

        return "redirect:/p?addemployee=success";
    }

    @PreAuthorize("hasAnyAuthority(" +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).SECRETARIAT, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).PROJECT_ADMIN, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).ADMIN) " +
            " or @securityService.isProjectManager(#id) or @securityService.isWorkplaceManager(#id)")
    @GetMapping("/{id}/manage")
    public String manageProject(Model model, @PathVariable long id) {
        var project = projectService.getProject(id);
        project.getEmployees().addAll(projectService.getProjectEmployees(project.getId()));

        var payload = allocationService.getProjectAllocations(id);
        model.addAttribute("allocations", payload.getAllocations());
        model.addAttribute("projects", payload.getAssignmentsProjects());
        model.addAttribute(EMPLOYEES, payload.getAllocationsEmployees());

        model.addAttribute(PROJECT, project);
        return "views/project_management";
    }

    private ProjectVO newProject() {
        ProjectVO projectVO = new ProjectVO();
        projectVO.setState(ProjectState.NOVY.getValue());
        projectVO.setName("Nový projekt");
        projectVO.setDateFrom(utils.convertToDate(LocalDate.now()));
        projectVO.setDateUntil(utils.convertToDate(LocalDate.now().plusYears(1)));
        return projectVO;
    }

    private AllocationVO newProjectAllocation(Project project) {
        AllocationVO newAllocation = new AllocationVO();
        newAllocation.setProjectId(project.getId());
        newAllocation.setWorkerId(1);
        newAllocation.setRole("nový");
        newAllocation.setIsCertain(1.0F);
        newAllocation.setAllocationScope(1);
        newAllocation.setDateFrom(utils.convertToLocalDateTime(project.getDateFrom()));
        newAllocation.setDateUntil(utils.convertToLocalDateTime(project.getDateUntil()));
        newAllocation.setDescription("description");

        return newAllocation;
    }

    private List<AllocationVO> mapAllocations(List<Allocation> allocations) {
        List<AllocationVO> allocationsVO = new LinkedList<>();
        for (Allocation allocation : allocations) {
            AllocationVO allocationVO = new AllocationVO();
            allocationVO.setId(allocation.getId());
            allocationVO.setProjectId(allocation.getProject().getId());
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

    private void prepareForProjectDetail(Model model, long id) {
        Project project = projectService.getProject(id);
        var allocations = allocationService.getProjectAllocations(id).getAllocations();
        model.addAttribute("allocations", mapAllocations(allocations));
        model.addAttribute("newAllocation", newProjectAllocation(project));
        model.addAttribute("states", ProjectState.values());

        var employees = employeeService.getEmployees();
        employees.forEach(employee ->
                employee.getSubordinates().addAll(employeeService.getSubordinates(employee.getId())));
        List<List<AllocationCell>> allList = projectService.prepareProjectsCells(allocations);
        List<AllocationCell> certainList =
                !allList.isEmpty() ? new java.util.ArrayList<>(Collections.nCopies(allList.get(0).size(), new AllocationCell())) : new LinkedList<>();
        List<AllocationCell> uncertainList =
                !allList.isEmpty() ? new java.util.ArrayList<>(Collections.nCopies(allList.get(0).size(), new AllocationCell())) : new LinkedList<>();
        if (!allList.isEmpty()) {
            projectService.prepareProjectsCells(allList, certainList, uncertainList);
        }

        if (certainList.isEmpty()) {
            certainList = Collections.nCopies(12, new AllocationCell(0, 1));
        }

        if (uncertainList.isEmpty()) {
            uncertainList = Collections.nCopies(12, new AllocationCell(0, 1));
        }


        model.addAttribute("certainAllocations", certainList);
        model.addAttribute("uncertainAllocations", uncertainList);
        model.addAttribute("monthlyAllocations", allList);

        allocations.sort(Comparator.comparing(Allocation::getDateFrom));

        ProjectVO projectVO = new ProjectVO()
                .name(project.getName())
                .projectManagerId(project.getProjectManager().getId())
                .projectManagerName(project.getProjectManager().getLastName())
                .workplaceId(project.getProjectWorkplace().getId())
                .dateFrom(project.getDateFrom())
                .dateUntil(project.getDateUntil())
                .description(project.getDescription())
                .budget(project.getBudget())
                .probability(project.getProbability())
                .budgetParticipation(project.getBudgetParticipation())
                .totalTime(project.getTotalTime())
                .agency(project.getAgency())
                .grantTitle(project.getGrantTitle())
                .firstYear(
                        allocations.isEmpty() ? utils.convertToLocalDateTime(project.getDateFrom()).getYear() :
                                utils.convertToLocalDateTime(allocations.get(0).getDateFrom()).getYear())
                .state(project.getState().getValue());
        model.addAttribute(PROJECT, projectVO);

        model.addAttribute(EMPLOYEES, employees);
        List<Workplace> workplaces = workplaceService.getWorkplaces();
        model.addAttribute("workplaces", workplaces);
        model.addAttribute("canEdit", projectService.canEditProject(project.getId()));
        model.addAttribute("canDelete", projectService.canDeleteProject(project.getId()));
        model.addAttribute("canCreateAllocation", projectService.canCreateAllocation(project.getId()));
        model.addAttribute("canEditAllocation", projectService.canEditAllocation(project.getId()));
        model.addAttribute("canDeleteAllocation", projectService.canDeleteAllocation(project.getId()));
    }
}

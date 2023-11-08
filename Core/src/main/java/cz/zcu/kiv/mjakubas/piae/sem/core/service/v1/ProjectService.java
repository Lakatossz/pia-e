package cz.zcu.kiv.mjakubas.piae.sem.core.service.v1;

import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Employee;
import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Project;
import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Workplace;
import cz.zcu.kiv.mjakubas.piae.sem.core.repository.IProjectRepository;
import cz.zcu.kiv.mjakubas.piae.sem.core.service.ServiceException;
import cz.zcu.kiv.mjakubas.piae.sem.core.vo.EmployeeVO;
import cz.zcu.kiv.mjakubas.piae.sem.core.vo.ProjectVO;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;

/**
 * Services for working with projects.
 */
@Service
@Transactional(readOnly = true)
@AllArgsConstructor
public class ProjectService {

    private final IProjectRepository projectRepository;
    private final EmployeeService employeeService;
    private final WorkplaceService workplaceService;

    private final AllocationService allocationService;


    /**
     * Gets project by its id. Throws SQL error if project doesn't exists.
     *
     * @param id project id
     * @return project
     */
    public Project getProject(long id) {
        return projectRepository.fetchProject(id);
    }

    /**
     * Gets all projects. Throws SQL error if something happens.
     *
     * @return list of {@link Project}
     */
    public List<Project> getProjects() {
        return projectRepository.fetchProjects();
    }

    /**
     * Gets all project employees. Throws SQL error if project doesn't exist.
     *
     * @param id project id
     * @return list of project {@link Employee}
     */
    public List<Employee> getProjectEmployees(long id) {
        return projectRepository.fetchProjectEmployees(id);
    }

    /**
     * Gets all projects of workplace manager. Throws SQL error if manager doesnt exist.
     *
     * @param id manager id
     * @return list of workplace {@link Project}
     */
    public List<Project> getWorkplaceManagerProjects(long id) {
        var workplaces = workplaceService.getWorkplaces();
        var myWorkplaces = new ArrayList<Workplace>();

        for (Workplace w : workplaces) {
            if (w.getManager().getId() == id)
                myWorkplaces.add(w);
        }

        var projects = projectRepository.fetchProjects();
        var myProjects = new ArrayList<Project>();
        for (Project p : projects) {
            for (Workplace myW : myWorkplaces) {
                if (p.getProjectWorkplace().getId() == myW.getId())
                    myProjects.add(p);
            }
        }

        return myProjects;
    }

    /**
     * Creates new project. All {@link ProjectVO} data must be available.
     * Might throw SQL or Service exception if data validation fails.
     *
     * @param projectVO projectVO
     */
    @Transactional
    public void createProject(@NonNull ProjectVO projectVO) {
        var data = employeeService.getEmployee(projectVO.getProjectManagerOrionLogin());
        if (projectVO.getDateValidUntil() != null) {
            if (projectVO.getDateValidFrom().isAfter(projectVO.getDateValidUntil()))
                throw new ServiceException();
        }

        Project project = Project.builder()
                .projectName(projectVO.getProjectName())
                .projectManager(Employee.builder().id(data.getId()).build())
                .projectWorkplace(Workplace.builder().id(projectVO.getWorkplaceId()).build())
                .validFrom(projectVO.getDateValidFrom())
                .validUntil(projectVO.getDateValidUntil() != null ? projectVO.getDateValidUntil() : LocalDate.of(9999, 9, 9))
                .description(projectVO.getDescription())
                .build();

        if (!projectRepository.createProject(project))
            throw new ServiceException();
    }

    /**
     * Updates existing project. Throws SQL or Service exception if project doesn't exists or data validation fails.
     *
     * @param projectVO project data
     * @param id        project id
     */
    @Transactional
    public void editProject(@NonNull ProjectVO projectVO, long id) {
        var data = employeeService.getEmployee(projectVO.getProjectManagerOrionLogin());
        if (projectVO.getDateValidUntil() != null) {
            if (projectVO.getDateValidFrom().isAfter(projectVO.getDateValidUntil()))
                throw new ServiceException();
        }
        var processed = allocationService.processAllocations(allocationService.getProjectAllocations(id).getAllocations());
        if (processed.size() > 0) {
            if (processed.get(0).getFrom().isBefore(projectVO.getDateValidFrom())
                    || processed.get(processed.size() - 1).getUntil().isAfter(projectVO.getDateValidUntil()))
                throw new ServiceException();
        }


        Project project = Project.builder()
                .id(id)
                .projectName(projectVO.getProjectName())
                .projectManager(Employee.builder().id(data.getId()).build())
                .projectWorkplace(Workplace.builder().id(projectVO.getWorkplaceId()).build())
                .validFrom(projectVO.getDateValidFrom())
                .validUntil(projectVO.getDateValidUntil() != null ? projectVO.getDateValidUntil() : LocalDate.of(9999, 9, 9))
                .description(projectVO.getDescription())
                .build();

        if (!projectRepository.updateProject(project, id))
            throw new ServiceException();
    }

    /**
     * Assigns employee to a project.
     *
     * @param userVO user data
     * @param id     project id
     */
    @Transactional
    public void assignEmployee(@NonNull EmployeeVO userVO, long id) {
        var legitId = employeeService.getEmployee(userVO.getOrionLogin()).getId();

        var employees = projectRepository.fetchProjectEmployees(id);
        var check = new HashSet<Long>();
        for (Employee e : employees) {
            check.add(e.getId());
        }
        if (check.contains(legitId))
            throw new ServiceException();

        if (!projectRepository.addEmployee(legitId, id))
            throw new ServiceException();
    }

    /**
     * Gets all projects of a project manager.
     *
     * @param employeeId project manager id
     * @return list of {@link Project}
     */
    public List<Project> getManagerProjects(long employeeId) {
        var projects = projectRepository.fetchProjects();
        var myProjects = new ArrayList<Project>();
        projects.forEach(project -> {
            if (project.getProjectManager().getId() == employeeId)
                myProjects.add(project);
        });
        return myProjects;
    }
}

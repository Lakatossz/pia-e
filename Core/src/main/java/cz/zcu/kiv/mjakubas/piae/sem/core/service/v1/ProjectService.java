package cz.zcu.kiv.mjakubas.piae.sem.core.service.v1;

import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Allocation;
import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Employee;
import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Project;
import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Workplace;
import cz.zcu.kiv.mjakubas.piae.sem.core.repository.IEmployeeRepository;
import cz.zcu.kiv.mjakubas.piae.sem.core.repository.IProjectRepository;
import cz.zcu.kiv.mjakubas.piae.sem.core.repository.IWorkplaceRepository;
import cz.zcu.kiv.mjakubas.piae.sem.core.service.ServiceException;
import cz.zcu.kiv.mjakubas.piae.sem.core.vo.EmployeeVO;
import cz.zcu.kiv.mjakubas.piae.sem.core.vo.ProjectVO;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

/**
 * Services for working with projects.
 */
@Service
@Transactional(readOnly = true)
@AllArgsConstructor
public class ProjectService {

    private final IProjectRepository projectRepository;
    private final IEmployeeRepository employeeRepository;
    private final IWorkplaceRepository workplaceRepository;

    private final AllocationService allocationService;


    /**
     * Gets project by its id. Throws SQL error if project doesn't exist.
     *
     * @param id project id
     * @return project
     */
    public Project getProject(long id) {
        Project project = projectRepository.fetchProject(id);
        List<Allocation> allocations = allocationService.getProjectAllocations(id).getAllocations();
        if (!allocations.isEmpty())
            project.setYearAllocation(prepareAllocations(allocations));
        return project;
    }

    /**
     * Gets all projects. Throws SQL error if something happens.
     *
     * @return list of {@link Project}
     */
    public List<Project> getProjects() {
        List<Project> projects = projectRepository.fetchProjects();
        projects.forEach(project -> {
                List<Allocation> allocations = allocationService.getProjectAllocations(project.getId()).getAllocations();
                if (!allocations.isEmpty())
                    project.setYearAllocation(prepareAllocations(allocations));
        });
        return projectRepository.fetchProjects();
    }

    /**
     * Gets all project employees. Throws SQL error if project doesn't exist.
     *
     * @param id project id
     * @return list of project {@link Employee}
     */
    public List<Employee> getProjectEmployees(long id) {
        List<Employee> employees = projectRepository.fetchProjectEmployees(id);
        employees.forEach(employee -> {
            employee.setUncertainTime((float) 0.0);
            employee.setCertainTime((float) 0.0);
        });
        return employees;
    }

    /**
     * Gets all projects of workplace manager. Throws SQL error if manager doesn't exist.
     *
     * @param id manager id
     * @return list of workplace {@link Project}
     */
    public List<Project> getWorkplaceManagerProjects(long id) {
        var workplaces = workplaceRepository.fetchWorkplaces();
        var myWorkplaces = new ArrayList<Workplace>();

        for (Workplace w : workplaces) {
            if (w.getManager().getId() == id)
                myWorkplaces.add(w);
        }

        var projects = projectRepository.fetchProjects();
        var myProjects = new ArrayList<Project>();
        for (Project p : projects) {
            for (Workplace myW : myWorkplaces) {
                if (p.getProjectWorkplace().getId().equals(myW.getId()))
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
        var manager = employeeRepository.fetchEmployee(projectVO.getProjectManagerId());
        if (projectVO.getDateUntil() != null && (projectVO.getDateFrom().isAfter(projectVO.getDateUntil())))
                {throw new ServiceException();
        }

        Project project = new Project()
                .name(projectVO.getName())
                .dateFrom(projectVO.getDateFrom())
                .dateUntil(projectVO.getDateUntil() != null ? projectVO.getDateUntil() : LocalDate.of(9999, 9, 9))
                .probability(projectVO.getProbability())
                .projectManager(manager)
                .projectWorkplace(Workplace.builder().id(projectVO.getWorkplaceId()).build())
                .description(projectVO.getDescription())
                .budget(projectVO.getBudget())
                .participation(projectVO.getParticipation())
                .totalTime(projectVO.getTotalTime());

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
        var manager = employeeRepository.fetchEmployee(projectVO.getProjectManagerId());
        if (projectVO.getDateUntil() != null && (projectVO.getDateFrom().isAfter(projectVO.getDateUntil())))
                {throw new ServiceException();
        }
        var processed = allocationService.processAllocations(allocationService.getProjectAllocations(id).getAllocations());
        if (!processed.isEmpty() && (processed.get(0).getFrom().isBefore(projectVO.getDateFrom())
                    || processed.get(processed.size() - 1).getUntil().isAfter(projectVO.getDateUntil())))
                {throw new ServiceException();
        }


        Project project = new Project()
                .id(id)
                .name(projectVO.getName())
                .dateFrom(projectVO.getDateFrom())
                .dateUntil(projectVO.getDateUntil() != null ? projectVO.getDateUntil() : LocalDate.of(9999, 9, 9))
                .probability(projectVO.getProbability())
                .projectManager(manager)
                .projectWorkplace(Workplace.builder().id(projectVO.getWorkplaceId()).build())
                .description(projectVO.getDescription())
                .budget(projectVO.getBudget())
                .participation(projectVO.getParticipation())
                .totalTime(projectVO.getTotalTime());

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
        var legitId = employeeRepository.fetchEmployee(userVO.getOrionLogin()).getId();

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
     * Gets all projects of a project employee.
     *
     * @param employeeId project employee id
     * @return list of {@link Project}
     */
    public List<Project> getEmployeeProjects(long employeeId) {
        var projects = projectRepository.fetchProjects();
        var myProjects = new ArrayList<Project>();
        projects.forEach(project -> {
            project.setYearAllocation(
                    prepareAllocations(allocationService.getProjectAllocations(project.getId()).getAllocations()));
            if (project.getEmployees().stream().filter(employee -> employee.getId() == employeeId).toList().size() == 1)
                myProjects.add(project);
        });

        System.out.println("Pocet projektu: " + myProjects.size());

        return myProjects;
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
            project.setYearAllocation(
                    prepareAllocations(allocationService.getProjectAllocations(project.getId()).getAllocations()));
            if (project.getProjectManager().getId() == employeeId)
                myProjects.add(project);
        });
        return myProjects;
    }

    /**
     * Prepares allocations times for using.
     * @param allocations allocations
     * @return list of allocations
     */
    private List<Float> prepareAllocations(List<Allocation> allocations) {
        List<Float> yearAllocations = new ArrayList<>(Collections.nCopies(12, (float) 0));

        List<Allocation> thisYearsAllocations = new ArrayList<>();
        allocations.forEach(allocation -> {
            if (isThisYearAllocation(allocation))
                thisYearsAllocations.add(allocation);
        });

        int allocationsIndex = 0;

        if (!thisYearsAllocations.isEmpty()) {
            Allocation allocation = thisYearsAllocations.get(allocationsIndex);

//        Here I will go through every month of the year and add to list 0 or time for project.
            for (int i = 1; i < 13; i++) {
                if ((allocation.getDateFrom().getMonthValue() <= i
                        && allocation.getDateUntil().getMonthValue() >= i)
                        || (i == 1 && isThisYearAllocation(allocation)
                        && allocation.getDateFrom().getYear() < LocalDate.now().getYear())
                        || (i == 12 && isThisYearAllocation(allocation)
                        && allocation.getDateUntil().getYear() > LocalDate.now().getYear())) {
                    yearAllocations.set(i - 1, allocation.getTime());
                }
                else {
                    if (allocation.getDateUntil().getMonthValue() == i)
                        allocation = thisYearsAllocations.get(allocationsIndex++);
                    else
                        yearAllocations.set(i - 1, (float) 0);
                }
            }
            return yearAllocations;
        }

        return new ArrayList<>();
    }

    private boolean isThisYearAllocation(Allocation allocation) {
        return allocation.getDateFrom().getYear() == LocalDate.now().getYear()
                || allocation.getDateUntil().getYear() == LocalDate.now().getYear();
    }
}

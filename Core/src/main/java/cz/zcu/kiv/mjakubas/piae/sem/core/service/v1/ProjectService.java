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

import java.time.Instant;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.LinkedList;
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
        if (!allocations.isEmpty()) {
            project.setYearAllocation(prepareAllocations(allocations));
            project.setProjectAllocations(allocations);
            project.setEmployees(projectRepository.fetchProjectEmployees(id));
        }
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
                if (!allocations.isEmpty()) {
                    project.setYearAllocation(prepareAllocations(allocations));
                    project.setProjectAllocations(allocations);
                    project.setEmployees(projectRepository.fetchProjectEmployees(project.getId()));
                }
        });
        return projects;
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
        var myWorkplaces = new LinkedList<Workplace>();

        for (Workplace w : workplaces) {
            if (w.getManager().getId() == id)
                myWorkplaces.add(w);
        }

        var projects = projectRepository.fetchProjects();
        var myProjects = new LinkedList<Project>();
        for (Project p : projects) {
            for (Workplace myW : myWorkplaces) {
                if (p.getProjectWorkplace().getId() != null && p.getProjectWorkplace().getId().equals(myW.getId()))
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
        if (projectVO.getDateUntil() != null && (projectVO.getDateFrom().after(projectVO.getDateUntil())))
                {throw new ServiceException();
        }

        Project project = new Project()
                .name(projectVO.getName())
                .dateFrom(projectVO.getDateFrom())
                .dateUntil(projectVO.getDateUntil() != null ? projectVO.getDateUntil() : Date.from(
                        Instant.from(LocalDate.of(9999, 9, 9))))
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
        if (projectVO.getDateUntil() != null && (projectVO.getDateFrom().after(projectVO.getDateUntil())))
                {throw new ServiceException();
        }
        var processed = allocationService.processAllocations(allocationService.getProjectAllocations(id).getAllocations());
        if (!processed.isEmpty() && (processed.get(0).getFrom().before(projectVO.getDateFrom())
                    || processed.get(processed.size() - 1).getUntil().after(projectVO.getDateUntil())))
                {throw new ServiceException();
        }

        Project project = new Project()
                .id(id)
                .name(projectVO.getName())
                .dateFrom(projectVO.getDateFrom())
                .dateUntil(projectVO.getDateUntil() != null ? projectVO.getDateUntil() : Date.from(
                        Instant.from(LocalDate.of(9999, 9, 9))))
                .probability(projectVO.getProbability())
                .projectManager(manager)
                .projectWorkplace(Workplace.builder().id(projectVO.getWorkplaceId()).build())
                .description(projectVO.getDescription())
                .budget(projectVO.getBudget())
                .budgetParticipation(projectVO.getBudgetParticipation())
                .participation(projectVO.getParticipation())
                .totalTime(projectVO.getTotalTime())
                .agency(projectVO.getAgency())
                .grantTitle(projectVO.getGrantTitle());

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
        var myProjects = new LinkedList<Project>();
        projects.forEach(project -> {
            project.setEmployees(projectRepository.fetchProjectEmployees(project.getId()));
            project.setYearAllocation(
                    prepareAllocations(allocationService.getProjectAllocations(project.getId()).getAllocations()));
            if (project.getEmployees().stream().filter(employee -> employee.getId() == employeeId).toList().size() == 1)
                myProjects.add(project);
            project.setProjectAllocations(new LinkedList<>(allocationService.getProjectAllocations(project.getId()).getAllocations()
                    .stream().filter(allocation -> allocation.getWorker().getId() == employeeId).toList()));
        });

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
        var myProjects = new LinkedList<Project>();
        projects.forEach(project -> {
            project.setYearAllocation(
                    prepareAllocations(allocationService.getProjectAllocations(project.getId()).getAllocations()));
            if (project.getProjectManager().getId() == employeeId)
                myProjects.add(project);
        });
        return myProjects;
    }

    public List<Allocation> prepareFirst(List<Project> projects) {
        List<Allocation> firstAllocations = new LinkedList<>();
        projects.forEach(project -> {
            switch(project.getProjectAllocations().size()) {
                case 0: {
                    firstAllocations.add(new Allocation().time(-1));
                    project.projectAllocations(new LinkedList<>());
                    break;
                }
                case 1: {
                    firstAllocations.add(project.getProjectAllocations().get(0));
                    project.projectAllocations(new LinkedList<>());
                    break;
                }
                default: {
                    firstAllocations.add(project.getProjectAllocations().remove(0));
                    project.projectAllocations(project.getProjectAllocations());
                    break;
                }
            }
        });

        return firstAllocations;
    }

    public List<Float> averageAllocation(Project project, List<Allocation> projectAllocations) {
        List<Float> averages = new LinkedList<>();



        return averages;
    }

    /**
     * Prepares allocations times for using.
     * @param allocations allocations
     * @return list of allocations
     */
    private List<Float> prepareAllocations(List<Allocation> allocations) {
        List<Float> yearAllocations = new LinkedList<>(Collections.nCopies(12, (float) 0));

        List<Allocation> thisYearsAllocations = new LinkedList<>();
        allocations.forEach(allocation -> {
            if (isThisYearAllocation(allocation))
                thisYearsAllocations.add(allocation);
        });

        int allocationsIndex = 0;

        Calendar dateFrom = new GregorianCalendar();
        Calendar dateUntil = new GregorianCalendar();

        if (!thisYearsAllocations.isEmpty()) {
            Allocation allocation = thisYearsAllocations.get(allocationsIndex);

            dateFrom.setTime(allocation.getDateFrom());
            dateUntil.setTime(allocation.getDateUntil());

//        Here I will go through every month of the year and add to list 0 or time for project.
            for (int i = 1; i < 13; i++) {
                if ((dateFrom.get(Calendar.MONTH) <= i
                        && dateUntil.get(Calendar.MONTH) >= i)
                        || (i == 1 && isThisYearAllocation(allocation)
                        && dateFrom.get(Calendar.YEAR) < LocalDate.now().getYear())
                        || (i == 12 && isThisYearAllocation(allocation)
                        && dateUntil.get(Calendar.YEAR) > LocalDate.now().getYear())) {
                    yearAllocations.set(i - 1, allocation.getTime());
                }
                else {
                    if (dateUntil.get(Calendar.MONTH) == i)
                        allocation = thisYearsAllocations.get(allocationsIndex++);
                    else
                        yearAllocations.set(i - 1, (float) 0);
                }
            }
            return yearAllocations;
        }

        return new LinkedList<>();
    }

    private boolean isThisYearAllocation(Allocation allocation) {
        Calendar dateFrom = new GregorianCalendar();
        Calendar dateUntil = new GregorianCalendar();
        dateFrom.setTime(allocation.getDateFrom());
        dateUntil.setTime(allocation.getDateUntil());
        return dateFrom.get(Calendar.YEAR) == LocalDate.now().getYear()
                || dateUntil.get(Calendar.YEAR) == LocalDate.now().getYear();
    }
}

package cz.zcu.kiv.mjakubas.piae.sem.core.service.v1;

import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Allocation;
import cz.zcu.kiv.mjakubas.piae.sem.core.domain.AllocationCell;
import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Employee;
import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Project;
import cz.zcu.kiv.mjakubas.piae.sem.core.domain.ProjectState;
import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Workplace;
import cz.zcu.kiv.mjakubas.piae.sem.core.repository.IEmployeeRepository;
import cz.zcu.kiv.mjakubas.piae.sem.core.repository.IProjectRepository;
import cz.zcu.kiv.mjakubas.piae.sem.core.repository.IWorkplaceRepository;
import cz.zcu.kiv.mjakubas.piae.sem.core.service.ServiceException;
import cz.zcu.kiv.mjakubas.piae.sem.core.vo.EmployeeVO;
import cz.zcu.kiv.mjakubas.piae.sem.core.vo.ProjectVO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

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
    public long createProject(@NonNull ProjectVO projectVO) {
        var manager = employeeRepository.fetchEmployee(projectVO.getProjectManagerId());
        if (projectVO.getDateUntil() != null && (projectVO.getDateFrom().after(projectVO.getDateUntil())))
                {throw new ServiceException();
        }

        Project project = new Project()
                .name(projectVO.getName())
                .shortcut(projectVO.getShortcut())
                .dateFrom(projectVO.getDateFrom())
                .dateUntil(projectVO.getDateUntil() != null ? projectVO.getDateUntil() : Date.from(
                        Instant.from(LocalDate.of(9999, 9, 9))))
                .probability(projectVO.getProbability())
                .projectManager(manager)
                .projectWorkplace(Workplace.builder().id(projectVO.getWorkplaceId()).build())
                .description(projectVO.getDescription())
                .budget(projectVO.getBudget())
                .budgetParticipation(projectVO.getBudgetParticipation())
                .agency(projectVO.getAgency())
                .grantTitle(projectVO.getGrantTitle())
                .state(ProjectState.valueOf(projectVO.getState()))
                .totalTime(projectVO.getTotalTime());

        long id = projectRepository.createProject(project);

        if (id > 0)
            return id;
        else
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
                .shortcut(projectVO.getShortcut())
                .dateFrom(projectVO.getDateFrom())
                .dateUntil(projectVO.getDateUntil() != null ? projectVO.getDateUntil() : Date.from(
                        Instant.from(LocalDate.of(9999, 9, 9))))
                .probability(projectVO.getProbability())
                .projectManager(manager)
                .projectWorkplace(Workplace.builder().id(projectVO.getWorkplaceId()).build())
                .description(projectVO.getDescription())
                .budget(projectVO.getBudget())
                .budgetParticipation(projectVO.getBudgetParticipation())
                .totalTime(projectVO.getTotalTime())
                .agency(projectVO.getAgency())
                .state(ProjectState.getByValue(projectVO.getState()))
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

    /**
     * Nastavi bunky pro alokace na projektu pro detail projektu.
     * Oproti predchozi metode nastavuje pro vsechny zamestnance u projektu.
     * @param projectAllocations Alokace na projekt
     */
    public List<List<AllocationCell>>  prepareProjectsCells(List<Allocation> projectAllocations) {
        List<MergingObject> allocationsByProjectAndRole = splitByRoleAndEmployeeId(projectAllocations, Allocation::getWorker);

        return prepareActivityCells(allocationsByProjectAndRole);
    }

    public List<AllocationCell>  prepareCertainProjectsCells(List<List<AllocationCell>> list) {
        List<AllocationCell> cellList = new java.util.ArrayList<>(Collections.nCopies(list.get(0).size(), new AllocationCell()));

        for (List<AllocationCell> l : list) {
            for (int i = 0; i < l.size(); ++i) {
                if (l.get(i).getCertain() == 1) {
                    cellList.set(i, new AllocationCell(cellList.get(i).getTime() + l.get(i).getTime(), (float) 1.0));
                }
            }
        }

        return cellList;
    }

    public List<AllocationCell>  prepareUncertainProjectsCells(List<List<AllocationCell>> list) {
        List<AllocationCell> cellList = new java.util.ArrayList<>(Collections.nCopies(list.get(0).size(), new AllocationCell()));

        for (List<AllocationCell> l : list) {
            for (int i = 0; i < l.size(); ++i) {
                if (l.get(i).getCertain() != 1) {
                    cellList.set(i, new AllocationCell(cellList.get(i).getTime() + l.get(i).getTime(), (float) 1.0));
                }
            }
        }

        return cellList;
    }

    /**
     * Projdu vsechny alokace a spojim ty se stejnou roli a projektem
     * @param current
     * @param attributeSelector
     * @return
     */
    private List<MergingObject> splitByRoleAndEmployeeId(List<Allocation> current,
                                                                         Function<Allocation, Employee> attributeSelector) {
        List<MergingObject> allocationsByActivityAndRole = new LinkedList<>();

        for (Allocation allocation : current) {
            List<MergingObject> list = allocationsByActivityAndRole.stream().filter(
                    o -> o.getEmployeeId() == attributeSelector.apply(allocation).getId()
                            && o.getRole().equals(allocation.getRole())).toList();
            if (!list.isEmpty()) {
                MergingObject currentObject = list.get(0);
                int index = allocationsByActivityAndRole.indexOf(currentObject);
                if (index != -1) {
                    allocationsByActivityAndRole.get(index).allocations(allocation);
                    continue;
                }
            }

            allocationsByActivityAndRole.add(new MergingObject(allocation.getRole(),
                    attributeSelector.apply(allocation).getId(), allocation));
        }

//        Seradim spojene alokace podle pocatecniho datumu
        allocationsByActivityAndRole.forEach(MergingObject::sortAllocationsByDate);

        return allocationsByActivityAndRole;
    }

    public LocalDate convertToLocalDateTime(Date dateToConvert) {
        return dateToConvert.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
    }

    private void addAllocationsPerMonth(Allocation allocation,
                                        List<AllocationCell> cellsList,
                                        int firstYear) {
        Date dateFrom = allocation.getDateFrom();
        Date dateUntil = allocation.getDateUntil();
        int yearsDiff = Period.between(convertToLocalDateTime(dateFrom), convertToLocalDateTime(dateUntil)).getYears();
        int monthsDiff = yearsDiff * 12 + Period.between(convertToLocalDateTime(dateFrom), convertToLocalDateTime(dateUntil)).getMonths() + 1; // leden - leden je porad 1 mesic xD

        Calendar date = new GregorianCalendar();
        date.setTime(dateFrom);
        int startIndex = (date.get(Calendar.YEAR) - firstYear) * 12 + date.get(Calendar.MONTH);

        for (int i = 0; i < monthsDiff; ++i) {
            cellsList.set(startIndex + i, new AllocationCell(allocation.getTime(), allocation.getIsCertain()));
        }
    }

    private List<List<AllocationCell>> prepareAllocationCells(List<MergingObject> allocationsByActivityAndRole,
                                                              int totalFirstYear,
                                                              int totalNumberOfYears) {
        List<List<AllocationCell>> list = new LinkedList<>();
        for (MergingObject object : allocationsByActivityAndRole) {
//            Pripravim cele pole
            int cellsSize = (12 * totalNumberOfYears);
            List<AllocationCell> cellsList = new java.util.ArrayList<>(Collections.nCopies(cellsSize, new AllocationCell()));
            object.allocations.forEach(allocation -> addAllocationsPerMonth(allocation, cellsList, totalFirstYear));
            list.add(cellsList);
        }

        return list;
    }

    public List<List<AllocationCell>> prepareActivityCells(List<MergingObject> allocationsByActivityAndRole) {

        List<List<AllocationCell>> list = new ArrayList<>();

        if (!allocationsByActivityAndRole.isEmpty()) {

            long totalFirstYear = Integer.MAX_VALUE;
            long totalNumberOfYears = 0;

            for (MergingObject o : allocationsByActivityAndRole) {
                if (o.firstYear < totalFirstYear)
                    totalFirstYear = o.firstYear;
                if (o.numberOfYears + (o.firstYear - totalFirstYear) > totalNumberOfYears) {
                    totalNumberOfYears = o.numberOfYears + (o.firstYear - totalFirstYear);
                }
            }

            System.out.println("f:" + totalFirstYear + " " + "n: " + totalNumberOfYears);

            list = prepareAllocationCells(allocationsByActivityAndRole, (int) totalFirstYear, (int) totalNumberOfYears);
        }

        return list;
    }

    @Data
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    private class MergingObject {
        long activityId;
        long employeeId;
        String role;
        long firstYear;
        long numberOfYears;
        List<Allocation> allocations = new LinkedList<>();

        public MergingObject(long activityId, String role, Allocation allocation) {
            this.activityId = activityId;
            this.role = role;
            this.allocations.add(allocation);
        }

        public MergingObject(String role, long employeeId, Allocation allocation) {
            this.employeeId = employeeId;
            this.role = role;
            this.allocations.add(allocation);
        }

        public MergingObject allocations(Allocation allocations) {
            this.allocations.add(allocations);
            return this;
        }

        public void sortAllocationsByDate() {
            this.allocations.sort(Comparator.comparing(Allocation::getDateFrom));
            Calendar date = new GregorianCalendar();
            date.setTime(allocations.get(0).getDateFrom());
            this.firstYear = date.get(Calendar.YEAR);
            date.setTime(allocations.get(allocations.size() - 1).getDateUntil());
            this.numberOfYears = (date.get(Calendar.YEAR) - this.firstYear) + 1;
        }
    }
}

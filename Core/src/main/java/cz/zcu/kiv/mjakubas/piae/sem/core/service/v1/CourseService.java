package cz.zcu.kiv.mjakubas.piae.sem.core.service.v1;

import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Allocation;
import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Course;
import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Employee;
import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Workplace;
import cz.zcu.kiv.mjakubas.piae.sem.core.exceptions.SecurityException;
import cz.zcu.kiv.mjakubas.piae.sem.core.repository.ICourseRepository;
import cz.zcu.kiv.mjakubas.piae.sem.core.repository.IEmployeeRepository;
import cz.zcu.kiv.mjakubas.piae.sem.core.repository.IWorkplaceRepository;
import cz.zcu.kiv.mjakubas.piae.sem.core.service.MyUtils;
import cz.zcu.kiv.mjakubas.piae.sem.core.service.SecurityService;
import cz.zcu.kiv.mjakubas.piae.sem.core.exceptions.ServiceException;
import cz.zcu.kiv.mjakubas.piae.sem.core.vo.AllocationVO;
import cz.zcu.kiv.mjakubas.piae.sem.core.vo.EmployeeVO;
import cz.zcu.kiv.mjakubas.piae.sem.core.vo.CourseVO;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import javafx.util.Pair;

/**
 * Service for working with allocations.
 */
@Service
@Transactional(readOnly = true)
public class CourseService {

    @Value("${fisrt.semester.start.month}")
    private String firstSemesterStartMonth;

    @Value("${fisrt.semester.start.day}")
    private String firstSemesterStartDay;

    @Value("${fisrt.semester.end.month}")
    private String firstSemesterEndMonth;

    @Value("${fisrt.semester.end.day}")
    private String firstSemesterEndDay;

    @Value("${second.semester.start.month}")
    private String secondSemesterStartMonth;

    @Value("${second.semester.start.day}")
    private String secondSemesterStartDay;

    @Value("${second.semester.end.month}")
    private String secondSemesterEndMonth;

    @Value("${second.semester.end.day}")
    private String secondSemesterEndDay;

    @Autowired
    private ICourseRepository courseRepository;

    @Autowired
    private IEmployeeRepository employeeRepository;

    @Autowired
    private IWorkplaceRepository workplaceRepository;

    @Autowired
    private AllocationService allocationService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private MyUtils utils;

    /**
     * Gets course by its id. Throws SQL error if course doesn't exist.
     *
     * @param id course id
     * @return course
     */
    public Course getCourse(long id) {
        Course course = courseRepository.fetchCourse(id);
        List<Allocation> allocations = allocationService.getCourseAllocations(id).getAllocations();
        if(!allocations.isEmpty()) {
            course.setYearAllocation(formatAllocations(allocations));
            course.setCourseAllocations(allocations);
            course.setEmployees(courseRepository.fetchCourseEmployees(course.getId()));
            List<Allocation> sortedAllocations = new LinkedList<>(
                    allocations.stream().sorted(Comparator.comparing(Allocation::getDateFrom)).toList());
            course.setYears(getYears(course));
            course.setAllocationsByYears(getAllocationsByYears_(sortedAllocations, course.getYears().get(0), course.getYears().size()));
        }

        return course;
    }

    /**
     * Gets all courses. Throws SQL error if something happens.
     *
     * @return list of {@link Course}
     */
    public List<Course> getCourses() {
        List<Course> courses = courseRepository.fetchCourses();
        courses.forEach(course -> {
            List<Allocation> allocations = allocationService.getCourseAllocations(course.getId()).getAllocations();
            if(!allocations.isEmpty()) {
                course.setYearAllocation(formatAllocations(allocations));
                course.setCourseAllocations(allocations);
                course.setEmployees(courseRepository.fetchCourseEmployees(course.getId()));
            }
        });
        return courses;
    }

    /**
     * Gets all course employees. Throws SQL error if course doesn't exist.
     *
     * @param id course id
     * @return list of course {@link Employee}
     */
    public List<Employee> getCourseEmployees(long id) {
        List<Employee> employees = courseRepository.fetchCourseEmployees(id);
        employees.forEach(employee -> {
            employee.setUncertainTime((float) 0.0);
            employee.setCertainTime((float) 0.0);
        });
        return employees;
    }

    /**
     * Gets all courses of workplace manager. Throws SQL error if manager doesn't exist.
     *
     * @param id manager id
     * @return list of workplace {@link Course}
     */
    public List<Course> getWorkplaceManagerCourses(long id) {
        var workplaces = workplaceRepository.fetchWorkplaces();
        var myWorkplaces = new ArrayList<Workplace>();

        for (Workplace w : workplaces) {
            if (w.getManager().getId() == id)
                myWorkplaces.add(w);
        }

        var courses = courseRepository.fetchCourses();
        var myCourses = new ArrayList<Course>();
        for (Course c : courses) {
            for (Workplace myW : myWorkplaces) {
                if (Objects.equals(c.getCourseWorkplace().getId(), myW.getId()))
                    myCourses.add(c);
            }
        }

        return myCourses;
    }

    /**
     * Creates new course. All {@link CourseVO} data must be available.
     * Might throw SQL or Service exception if data validation fails.
     *
     * @param courseVO courseVO
     */
    @Transactional
    public long createCourse(@NonNull CourseVO courseVO) {
        var manager = employeeRepository.fetchEmployee(courseVO.getCourseManagerId());
        if (courseVO.getDateUntil() != null && (courseVO.getDateFrom().after(courseVO.getDateUntil())))
                {throw new ServiceException();
        }

        Course course = new Course()
                .name(courseVO.getName())
                .shortcut(courseVO.getShortcut())
                .dateFrom(courseVO.getDateFrom())
                .dateUntil(courseVO.getDateUntil() != null ?
                        courseVO.getDateUntil() : Date.from(
                                Instant.from(LocalDate.of(9999, 9, 9))))
                .probability(courseVO.getProbability())
                .courseManager(manager)
                .courseWorkplace(
                        Workplace.builder().id(courseVO.getCourseWorkplace()).build()).term(courseVO.getTerm())
                .numberOfStudents(courseVO.getNumberOfStudents())
                .lectureLength(courseVO.getLectureLength())
                .term("N")
                .exerciseLength(courseVO.getExerciseLength())
                .description(courseVO.getDescription())
                .credits(courseVO.getCredits());

        long id = courseRepository.createCourse(course);

        if (id > 0)
            return id;
        else
            throw new ServiceException();
    }

    /**
     * Removes course and its allocations.
     * Can be done only by course manager.
     *
     * @param courseId id of removed course.
     */
    @Transactional
    public void removeCourse(long courseId) {
        if (securityService.isCourseManager(courseId)) {
            var course = getCourse(courseId);
            for (Allocation allocation : course.getCourseAllocations())
                allocationService.removeAllocation(allocation.getId());
            if (!courseRepository.removeCourse(courseId))
                throw new ServiceException();
        } else
            throw new SecurityException();
    }

    /**
     * Updates existing course. Throws SQL or Service exception if course doesn't exist or data validation fails.
     *
     * @param courseVO course data
     * @param id        course id
     */
    @Transactional
    public void editCourse(@NonNull CourseVO courseVO, long id) {
        var data = employeeRepository.fetchEmployee(courseVO.getCourseManagerId());
        if (courseVO.getDateUntil() != null && (courseVO.getDateFrom().after(courseVO.getDateUntil())))
        {throw new ServiceException();
        }

        System.out.println(courseVO);

//        var processed = allocationService.processAllocations(allocationService.getCourseAllocations(id).getAllocations());
//        if (!processed.isEmpty() && (processed.get(0).getFrom().before(courseVO.getDateFrom())
//                    || processed.get(processed.size() - 1).getUntil().after(courseVO.getDateUntil())))
//                {throw new ServiceException();
//        }

        Course course = new Course()
                .id(id)
                .name(courseVO.getName())
                .shortcut(courseVO.getShortcut())
                .dateFrom(courseVO.getDateFrom())
                .dateUntil(courseVO.getDateUntil() != null ?
                        courseVO.getDateUntil() : Date.from(
                        Instant.from(LocalDate.of(9999, 9, 9))))
                .probability(courseVO.getProbability())
                .courseManager(data)
                .courseWorkplace(Workplace.builder().id(courseVO.getCourseWorkplace()).build())
                .numberOfStudents(courseVO.getNumberOfStudents())
                .term(courseVO.getTerm())
                .lectureLength(courseVO.getLectureLength())
                .exerciseLength(courseVO.getExerciseLength())
                .credits(courseVO.getCredits());

        if (!courseRepository.updateCourse(course, id))
            throw new ServiceException();
    }

    /**
     * Assigns employee to a course.
     *
     * @param userVO user data
     * @param id     course id
     */
    @Transactional
    public void assignEmployee(@NonNull EmployeeVO userVO, long id) {
        var legitId = employeeRepository.fetchEmployee(userVO.getOrionLogin()).getId();

        var employees = courseRepository.fetchCourseEmployees(id);
        var check = new HashSet<Long>();
        for (Employee e : employees) {
            check.add(e.getId());
        }
        if (check.contains(legitId))
            throw new ServiceException();

        if (!courseRepository.addEmployee(legitId, id))
            throw new ServiceException();
    }

    /**
     * Gets all courses of a course employee.
     *
     * @param employeeId course employee id
     * @return list of {@link Course}
     */
    public List<Course> getEmployeesCourses(long employeeId) {
        var courses = courseRepository.fetchCourses();
        var myCourses = new ArrayList<Course>();
        courses.forEach(course -> {
            course.setEmployees(courseRepository.fetchCourseEmployees(course.getId()));
            course.setYearAllocation(
                    formatAllocations(allocationService.getCourseAllocations(course.getId()).getAllocations()));
            if (course.getEmployees().stream().filter(employee -> employee.getId() == employeeId).toList().size() == 1)
                myCourses.add(course);
            course.setCourseAllocations(allocationService.getCourseAllocations(course.getId()).getAllocations()
                    .stream().filter(allocation -> allocation.getWorker().getId() == employeeId).toList());
        });

        return myCourses;
    }

    /**
     * Gets all courses of a course manager.
     *
     * @param employeeId course manager id
     * @return list of {@link Course}
     */
    public List<Course> getManagerCourses(long employeeId) {
        var courses = courseRepository.fetchCourses();
        var myCourses = new ArrayList<Course>();
        courses.forEach(course -> {
            course.setYearAllocation(
                    formatAllocations(allocationService.getCourseAllocations(course.getId()).getAllocations()));
            if (course.getCourseManager().getId() == employeeId)
                myCourses.add(course);
        });
        return myCourses;
    }

    public List<Allocation> getFirstAllocationsForEachCourse(List<Course> courses) {
        List<Allocation> firstAllocations = new ArrayList<>();
        courses.forEach(course -> {
            switch(course.getCourseAllocations().size()) {
                case 0: {
                    firstAllocations.add(new Allocation().time(-1));
                    course.setCourseAllocations(new ArrayList<>());
                    break;
                }
                case 1: {
                    firstAllocations.add(course.getCourseAllocations().get(0));
                    course.setCourseAllocations(new ArrayList<>());
                    break;
                }
                default: {
                    firstAllocations.add(course.getCourseAllocations().remove(0));
                    course.setCourseAllocations(course.getCourseAllocations());
                    break;
                }
            }
        });

        return firstAllocations;
    }

    public List<Integer> getYears(Course course) {
        List<Integer> years = new LinkedList<>();

        Calendar dateFrom = Calendar.getInstance();
        Calendar dateUntil = Calendar.getInstance();

        dateFrom.setTime(course.getDateFrom());
        dateUntil.setTime(course.getDateUntil());

        int numberOfYears = dateUntil.get(Calendar.YEAR) - dateFrom.get(Calendar.YEAR);

        for (int i = 0; i <= numberOfYears; ++i)
            years.add(dateFrom.get(Calendar.YEAR) + i);

        return years;
    }

    public List<Integer> getYearsOfAllocations(List<Allocation> allocations) {
        List<Integer> years = new LinkedList<>();
        Calendar date = Calendar.getInstance();
        int lastAllYear = 0;
        int index = -1;

        List<Pair<Integer, Integer>> pairs = new LinkedList<>();

        for (Allocation allocation : allocations)  {
            date.setTime(allocation.getDateFrom());
            if (lastAllYear == date.get(Calendar.YEAR)) {
                int currentValue = pairs.get(index).getValue() + 1;
                pairs.set(index, new Pair<>(lastAllYear, currentValue));
            } else {
                date.setTime(allocation.getDateFrom());
                pairs.add(new Pair<>(date.get(Calendar.YEAR), 1));
                lastAllYear = date.get(Calendar.YEAR);
                index++;
            }
        }

        for (Pair<Integer, Integer> pair : pairs) {
            years.add(pair.getKey());
        }

        return years;
    }

    /* Tady pocitam s alokacema max na rok - koukam jen na zacatek. */
    public List<List<Allocation>> getAllocationsByYears(List<Allocation> allocations, int firstYear) {
        List<List<Allocation>> splitAllocations = new ArrayList<>();
        if (!allocations.isEmpty()) {
            Calendar date = Calendar.getInstance();
            int thisYear = firstYear;

            List<Allocation> tempList = new ArrayList<>();

            for (Allocation allocation : allocations) {
                date.setTime(allocation.getDateFrom());
                if (thisYear == date.get(Calendar.YEAR)) {
                    tempList.add(allocation);
                } else {
                    date.setTime(allocation.getDateFrom());
                    thisYear = date.get(Calendar.YEAR);
                    splitAllocations.add(tempList);
                    tempList = new ArrayList<>();
                    tempList.add(allocation);
                }
            }

            if (!tempList.isEmpty()) {
                splitAllocations.add(tempList);
            }
        }

        return splitAllocations;
    }

    /* Tady pocitam s alokacema max na rok - koukam jen na zacatek. */
    public List<List<Allocation>> getAllocationsByYears_(List<Allocation> allocations, int firstYear, int numberOfYears) {
        List<List<Allocation>> splitAllocations = new ArrayList<>();
        if (!allocations.isEmpty()) {
            Calendar date = Calendar.getInstance();
            int thisYear = firstYear;

            List<Allocation> tempList = new ArrayList<>();

            int offset = 0;
            date.setTime(allocations.get(0).getDateFrom());
            while (thisYear + offset < date.get(Calendar.YEAR)) {
                splitAllocations.add(new ArrayList<>());
                offset++;
            }

            for (Allocation allocation : allocations) {
                date.setTime(allocation.getDateFrom());
                if (thisYear == date.get(Calendar.YEAR)) {
                    tempList.add(allocation);
                } else {
                    date.setTime(allocation.getDateFrom());
                    thisYear = date.get(Calendar.YEAR);
                    splitAllocations.add(tempList);
                    tempList = new ArrayList<>();
                    tempList.add(allocation);
                    offset++;
                }
            }

            if (!tempList.isEmpty()) {
                offset++;
                splitAllocations.add(tempList);
            }

            date.setTime(allocations.get(0).getDateFrom());
            while (offset < numberOfYears) {
                splitAllocations.add(new ArrayList<>());
                offset++;
            }
        }

        return splitAllocations;
    }

    /**
     * Formats allocations times for using on FE.
     * @param allocations allocations
     * @return list of allocations
     */
    private List<Float> formatAllocations(List<Allocation> allocations) {
        List<Float> yearAllocations = new ArrayList<>(Collections.nCopies(12, (float) 0));
        List<Allocation> thisYearsAllocations = new ArrayList<>();

        Calendar dateFrom = new GregorianCalendar();
        Calendar dateUntil = new GregorianCalendar();

        int allocationsIndex = 0;
        allocations.stream().filter(this::isThisYearAllocation).forEach(thisYearsAllocations::add);

        if (!thisYearsAllocations.isEmpty()) {
            Allocation allocation = thisYearsAllocations.get(allocationsIndex);

            dateFrom.setTime(allocation.getDateFrom());
            dateUntil.setTime(allocation.getDateUntil());

//        Here I will go through every month of the year and add to list 0 or time for project.
            for (int i = 1; i < 13; i++) {
                if (fitsDate(dateFrom, dateUntil, i, allocation))
                    yearAllocations.set(i - 1, allocation.getTime());
                else {
                    if (dateUntil.get(Calendar.MONTH) == i)
                        allocation = thisYearsAllocations.get(allocationsIndex++);
                    else
                        yearAllocations.set(i - 1, (float) 0);
                }
            }
            return yearAllocations;
        } else
            return new ArrayList<>();
    }

    private boolean fitsDate(Calendar dateFrom, Calendar dateUntil, int i, Allocation allocation) {
        return (dateFrom.get(Calendar.MONTH) <= i && dateUntil.get(Calendar.MONTH) >= i)
                || (isThisYearAllocation(allocation) && dateFrom.get(Calendar.YEAR) == i)
                || (isThisYearAllocation(allocation) && dateUntil.get(Calendar.YEAR) == i);
    }

    private boolean isThisYearAllocation(Allocation allocation) {
        Calendar dateFrom = new GregorianCalendar();
        Calendar dateUntil = new GregorianCalendar();
        dateFrom.setTime(allocation.getDateFrom());
        dateUntil.setTime(allocation.getDateUntil());
        return dateFrom.get(Calendar.YEAR) == LocalDate.now().getYear() || dateUntil.get(Calendar.YEAR) == LocalDate.now().getYear();
    }

    /**
     * Nastaveni datumu pri zmene semestru.
     * Pokud ma term hodnotu "Z" nastavi se datumy od zari do unora (roky jsou zachovane).
     * Pokud ma term hodnotu "L" nastavi se datumy od brezna (rok se pricte) do srpna (rok se zachova).
     * Pokud ma term hodnotu "N" zachovaji se datumy predmetu.
     */
    public void addCoursesValues(AllocationVO allocationVO, int year) {
        if (allocationVO.getTerm().equals("Z")) {
            allocationVO.setDateFrom(LocalDate.of(
                    year,
                    Integer.parseInt(firstSemesterStartMonth),
                    Integer.parseInt(firstSemesterStartDay)));
            allocationVO.setDateUntil(LocalDate.of(
                    year + 1,
                    Integer.parseInt(firstSemesterEndMonth),
                    Integer.parseInt(firstSemesterEndDay)));
        } else if (allocationVO.getTerm().equals("L")) {
            allocationVO.setDateFrom(LocalDate.of(
                    year + 1,
                    Integer.parseInt(secondSemesterStartMonth),
                    Integer.parseInt(secondSemesterStartDay)));
            allocationVO.setDateUntil(LocalDate.of(
                    year + 1,
                    Integer.parseInt(secondSemesterEndMonth),
                    Integer.parseInt(secondSemesterEndDay)));
        } else {
            allocationVO.setDateFrom(LocalDate.of(
                    year,
                    Integer.parseInt(firstSemesterStartMonth),
                    Integer.parseInt(firstSemesterStartDay)));
            allocationVO.setDateUntil(LocalDate.of(
                    year + 1,
                    Integer.parseInt(secondSemesterEndMonth),
                    Integer.parseInt(secondSemesterEndDay)));
        }
    }
}

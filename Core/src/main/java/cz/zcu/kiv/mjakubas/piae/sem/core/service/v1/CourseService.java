package cz.zcu.kiv.mjakubas.piae.sem.core.service.v1;

import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Allocation;
import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Course;
import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Employee;
import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Workplace;
import cz.zcu.kiv.mjakubas.piae.sem.core.repository.ICourseRepository;
import cz.zcu.kiv.mjakubas.piae.sem.core.repository.IEmployeeRepository;
import cz.zcu.kiv.mjakubas.piae.sem.core.repository.IWorkplaceRepository;
import cz.zcu.kiv.mjakubas.piae.sem.core.service.ServiceException;
import cz.zcu.kiv.mjakubas.piae.sem.core.vo.EmployeeVO;
import cz.zcu.kiv.mjakubas.piae.sem.core.vo.CourseVO;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
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
@AllArgsConstructor
public class CourseService {

    private final ICourseRepository courseRepository;
    private final IEmployeeRepository employeeRepository;
    private final IWorkplaceRepository workplaceRepository;

    private final AllocationService allocationService;

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
            course.setYearAllocation(prepareAllocations(allocations));
            course.setCourseAllocations(allocations);
            course.setEmployees(courseRepository.fetchCourseEmployees(course.getId()));
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
                course.setYearAllocation(prepareAllocations(allocations));
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
                .term(courseVO.getTerm())
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
                    prepareAllocations(allocationService.getCourseAllocations(course.getId()).getAllocations()));
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
                    prepareAllocations(allocationService.getCourseAllocations(course.getId()).getAllocations()));
            if (course.getCourseManager().getId() == employeeId)
                myCourses.add(course);
        });
        return myCourses;
    }

    public List<Allocation> prepareFirst(List<Course> courses) {
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

    public List<Integer> prepareYearsForDetail(List<Allocation> allocations) {
        List<Allocation> sortedAllocations = new LinkedList<>(
                allocations.stream().sorted(Comparator.comparing(Allocation::getDateFrom)).toList());

        List<Integer> years = new LinkedList<>();
        Calendar date = new GregorianCalendar();
        int lastAllYear = 0;
        int index = -1;

        List<Pair<Integer, Integer>> pairs = new LinkedList<>();

        for (Allocation allocation : sortedAllocations)  {
            date.setTime(allocation.getDateFrom());
            if (lastAllYear == date.get(Calendar.YEAR)) {
                int value = pairs.get(index).getValue() + 1;
                pairs.set(index, new Pair<>(lastAllYear, value));
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

    public List<Integer> prepareCountsDetail(List<Allocation> allocations) {
        List<Integer> counts = new LinkedList<>();
        Calendar date = new GregorianCalendar();
        int lastAllYear = 0;
        int index = -1;

        List<Pair<Integer, Integer>> pairs = new LinkedList<>();

        for (Allocation allocation : allocations)  {
            date.setTime(allocation.getDateFrom());
            if (lastAllYear == date.get(Calendar.YEAR)) {
                int value = pairs.get(index).getValue() + 1;
                pairs.set(index, new Pair<>(lastAllYear, value));
            } else {
                date.setTime(allocation.getDateFrom());
                pairs.add(new Pair<>(date.get(Calendar.YEAR), 1));
                lastAllYear = date.get(Calendar.YEAR);
                index++;
            }
        }

        for (Pair<Integer, Integer> pair : pairs) {
            counts.add(pair.getValue());
        }

        return counts;
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
                if ((allocation.getDateFrom().getMonth() <= i && allocation.getDateUntil().getMonth() >= i)
                        || (isThisYearAllocation(allocation) && allocation.getDateFrom().getYear() == i)
                        || (isThisYearAllocation(allocation) && allocation.getDateUntil().getYear() == i))
                    yearAllocations.set(i - 1, allocation.getTime());
                else {
                    if (allocation.getDateUntil().getMonth() == i)
                        allocation = thisYearsAllocations.get(allocationsIndex++);
                    else
                        yearAllocations.set(i - 1, (float) 0);
                }
            }
            return yearAllocations;
        } else
            return new ArrayList<>();
    }

    private boolean isThisYearAllocation(Allocation allocation) {
        return allocation.getDateFrom().getYear() <= LocalDate.now().getYear()
                && allocation.getDateUntil().getYear() >= LocalDate.now().getYear();
    }
}

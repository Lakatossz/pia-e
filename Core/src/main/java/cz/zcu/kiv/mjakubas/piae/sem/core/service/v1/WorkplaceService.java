package cz.zcu.kiv.mjakubas.piae.sem.core.service.v1;

import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Employee;
import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Workplace;
import cz.zcu.kiv.mjakubas.piae.sem.core.repository.IWorkplaceRepository;
import cz.zcu.kiv.mjakubas.piae.sem.core.service.ServiceException;
import cz.zcu.kiv.mjakubas.piae.sem.core.vo.WorkplaceVO;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Services for working with workplaces.
 */
@Service
@Transactional(readOnly = true)
@AllArgsConstructor
public class WorkplaceService {

    private final IWorkplaceRepository workplaceRepository;

    private final EmployeeService employeeService;

    /**
     * Gets workplace given its id.
     *
     * @param id workplace id
     * @return workplace
     */
    public Workplace getWorkplace(long id) {
        return workplaceRepository.fetchWorkplace(id);
    }

    /**
     * Gets workplace given its abbrevation.
     *
     * @param abbrevation workplace abbrevation
     * @return workplace
     */
    public Workplace getWorkplace(@NonNull String abbrevation) {
        return workplaceRepository.fetchWorkplace(abbrevation);
    }

    /**
     * Gets all workplaces.
     *
     * @return list of {@link Workplace}
     */
    public List<Workplace> getWorkplaces() {
        return workplaceRepository.fetchWorkplaces();
    }

    /**
     * Creates new workplace.
     *
     * @param workplaceVO value object
     */
    @Transactional
    public void createWorkplace(@NonNull WorkplaceVO workplaceVO) {
        Workplace workplace = Workplace.builder()
                .abbreviation(workplaceVO.getAbbreviation())
                .fullName(workplaceVO.getName())
                .build();

        if (!workplaceRepository.createWorkplace(workplace))
            throw new ServiceException();
    }

    @Transactional
    public void updateWorkplace(@NonNull WorkplaceVO workplaceVO, long id) {
        Employee manager = (workplaceVO.getManager() == null) ? null : employeeService.getEmployee(workplaceVO.getManager());

        Workplace workplace = Workplace.builder()
                .id(id)
                .abbreviation(workplaceVO.getAbbreviation())
                .fullName(workplaceVO.getName())
                .manager(manager)
                .build();

        if (!workplaceRepository.updateWorkplace(workplace, id))
            throw new ServiceException();
    }

    /**
     * Deletes workplace given its id.
     *
     * @param workplaceId workplace id
     */
    @Transactional
    public void removeWorkplace(long workplaceId) {
        return;
    }

}

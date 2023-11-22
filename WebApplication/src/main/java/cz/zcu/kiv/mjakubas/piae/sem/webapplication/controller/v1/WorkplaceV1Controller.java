package cz.zcu.kiv.mjakubas.piae.sem.webapplication.controller.v1;

import cz.zcu.kiv.mjakubas.piae.sem.core.service.v1.EmployeeService;
import cz.zcu.kiv.mjakubas.piae.sem.core.service.v1.WorkplaceService;
import cz.zcu.kiv.mjakubas.piae.sem.core.vo.WorkplaceVO;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

/**
 * Contains all sites for working with workplaces.
 */
@Controller
@RequestMapping("/w")
@AllArgsConstructor
@PreAuthorize("hasAuthority(T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).SECRETARIAT)")
public class WorkplaceV1Controller {

    private final WorkplaceService workplaceService;
    private final EmployeeService employeeService;

    @GetMapping()
    public String viewWorkplaces(Model model) {
        model.addAttribute("workplaces", workplaceService.getWorkplaces());
        return "views/workplaces";
    }

    @GetMapping("/create")
    public String createWorkplace(Model model) {
        var data = workplaceService.getWorkplaces();
        model.addAttribute("restrictions", data);

        model.addAttribute("workplaceVO", new WorkplaceVO());
        return "forms/workplace/create_workplace_form";
    }

    @PostMapping("/create")
    public String createWorkplace(Model model, @ModelAttribute WorkplaceVO workplaceVO, BindingResult errors) {

        workplaceService.createWorkplace(workplaceVO);
        return "redirect:/w?create=success";
    }

    @GetMapping("/{id}/delete")
    public String deleteWorkplace(Model model, @PathVariable long id) {

        workplaceService.removeWorkplace(id);
        return "redirect:/w?delete=success";
    }

    @GetMapping("/{id}/edit")
    public String editWorkplace(Model model, @PathVariable long id) {
        var workplace = workplaceService.getWorkplace(id);
        model.addAttribute("restrictions", workplace);
        var employees = employeeService.getEmployees();
        model.addAttribute("employees", employees);

        var wm = workplace.getManager();
        String vis = null;
        if (wm != null && wm.getFirstName() != null)
            vis = String.format("%s %s (%s)", wm.getFirstName(), wm.getLastName(), wm.getOrionLogin());

        model.addAttribute("workplaceVO",
                new WorkplaceVO(workplace.getFullName(), workplace.getAbbreviation(), vis));
        return "forms/workplace/edit_workplace_form";
    }

    @PostMapping("/{id}/edit")
    public String editWorkplace(Model model, @PathVariable long id, @ModelAttribute WorkplaceVO workplaceVO,
                                BindingResult errors) {

        /* it is what it is... */
        if (!workplaceVO.getManager().equals("")) {
            var m = workplaceVO.getManager();
            m = m.substring(m.indexOf('(') + 1, m.indexOf(')'));
            workplaceVO.setManager(m);
        } else {
            workplaceVO.setManager(null);
        }

        workplaceService.updateWorkplace(workplaceVO, id);

        return "redirect:/w?edit=success";
    }
}

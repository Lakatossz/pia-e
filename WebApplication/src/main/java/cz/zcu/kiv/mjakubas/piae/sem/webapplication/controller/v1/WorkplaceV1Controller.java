package cz.zcu.kiv.mjakubas.piae.sem.webapplication.controller.v1;

import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Workplace;
import cz.zcu.kiv.mjakubas.piae.sem.core.service.v1.EmployeeService;
import cz.zcu.kiv.mjakubas.piae.sem.core.service.v1.WorkplaceService;
import cz.zcu.kiv.mjakubas.piae.sem.core.vo.WorkplaceVO;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
    @PreAuthorize("hasAnyAuthority(" +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).SECRETARIAT, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).ADMIN)")
    public String viewWorkplaces(Model model) {
        model.addAttribute("workplaces", workplaceService.getWorkplaces());
        return "views/workplaces";
    }

    @GetMapping("/create")
    @PreAuthorize("hasAnyAuthority(" +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).SECRETARIAT, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).ADMIN)")
    public String createWorkplace(Model model) {
        var data = workplaceService.getWorkplaces();
        model.addAttribute("restrictions", data);

        var employees = employeeService.getEmployees();
        model.addAttribute("employees", employees);

        WorkplaceVO newWorkplace = new WorkplaceVO();
        newWorkplace.setName("Nové pracoviště");

        model.addAttribute("workplace", newWorkplace);
        return "forms/workplace/create_workplace_form";
    }

    @PostMapping("/create")
    @PreAuthorize("hasAnyAuthority(" +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).SECRETARIAT, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).ADMIN)")
    public String createWorkplace(Model model, @ModelAttribute WorkplaceVO workplaceVO,
                                  BindingResult errors, RedirectAttributes redirectAttributes) {
        long id = workplaceService.createWorkplace(workplaceVO);
        redirectAttributes.addAttribute("id", id);
        return "redirect:/w/{id}/detail?create=success";
    }

    @PostMapping("/{id}/delete")
    @PreAuthorize("hasAnyAuthority(" +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).SECRETARIAT, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).ADMIN)")
    public String deleteWorkplace(Model model, @PathVariable long id) {

        workplaceService.removeWorkplace(id);
        return "redirect:/w?delete=success";
    }

    @GetMapping("/{id}/edit")
    @PreAuthorize("hasAnyAuthority(" +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).SECRETARIAT, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).ADMIN)")
    public String editWorkplace(Model model, @PathVariable long id) {
        var workplace = workplaceService.getWorkplace(id);
        model.addAttribute("restrictions", workplace);
        var employees = employeeService.getEmployees();
        model.addAttribute("employees", employees);

//        var wm = workplace.getManager();
//        String vis = null;
//        if (wm != null && wm.getFirstName() != null)
//            vis = String.format("%s %s (%s)", wm.getFirstName(), wm.getLastName(), wm.getOrionLogin());

        model.addAttribute("workplaceVO",
                new WorkplaceVO(workplace.getId(), workplace.getFullName(), workplace.getAbbreviation(), workplace.getManager().getId(), workplace.getDescription()));
        return "forms/workplace/edit_workplace_form";
    }

    @PostMapping("/{id}/edit")
    @PreAuthorize("hasAnyAuthority(" +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).SECRETARIAT, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).ADMIN)")
    public String editWorkplace(Model model, @PathVariable long id, @ModelAttribute WorkplaceVO workplaceVO,
                                BindingResult errors) {

//        /* it is what it is... */
//        if (!workplaceVO.getManager().equals("")) {
//            var m = workplaceVO.getManager();
//            m = m.substring(m.indexOf('(') + 1, m.indexOf(')'));
//            workplaceVO.setManager(m);
//        } else {
//            workplaceVO.setManager(null);
//        }

        workplaceService.updateWorkplace(workplaceVO, id);

        return "redirect:/w/{id}/detail?edit=success";
    }

    @PreAuthorize("hasAnyAuthority(" +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).SECRETARIAT, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).ADMIN) " +
            " or @securityService.isWorkplaceManager(#id)")
    @GetMapping("/{id}/detail")
    public String detailWorkplace(Model model, @PathVariable long id,
                                @RequestParam(required = false) Boolean manage) {
        Workplace data = workplaceService.getWorkplace(id);

        model.addAttribute("workplace",
                new WorkplaceVO(data.getId(), data.getFullName(), data.getAbbreviation(), data.getManager().getId(), data.getDescription()));

        model.addAttribute("employees", employeeService.getEmployees());
        model.addAttribute("manage", manage);

        return "details/workplace_detail";
    }
}

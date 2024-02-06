package cz.zcu.kiv.mjakubas.piae.sem.webapplication.controller.v1;

import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Workplace;
import cz.zcu.kiv.mjakubas.piae.sem.core.exceptions.SecurityException;
import cz.zcu.kiv.mjakubas.piae.sem.core.service.v1.EmployeeService;
import cz.zcu.kiv.mjakubas.piae.sem.core.service.v1.WorkplaceService;
import cz.zcu.kiv.mjakubas.piae.sem.core.vo.WorkplaceVO;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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

    @Autowired
    private WorkplaceService workplaceService;

    @Autowired
    private EmployeeService employeeService;

    private static final String WORKPLACES_REDIRECT = "redirect:/w";
    private static final String WORKPLACE_DETAIL_REDIRECT = "redirect:/w/%s/detail";
    private static final String PERMISSION_ERROR = "permissionError";
    private static final String GENEREAL_ERROR = "generalError";

    @GetMapping()
    @PreAuthorize("hasAnyAuthority(" +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).SECRETARIAT, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).ADMIN)")
    public String viewWorkplaces(Model model,
                                 RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("workplaces", workplaceService.getWorkplaces());
            return "views/workplaces";
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
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).ADMIN)")
    public String createWorkplace(Model model,
                                  RedirectAttributes redirectAttributes) {
        try {
            var data = workplaceService.getWorkplaces();
            model.addAttribute("restrictions", data);

            var employees = employeeService.getEmployees();
            model.addAttribute("employees", employees);

            WorkplaceVO newWorkplace = new WorkplaceVO();
            newWorkplace.setName("Nové pracoviště");

            model.addAttribute("workplace", newWorkplace);
            return "forms/workplace/create_workplace_form";
        } catch (SecurityException e) {
            redirectAttributes.addFlashAttribute(PERMISSION_ERROR, true);
            return WORKPLACES_REDIRECT;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(GENEREAL_ERROR, true);
            return WORKPLACES_REDIRECT;
        }
    }

    @PostMapping("/create")
    @PreAuthorize("hasAnyAuthority(" +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).SECRETARIAT, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).ADMIN)")
    public String createWorkplace(@ModelAttribute WorkplaceVO workplaceVO,
                                  RedirectAttributes redirectAttributes) {
        try {
            long id = workplaceService.createWorkplace(workplaceVO);
            redirectAttributes.addAttribute("id", id);
            return "redirect:/w/{id}/detail?create=success";
        } catch (SecurityException e) {
            redirectAttributes.addFlashAttribute(PERMISSION_ERROR, true);
            return WORKPLACES_REDIRECT;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(GENEREAL_ERROR, true);
            return WORKPLACES_REDIRECT;
        }
    }

    @PreAuthorize("hasAnyAuthority(" +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).SECRETARIAT, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).ADMIN) " +
            " or @securityService.isWorkplaceManager(#id)")
    @GetMapping("/{id}/detail")
    public String detailWorkplace(Model model, @PathVariable long id,
                                  RedirectAttributes redirectAttributes) {
        try {
            Workplace data = workplaceService.getWorkplace(id);
            model.addAttribute("workplace", new WorkplaceVO(
                    data.getId(),
                    data.getFullName(),
                    data.getAbbreviation(),
                    data.getManager().getId(),
                    data.getDescription()));
            return "details/workplace_detail";
        } catch (SecurityException e) {
            redirectAttributes.addFlashAttribute(PERMISSION_ERROR, true);
            return WORKPLACES_REDIRECT;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(GENEREAL_ERROR, true);
            return WORKPLACES_REDIRECT;
        }
    }

    @PostMapping("/{id}/edit")
    @PreAuthorize("hasAnyAuthority(" +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).SECRETARIAT, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).ADMIN)")
    public String editWorkplace(@PathVariable long id, @ModelAttribute WorkplaceVO workplaceVO,
                                RedirectAttributes redirectAttributes) {
        try {
            workplaceService.updateWorkplace(workplaceVO, id);
            return "redirect:/w/{id}/detail?edit=success";
        } catch (SecurityException e) {
            redirectAttributes.addFlashAttribute(PERMISSION_ERROR, true);
            return String.format(WORKPLACE_DETAIL_REDIRECT, id);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(GENEREAL_ERROR, true);
            return String.format(WORKPLACE_DETAIL_REDIRECT, id);
        }
    }

    @PostMapping("/{id}/delete")
    @PreAuthorize("hasAnyAuthority(" +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).SECRETARIAT, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).ADMIN)")
    public String deleteWorkplace(@PathVariable long id,
                                  RedirectAttributes redirectAttributes) {
        try {
            workplaceService.removeWorkplace(id);
            return "redirect:/w?delete=success";
        } catch (SecurityException e) {
            redirectAttributes.addFlashAttribute(PERMISSION_ERROR, true);
            return String.format(WORKPLACE_DETAIL_REDIRECT, id);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(GENEREAL_ERROR, true);
            return String.format(WORKPLACE_DETAIL_REDIRECT, id);
        }
    }
}

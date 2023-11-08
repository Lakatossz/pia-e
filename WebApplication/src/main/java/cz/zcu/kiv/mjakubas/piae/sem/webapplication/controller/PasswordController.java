package cz.zcu.kiv.mjakubas.piae.sem.webapplication.controller;

import cz.zcu.kiv.mjakubas.piae.sem.core.service.SecurityService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controls password change views.
 */
@Controller
@RequestMapping("/")
@AllArgsConstructor
public class PasswordController {

    private final SecurityService securityService;

    @GetMapping("/pw/change")
    public String changePassword(Model model) {

        model.addAttribute("passwordVO", new PasswordVO());
        return "forms/change_password";
    }

    @PostMapping("/pw/change")
    public String changePassword(Model model, @ModelAttribute PasswordVO passwordVO, BindingResult errors) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        securityService.updateUserPassword(auth.getName(), passwordVO.password);
        return "redirect:/index";
    }

    @Getter
    @Setter
    public class PasswordVO {
        private String password;
    }
}

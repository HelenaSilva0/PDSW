package vivamama.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/** Encaminha somente as rotas do React Router; endpoints da API não são afetados. */
@Controller
public class SpaController {
    @GetMapping({
            "/", "/login", "/cadastro", "/paciente", "/paciente/**",
            "/medico", "/medico/**"
    })
    public String reactApp() {
        return "forward:/index.html";
    }
}

package vivamama.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vivamama.dto.*;
import vivamama.model.User;
import vivamama.service.UserService;

@RestController
@RequestMapping("/auth")
public class UserController {
	 @Autowired private UserService userService;

	    @PostMapping("/register")
	    public ResponseEntity<?> register(@RequestBody RegisterRequest req){
	    
	        if (userService.buscarPorEmail(req.email).isPresent()) {
	            return ResponseEntity
	                    .status(409)//409 serve para verificar se esse dado ja existe
	                    .body("E-mail já cadastrado. Tente outro e-mail ou faça login.");
	        }	
	    	
	    	
	    	User u = new User();
	        u.setEmail(req.email);
	        u.setSenha(req.senha);   
	        u.setTelefone(req.telefone);
	        u.setRole(req.role);
	        User saved = userService.criar(u);
	        return ResponseEntity.ok(new Response(saved.getId(), saved.getRole()));
	    }

	    @PostMapping("/login")
	    public ResponseEntity<Response> login(@RequestBody LoginRequest req){
	        return userService.autenticar(req.email, req.senha)
	                .map(u -> ResponseEntity.ok(new Response(u.getId(), u.getRole())))
	                .orElse(ResponseEntity.status(401).build());
	    }

}

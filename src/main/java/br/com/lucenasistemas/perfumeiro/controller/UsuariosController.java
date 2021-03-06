package br.com.lucenasistemas.perfumeiro.controller;


import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import br.com.lucenasistemas.perfumeiro.controller.page.PageWrapper;
import br.com.lucenasistemas.perfumeiro.model.Usuario;
import br.com.lucenasistemas.perfumeiro.repository.Grupos;
import br.com.lucenasistemas.perfumeiro.repository.Usuarios;
import br.com.lucenasistemas.perfumeiro.repository.filter.UsuarioFilter;
import br.com.lucenasistemas.perfumeiro.service.StatusUsuario;
import br.com.lucenasistemas.perfumeiro.service.UsuarioService;
import br.com.lucenasistemas.perfumeiro.service.exception.EmailUsuarioJaCadastradoException;
import br.com.lucenasistemas.perfumeiro.service.exception.ImpossivelExcluirEntidadeException;
import br.com.lucenasistemas.perfumeiro.service.exception.SenhaObrigatoriaUsuarioException;

@Controller
@RequestMapping("/usuarios")
public class UsuariosController {
	
	@Autowired
	private UsuarioService service;

	@Autowired
	private Grupos grupos;
	
	@Autowired
	private Usuarios usuarios;
	
	@RequestMapping("/novo")
	public ModelAndView novo(Usuario usuario) {
		ModelAndView mv = new ModelAndView("usuario/cadastroUsuario");
		mv.addObject("grupos", grupos.findAll());
		return mv;
	}
	
	@PostMapping({ "/novo", "{\\+d}" })
	public ModelAndView salvar(@Valid Usuario usuario, BindingResult result, RedirectAttributes attributes) {
		if (result.hasErrors()) {
			return novo(usuario);
		}
		
		try {
			service.salvar(usuario);
		} catch (EmailUsuarioJaCadastradoException e) {
			result.rejectValue("email", e.getMessage(), e.getMessage());
			return novo(usuario);
		} catch (SenhaObrigatoriaUsuarioException e) {
			result.rejectValue("senha", e.getMessage(), e.getMessage());
			return novo(usuario);
		}
		
		attributes.addFlashAttribute("mensagem", "Usuário salvo com sucesso");
		return new ModelAndView("redirect:/usuarios/novo");
	}
	
	@GetMapping
	public ModelAndView pesquisar(UsuarioFilter usuarioFilter
			, @PageableDefault(size = 3) Pageable pageable, HttpServletRequest httpServletRequest) {
		ModelAndView mv = new ModelAndView("/usuario/pesquisaUsuario");
		mv.addObject("grupos", grupos.findAll());
		
		PageWrapper<Usuario> paginaWrapper = new PageWrapper<>(usuarios.filtrar(usuarioFilter, pageable)
				, httpServletRequest);
		mv.addObject("pagina", paginaWrapper);
		return mv;
	}
	
	@PutMapping("/status")
	@ResponseStatus(HttpStatus.OK)
	public void atualizarStatus(@RequestParam("ids[]") Long[] ids, @RequestParam("status") StatusUsuario statusUsuario) {
		service.alterarStatus(ids, statusUsuario);
	}
	
	@GetMapping("/{id}")
	public ModelAndView editar(@PathVariable Long id) {
		Usuario usuario = usuarios.buscarComGrupos(id);
		ModelAndView mv = novo(usuario);
		mv.addObject(usuario);
		return mv;
	}
	
	@DeleteMapping("/{id}")
	public @ResponseBody ResponseEntity<?> excluir(@PathVariable("id") Usuario usuario) {
		try {
			service.excluir(usuario);
		} catch (ImpossivelExcluirEntidadeException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}
		return ResponseEntity.ok().build();
	}
	
}
package br.com.lucenasistemas.perfumeiro.controller;



import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import br.com.lucenasistemas.perfumeiro.controller.page.PageWrapper;
import br.com.lucenasistemas.perfumeiro.model.Cliente;
import br.com.lucenasistemas.perfumeiro.model.TipoPessoa;
import br.com.lucenasistemas.perfumeiro.repository.Clientes;
import br.com.lucenasistemas.perfumeiro.repository.Estados;
import br.com.lucenasistemas.perfumeiro.repository.filter.ClienteFilter;
import br.com.lucenasistemas.perfumeiro.service.ClienteService;
import br.com.lucenasistemas.perfumeiro.service.exception.CpfCnpjClienteJaCadastradoException;


@Controller
@RequestMapping("/clientes")
public class ClientesController {

	@Autowired
	private Estados estados;
	
	@Autowired
	private Clientes clientes;
	
	@Autowired
	private ClienteService service;
	
	@RequestMapping("/novo")
	public ModelAndView novo(Cliente cliente) {
		ModelAndView mv = new ModelAndView("cliente/cadastroCliente");
		mv.addObject("estados", estados.findAll());
		mv.addObject("tiposPessoa", TipoPessoa.values());
		return mv;
	}
	
	@PostMapping("/novo")
	public ModelAndView salvar(@Valid Cliente cliente, BindingResult result, RedirectAttributes attributes) {
		
		if (result.hasErrors()) {
			return novo(cliente);
		}
		try{
			service.salvar(cliente);
		}catch(CpfCnpjClienteJaCadastradoException e){
			result.rejectValue("CpfOuCnpj", e.getMessage(),e.getMessage());
			return novo(cliente);
		}
		attributes.addFlashAttribute("mensagem", "Cliente salvo com sucesso!");
		return new ModelAndView("redirect:/clientes/novo");
	}
	
	@GetMapping
	public ModelAndView pesquisar(ClienteFilter clienteFilter, BindingResult result
			, @PageableDefault(size = 3) Pageable pageable, HttpServletRequest httpServletRequest) {
		ModelAndView mv = new ModelAndView("cliente/pesquisaCliente");
		mv.addObject("tiposPessoa", TipoPessoa.values());
		
		PageWrapper<Cliente> paginaWrapper = new PageWrapper<>(clientes.filtrar(clienteFilter, pageable)
				, httpServletRequest);
		mv.addObject("pagina", paginaWrapper);
		return mv;
	}
	
	@RequestMapping(consumes = { MediaType.APPLICATION_JSON_VALUE })
	public @ResponseBody List<Cliente> pesquisar(String nome) {
		validarTamanhoNome(nome);
		return clientes.findByNomeStartingWithIgnoreCase(nome);
	}

	private void validarTamanhoNome(String nome) {
		if (StringUtils.isEmpty(nome) || nome.length() < 3) {
			throw new IllegalArgumentException();
		}
	}
	
	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<Void> tratarIllegalArgumentException(IllegalArgumentException e) {
		return ResponseEntity.badRequest().build();
	}
	
}

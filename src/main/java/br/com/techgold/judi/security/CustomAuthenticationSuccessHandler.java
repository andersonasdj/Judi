package br.com.techgold.judi.security;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import br.com.techgold.judi.model.Funcionario;
import br.com.techgold.judi.services.FuncionarioService;
import br.com.techgold.judi.services.TwoFactorAuthService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

	final FuncionarioService funcionarioService;
	final TwoFactorAuthService emailService;

    CustomAuthenticationSuccessHandler(FuncionarioService funcionarioService, TwoFactorAuthService emailService) {
        this.funcionarioService = funcionarioService;
        this.emailService = emailService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)throws IOException, ServletException {

        // Usuário autenticado (já contém PRE_2FA)
        Funcionario funcionario = (Funcionario) authentication.getPrincipal();

        // Adiciona autoridade temporária PRE_2FA
        Collection<? extends GrantedAuthority>  authorities = List.of(new SimpleGrantedAuthority("PRE_2FA"));
        UsernamePasswordAuthenticationToken pre2faAuth = new UsernamePasswordAuthenticationToken(funcionario, null, authorities);

        // Atualiza contexto com a nova autoridade
        SecurityContextHolder.getContext().setAuthentication(pre2faAuth);

        // Armazena na sessão para o controller recuperar
        HttpSession session = request.getSession(true);
        session.setAttribute("TEMP_USER", funcionario);
        
        if(!funcionario.getMfa()) {
        	// Recupera authorities reais
            authorities = funcionario.getAuthorities();

            // Promove a autenticação
            UsernamePasswordAuthenticationToken fullAuth = new UsernamePasswordAuthenticationToken(funcionario, null, authorities);
            SecurityContextHolder.getContext().setAuthentication(fullAuth);
        	response.sendRedirect(request.getContextPath() + "/home");
        }
        else {
            emailService.gerarEEnviarCodigo(funcionario);
            response.sendRedirect("/judi/2fa");
        }
        
    }
}

package br.com.techgold.judi.services;

import java.security.SecureRandom;

import org.springframework.stereotype.Service;

import br.com.techgold.judi.email.EnviadorEmail;
import br.com.techgold.judi.model.Funcionario;

@Service
public class TwoFactorAuthService {

    private final FuncionarioService funcionarioService;
    private final EnviadorEmail enviador;
    
    private static final SecureRandom random = new SecureRandom();

    TwoFactorAuthService(FuncionarioService funcionarioService, EnviadorEmail enviador) {
        this.funcionarioService = funcionarioService;
        this.enviador = enviador;
    }

    public void gerarEEnviarCodigo(Funcionario funcionario) {
        String codigo = String.format("%06d", random.nextInt(1_000_000));
        
        funcionario.setCode(codigo);
        funcionario.setTwoFactorVerified(false);
        funcionarioService.atualizarCode(funcionario);

        String assunto = "Seu código de verificação";
        String mensagem = "Olá, " + funcionario.getNomeFuncionario() + ".<br><br>"
                + "Seu código de verificação é: <b>" + codigo + "</b><br><br>"
                + "Este código é pessoal e intransferível. Não compartilhe com ninguém. <br><br>Atenciosamente, <br>Equipe de Segurança";

        enviador.enviar2fa(funcionario.getEmail(), assunto, mensagem);
    }
}

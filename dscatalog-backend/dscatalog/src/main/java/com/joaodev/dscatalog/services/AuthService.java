package com.joaodev.dscatalog.services;

import java.time.Instant;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.joaodev.dscatalog.dto.EmailDTO;
import com.joaodev.dscatalog.entities.PasswordRecover;
import com.joaodev.dscatalog.entities.User;
import com.joaodev.dscatalog.repositories.PassowrdRecoverRepository;
import com.joaodev.dscatalog.repositories.UserRepository;
import com.joaodev.dscatalog.services.exceptions.ResourceNotFoundException;

@Service
public class AuthService {

    @Value("${email.password-recover.token.minutes}")
    private Long tokenMinutes;

    @Value("${email.password-recover.uri}")
    private String recoverUri;

    @Autowired
    private UserRepository userRepository;

    @Autowired 
    private PassowrdRecoverRepository passowrdRecoverRepository;

    @Autowired
    private EmailService emailService;

    public void createRecoverToken(EmailDTO body) {
       
        User user = userRepository.findByEmail(body.getEmail());
        if (user == null) {
            throw new ResourceNotFoundException("Email não encontrado");
        }

        String token = UUID.randomUUID().toString();

        PasswordRecover entity = new PasswordRecover();
        entity.setEmail(body.getEmail());
        entity.setToken(token);
        entity.setExpiration(Instant.now().plusSeconds(tokenMinutes * 60L));

        entity = passowrdRecoverRepository.save(entity);

        String text = "Acesse o link para definir uma nova senha\n\n"
                + recoverUri  + token + " . Validade de " + tokenMinutes + " minutos";

        emailService.sendEmail(body.getEmail(), "Recuperação de senha", text);
    }
}

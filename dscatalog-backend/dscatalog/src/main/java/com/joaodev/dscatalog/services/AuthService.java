package com.joaodev.dscatalog.services;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.joaodev.dscatalog.dto.EmailDTO;
import com.joaodev.dscatalog.dto.NewPasswordDTO;
import com.joaodev.dscatalog.entities.PasswordRecover;
import com.joaodev.dscatalog.entities.User;
import com.joaodev.dscatalog.repositories.PassowrdRecoverRepository;
import com.joaodev.dscatalog.repositories.UserRepository;
import com.joaodev.dscatalog.services.exceptions.ResourceNotFoundException;

import jakarta.transaction.Transactional;


@Service
public class AuthService {

    @Value("${email.password-recover.token.minutes}")
    private Long tokenMinutes;

    @Value("${email.password-recover.uri}")
    private String recoverUri;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired 
    private PassowrdRecoverRepository passowrdRecoverRepository;

    @Autowired
    private EmailService emailService;

    @Transactional
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

    @Transactional
    public void saveNewPassord(NewPasswordDTO body) {
        List<PasswordRecover> result = passowrdRecoverRepository.searchValidTokens(body.getToken(), Instant.now());
        if (result.size() == 0) {
            throw new ResourceNotFoundException("Token inválido.");
        }

        User user = userRepository.findByEmail(result.get(0).getEmail());
        user.setPassword(passwordEncoder.encode(body.getPassword()));
        user = userRepository.save(user);
    }
}

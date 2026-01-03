package com.joaodev.dscatalog.services.validation;

import java.util.ArrayList;
import java.util.List;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.springframework.beans.factory.annotation.Autowired;

import com.joaodev.dscatalog.dto.UserInsertDTO;
import com.joaodev.dscatalog.entities.User;
import com.joaodev.dscatalog.repositories.UserRepository;
import com.joaodev.dscatalog.resources.exceptions.FieldMessage;

public class UserInsertValidator implements ConstraintValidator<UserInsertValid, UserInsertDTO> {
	
    @Autowired
    private UserRepository repository;

	@Override
	public void initialize(UserInsertValid ann) {
	}

	@Override
	public boolean isValid(UserInsertDTO dto, ConstraintValidatorContext context) {
		
		List<FieldMessage> list = new ArrayList<>();
		
        User user = repository.findByEmail(dto.getEmail());
        if (user != null) {
            list.add(new FieldMessage("email", "Email já existe"));
        }

		for (FieldMessage e : list) {
			context.disableDefaultConstraintViolation();
			context.buildConstraintViolationWithTemplate(e.getMessage()).addPropertyNode(e.getFieldName())
					.addConstraintViolation();
		}
		return list.isEmpty();
	}
}
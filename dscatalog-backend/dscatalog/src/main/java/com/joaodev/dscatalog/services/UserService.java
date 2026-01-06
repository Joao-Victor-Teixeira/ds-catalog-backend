package com.joaodev.dscatalog.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.joaodev.dscatalog.dto.RoleDTO;
import com.joaodev.dscatalog.dto.UserDTO;
import com.joaodev.dscatalog.dto.UserInsertDTO;
import com.joaodev.dscatalog.dto.UserUpdateDTO;
import com.joaodev.dscatalog.entities.Role;
import com.joaodev.dscatalog.entities.User;
import com.joaodev.dscatalog.exceptions.ResourceNotFoundException;
import com.joaodev.dscatalog.projections.UserDetailsProjection;
import com.joaodev.dscatalog.repositories.RoleRepository;
import com.joaodev.dscatalog.repositories.UserRepository;
import com.joaodev.dscatalog.services.exceptions.DatabaseException;

import jakarta.persistence.EntityNotFoundException;

@Service
public class UserService implements UserDetailsService {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository repository;

    @Autowired
    private RoleRepository roleRepository;

   
    @Transactional(readOnly = true)
    public Page<UserDTO> findAllPaged(Pageable pageable){
       Page<User> list = repository.findAll(pageable);
       return list.map(x -> new UserDTO(x));
    }

    @Transactional(readOnly = true)
    public UserDTO findById(Long id){
        Optional<User> obj = repository.findById(id);
        User entity = obj.orElseThrow(() -> new ResourceNotFoundException("Entidade não encontrada"));
        return new UserDTO(entity);
    }

    @Transactional
    public UserDTO insert(UserInsertDTO dto) {
        User entity = new User();
        copyDtoToEntity(dto, entity);

        entity.getRoles().clear();
        Role role = roleRepository.findByAuthority("ROLE_OPERATOR");
        entity.getRoles().add(role);
        
        entity.setPassword(passwordEncoder.encode(dto.getPassword()));
        entity = repository.save(entity);
        return new UserDTO(entity);
    }

    @Transactional
    public UserDTO update(Long id, UserUpdateDTO dto) {
      try{
       User entity = repository.getReferenceById(id);
       copyDtoToEntity(dto, entity); 
       entity = repository.save(entity);
       return new UserDTO(entity);
      }catch(EntityNotFoundException e){
        throw new ResourceNotFoundException("Id não encontrado" + id);
      }
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    public void delete(Long id){
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Recurso não encontrado");
        }
        try{
            repository.deleteById(id);
        }
        catch(DataIntegrityViolationException e){
            throw new DatabaseException("Falha de integridade referencial");
        }
    }

    private void copyDtoToEntity(UserDTO dto,User entity){
        
        entity.setFirstName(dto.getFirstName());
        entity.setLastName(dto.getLastName());
        entity.setEmail(dto.getEmail());
        
        entity.getRoles().clear();
        for (RoleDTO roleDTO : dto.getRoles()) {
            Role role = roleRepository.getReferenceById(roleDTO.getId());
            entity.getRoles().add(role);
        }
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
          	
		List<UserDetailsProjection> result = repository.searchUserAndRolesByEmail(username);
		if (result.size() == 0) {
			throw new UsernameNotFoundException("Email não encontrado.");
		}
		
		User user = new User();
		user.setEmail(result.get(0).getUsername());
		user.setPassword(result.get(0).getPassword());
		for (UserDetailsProjection projection : result) {
			user.addRole(new Role(projection.getRoleId(), projection.getAuthority()));
		}
        
		return user;
    }
}

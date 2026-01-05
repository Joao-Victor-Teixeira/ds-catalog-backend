package com.joaodev.dscatalog.services;

import java.util.Arrays;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.joaodev.dscatalog.dto.CategoryDTO;
import com.joaodev.dscatalog.dto.ProductDTO;
import com.joaodev.dscatalog.entities.Category;
import com.joaodev.dscatalog.entities.Product;
import com.joaodev.dscatalog.exceptions.ResourceNotFoundException;
import com.joaodev.dscatalog.projections.ProductProjection;
import com.joaodev.dscatalog.repositories.CategoryRepository;
import com.joaodev.dscatalog.repositories.ProductRepository;
import com.joaodev.dscatalog.services.exceptions.DatabaseException;

import jakarta.persistence.EntityNotFoundException;

@Service
public class ProductService {


    @Autowired
    private ProductRepository repository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public Page<ProductDTO> findAllPaged(Pageable pageable){
       Page<Product> list = repository.findAll(pageable);
       return list.map(x -> new ProductDTO(x));
    }

    @Transactional(readOnly = true)
    public ProductDTO findById(Long id){
        Optional<Product> obj = repository.findById(id);
        Product entity = obj.orElseThrow(() -> new ResourceNotFoundException("Entidade não encontrada"));
        return new ProductDTO(entity, entity.getCategories());
    }

    @Transactional
    public ProductDTO insert(ProductDTO dto) {
        Product entity = new Product();
        copyDtoToEntity(dto, entity);
        entity = repository.save(entity);
        return new ProductDTO(entity);
    }

    @Transactional
    public ProductDTO update(Long id, ProductDTO dto) {
      try{
       Product entity = repository.getReferenceById(id);
       copyDtoToEntity(dto, entity); 
       entity = repository.save(entity);
       return new ProductDTO(entity);
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

    private void copyDtoToEntity(ProductDTO dto,Product entity){
        
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        entity.setDate(dto.getDate());
        entity.setImgUrl(dto.getImgUrl());
        entity.setPrice(dto.getPrice());

        entity.getCategories().clear();
        for (CategoryDTO catDTO : dto.getCategories()) {
            Category cat = categoryRepository.getReferenceById(catDTO.getId());
            entity.getCategories().add(cat);
        }
    }

    @Transactional(readOnly = true)
    public Page<ProductProjection> testQuery(Pageable pageable) {
        return repository.searchProducts(Arrays.asList(1L, 3L), "", pageable);
    }
}

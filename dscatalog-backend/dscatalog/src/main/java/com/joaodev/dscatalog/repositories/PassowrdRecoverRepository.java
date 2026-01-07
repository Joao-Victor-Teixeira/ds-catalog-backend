package com.joaodev.dscatalog.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.joaodev.dscatalog.entities.PasswordRecover;

public interface PassowrdRecoverRepository extends JpaRepository<PasswordRecover, Long> {

}

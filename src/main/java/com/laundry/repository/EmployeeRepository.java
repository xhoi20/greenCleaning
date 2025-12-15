package com.laundry.repository;


import com.laundry.entity.Employee;
import com.laundry.entity.EmployeeRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Integer> {
    Optional<Employee> findByPhone(String phone);
    List<Employee> findByRole(EmployeeRole role);

    Employee findByFirstName(String firstName);
    boolean existsByPhone(String phone);
    boolean existsByFirstName(String firstName);

}
package com.laundry.service;

import com.laundry.entity.Employee;
import com.laundry.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class CustomEmployeeDetailsService  implements UserDetailsService {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Override
    public UserDetails loadUserByUsername(String firstName) throws UsernameNotFoundException {
        Employee employee = employeeRepository.findByFirstName(firstName);
        if (employee == null) {
            throw new UsernameNotFoundException("Employee not found with name: " + firstName);
        }
        return new org.springframework.security.core.userdetails.User(
                employee.getFirstName(),
                employee.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + employee.getRole().name()))
        );
    }
}
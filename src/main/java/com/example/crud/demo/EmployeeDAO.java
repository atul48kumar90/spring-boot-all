package com.example.crud.demo;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class EmployeeDAO {
    
    private EmployeeRepository employeeRepository;

    @Autowired
    public EmployeeDAO(EmployeeRepository employeeRepository)
    {
        this.employeeRepository = employeeRepository;
    }

    public Employee saveEmployee(Employee employee)
    {
        return employeeRepository.save(employee);
    }

    public Optional<Employee> getEmployeeById(long id)
    {
        return employeeRepository.findById(id);
    }

    public List<Employee> getAllEmployee()
    {
        return employeeRepository.findAll();
    }

    public void deleteEmployeeById(long id)
    {
        employeeRepository.deleteById(id);
    }
}

package com.company.gatepass.service;

import com.company.gatepass.dto.MasterRequest;
import com.company.gatepass.entity.Department;
import com.company.gatepass.entity.Plant;
import com.company.gatepass.repository.DepartmentRepository;
import com.company.gatepass.repository.PlantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MasterService {
    private final DepartmentRepository departmentRepository;
    private final PlantRepository plantRepository;

    // Returns department master records for dropdowns and admin management.
    public List<Department> getDepartments() {
        return departmentRepository.findAll();
    }

    // Creates a department master record from the admin form.
    public Department createDepartment(MasterRequest request) {
        Department department = new Department();
        department.setDepartmentName(request.getName());
        return departmentRepository.save(department);
    }

    // Returns plant master records for dropdowns and movement requests.
    public List<Plant> getPlants() {
        return plantRepository.findAll();
    }

    // Creates a plant master record from the admin form.
    public Plant createPlant(MasterRequest request) {
        Plant plant = new Plant();
        plant.setPlantCode(request.getCode());
        plant.setPlantName(request.getName());
        plant.setLocation(request.getLocation());
        return plantRepository.save(plant);
    }
}

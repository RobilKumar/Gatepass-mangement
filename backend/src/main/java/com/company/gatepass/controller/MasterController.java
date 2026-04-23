package com.company.gatepass.controller;

import com.company.gatepass.dto.MasterRequest;
import com.company.gatepass.entity.Department;
import com.company.gatepass.entity.Plant;
import com.company.gatepass.service.MasterService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/masters")
@RequiredArgsConstructor
public class MasterController {
    private final MasterService masterService;

    // Returns department master data for dropdowns.
    @GetMapping("/departments")
    public List<Department> getDepartments() {
        return masterService.getDepartments();
    }

    // Creates a department master entry.
    @PostMapping("/departments")
    public Department createDepartment(@RequestBody MasterRequest request) {
        return masterService.createDepartment(request);
    }

    // Returns plant master data for dropdowns.
    @GetMapping("/plants")
    public List<Plant> getPlants() {
        return masterService.getPlants();
    }

    // Creates a plant master entry.
    @PostMapping("/plants")
    public Plant createPlant(@RequestBody MasterRequest request) {
        return masterService.createPlant(request);
    }
}

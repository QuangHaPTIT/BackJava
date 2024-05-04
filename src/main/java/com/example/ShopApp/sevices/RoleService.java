package com.example.ShopApp.sevices;

import com.example.ShopApp.entity.Role;
import com.example.ShopApp.repositories.RoleRepository;
import com.example.ShopApp.response.RoleResponse;
import com.example.ShopApp.sevices.impl.RoleServiceImpl;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor

public class RoleService implements RoleServiceImpl {
    private final RoleRepository roleRepository;
    @Override
    public List<RoleResponse> getAllRole() {
        List<Role> roles = roleRepository.findAll();
        List<RoleResponse> roleResponses = new ArrayList<>();
        for(Role data : roles){
            RoleResponse temp = RoleResponse.fromRole(data);
            roleResponses.add(temp);
        }
        return roleResponses;
    }
}

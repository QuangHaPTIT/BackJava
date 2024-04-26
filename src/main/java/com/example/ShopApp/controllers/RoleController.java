package com.example.ShopApp.controllers;

import com.example.ShopApp.entity.Role;
import com.example.ShopApp.response.RoleResponse;
import com.example.ShopApp.sevices.impl.RoleServiceImpl;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@CrossOrigin("*")
@RestController
@RequestMapping("${api.prefix}/roles")
@RequiredArgsConstructor
public class RoleController {
    private final RoleServiceImpl roleService;
    @GetMapping("")
    public ResponseEntity<?> getAllRoles(){
        List<RoleResponse> roleResponses = roleService.getAllRole();
        return ResponseEntity.ok(roleResponses);
    }
}

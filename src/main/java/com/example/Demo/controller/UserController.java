package com.example.Demo.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.Demo.config.CommonResult;
import com.example.Demo.dto.UserDto;
import com.example.Demo.model.User;
import com.example.Demo.service.UserService;
import com.example.Demo.vo.UserVo;

@RestController
@RequestMapping("/api/users")
public class UserController {
    @Autowired
    private UserService userService;

    @PostMapping
    public CommonResult<UserVo> createUser(@RequestBody UserDto userDto) {
        User user = new User();
        if(userService.findByUsername(userDto.getUsername()) != null) {
            return CommonResult.fail("User with username already exist");
        }
        user.setUsername(userDto.getUsername());
        user.setPassword(userDto.getPassword());
        if(userDto.getPartnerId() != null) {
            User partner = userService.findById(userDto.getPartnerId());
            user.setPartner(partner);
        }
        User savedUser = userService.saveUser(user);
        UserVo userVo = new UserVo();
        userVo.setUsername(savedUser.getUsername());
        userVo.setDateCreated(savedUser.getDateCreated());
        return CommonResult.success(userVo, "User successfully created");
    }


    @GetMapping
    public CommonResult<UserVo> getUser(@RequestParam Long id) {
        if(id == null) {
            return CommonResult.fail("Please provide id as a request parameter");
        }
        User user =  userService.findById(id);
        UserVo userVo = new UserVo();
        userVo.setUsername(user.getUsername());
        userVo.setDateCreated(user.getDateCreated());
        return CommonResult.success(userVo, "User successfully retrieved");
    }

    @RequestMapping("getAll")
    @GetMapping
    public CommonResult<List<UserVo>> getAllUsers() {
        List<User> allUsers = userService.findAll();
        List<UserVo> result = new ArrayList<>();
        for(User user: allUsers) {
            UserVo userVo = new UserVo();
            userVo.setUsername(user.getUsername());
            userVo.setDateCreated(user.getDateCreated());
            result.add(userVo);
        }
        return CommonResult.success(result, "All users successfully retrieved");
    }
}

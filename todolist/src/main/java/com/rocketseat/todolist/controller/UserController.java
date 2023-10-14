package com.rocketseat.todolist.controller;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.rocketseat.todolist.user.User;
import com.rocketseat.todolist.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/create")
    public ResponseEntity create(@RequestBody User user){
        var validation = this.userRepository.findByUsername(user.getUsername());

        if (validation != null){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Usuário já existe!");
        }

        var passwordHashed = BCrypt.withDefaults().hashToString(12,user.getPassword().toCharArray());
        user.setPassword(passwordHashed);

        var userCreated = this.userRepository.save(user);
        return ResponseEntity.ok().body(userCreated);
    }


}

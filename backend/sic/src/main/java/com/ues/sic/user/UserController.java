package com.ues.sic.user;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;


    @GetMapping
    public List<UserModel> getAllUsers(){
        return userRepository.findAll();
    }

    @GetMapping("/{id}")
    public UserModel getUserById(@PathVariable Long id){
        return userRepository.findById(id).orElse(null);
    }

    @PostMapping
    public UserModel createUser(@RequestBody UserModel user){
        return userRepository.save(user);
    }

    @PutMapping("/{id}")
    public UserModel updateUser(@PathVariable Long id, @RequestBody UserModel userDetails){
        UserModel user = userRepository.findById(id).orElse(null);
        if(user != null){
            user.setName(userDetails.getName());
            user.setEmail(userDetails.getEmail());
            return userRepository.save(user);
        }
        return null;
    }

    @DeleteMapping("/{id}")
    public String deleteUser(@PathVariable Long id){
        //encontrar el user
        UserModel user = userRepository.findById(id).orElse(null);
        if(user == null){
            return "User not found";
        }
        try {
            userRepository.deleteById(id);
            return "User deleted successfully";
        } catch (Exception e) {
            return "Error deleting user: " + e.getMessage();
        }
    }
}

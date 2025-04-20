package com.project.lawrence.insurance_tracker.service;

import com.project.lawrence.insurance_tracker.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import com.project.lawrence.insurance_tracker.repository.UserRepository;

import java.util.List;

@Service
public class UserService implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;

    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user =  userRepository.findByUserName(username);
        if(user == null){
            throw new UsernameNotFoundException("User not found");
        }
        return new org.springframework.security.core.userdetails.User(user.getUserName(), user.getUserPassword(), List.of(new SimpleGrantedAuthority("USER")));
    }

    public User getUserByUsername(String username) {
        return userRepository.findByUserName(username);
    }

    public User getUserByEmail(String useremail) {
        return userRepository.findByUserEmail(useremail);
    }

}

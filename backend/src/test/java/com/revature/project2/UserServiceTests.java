package com.revature.project2;

import com.revature.project2.models.DTOs.OutgoingUserDTO;
import com.revature.project2.models.User;
import com.revature.project2.repositories.UserRepository;
import com.revature.project2.services.UserServices;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;


public class UserServiceTests {
    private UserRepository userRepository;
    private UserServices userServices;

    @BeforeEach
    void contextLoads(){
        userRepository = Mockito.mock(UserRepository.class);
        userServices = new UserServices(userRepository);

    }

    @Test
    void test_register_userDoesntExist(){
        User newUser = new User("NewUser","password","User@users.com","User","User", "Employee");
        Optional<User> notFound = Optional.empty();
        when(userRepository.findByUsername("User")).thenReturn(notFound);
        when(userRepository.save(newUser)).thenReturn(newUser);
        User responseUser =  userServices.register(newUser);
        Assertions.assertTrue(responseUser.getUsername().equals(newUser.getUsername()));
    }

    @Test
    void test_register_userExists(){
        User newUser = new User("NewUser","password","User@users.com","User","User", "Employee");

        when(userRepository.findByUsername("NewUser")).thenReturn(Optional.of(newUser));

        Assertions.assertThrows(IllegalArgumentException.class, ()->userServices.register(newUser));
    }
    @ParameterizedTest
    @CsvSource({"Username,,User,User",",password,User,User","Username,password,,User","Username,password,User,"})
    void test_register_inValid(String username, String password){
        User newUser = new User("NewUser","","User@users.com","User","User", "Employee");

        when(userRepository.findByUsername("NewUser")).thenReturn(Optional.of(newUser));

        Assertions.assertThrows(IllegalArgumentException.class, ()->userServices.register(newUser));
    }

    @Test
    void test_update(){
        User user = new User("User","password","User@users.com","User","User", "Employee");
        User userUpdate = new User("User","password2","User2@users.com","User2","User3", "Employee");

        ArgumentCaptor<User> userCapture = ArgumentCaptor.forClass(User.class);
        when(userRepository.findByUsername("User")).thenReturn(Optional.of(user));
        when(userRepository.save(userCapture.capture())).thenReturn(userUpdate);
        userServices.update(userUpdate);
        User userUpdated = userCapture.getValue();
        Assertions.assertTrue("password2".equals(userUpdated.getPassword()));
        Assertions.assertTrue("User2@users.com".equals(userUpdated.getEmail()));
        Assertions.assertTrue("User2".equals(userUpdated.getFirstName()));
        Assertions.assertTrue("User3".equals(userUpdated.getLastName()));
        Mockito.verify(userRepository).save(Mockito.any(User.class));
    }

    @Test
    void test_userExists(){
        User user = new User("User","password","User@users.com","User","User", "Employee");
        when(userRepository.findByUsername("User")).thenReturn(Optional.of(user));
        when(userRepository.findByUsername("NotUser")).thenReturn(Optional.empty());
        Assertions.assertTrue(userServices.userExists("User"));
        Assertions.assertFalse(userServices.userExists("NotUser"));
    }

    @Test
    void test_getByUsername(){
        User user = new User("User","password","User@users.com","User","User", "Employee");
        when(userRepository.findByUsername("User")).thenReturn(Optional.of(user));
        when(userRepository.findByUsername("NotUser")).thenReturn(Optional.empty());
        User responseUser = userServices.getUserByUsername("User");
        Assertions.assertTrue(responseUser.getUsername().equals("User"));
        Assertions.assertThrows(UsernameNotFoundException.class, ()->userServices.getUserByUsername("NotUser"));    }


    @Test
    void test_deleteByUsername(){
        User user = new User("User","password","User@users.com","User","User", "Employee");
        user.setEnvelopes(new ArrayList<>()); // setting list of envelopes to empty
        when(userRepository.findByUsername("User")).thenReturn(Optional.of(user));
        when(userRepository.findByUsername("NotUser")).thenReturn(Optional.empty());
        ArgumentCaptor<User> userCapture = ArgumentCaptor.forClass(User.class);
        doNothing().when(userRepository).delete(userCapture.capture());
        userServices.deleteByUsername("User");
        Assertions.assertTrue("User".equals(userCapture.getValue().getUsername()));
        Mockito.verify(userRepository).delete(Mockito.any(User.class));
        Assertions.assertThrows(UsernameNotFoundException.class, ()->userServices.deleteByUsername("NotUser"));
    }



    @Test
    void test_findAllUsers(){
        List<User> userList = new ArrayList<>();
        userList.add(new  User("User1","password","User@users.com","User","User", "Employee"));
        when(userRepository.findAll()).thenReturn(userList);
        List<OutgoingUserDTO> outUserList = userServices.getAllUsers();
        Assertions.assertTrue(outUserList.size()==1);
        Assertions.assertTrue("User1".equals(outUserList.get(0).getUsername()));
    }

}

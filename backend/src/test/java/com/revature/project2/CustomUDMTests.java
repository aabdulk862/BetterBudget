package com.revature.project2;

import com.revature.project2.models.User;
import com.revature.project2.security.authentication.CustomUDM;
import com.revature.project2.security.authentication.CustomUserDetails;
import com.revature.project2.services.UserServices;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.mockito.Mockito.*;

public class CustomUDMTests {
    private CustomUDM customUDM;
    private PasswordEncoder passwordEncoder;
    private UserServices userServices;

    @BeforeEach
    void contextLoads(){
        passwordEncoder = Mockito.mock(PasswordEncoder.class);
        userServices = Mockito.mock(UserServices.class);
        customUDM = new CustomUDM(userServices, passwordEncoder);
    }

    @Test
    public void test_userExists(){
        when(userServices.userExists("User")).thenReturn(true);
        when(userServices.userExists("NotUser")).thenReturn(false);
        Assertions.assertTrue(customUDM.userExists("User"));
        Assertions.assertFalse(customUDM.userExists("NotUser"));
    }

    @Test
    public void test_loadUserByUsername(){
        User user = new User("User", "password","user@user.com","user_fn","user_ln","employee");
        when(userServices.getUserByUsername("User")).thenReturn(user);
        UserDetails findUser = customUDM.loadUserByUsername("User");

        Assertions.assertTrue("User".equals(findUser.getUsername()));
    }

    @Test
    public void test_createUser_valid(){
        User user = new User("User", "password","user@user.com","user_fn","user_ln","employee");
        when(passwordEncoder.encode("password")).thenReturn("passwordEncoded");
        CustomUserDetails customUserDetails = new CustomUserDetails(user, passwordEncoder);
        ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);
        when(userServices.register(userArgumentCaptor.capture())).thenReturn(user);
        customUDM.createUser(customUserDetails);
        User createUser = userArgumentCaptor.getValue();
        Assertions.assertTrue("User".equals(createUser.getUsername()));
        Assertions.assertTrue("passwordEncoded".equals(createUser.getPassword()));
        Assertions.assertTrue("user@user.com".equals(createUser.getEmail()));
        Assertions.assertTrue("user_fn".equals(createUser.getFirstName()));
        Assertions.assertTrue("user_ln".equals(createUser.getLastName()));
    }

    @Test
    public void test_createUser_invalid(){
        Assertions.assertThrows(IllegalArgumentException.class, ()->customUDM.createUser(null));
        verify(userServices, never()).register(Mockito.any(User.class));
    }

    @Test
    public void test_updateUser_valid(){
        User user = new User("User", "password","user@user.com","user_fn","user_ln","employee");
        when(passwordEncoder.encode("password")).thenReturn("passwordEncoded");
        CustomUserDetails customUserDetails = new CustomUserDetails(user, passwordEncoder);
        ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);
        doNothing().when(userServices).update(userArgumentCaptor.capture());
        customUDM.updateUser(customUserDetails);
        User updateUser = userArgumentCaptor.getValue();
        Assertions.assertTrue("User".equals(updateUser.getUsername()));
        Assertions.assertTrue("passwordEncoded".equals(updateUser.getPassword()));
        Assertions.assertTrue("user@user.com".equals(updateUser.getEmail()));
        Assertions.assertTrue("user_fn".equals(updateUser.getFirstName()));
        Assertions.assertTrue("user_ln".equals(updateUser.getLastName()));
    }

    @Test
    public void test_updateUser_invalid(){
        Assertions.assertThrows(IllegalArgumentException.class, ()->customUDM.updateUser(null));
        verify(userServices, never()).update(Mockito.any(User.class));
    }

    @Test
    public void test_deleteUser(){
        ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
        doNothing().when(userServices).deleteByUsername(stringArgumentCaptor.capture());
        customUDM.deleteUser("User");
        Assertions.assertTrue("User".equals(stringArgumentCaptor.getValue()));
    }


    @Test
    public void test_changePassword_valid(){
        Authentication authentication = Mockito.mock(Authentication.class);
        when(authentication.getName()).thenReturn("User");
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        User user = new User();
        user.setUserId(0);
        user.setUsername("User");
        user.setPassword("OldPassword");
        ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);
        when(userServices.getUserByUsername("User")).thenReturn(user);

        when(passwordEncoder.matches("NewPassword", "OldPassword")).thenReturn(false);
        when(passwordEncoder.matches("OldPassword", "NewPassword")).thenReturn(false);
        when(passwordEncoder.matches("OldPassword", "OldPassword")).thenReturn(true);
        when(passwordEncoder.encode("OldPassword")).thenReturn("OldPasswordEncoded");
        when(passwordEncoder.encode("NewPassword")).thenReturn("NewPasswordEncoded");
        doNothing().when(userServices).update(userArgumentCaptor.capture());

        customUDM.changePassword("OldPassword", "NewPassword");

        User savedUser = userArgumentCaptor.getValue();
        Assertions.assertTrue("User".equals(savedUser.getUsername()));
        Assertions.assertTrue("NewPasswordEncoded".equals(savedUser.getPassword()));
    }


    @Test
    public void test_changePassword_invalid_wrongPassword(){
        Authentication authentication = Mockito.mock(Authentication.class);
        when(authentication.getName()).thenReturn("User");
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        User user = new User();
        user.setUserId(0);
        user.setUsername("User");
        user.setPassword("OldPassword");
        when(userServices.getUserByUsername("User")).thenReturn(user);

        when(passwordEncoder.matches("OldPassword", "WrongPassword")).thenReturn(false);
        when(passwordEncoder.matches("WrongPassword", "OldPassword")).thenReturn(false);
        when(passwordEncoder.matches("OldPassword", "NewPassword")).thenReturn(false);
        when(passwordEncoder.matches("OldPassword", "OldPassword")).thenReturn(true);
        when(passwordEncoder.encode("OldPassword")).thenReturn("OldPasswordEncoded");
        when(passwordEncoder.encode("NewPassword")).thenReturn("NewPasswordEncoded");

        Assertions.assertThrows(BadCredentialsException.class, ()->customUDM.changePassword("WrongPassword", "NewPassword"));
        Mockito.verify(userServices, never()).update(Mockito.any(User.class));
    }


    @Test
    public void test_changePassword_invalid_emptyContext(){
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(null);
        SecurityContextHolder.setContext(securityContext);

        Assertions.assertThrows(UsernameNotFoundException.class, ()->customUDM.changePassword("OldPassword", "NewPassword"));
        Mockito.verify(userServices, never()).update(Mockito.any(User.class));
    }


}

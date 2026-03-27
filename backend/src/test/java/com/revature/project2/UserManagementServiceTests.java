package com.revature.project2;

import com.revature.project2.models.User;
import com.revature.project2.security.UserRoles;
import com.revature.project2.security.authentication.CustomUDM;
import com.revature.project2.security.authentication.CustomUserDetails;
import com.revature.project2.services.AuthenticationService;
import com.revature.project2.services.UserManagementService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;

import static org.mockito.Mockito.*;

public class UserManagementServiceTests {
    private CustomUDM customUDM;
    private AuthenticationService authenticationService;
    private PasswordEncoder passwordEncoder;
    private UserManagementService userManagementService;

    @BeforeEach
    public void contextLoad(){
        customUDM = Mockito.mock(CustomUDM.class);
        authenticationService = Mockito.mock(AuthenticationService.class);
        passwordEncoder = Mockito.mock(PasswordEncoder.class);
        userManagementService = new UserManagementService(customUDM, authenticationService, passwordEncoder);
    }

    @Test
    public void test_createUser_valid_noRole(){
        User user = new User("User", "password", "User@User.com","user_fn","user_ln",null);
        when(passwordEncoder.encode("password")).thenReturn("passwordEncoded");
        ArgumentCaptor<CustomUserDetails> customUserDetailsArgumentCaptor = ArgumentCaptor.forClass(CustomUserDetails.class);
        doNothing().when(customUDM).createUser(customUserDetailsArgumentCaptor.capture());

        userManagementService.createUser(user);

        CustomUserDetails createUser = customUserDetailsArgumentCaptor.getValue();
        Assertions.assertTrue("User".equals(createUser.getUsername()));
        Assertions.assertTrue("passwordEncoded".equals(createUser.getPassword()));
        Assertions.assertTrue("ROLE_EMPLOYEE".equals(createUser.getRoles().get(0).toString()));
    }

    @Test
    public void test_createUser_valid_Employee(){
        User user = new User("User", "password", "User@User.com","user_fn","user_ln","ROLE_EMPLOYEE");
        when(passwordEncoder.encode("password")).thenReturn("passwordEncoded");
        ArgumentCaptor<CustomUserDetails> customUserDetailsArgumentCaptor = ArgumentCaptor.forClass(CustomUserDetails.class);
        doNothing().when(customUDM).createUser(customUserDetailsArgumentCaptor.capture());

        userManagementService.createUser(user);

        CustomUserDetails createUser = customUserDetailsArgumentCaptor.getValue();
        Assertions.assertTrue("User".equals(createUser.getUsername()));
        Assertions.assertTrue("passwordEncoded".equals(createUser.getPassword()));
        Assertions.assertTrue("ROLE_EMPLOYEE".equals(createUser.getRoles().get(0).toString()));
    }

    @Test
    public void test_createUser_valid_Manager(){
        User user = new User("User", "password", "User@User.com","user_fn","user_ln","ROLE_MANAGER");
        when(passwordEncoder.encode("password")).thenReturn("passwordEncoded");
        ArgumentCaptor<CustomUserDetails> customUserDetailsArgumentCaptor = ArgumentCaptor.forClass(CustomUserDetails.class);
        doNothing().when(customUDM).createUser(customUserDetailsArgumentCaptor.capture());

        userManagementService.createUser(user);

        CustomUserDetails createUser = customUserDetailsArgumentCaptor.getValue();
        Assertions.assertTrue("User".equals(createUser.getUsername()));
        Assertions.assertTrue("passwordEncoded".equals(createUser.getPassword()));
        Assertions.assertTrue("ROLE_MANAGER".equals(createUser.getRoles().get(0).toString()));
    }

    @Test
    public void test_createUser_invalid_roleInvalid(){
        User user = new User("User", "password", "User@User.com","user_fn","user_ln","ROLE_DARTH");
        when(passwordEncoder.encode("password")).thenReturn("passwordEncoded");
        ArgumentCaptor<CustomUserDetails> customUserDetailsArgumentCaptor = ArgumentCaptor.forClass(CustomUserDetails.class);
        doNothing().when(customUDM).createUser(customUserDetailsArgumentCaptor.capture());


        Assertions.assertThrows(Exception.class, ()->userManagementService.createUser(user));
        verify(customUDM, never()).createUser(Mockito.any(CustomUserDetails.class));
    }

    @Test
    public void test_createUser_invalid_passwordShort(){
        User user = new User("User", "pw", "User@User.com","user_fn","user_ln","ROLE_JEDI_MASTER");
        when(passwordEncoder.encode("pw")).thenReturn("passwordEncoded");
        ArgumentCaptor<CustomUserDetails> customUserDetailsArgumentCaptor = ArgumentCaptor.forClass(CustomUserDetails.class);
        doNothing().when(customUDM).createUser(customUserDetailsArgumentCaptor.capture());

        Assertions.assertThrows(Exception.class, ()->userManagementService.createUser(user));
        verify(customUDM, never()).createUser(Mockito.any(CustomUserDetails.class));
    }

    @Test
    public void test_updateUser_valid_employee(){
        User user = new User("User", "password", "User@User.com","user_fn","user_ln","ROLE_EMPLOYEE");
        when(passwordEncoder.encode("password")).thenReturn("passwordEncoded");
        ArgumentCaptor<CustomUserDetails> customUserDetailsArgumentCaptor = ArgumentCaptor.forClass(CustomUserDetails.class);
        doNothing().when(customUDM).updateUser(customUserDetailsArgumentCaptor.capture());
        Map.Entry<String, List<UserRoles>> authObj = new AbstractMap.SimpleEntry<>("User", Arrays.asList(UserRoles.ROLE_EMPLOYEE));
        when(authenticationService.getAuthenticatedUser()).thenReturn(Optional.of(authObj));

        userManagementService.updateUser(user);

        CustomUserDetails createUser = customUserDetailsArgumentCaptor.getValue();
        Assertions.assertTrue("User".equals(createUser.getUsername()));
        Assertions.assertTrue("passwordEncoded".equals(createUser.getPassword()));
        Assertions.assertTrue("ROLE_EMPLOYEE".equals(createUser.getRoles().get(0).toString()));
    }

    @Test
    public void test_updateUser_valid_managerUpdateSelf(){
        User user = new User("User", "password", "User@User.com","user_fn","user_ln","ROLE_MANAGER");
        when(passwordEncoder.encode("password")).thenReturn("passwordEncoded");
        ArgumentCaptor<CustomUserDetails> customUserDetailsArgumentCaptor = ArgumentCaptor.forClass(CustomUserDetails.class);
        doNothing().when(customUDM).updateUser(customUserDetailsArgumentCaptor.capture());
        Map.Entry<String, List<UserRoles>> authObj = new AbstractMap.SimpleEntry<>("User", Arrays.asList(UserRoles.ROLE_MANAGER));
        when(authenticationService.getAuthenticatedUser()).thenReturn(Optional.of(authObj));

        userManagementService.updateUser(user);

        CustomUserDetails createUser = customUserDetailsArgumentCaptor.getValue();
        Assertions.assertTrue("User".equals(createUser.getUsername()));
        Assertions.assertTrue("passwordEncoded".equals(createUser.getPassword()));
        Assertions.assertTrue("ROLE_MANAGER".equals(createUser.getRoles().get(0).toString()));
    }

    @Test
    public void test_updateUser_valid_managerUpdateOther(){
        User user = new User("User", "password", "User@User.com","user_fn","user_ln","ROLE_EMPLOYEE");
        when(passwordEncoder.encode("password")).thenReturn("passwordEncoded");
        ArgumentCaptor<CustomUserDetails> customUserDetailsArgumentCaptor = ArgumentCaptor.forClass(CustomUserDetails.class);
        doNothing().when(customUDM).updateUser(customUserDetailsArgumentCaptor.capture());
        Map.Entry<String, List<UserRoles>> authObj = new AbstractMap.SimpleEntry<>("notUser", Arrays.asList(UserRoles.ROLE_MANAGER));
        when(authenticationService.getAuthenticatedUser()).thenReturn(Optional.of(authObj));

        userManagementService.updateUser(user);

        CustomUserDetails createUser = customUserDetailsArgumentCaptor.getValue();
        Assertions.assertTrue("User".equals(createUser.getUsername()));
        Assertions.assertTrue("passwordEncoded".equals(createUser.getPassword()));
        Assertions.assertTrue("ROLE_EMPLOYEE".equals(createUser.getRoles().get(0).toString()));
    }

    @Test
    public void test_updateUser_invalid_employeeUpdateOther(){
        User user = new User("User", "password", "User@User.com","user_fn","user_ln","ROLE_MANAGER");
        when(passwordEncoder.encode("password")).thenReturn("passwordEncoded");
        ArgumentCaptor<CustomUserDetails> customUserDetailsArgumentCaptor = ArgumentCaptor.forClass(CustomUserDetails.class);
        doNothing().when(customUDM).updateUser(customUserDetailsArgumentCaptor.capture());
        Map.Entry<String, List<UserRoles>> authObj = new AbstractMap.SimpleEntry<>("notUser", Arrays.asList(UserRoles.ROLE_EMPLOYEE));
        when(authenticationService.getAuthenticatedUser()).thenReturn(Optional.of(authObj));

        Assertions.assertThrows(Exception.class, ()->userManagementService.updateUser(user));
        verify(customUDM, never()).updateUser(Mockito.any(CustomUserDetails.class));
    }


    @Test
    public void test_updateUser_invalid_shortPassword(){
        User user = new User("User", "pw", "User@User.com","user_fn","user_ln","ROLE_MANAGER");
        when(passwordEncoder.encode("pw")).thenReturn("passwordEncoded");
        ArgumentCaptor<CustomUserDetails> customUserDetailsArgumentCaptor = ArgumentCaptor.forClass(CustomUserDetails.class);
        doNothing().when(customUDM).updateUser(customUserDetailsArgumentCaptor.capture());
        Map.Entry<String, List<UserRoles>> authObj = new AbstractMap.SimpleEntry<>("User", Arrays.asList(UserRoles.ROLE_MANAGER));
        when(authenticationService.getAuthenticatedUser()).thenReturn(Optional.of(authObj));

        Assertions.assertThrows(Exception.class, ()->userManagementService.updateUser(user));
        verify(customUDM, never()).updateUser(Mockito.any(CustomUserDetails.class));
    }


    @Test
    public void test_updateUser_invalid_roleInvalid(){
        User user = new User("User", "password", "User@User.com","user_fn","user_ln","ROLE_SITH_LORD");
        when(passwordEncoder.encode("password")).thenReturn("passwordEncoded");
        ArgumentCaptor<CustomUserDetails> customUserDetailsArgumentCaptor = ArgumentCaptor.forClass(CustomUserDetails.class);
        doNothing().when(customUDM).updateUser(customUserDetailsArgumentCaptor.capture());
        Map.Entry<String, List<UserRoles>> authObj = new AbstractMap.SimpleEntry<>("User", Arrays.asList(UserRoles.ROLE_EMPLOYEE));
        when(authenticationService.getAuthenticatedUser()).thenReturn(Optional.of(authObj));

        Assertions.assertThrows(Exception.class, ()->userManagementService.updateUser(user));
        verify(customUDM, never()).updateUser(Mockito.any(CustomUserDetails.class));
    }

    @Test
    public void test_deleteUser_valid_employee(){
        String username = "User";
        ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
        doNothing().when(customUDM).deleteUser(stringArgumentCaptor.capture());
        Map.Entry<String, List<UserRoles>> authObj = new AbstractMap.SimpleEntry<>("User", Arrays.asList(UserRoles.ROLE_EMPLOYEE));
        when(authenticationService.getAuthenticatedUser()).thenReturn(Optional.of(authObj));

        userManagementService.deleteUser(username);
        String deletedUser = stringArgumentCaptor.getValue();
        Assertions.assertTrue("User".equals(deletedUser));
    }

    @Test
    public void test_deleteUser_valid_managerSelf(){
        String username = "User";
        ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
        doNothing().when(customUDM).deleteUser(stringArgumentCaptor.capture());
        Map.Entry<String, List<UserRoles>> authObj = new AbstractMap.SimpleEntry<>("User", Arrays.asList(UserRoles.ROLE_MANAGER));
        when(authenticationService.getAuthenticatedUser()).thenReturn(Optional.of(authObj));

        userManagementService.deleteUser(username);
        String deletedUser = stringArgumentCaptor.getValue();
        Assertions.assertTrue("User".equals(deletedUser));
    }

    @Test
    public void test_deleteUser_valid_managerOther(){
        String username = "User";
        ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
        doNothing().when(customUDM).deleteUser(stringArgumentCaptor.capture());
        Map.Entry<String, List<UserRoles>> authObj = new AbstractMap.SimpleEntry<>("notUser", Arrays.asList(UserRoles.ROLE_MANAGER));
        when(authenticationService.getAuthenticatedUser()).thenReturn(Optional.of(authObj));

        userManagementService.deleteUser(username);
        String deletedUser = stringArgumentCaptor.getValue();
        Assertions.assertTrue("User".equals(deletedUser));
    }

    @Test
    public void test_deleteUser_invalid_employeeDeleteOther(){
        String username = "User";
        ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
        doNothing().when(customUDM).deleteUser(stringArgumentCaptor.capture());
        Map.Entry<String, List<UserRoles>> authObj = new AbstractMap.SimpleEntry<>("notUser", Arrays.asList(UserRoles.ROLE_EMPLOYEE));
        when(authenticationService.getAuthenticatedUser()).thenReturn(Optional.of(authObj));

        Assertions.assertThrows(Exception.class, ()->userManagementService.deleteUser(username));
        verify(customUDM, never()).deleteUser(Mockito.any(String.class));
    }




}

package edu.tcu.cs.hogwartsartifactsonline.hogwartsuser;

import edu.tcu.cs.hogwartsartifactsonline.system.exception.ObjectNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    UserRepository userRepository;

    @Mock
    PasswordEncoder passwordEncoder;

    @InjectMocks
    UserService userService;

    List<HogwartsUser> hogwartsUsers;

    @BeforeEach
    void setUp() {
        HogwartsUser u1 = new HogwartsUser(1, "john", "123456", true, "admin user");
        HogwartsUser u2 = new HogwartsUser(2, "eric", "654321", true, "user");
        HogwartsUser u3 = new HogwartsUser(3, "tom", "qwerty", false, "user");

        this.hogwartsUsers = new ArrayList<>();
        this.hogwartsUsers.add(u1);
        this.hogwartsUsers.add(u2);
        this.hogwartsUsers.add(u3);
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void testFindAllSuccess() {
        given(this.userRepository.findAll()).willReturn(this.hogwartsUsers);

        List<HogwartsUser> actualUsers = this.userService.findAll();

        assertThat(actualUsers.size()).isEqualTo(this.hogwartsUsers.size());

        verify(this.userRepository, times(1)).findAll();
    }

    @Test
    void testFindByIdSuccess() {
        HogwartsUser u = new HogwartsUser(1, "john", "123456", true, "admin user");

        given(this.userRepository.findById(1)).willReturn(Optional.of(u));

        HogwartsUser returnedUser = this.userService.findById(1);

        assertThat(returnedUser.getId()).isEqualTo(u.getId());
        assertThat(returnedUser.getUsername()).isEqualTo(u.getUsername());
        assertThat(returnedUser.getPassword()).isEqualTo(u.getPassword());
        assertThat(returnedUser.isEnabled()).isEqualTo(u.isEnabled());
        assertThat(returnedUser.getRoles()).isEqualTo(u.getRoles());
        verify(this.userRepository, times(1)).findById(1);
    }

    @Test
    void testFindByIdNotFound() {
        given(this.userRepository.findById(Mockito.any(Integer.class))).willReturn(Optional.empty());

        Throwable thrown = catchThrowable(() -> this.userService.findById(1));

        assertThat(thrown)
                .isInstanceOf(ObjectNotFoundException.class)
                .hasMessage("Could not find user with Id 1 :(");
        verify(this.userRepository, times(1)).findById(Mockito.any(Integer.class));
    }

    @Test
    void testSaveSuccess() {
        HogwartsUser newUser = new HogwartsUser(null, "lily", "123456", true, "user");

        given(this.passwordEncoder.encode(newUser.getPassword())).willReturn("Encoded Password");
        given(this.userRepository.save(newUser)).willReturn(newUser);

        HogwartsUser returnedUser = this.userService.save(newUser);

        assertThat(returnedUser.getUsername()).isEqualTo(newUser.getUsername());
        assertThat(returnedUser.getPassword()).isEqualTo(newUser.getPassword());
        assertThat(returnedUser.isEnabled()).isEqualTo(newUser.isEnabled());
        assertThat(returnedUser.getRoles()).isEqualTo(newUser.getRoles());
        verify(this.userRepository, times(1)).save(newUser);
    }

    @Test
    void testUpdateSuccess() {
        HogwartsUser oldUser = new HogwartsUser(1, "john", "123456", true, "admin user");

        HogwartsUser update = new HogwartsUser(null, "john - update", "123456", true, "admin user");

        given(this.userRepository.findById(1)).willReturn(Optional.of(oldUser));
        given(this.userRepository.save(oldUser)).willReturn(oldUser);

        HogwartsUser updatedUser = this.userService.update(1, update);

        assertThat(updatedUser.getId()).isEqualTo(1);
        assertThat(updatedUser.getUsername()).isEqualTo(update.getUsername());
        verify(this.userRepository, times(1)).findById(1);
        verify(this.userRepository, times(1)).save(oldUser);
    }

    @Test
    void testUpdateNotFound() {
        HogwartsUser update = new HogwartsUser(null, "john - update", "123456", true, "admin user");

        given(this.userRepository.findById(1)).willReturn(Optional.empty());

        Throwable thrown = assertThrows(ObjectNotFoundException.class, () -> this.userService.update(1, update));

        assertThat(thrown)
                .isInstanceOf(ObjectNotFoundException.class)
                .hasMessage("Could not find user with Id 1 :(");
        verify(this.userRepository, times(1)).findById(1);
    }

    @Test
    void testDeleteSuccess() {
        HogwartsUser user = new HogwartsUser(1, "john", "123456", true, "admin user");

        given(this.userRepository.findById(1)).willReturn(Optional.of(user));
        doNothing().when(this.userRepository).deleteById(1);

        this.userService.delete(1);

        verify(this.userRepository, times(1)).deleteById(1);
    }

    @Test
    void testDeleteNotFound() {
        given(this.userRepository.findById(1)).willReturn(Optional.empty());

        Throwable thrown = assertThrows(ObjectNotFoundException.class, () -> this.userService.delete(1));

        assertThat(thrown)
                .isInstanceOf(ObjectNotFoundException.class)
                .hasMessage("Could not find user with Id 1 :(");
        verify(this.userRepository, times(1)).findById(1);
    }
}
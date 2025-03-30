package edu.tcu.cs.hogwartsartifactsonline.hogwartsuser;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.tcu.cs.hogwartsartifactsonline.hogwartsuser.converter.UserDtoToUserConverter;
import edu.tcu.cs.hogwartsartifactsonline.hogwartsuser.converter.UserToUserDtoConverter;
import edu.tcu.cs.hogwartsartifactsonline.hogwartsuser.dto.UserDto;
import edu.tcu.cs.hogwartsartifactsonline.system.StatusCode;
import edu.tcu.cs.hogwartsartifactsonline.system.exception.ObjectNotFoundException;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    UserService userService;

    @MockBean
    UserToUserDtoConverter userToUserDtoConverter;

    @MockBean
    UserDtoToUserConverter userDtoToUserConverter;

    List<HogwartsUser> users;

    @Value("${api.endpoint.base-url}")
    String baseUrl;

    @BeforeEach
    void setUp() {
        this.users = new ArrayList<>();

        HogwartsUser u1 = new HogwartsUser(1, "john", "123456", true, "admin user");
        HogwartsUser u2 = new HogwartsUser(2, "eric", "654321", true, "user");
        HogwartsUser u3 = new HogwartsUser(3, "tom", "qwerty", false, "user");

        this.users.add(u1);
        this.users.add(u2);
        this.users.add(u3);
    }

    @Test
    void testFindAllUsersSuccess() throws Exception {
        given(this.userService.findAll()).willReturn(this.users);

        given(this.userToUserDtoConverter.convert(this.users.get(0)))
                .willReturn(new UserDto(1, "john", true, "admin user"));
        given(this.userToUserDtoConverter.convert(this.users.get(1)))
                .willReturn(new UserDto(2, "eric", true, "user"));
        given(this.userToUserDtoConverter.convert(this.users.get(2)))
                .willReturn(new UserDto(3, "tom", false, "user"));

        this.mockMvc.perform(get(this.baseUrl + "/users").accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Find All Success"))
                .andExpect(jsonPath("$.data", Matchers.hasSize(this.users.size())))
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].username").value("john"))
                .andExpect(jsonPath("$.data[1].id").value(2))
                .andExpect(jsonPath("$.data[1].username").value("eric"));
    }

    @Test
    void testFindUserByIdSuccess() throws Exception {
        HogwartsUser foundUser = this.users.get(1);
        given(this.userService.findById(2)).willReturn(foundUser);
        given(this.userToUserDtoConverter.convert(foundUser)).willReturn(new UserDto(2, "eric", true, "user"));

        this.mockMvc.perform(get(this.baseUrl + "/users/2").accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Find One Success"))
                .andExpect(jsonPath("$.data.id").value(2))
                .andExpect(jsonPath("$.data.username").value("eric"));
    }

    @Test
    void testFindUserByIdNotFound() throws Exception {
        given(this.userService.findById(5)).willThrow(new ObjectNotFoundException("user", 5));

        this.mockMvc.perform(get(this.baseUrl + "/users/5").accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
                .andExpect(jsonPath("$.message").value("Could not find user with Id 5 :("))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void testAddUserSuccess() throws Exception {
        HogwartsUser user = new HogwartsUser(4, "lily", "123456", true, "admin user");
        String json = this.objectMapper.writeValueAsString(user);

        given(this.userService.save(Mockito.any(HogwartsUser.class))).willReturn(user);
        given(this.userToUserDtoConverter.convert(Mockito.any(HogwartsUser.class))).willReturn(new UserDto(4, "lily", true, "admin user"));

        this.mockMvc.perform(post(this.baseUrl + "/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Add Success"))
                .andExpect(jsonPath("$.data.id").value(4))
                .andExpect(jsonPath("$.data.username").value("lily"))
                .andExpect(jsonPath("$.data.enabled").value(true))
                .andExpect(jsonPath("$.data.roles").value("admin user"));
    }

    @Test
    void testUpdateUserSuccess() throws Exception {
        UserDto userDto = new UserDto(3, "tom123", false, "user");

        HogwartsUser updatedUser = new HogwartsUser(3, "tom123", "qwerty", false, "user");

        String json = this.objectMapper.writeValueAsString(userDto);

        given(this.userDtoToUserConverter.convert(Mockito.any(UserDto.class))).willReturn(updatedUser);
        given(this.userService.update(eq(3), Mockito.any(HogwartsUser.class))).willReturn(updatedUser);
        given(this.userToUserDtoConverter.convert(updatedUser)).willReturn(userDto);

        this.mockMvc.perform(put(this.baseUrl + "/users/3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Update Success"))
                .andExpect(jsonPath("$.data.id").value(3))
                .andExpect(jsonPath("$.data.username").value("tom123"))
                .andExpect(jsonPath("$.data.enabled").value(false))
                .andExpect(jsonPath("$.data.roles").value("user"));
    }

    @Test
    void testUpdateUserErrorWithNonExistentId() throws Exception {
        given(this.userDtoToUserConverter.convert(Mockito.any(UserDto.class)))
                .willReturn(new HogwartsUser(5, "tom123", "qwerty", false, "user"));
        given(this.userService.update(eq(5), Mockito.any(HogwartsUser.class)))
                .willThrow(new ObjectNotFoundException("user", 5));

        UserDto userDto = new UserDto(5, "tom123", false, "user");
        String json = this.objectMapper.writeValueAsString(userDto);

        this.mockMvc.perform(put(this.baseUrl + "/users/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
                .andExpect(jsonPath("$.message").value("Could not find user with Id 5 :("))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void testDeleteUserSuccess() throws Exception {
        doNothing().when(this.userService).delete(2);

        this.mockMvc.perform(delete(this.baseUrl + "/users/2").accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Delete Success"));
    }

    @Test
    void testDeleteUserErrorWithNonExistentId() throws Exception {
        doThrow(new ObjectNotFoundException("user", 5)).when(this.userService).delete(5);

        this.mockMvc.perform(delete(this.baseUrl + "/users/5").accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
                .andExpect(jsonPath("$.message").value("Could not find user with Id 5 :("))
                .andExpect(jsonPath("$.data").isEmpty());
    }
}
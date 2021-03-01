package hsbc.library;

import com.fasterxml.jackson.databind.ObjectMapper;
import hsbc.library.controller.UserController;
import hsbc.library.model.User;
import hsbc.library.repository.UserRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = LibraryApplication.class)
public class UserControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private UserRepository usersRepo;

    @Autowired
    private ObjectMapper mapper;

    private UserController userResource;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        userResource = new UserController();
        mockMvc = MockMvcBuilders.standaloneSetup(userResource).build();
        ReflectionTestUtils.setField(userResource, "usersRepo", usersRepo);
    }

    @Test
    public void addBookTest() throws Exception {
        User user = new User("user1", "name1", "address1");
        String body = mapper.writeValueAsString(user);
        mockMvc.perform(post("/api/user")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isCreated());
    }

    @Test
    public void getUserByIdTest() throws Exception {
        User user = new User("user1", "name1", "address1");
        usersRepo.addUser(user);
        MvcResult result = mockMvc.perform(get("/api/user/user1")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("name1")))
                .andReturn();
    }

    @Test
    public void getUsersTest() throws Exception {
        usersRepo.addUser(new User("user1", "name1", "address1"));
        usersRepo.addUser(new User("user2", "name2", "address2"));
        usersRepo.addUser(new User("user3", "name3", "address3"));
        usersRepo.addUser(new User("user4", "name4", "address4"));

        MvcResult result = mockMvc.perform(get("/api/users")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].name", hasItems("name2", "name3")))
                .andReturn();
    }
}

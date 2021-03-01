package hsbc.library;

import com.fasterxml.jackson.databind.ObjectMapper;
import hsbc.library.controller.BookController;
import hsbc.library.model.Book;
import hsbc.library.repository.BookRepository;
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
public class BookControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private BookRepository booksRepo;

    @Autowired
    private ObjectMapper mapper;

    private BookController booksResource;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        booksResource = new BookController();
        mockMvc = MockMvcBuilders.standaloneSetup(booksResource).build();
        ReflectionTestUtils.setField(booksResource, "booksRepo", booksRepo);
    }

    @Test
    public void addBookTest() throws Exception {
        Book book = new Book("isbn1", "Test1", "Test Author", "");
        String body = mapper.writeValueAsString(book);
        mockMvc.perform(post("/api/book")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isCreated());
    }

    @Test
    public void getBookByIsbnTest() throws Exception {
        Book book = new Book("isbn2", "Test2", "Test Author", "");
        booksRepo.addBook(book);
        MvcResult result = mockMvc.perform(get("/api/book/isbn2")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Test2")))
                .andReturn();
    }

    @Test
    public void getBooksTest() throws Exception {
        Book book1 = new Book("isbn3", "Test3", "Test Author", "");
        booksRepo.addBook(book1);
        Book book2 = new Book("isbn4", "Test4", "Test Author", "");
        booksRepo.addBook(book2);
        MvcResult result = mockMvc.perform(get("/api/books")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].title", hasItems("Test4", "Test3")))
                .andReturn();
    }
}

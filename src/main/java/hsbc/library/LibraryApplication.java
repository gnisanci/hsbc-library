package hsbc.library;

import hsbc.library.model.Book;
import hsbc.library.model.User;
import hsbc.library.repository.BookRepository;
import hsbc.library.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;
import java.io.IOException;

@SpringBootApplication
public class LibraryApplication {
    @Autowired
    private BookRepository booksRepo;

    @Autowired
    private UserRepository usersRepo;

    public static void main(String[] args) {
        SpringApplication.run(LibraryApplication.class, args);
    }

    @PostConstruct
    public void initApplication() throws IOException {
        booksRepo.addBook(new Book("isbn1", "book1", "author1", "publisher1"));
        booksRepo.addBook(new Book("isbn2", "book2", "author2", "publisher2"));
        booksRepo.addBook(new Book("isbn3", "book3", "author3", "publisher3"));
        booksRepo.addBook(new Book("isbn4", "book4", "author4", "publisher4"));
        booksRepo.addBook(new Book("isbn5", "book5", "author5", "publisher5"));

        usersRepo.addUser(new User("user1", "name1", "address1"));
        usersRepo.addUser(new User("user2", "name2", "address2"));
        usersRepo.addUser(new User("user3", "name3", "address3"));
        usersRepo.addUser(new User("user4", "name4", "address4"));
    }
}


package hsbc.library.configuration;

import hsbc.library.repository.BookRepository;
import hsbc.library.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfiguration {
    @Bean
    public BookRepository booksRepo() {
        return new BookRepository();
    }

    @Bean
    public UserRepository usersRepo() {
        return new UserRepository();
    }
}


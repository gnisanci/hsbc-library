package hsbc.library.controller;

import hsbc.library.model.Book;
import hsbc.library.model.User;
import hsbc.library.repository.BookRepository;
import hsbc.library.repository.UserRepository;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.config.EnableHypermediaSupport;
import org.springframework.hateoas.config.EnableHypermediaSupport.HypermediaType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api")
@EnableHypermediaSupport(type = HypermediaType.HAL)
public class BookController {

    @Autowired
    private UserRepository usersRepo;

    @Autowired
    private BookRepository booksRepo;

    @GetMapping("/book/{isbn}")
    @ApiOperation(value = "Get a book with ISBN")
    public ResponseEntity<Book> getBookByIsbn(@PathVariable String isbn) {
        Book book = booksRepo.getBook(isbn);
        HttpStatus httpStatus = HttpStatus.NOT_FOUND;

        if (book != null) {
            addLinks(book, true);
            httpStatus = HttpStatus.OK;
        }

        return new ResponseEntity<>(book, httpStatus);
    }

    @PostMapping("/book")
    @ApiOperation(value = "Add a book")
    public ResponseEntity<Book> addBook(@RequestBody Book book) {
        booksRepo.addBook(book);
        book.add(linkTo(methodOn(BookController.class).getBookByIsbn(book.getIsbn())).withSelfRel());
        book.add(linkTo(methodOn(BookController.class).getAllBooks()).withRel("Books"));

        return new ResponseEntity<>(book, HttpStatus.CREATED);
    }

    @GetMapping("/books")
    @ApiOperation(value = "List all books")
    public ResponseEntity<List<Book>> getAllBooks() {
        List<Book> books = booksRepo.getAllBooks();

        books.forEach(book -> {
            addLinks(book, false);
        });

        return new ResponseEntity<>(books, HttpStatus.OK);
    }

    @GetMapping("/search/book/{name}")
    @ApiOperation(value = "Get a book by title or author")
    public ResponseEntity<List<Book>> getBooksByTitleOrAuthor(@PathVariable String name) {
        HttpStatus httpStatus = HttpStatus.NOT_FOUND;
        List<Book> books = booksRepo.getAllBooks();

        List<Book> foundBooks = books.stream().filter(book -> book.getTitle().toLowerCase().contains(name.toLowerCase()) || book.getAuthor().toLowerCase().contains(name.toLowerCase())).collect(Collectors.toList());
        foundBooks.forEach(book -> {
            addLinks(book, false);
        });

        if (foundBooks.size() > 0) {
            httpStatus = HttpStatus.OK;
        }

        return new ResponseEntity<>(foundBooks, httpStatus);
    }

    @PutMapping("/book/{isbn}")
    @ApiOperation(value = "Update a book")
    public ResponseEntity<Book> updateBook(@PathVariable String isbn, @RequestBody Book book) {
        HttpStatus httpStatus = HttpStatus.NOT_FOUND;

        if (booksRepo.isBookAvailable(isbn)) {
            book.setIsbn(isbn);
            booksRepo.addBook(book);
            httpStatus = HttpStatus.OK;
            addLinks(book, true);
        }

        return new ResponseEntity<>(book, httpStatus);
    }

    @DeleteMapping("/book/{isbn}")
    @ApiOperation(value = "Delete a book")
    public ResponseEntity<Void> deleteBook(@PathVariable String isbn) {
        HttpStatus httpStatus = HttpStatus.NOT_FOUND;

        if (booksRepo.isBookAvailable(isbn)) {
            Book book = booksRepo.getBook(isbn);
            if (book.getBorrower() != null) {
                User user = usersRepo.getUser(book.getBorrower());
                if (isbn.equals(user.getBorrowedBook1())) {
                    user.setBorrowedBook1(null);
                } else if (isbn.equals(user.getBorrowedBook2())) {
                    user.setBorrowedBook2(null);
                }
            }

            booksRepo.removeBook(isbn);
            httpStatus = HttpStatus.OK;
        }

        return new ResponseEntity<>(httpStatus);
    }

    private void addLinks(Book book, boolean showAll) {
        book.removeLinks();
        book.add(linkTo(methodOn(BookController.class).getBookByIsbn(book.getIsbn())).withSelfRel());

        if (book.getBorrower() != null) {
            book.add(linkTo(methodOn(UserController.class).getUserById(book.getBorrower())).withRel("borrower"));
        }

        if (showAll) {
            book.add(linkTo(methodOn(BookController.class).getAllBooks()).withRel("Books"));
        }
    }
}

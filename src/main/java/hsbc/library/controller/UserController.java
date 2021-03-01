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
import org.springframework.web.server.ResponseStatusException;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api")
@EnableHypermediaSupport(type = HypermediaType.HAL)
public class UserController {

    @Autowired
    private UserRepository usersRepo;

    @Autowired
    private BookRepository booksRepo;

    @GetMapping("/users")
    @ApiOperation(value = "List all users")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = usersRepo.getAllUsers();

        users.forEach(user -> {
            addLinks(user, false);
        });

        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    @GetMapping("/user/{userId}")
    @ApiOperation(value = "Get a user with id")
    public ResponseEntity<User> getUserById(@PathVariable String userId) {
        User user = usersRepo.getUser(userId);
        HttpStatus httpStatus = HttpStatus.NOT_FOUND;

        if (user != null) {
            addLinks(user, true);
            httpStatus = HttpStatus.OK;
        }

        return new ResponseEntity<>(user, httpStatus);
    }

    @PostMapping("/user")
    @ApiOperation(value = "Add a user")
    public ResponseEntity<User> addUser(@RequestBody User user) {
        usersRepo.addUser(user);
        addLinks(user, true);
        return new ResponseEntity<>(user, HttpStatus.CREATED);
    }

    @PutMapping("/user/{userId}")
    @ApiOperation(value = "Update a user")
    public ResponseEntity<User> updateUser(@PathVariable String userId, @RequestBody User user) {
        HttpStatus httpStatus = HttpStatus.NOT_FOUND;

        if (usersRepo.isUserAvailable(userId)) {
            user.setUserid(userId);
            usersRepo.addUser(user);
            httpStatus = HttpStatus.OK;
            addLinks(user, true);
        }

        return new ResponseEntity<>(user, httpStatus);
    }

    @DeleteMapping("/user/{userId}")
    @ApiOperation(value = "Delete a user")
    public ResponseEntity<Void> deleteUser(@PathVariable String userId) {
        HttpStatus httpStatus = HttpStatus.NOT_FOUND;

        if (usersRepo.isUserAvailable(userId)) {
            User user = usersRepo.getUser(userId);

            if (user.getBorrowedBook1() != null) {
                Book book = booksRepo.getBook(user.getBorrowedBook1());
                book.setBorrower(null);
                book.setBorrowDate(null);
            }

            if (user.getBorrowedBook2() != null) {
                Book book = booksRepo.getBook(user.getBorrowedBook2());
                book.setBorrower(null);
                book.setBorrowDate(null);
            }

            usersRepo.removeUser(userId);
            httpStatus = HttpStatus.OK;
        }

        return new ResponseEntity<>(httpStatus);
    }

    @PostMapping("borrow/{userId}/{isbn}")
    @ApiOperation(value = "Borrow book")
    public ResponseEntity<User> borrowBook(@PathVariable String userId, @PathVariable String isbn) {
        User borrower = usersRepo.getUser(userId);
        Book book = booksRepo.getBook(isbn);

        checkIfBookAndUserExists(borrower, book);

        if (book.getBorrower() != null) {
            return returnBadRequest("Book is not available to borrow");
        }

        if (borrower.getBorrowedBook1() != null && borrower.getBorrowedBook2() != null) {
            returnBadRequest("User can not borrow more than 2 books");
        }

        book.setBorrower(borrower.getUserid());

        Date date = Calendar.getInstance().getTime();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        book.setBorrowDate(dateFormat.format(date));

        if (borrower.getBorrowedBook1() == null) {
            borrower.setBorrowedBook1(book.getIsbn());
        } else {
            borrower.setBorrowedBook2(book.getIsbn());
        }

        addLinks(borrower, false);

        return new ResponseEntity<>(borrower, HttpStatus.OK);
    }

    @PostMapping("return/{userId}/{isbn}")
    @ApiOperation(value = "Return book")
    public ResponseEntity<User> returnBook(@PathVariable String userId, @PathVariable String isbn) {
        User borrower = usersRepo.getUser(userId);
        Book book = booksRepo.getBook(isbn);

        checkIfBookAndUserExists(borrower, book);

        if (book.getBorrower() == null) {
            returnBadRequest("Book is not borrowed");
        }

        if (!borrower.getUserid().equals(book.getBorrower())) {
            returnBadRequest("Book is borrowed by another user");
        }

        if (book.getIsbn().equals(borrower.getBorrowedBook1())) {
            borrower.setBorrowedBook1(null);
        } else {
            borrower.setBorrowedBook2(null);
        }

        book.setBorrower(null);
        book.setBorrowDate(null);

        addLinks(borrower, false);

        return new ResponseEntity<>(borrower, HttpStatus.OK);
    }

    @GetMapping("search/user/{name}")
    @ApiOperation(value = "Get a user with name")
    public ResponseEntity<List<User>> getUsersByName(@PathVariable String name) {
        HttpStatus httpStatus = HttpStatus.NOT_FOUND;
        List<User> users = usersRepo.getAllUsers();

        List<User> foundUsers = users.stream().filter(user -> user.getName().toLowerCase().contains(name.toLowerCase())).collect(Collectors.toList());
        foundUsers.forEach(user -> {
            addLinks(user, false);
        });

        if (foundUsers.size() > 0) {
            httpStatus = HttpStatus.OK;
        }

        return new ResponseEntity<>(foundUsers, httpStatus);
    }

    private void addLinks(User user, boolean showAll) {
        user.removeLinks();
        user.add(linkTo(methodOn(UserController.class).getUserById(user.getUserid())).withSelfRel());

        if (user.getBorrowedBook1() != null) {
            user.add(linkTo(methodOn(BookController.class).getBookByIsbn(user.getBorrowedBook1())).withRel("borrowedBook1"));
        }

        if (user.getBorrowedBook2() != null) {
            user.add(linkTo(methodOn(BookController.class).getBookByIsbn(user.getBorrowedBook2())).withRel("borrowedBook2"));
        }

        if (showAll) {
            user.add(linkTo(methodOn(UserController.class).getAllUsers()).withRel("Users"));
        }
    }

    private ResponseEntity<User> returnBadRequest(String s) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, s);
    }

    private void checkIfBookAndUserExists(User borrower, Book book) {
        if (borrower == null || book == null) {
            StringBuilder errorMsg = new StringBuilder();

            if (borrower == null) {
                errorMsg.append("User not found");
            }

            if (book == null) {
                if (errorMsg.length() > 0) {
                    errorMsg.append(" and ");
                }

                errorMsg.append("Book not found");
            }

            returnBadRequest(errorMsg.toString());
        }
    }
}

package hsbc.library.repository;

import hsbc.library.model.User;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Component
public class UserRepository {
    HashMap<String, User> users = new HashMap<>();

    public void addUser(User user) {
        users.put(user.getUserid(), user);
    }

    public User getUser(String id) {
        return users.get(id);
    }

    public List<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }

    public Boolean isUserAvailable(String id) {
        return users.containsKey(id);
    }

    public Boolean removeUser(String id) {
        if (users.containsKey(id)) {
            users.remove(id);
            return true;
        }

        return false;
    }
}

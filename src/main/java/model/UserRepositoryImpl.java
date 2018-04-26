package model;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import util.JSONParser;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Alexander Diachenko.
 */
public class UserRepositoryImpl implements UserRepository {

    private ObjectMapper mapper = new ObjectMapper();

    private Set<User> users;

    public UserRepositoryImpl() {
        users = getUserList();
    }

    @Override
    public Set<User> getUsers() {
        return users;
    }

    @Override
    public User getUserByName(String name) {
        for (User user : users) {
            if (user.getName().equalsIgnoreCase(name)) {
                return user;
            }
        }
        return null;
    }


    @Override
    public void add(User user) {
        users.add(user);
        write();
    }

    @Override
    public void update(User user) {
        users.remove(user);
        users.add(user);
        write();
    }

    private void write() {
        try {
            mapper.writeValue(new FileOutputStream("./users.json"), users);
        } catch (IOException e) {
            e.printStackTrace();
        }
        users = getUserList();
    }

    private HashSet<User> getUserList() {
        try {
            return new HashSet<>(mapper.readValue(JSONParser.readFile("./users.json"), new TypeReference<List<User>>() {
            }));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new HashSet<>();
    }
}
package chat.model.repository;

import chat.model.entity.User;
import chat.util.JSONParser;
import chat.util.Paths;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.springframework.stereotype.Repository;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * @author Alexander Diachenko.
 */
@Repository
public class JSONUserRepository implements UserRepository {

    private final static Logger logger = LogManager.getLogger(JSONUserRepository.class);

    private ObjectMapper mapper = new ObjectMapper();
    private Set<User> users;
    private String path;
    private Paths paths;

    public JSONUserRepository() {
        //do nothing
    }

    public JSONUserRepository(final String path, final Paths paths) {
        this.path = path;
        this.paths = paths;
        this.users = getAll();
    }

    @Override
    public Set<User> getAll() {
        try {
            return new HashSet<>(
                    this.mapper.readValue(JSONParser.readFile(this.path), new TypeReference<List<User>>() {
            }));
        } catch (IOException exception) {
            logger.error(exception.getMessage(), exception);
        }
        return new HashSet<>();
    }

    @Override
    public Optional<User> getUserByName(final String name) {
        return this.users
                .stream()
                .filter(user -> user.getName().equalsIgnoreCase(name))
                .findFirst();
    }

    @Override
    public User add(final User user) {
        this.users.add(user);
        flush();
        return user;
    }

    @Override
    public User update(final User user) {
        this.users.remove(user);
        this.users.add(user);
        flush();
        return user;
    }

    @Override
    public User delete(final User user) {
        this.users.remove(user);
        return user;
    }

    private void flush() {
        final Thread thread = new Thread(() -> {
            synchronized (this) {
                try {
                    this.mapper.writeValue(new FileOutputStream(this.path), this.users);
                } catch (IOException exception) {
                    logger.error(exception.getMessage(), exception);
                    throw new RuntimeException("Users failed to save. Create " +
                            this.paths.getUsersJson());
                }
            }
        });
        thread.start();
    }
}

package chat.model.repository;

import chat.model.entity.Command;
import chat.util.FileUtil;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.springframework.stereotype.Repository;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * @author Alexander Diachenko.
 */
@Repository
@NoArgsConstructor
@Log4j2
public class JSONCommandRepository implements CommandRepository {

    private ObjectMapper mapper = new ObjectMapper();
    private Set<Command> commands;
    private String path;

    public JSONCommandRepository(String path) {
        this.path = path;
        getAll();
    }

    @Override
    public Set<Command> getAll() {
        try {
            commands = mapper.readValue(FileUtil.readFile(path), new TypeReference<Set<Command>>() {});
            return commands;
        } catch (IOException exception) {
            log.error(exception.getMessage(), exception);
        }
        return new HashSet<>();
    }

    @Override
    public Optional<Command> getCommandByName(String name) {
        for (Command command : commands) {
            if (command.getName().equalsIgnoreCase(name)) {
                return Optional.of(command);
            }
        }
        return Optional.empty();
    }

    @Override
    public Command add(Command command) {
        commands.add(command);
        flush();
        return command;
    }

    @Override
    public Command update(Command command) {
        commands.remove(command);
        commands.add(command);
        flush();
        return command;
    }

    @Override
    public Command delete(Command command) {
        commands.remove(command);
        flush();
        return command;
    }

    private void flush() {
        Thread thread = new Thread(() -> {
            synchronized (this) {
                try {
                    mapper.writeValue(new FileOutputStream(path), commands);
                } catch (IOException exception) {
                    log.error(exception.getMessage(), exception);
                    throw new RuntimeException("Commands failed to save. Create " +
                            path, exception);
                }
            }
        });
        thread.start();
    }
}

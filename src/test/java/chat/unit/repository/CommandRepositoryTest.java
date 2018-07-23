package chat.unit.repository;

import chat.model.entity.Command;
import chat.model.repository.CommandRepository;
import chat.model.repository.JSONCommandRepository;
import org.junit.Test;

import java.util.Optional;
import java.util.Set;

import static org.junit.Assert.assertTrue;

/**
 * @author Alexander Diachenko.
 */
public class CommandRepositoryTest {

    private CommandRepository commandRepository = new JSONCommandRepository(getResource("/json/commands.json"));

    @Test
    public void getAllCommandsTest() {
        final Set<Command> commands = this.commandRepository.getAll();
        assertTrue(!commands.isEmpty());
    }

    @Test
    public void getCommandByNameTest() {
        final Optional<Command> commandByName = this.commandRepository.getCommandByName("!info");
        assertTrue(commandByName.isPresent());
    }

    @Test
    public void getCommandByIncorrectNameTest() {
        final Optional<Command> commandByName = this.commandRepository.getCommandByName("!QWE");
        assertTrue(!commandByName.isPresent());
    }

    private String getResource(final String path) {
        return getClass().getResource(path).getPath();
    }
}

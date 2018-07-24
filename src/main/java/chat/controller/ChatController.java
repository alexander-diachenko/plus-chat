package chat.controller;

import chat.Main;
import chat.component.SettingsDialog;
import chat.model.entity.Direct;
import chat.model.entity.Rank;
import chat.model.entity.Smile;
import chat.model.entity.User;
import chat.model.repository.*;
import chat.observer.Observer;
import chat.sevice.Bot;
import chat.util.AppProperty;
import chat.util.Settings;
import chat.util.StringUtil;
import chat.util.StyleUtil;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pircbotx.Configuration;
import org.pircbotx.PircBotX;
import org.pircbotx.exception.IrcException;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

/**
 * @author Alexander Diachenko.
 */
public class ChatController implements Observer {

    private final static Logger logger = LogManager.getLogger(ChatController.class);

    public static PircBotX bot;
    @FXML
    private Button onTop;
    @FXML
    private Button setting;
    @FXML
    private VBox container;
    @FXML
    private VBox root;
    @FXML
    private ScrollPane scrollPane;
    private List<HBox> messages = new ArrayList<>();
    private Properties settings;
    private RankRepository rankRepository;
    private UserRepository userRepository;
    private CommandRepository commandRepository;
    private SmileRepository smileRepository;
    private DirectRepository directRepository;
    private int index = 0;
    private boolean isOnTop;

    @FXML
    public void initialize() {
        this.rankRepository = new JSONRankRepository("./data/ranks.json");
        this.userRepository = new JSONUserRepository("./data/users.json");
        this.smileRepository = new JSONSmileRepository("./data/smiles.json");
        this.commandRepository = new JSONCommandRepository("./data/commands.json");
        this.directRepository = new JSONDirectRepository("./data/directs.json");
        this.settings = AppProperty.getProperty("./settings/settings.properties");
        this.isOnTop = Boolean.parseBoolean(this.settings.getProperty(Settings.ROOT_ALWAYS_ON_TOP));
        onTopInit();
        this.root.setStyle(StyleUtil.getRootStyle(
                this.settings.getProperty(Settings.ROOT_BASE_COLOR),
                this.settings.getProperty(Settings.ROOT_BACKGROUND_COLOR)
        ));
        this.scrollPane.prefHeightProperty().bind(this.root.heightProperty());
        this.scrollPane.vvalueProperty().bind(this.container.heightProperty());
        startBot();
    }

    private void onTopInit() {
        String name = "/img/pin-enabled.png";
        if (!isOnTop) {
            name = "/img/pin-disabled.png";
        }
        final ImageView imageView = new ImageView(new Image(name));
        imageView.setFitWidth(15);
        imageView.setFitHeight(15);
        onTop.setGraphic(imageView);
    }

    private void startBot() {
        final Thread thread = new Thread(() -> {
            final Properties connect = AppProperty.getProperty("./settings/twitch.properties");
            final Bot listener = new Bot(connect, this.userRepository, this.rankRepository, this.commandRepository);
            listener.addObserver(this);
            final Configuration config = new Configuration.Builder()
                    .setName(connect.getProperty("botname"))
                    .addServer("irc.chat.twitch.tv", 6667)
                    .setServerPassword(connect.getProperty("oauth"))
                    .setAutoReconnect(true)
                    .addListener(listener)
                    .addAutoJoinChannel("#" + connect.getProperty("channel"))
                    .buildConfiguration();
            bot = new PircBotX(config);
            try {
                bot.startBot();
            } catch (IOException | IrcException exception) {
                logger.error(exception.getMessage(), exception);
                throw new RuntimeException("Bot failed to start.\n " +
                        "Check properties in settings/twitch.properties and restart application.");
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    public void settingsOnAction() {
        final SettingsDialog dialog = new SettingsDialog();
        dialog.openDialog(getStage(), this.root);
        this.setting.setDisable(true);
    }

    public void onTopOnAction() {
        this.isOnTop = !this.isOnTop;
        final Stage chatRoot = Main.stage;
        chatRoot.setAlwaysOnTop(this.isOnTop);
        String name = "/img/pin-enabled.png";
        if (!this.isOnTop) {
            name = "/img/pin-disabled.png";
        }
        final ImageView imageView = new ImageView(new Image(name));
        imageView.setFitWidth(15);
        imageView.setFitHeight(15);
        this.onTop.setGraphic(imageView);
        this.settings.setProperty(Settings.ROOT_ALWAYS_ON_TOP, String.valueOf(this.isOnTop));
        AppProperty.setProperties("./settings/settings.properties", this.settings);
    }

    @Override
    public void update(final String nick, final String message) {
        final HBox messageBox = new HBox();
        messageBox.setId("messageBox");
        final Optional<User> userByName = this.userRepository.getUserByName(nick);
        final TextFlow textFlow = new TextFlow();
        String customName = nick;
        if (userByName.isPresent()) {
            final User user = userByName.get();
            if (user.hasCustomName()) {
                customName = user.getCustomName();
            }
            final Label image = getRankImage(user);
            textFlow.getChildren().add(image);
        }
        final Text name = getText(customName, "user-name", this.settings.getProperty(Settings.FONT_NICK_COLOR));
        final Text separator = getText(": ", "separator", this.settings.getProperty(Settings.FONT_SEPARATOR_COLOR));
        textFlow.getChildren().addAll(name, separator);
        final List<Node> nodes = getMessageNodes(message, this.settings.getProperty(Settings.FONT_MESSAGE_COLOR),
                this.settings.getProperty(Settings.FONT_DIRECT_MESSAGE_COLOR));
        nodes.iterator().forEachRemaining(node -> textFlow.getChildren().add(node));
        messageBox.getChildren().add(textFlow);
        this.messages.add(messageBox);
        this.container.getChildren().add(this.messages.get(this.index));
        this.index++;
    }

    private List<Node> getMessageNodes(final String message, final String color, final String directColor) {
        final List<Node> nodes = new ArrayList<>();
        final String utf8Message = StringUtil.getUTF8String(message);
        final String[] words = utf8Message.split(" ");
        for (String word : words) {
            final Optional<Smile> smileByName = this.smileRepository.getSmileByName(word);
            if (smileByName.isPresent()) {
                final Smile smile = smileByName.get();
                try {
                    nodes.add(getImage(smile));
                } catch (FileNotFoundException exception) {
                    logger.error(exception.getMessage(), exception);
                    final Text text = getText(message, color, directColor, word);
                    nodes.add(text);
                }
            } else {
                final Text text = getText(message, color, directColor, word);
                nodes.add(text);
            }
        }
        return nodes;
    }

    private Text getText(final String message, final String color, final String directColor, final String word) {
        final Text text = new Text(word + " ");
        if (isDirect(message)) {
            text.setId("user-direct-message");
            text.setStyle(StyleUtil.getTextStyle(this.settings.getProperty(Settings.FONT_SIZE), directColor));
        } else {
            text.setId("user-message");
            text.setStyle(StyleUtil.getTextStyle(this.settings.getProperty(Settings.FONT_SIZE), color));
        }
        return text;
    }

    private Label getImage(final Smile smile) throws FileNotFoundException {
        final Label image = new Label();
        final ImageView imageView = getImageView(smile.getImagePath());
        image.setGraphic(imageView);
        return image;
    }

    private boolean isDirect(final String message) {
        final Set<Direct> directs = this.directRepository.getAll();
        for (Direct direct : directs) {
            final String word = direct.getWord();
            if (StringUtils.containsIgnoreCase(message, word)) {
                return true;
            }
        }
        return false;
    }

    private ImageView getImageView(final String path) throws FileNotFoundException {
        final FileInputStream fis = new FileInputStream(path);
            return new ImageView(new Image(fis));
        }

        private Label getRankImage ( final User user){
            final Label image = new Label();
            final Rank rank = this.rankRepository.getRankByExp(user.getExp());
            image.setId("rank-image");
            try (final FileInputStream fis = new FileInputStream(rank.getImagePath())) {
                final ImageView imageView = new ImageView(new Image(fis));
                imageView.setFitHeight(20);
                imageView.setFitWidth(20);
                image.setGraphic(imageView);
                return image;
            } catch (IOException exception) {
                logger.error(exception.getMessage(), exception);
            }
            return new Label();
        }

        private Text getText ( final String string, final String id, final String color){
            final Text text = new Text(StringUtil.getUTF8String(string));
            text.setId(id);
            text.setStyle(StyleUtil.getTextStyle(this.settings.getProperty(Settings.FONT_SIZE), color));
            return text;
        }

        public void setSettings ( final Properties settings){
            this.settings = settings;
        }

        public Button getSetting () {
            return this.setting;
        }

        private Stage getStage () {
            return (Stage) this.container.getScene().getWindow();
        }
    }

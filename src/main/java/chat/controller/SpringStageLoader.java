package chat.controller;

import chat.util.AppProperty;
import chat.util.ResourceBundleControl;
import chat.util.Settings;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.Region;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;

@Component
public class SpringStageLoader implements ApplicationContextAware {

    private static ApplicationContext staticContext;
    //инъекция заголовка главного окна
    @Value("${title}")
    private String appTitle;
    private static String staticTitle;

    private static final String FXML_DIR = "/view/";

    /**
     * Загрузка корневого узла и его дочерних элементов из fxml шаблона
     * @param fxmlName наименование *.fxml файла в ресурсах
     * @return объект типа Region
     * @throws IOException бросает исключение ввода-вывода
     */
    public static Region load(final String fxmlName) throws IOException {
        final Properties settings = AppProperty.getProperty("./settings/settings.properties");
        final String language = settings.getProperty(Settings.ROOT_LANGUAGE);
        final ResourceBundle bundle = ResourceBundle.getBundle("bundles.chat", new Locale(language), new ResourceBundleControl());
        final FXMLLoader loader = new FXMLLoader();
        loader.setResources(bundle);
        // setLocation необходим для корректной загрузки включенных шаблонов, таких как productTable.fxml,
        // без этого получим исключение javafx.fxml.LoadException: Base location is undefined.
        loader.setLocation(SpringStageLoader.class.getResource(FXML_DIR + fxmlName + ".fxml"));
        // setLocation необходим для корректной того чтобы loader видел наши кастомные котнролы
        loader.setClassLoader(SpringStageLoader.class.getClassLoader());
        loader.setControllerFactory(staticContext::getBean);
        return loader.load(SpringStageLoader.class.getResourceAsStream(FXML_DIR + fxmlName + ".fxml"));
    }

    /**
     * Передаем данные в статические поля в реализации метода интерфейса ApplicationContextAware,
     т.к. методы их использующие тоже статические
     */
    @Override
    public void setApplicationContext(final ApplicationContext context) throws BeansException {
        SpringStageLoader.staticContext = context;
        SpringStageLoader.staticTitle = appTitle;
    }
}

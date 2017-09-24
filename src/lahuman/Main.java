package lahuman;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.fxml.FXMLLoader;

public class Main extends Application {
	@Override
	public void start(Stage primaryStage) {
		try {
			setUserAgentStylesheet(STYLESHEET_MODENA);
			HostServicesProvider.INSTANCE.init(getHostServices());
			BorderPane root = (BorderPane) FXMLLoader.load(getClass().getResource("WordCount.fxml"));
			Scene scene = new Scene(root, 600, 250);
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			primaryStage.setScene(scene);
			primaryStage.show();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		launch(args);
	}

}

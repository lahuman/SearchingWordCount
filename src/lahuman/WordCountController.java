package lahuman;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.DirectoryStream.Filter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.io.FilenameUtils;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.stage.DirectoryChooser;

public class WordCountController {

	@FXML
	TextField wordPath;
	@FXML
	TextField contentsPath;
	@FXML
	TextField outputPath;
	@FXML
	BorderPane win;
	@FXML
	Button runBtn;
	
	@FXML
	ProgressBar progressBar;
	
	DirectoryChooser directoryChooser = new DirectoryChooser();
	Task worker;
	
	@FXML
	public void findPath(ActionEvent action) {
		setFilePath(getTextField(((Button) action.getSource()).getId()));
	}

	@FXML
	public void goHomePage(ActionEvent act) {
		HostServicesProvider.INSTANCE.getHostServices().showDocument("https://lahuman.github.io");
	}

	@FXML
	public void processing(ActionEvent act) {
		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setTitle("Result");
		alert.setHeaderText(null);
		runBtn.setDisable(true);
		progressBar.progressProperty().unbind();
		progressBar.setProgress(0);
		
		try (DirectoryStream<Path> wordDirStream = Files.newDirectoryStream(Paths.get(wordPath.getText()),
				fileFilter());
				DirectoryStream<Path> contentsDirStream = Files.newDirectoryStream(Paths.get(contentsPath.getText()),
						fileFilter());) {
			List<String> wordFiles = new ArrayList<>();
			List<String> contentsFiles = new ArrayList<>();
			wordDirStream.forEach(wFiles -> {
				wordFiles.add(wFiles.getFileName().toString());
			});
			contentsDirStream.forEach(cFiles -> {
				contentsFiles.add(cFiles.getFileName().toString());
			});
			worker = task(wordFiles, contentsFiles);
			progressBar.progressProperty().bind(worker.progressProperty());
			
			//값이 변경될때마다 호출 
			worker.messageProperty().addListener(new ChangeListener<String>() {
		          public void changed(ObservableValue<? extends String> observable,
		              String oldValue, String newValue) {
		            System.out.println(newValue);
		          }
		        });
			
			//작업 성공시 호출
			worker.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED, //
                      new EventHandler<WorkerStateEvent>() {
                          @Override
                          public void handle(WorkerStateEvent t) {
                        	resetFunction(alert, "Fininshed!");
                          }
                      });

			new Thread(worker).start();
		} catch (IOException ex) {
			System.out.println(ex);
			resetFunction(alert,  ex.getMessage());
		}
	}

	private void resetFunction(Alert alert, String message) {
		progressBar.progressProperty().unbind();
		progressBar.setProgress(0);
		runBtn.setDisable(false);
		alert.setContentText(message);
		alert.showAndWait();
	}

	private Task<Boolean> task(List<String> wordFiles , List<String> contentsFiles) {
		return new Task<Boolean>() {

			@Override
			protected Boolean call() throws Exception {
				int count = wordFiles.size() * contentsFiles.size();
				final AtomicInteger increase = new AtomicInteger();
				wordFiles.stream().forEach(wordFile -> {
					for (String contentsFile : contentsFiles) {
						final String outputTxtPath = outputPath.getText()+File.separator+FilenameUtils.removeExtension(wordFile) + "-"
								+ FilenameUtils.removeExtension(contentsFile) + ".txt";
						new WordFinder(wordPath.getText()+File.separator+wordFile, contentsPath.getText()+File.separator+contentsFile, outputTxtPath).run();
						updateMessage(outputTxtPath);
						updateProgress(increase.incrementAndGet(), count);
					}
				});
				return true;
			}
			
		};
		
	}

	private Filter<Path> fileFilter() {
		return new DirectoryStream.Filter<Path>() {
			@Override
			public boolean accept(Path entry) throws IOException {
				return entry.getFileName().toString().toLowerCase().endsWith(".txt");
			}
		};
	}

	private TextField getTextField(String btnId) {
		switch (btnId) {
		case "wordBtn":
			return wordPath;
		case "contentsBtn":
			return contentsPath;
		case "outputBtn":
			return outputPath;
		default:
			return null;
		}
	}

	private void setFilePath(TextField target) {
		File file = directoryChooser.showDialog(win.getScene().getWindow());
		if (file != null) {
			target.setText(file.getAbsolutePath());
		}
	}

}

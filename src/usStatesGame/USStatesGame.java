package usStatesGame;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class USStatesGame extends Application {
	
	private Stage stage;
	private Pane center;
	private BorderPane root;
	private String imagePath = "file:US.png"; // This png file is stored locally. 
	private ImageView imageView;
	private TextField wrongAnswerBox;
	private int wrongAnswerCounter;
	private TextField timerBox;
	private int secondTimer;
	private int minuteTimer;
	private int hourTimer;
	private String currentTimeString;
	private boolean currentlyPlaying;
	private TextField guessesHighScoreField;
	private TextField timeHighScoreField;
    private int statesLeft = 50; // Can be changed to 1 to test the app more easily. 
    private TextField statesLeftBox;
    private Timer timer;

	@Override
	public void start(Stage stage) throws Exception {
		
		this.stage = stage;
		root = new BorderPane();
		VBox top = new VBox();
	    root.setTop(top);
	    FlowPane flow = new FlowPane();
	    top.getChildren().add(flow);
	    flow.setAlignment(Pos.CENTER);
	    flow.setHgap(10); 
	    flow.setPadding(new Insets(10));
	    wrongAnswerCounter = 0;
	    wrongAnswerBox = new TextField();
	    wrongAnswerBox.setText(String.valueOf(wrongAnswerCounter));
	    wrongAnswerBox.setPrefHeight(50);
	    wrongAnswerBox.setPrefWidth(70);
	    wrongAnswerBox.setEditable(false);
	    wrongAnswerBox.setFont(Font.font("Arial", FontWeight.BOLD, 20));
	    Label wrongfulGuessesLabel = new Label("Wrongful Guesses:");
	    wrongfulGuessesLabel.setFont(new Font(15)); 
	    timerBox = new TextField("0");
	    timerBox.setPrefHeight(50);
	    timerBox.setPrefWidth(70);
	    timerBox.setEditable(false);
	    timerBox.setFont(Font.font("Arial", FontWeight.BOLD, 15));

	    timer = new Timer();
	    timer.scheduleAtFixedRate(new TimerTask() {
	        @Override
	        public void run() { // This runs concurrently. 
	    	    
	    	    while(currentlyPlaying) {
		            try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
            		if (statesLeft == 0) {
            			currentlyPlaying = false;
            			saveHighScore();
            		}
		    	    currentTimeString = String.valueOf(hourTimer + ":" + minuteTimer + ":" + secondTimer);
		            secondTimer++;
		            if (secondTimer == 60) {
		            	secondTimer = 0;
		            	minuteTimer++;
		            }
		            if (minuteTimer == 60) {
		            	minuteTimer = 0;
		            	hourTimer++;
		            }
		            timerBox.setText(String.valueOf(currentTimeString));
	    	    }
	        }
	    }, 0, 1000);

	    Label timerLabel = new Label("  Timer: ");
	    timerLabel.setFont(new Font(15)); 
	    
	    Label statesLeftLabel = new Label("  States left: ");
	    statesLeftLabel.setFont(new Font(15)); 
	    statesLeftBox = new TextField(String.valueOf(statesLeft));
	    statesLeftBox.setPrefHeight(50);
	    statesLeftBox.setPrefWidth(70);
	    statesLeftBox.setEditable(false);
	    statesLeftBox.setFont(Font.font("Arial", FontWeight.BOLD, 20));
	    
	    Button reset = new Button("Reset");
	    reset.setOnMouseClicked(new ResetHandler());
	    reset.setPrefWidth(100);
	    reset.setPrefHeight(50);
	    reset.setFont(new Font(20));
	    
	    Label highScoreGuesses = new Label("   Wrong guesses high score:");
	    highScoreGuesses.setFont(new Font(15));
	    guessesHighScoreField = new TextField();
	    guessesHighScoreField.setPrefHeight(50);
	    guessesHighScoreField.setPrefWidth(50);
	    guessesHighScoreField.setEditable(false);
	    guessesHighScoreField.setFont(Font.font("Arial", FontWeight.BOLD, 20));
	    Label highScoreTime = new Label("   Time high score:");
	    highScoreTime.setFont(new Font(15));
	    timeHighScoreField = new TextField();
	    timeHighScoreField.setPrefHeight(50);
	    timeHighScoreField.setPrefWidth(90);
	    timeHighScoreField.setEditable(false);
	    timeHighScoreField.setFont(Font.font("Arial", FontWeight.BOLD, 15));
	    
	    // Here we check if there is a previous high score recorded, and load it if so. 
	    if (loadWrongGuesses() >= 0) { 
	    	guessesHighScoreField.setText(String.valueOf(loadWrongGuesses()));
	    }
	    if (loadTime() != null) {
	    	timeHighScoreField.setText(loadTime());
	    }
	    
	    flow.getChildren().addAll(wrongfulGuessesLabel, wrongAnswerBox, timerLabel, timerBox, statesLeftLabel, statesLeftBox, reset
	    		, highScoreGuesses, guessesHighScoreField, highScoreTime, timeHighScoreField);
	    
        Image image = new Image(imagePath); 
        imageView = new ImageView(image); 
        imageView.setFitHeight(700);
        imageView.setFitWidth(1200);
        
        center = new Pane(); 
        root.setCenter(center);
        center.getChildren().add(imageView); 
        
//        center.setOnMouseClicked(new ClickCoordinateHandler());
        
        loadStates();
        
        stage.setOnCloseRequest(new ExitHandler()); 
        
	    Scene scene = new Scene(root, 1200, 750); 
        stage.setScene(scene);
        stage.setTitle("US States Guessing Game");
        stage.show();   
	}
	
	
	protected class State extends Circle { 
		
		private String name;
		private boolean marked; 
		private double x;
		private double y;
		
		protected State(String name, double x, double y){
			this.name = name;
			setRadius(6); // The object of this class itself is a Circle, so we act directly upon it. 
			this.x = x;
			this.y = y; 
			relocate(x-10, y-10); 
			setFill(Color.RED);
			setOnMouseClicked(new ClickHandler());
		}
		
		protected boolean isMarked() { // This is to check outside, to be able to connect two Places. 
			return marked;
		}
		
		protected void setMarked(boolean marked) {
			this.marked = marked;
		}
		
		protected String getName() {
			return name;
		}

		protected double getX() { 
			return x;
		}

		protected double getY() {
			return y;
		}

		@Override
		public String toString(){
			return name;
		}
		
	    class ClickHandler implements EventHandler<MouseEvent> {    	
			@Override
			public void handle(MouseEvent arg0) {
				currentlyPlaying = true;
				StateNameAlert alert = new StateNameAlert();
                Optional<ButtonType> result = alert.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.CANCEL) {
                	return;
                } else if (result.isPresent() && result.get() == ButtonType.OK) {
                	String name = alert.getName().toLowerCase();
                	if (name.equals(State.this.getName().toLowerCase())) { // If the answer is correct. 
                		State.this.setFill(Color.BLUE);
                		statesLeft--;
                		statesLeftBox.setText(String.valueOf(statesLeft));
            	    	Label label = new Label(State.this.getName());
            	    	label.relocate(x-23, y-23);
            	    	label.setFont(Font.font(null, FontWeight.BOLD, 10));
            	    	center.getChildren().add(label);
            	    	label.setDisable(true); // Just to make sure it is not clickable. To not get in the way. 
            	    	label.setStyle("-fx-opacity: 1;"); // Fix the color to make it black, not grayed out. 
                	} else {
                		wrongAnswerCounter++;
                		wrongAnswerBox.setText(String.valueOf(wrongAnswerCounter));
                	}
                }
			}
	    }
	}
	
	private void saveHighScore() {
		
		try {
			// We declare and initialize the values here out of necessity. 
			boolean lessWrongAnswers = false;
			boolean shorterTime = false;
			int hours = 0;
            int minutes = 0;
            int seconds = 0;	
            int currentWrongGuesses = loadWrongGuesses();
            String currentTime = loadTime();
            
            // We check that the new score is better than before, or if there was no score before at all. 
            if (wrongAnswerCounter <= currentWrongGuesses || currentWrongGuesses == -1) {
            	lessWrongAnswers = true; // We have this here because we cannot alter the file yet. 
            }
            if (currentTime != null) { // Here we check that we have a current high score in time. 
                String[] times = currentTime.split(":"); 
                hours = Integer.parseInt(times[0]);
                minutes = Integer.parseInt(times[1]);
                seconds = Integer.parseInt(times[2]);
                
                if (hourTimer <= hours) {
                	if (minuteTimer <= minutes) {
                		if (secondTimer <= seconds) {
                			shorterTime = true; // Again, we cannot alter the file yet because we are still checking the old one. 
                		}
                	}
                }
            }
            if (currentTime == null) { // This is for if we did NOT have a previous score. 
            	shorterTime = true;
            }
            // Now we can create the new file. 
    		String fileName =  "HighScore.txt";
			FileWriter writer = new FileWriter(fileName);
            PrintWriter out = new PrintWriter(writer);
            
            if (lessWrongAnswers) { // If the new score is better, we update the file to have the new score. 
            	out.println(wrongAnswerCounter);
            } else { // If not, we put the old one back. 
            	out.println(currentWrongGuesses);
            }
            if (shorterTime) { // Same as above. 
            	out.print(hourTimer + ":" + minuteTimer + ":" + secondTimer);
            } else {
            	out.print(hours + ":" + minutes + ":" + seconds);
            }
            out.close();
        } catch(IOException e){
        	Alert alert = new Alert(Alert.AlertType.ERROR,"IO-fel " + e.getMessage());
        	alert.showAndWait();
        }
	}
	
	private int loadWrongGuesses() {
	    try{
            String filename = "HighScore.txt";
         
         	FileReader reader = new FileReader(filename);
            BufferedReader in = new BufferedReader(reader);  
            
            String wrongGuessesString = in.readLine();
            if (wrongGuessesString.isBlank()) {
            	return -1; // To indicate that there was nothing there. 
            } 
            in.close();
            return Integer.parseInt(wrongGuessesString);
         }catch(IOException e){ 
             Alert alert = new Alert(Alert.AlertType.ERROR,"IO error " + e.getMessage());
             alert.showAndWait();
             return -1;
         }
	}
	
	private String loadTime() {
	    try{
            String filename = "HighScore.txt";
         
         	FileReader reader = new FileReader(filename);
            BufferedReader in = new BufferedReader(reader);  
            
            in.readLine(); // The first line is the wrong guesses. 
            String time = in.readLine();
            if (time.isBlank()) {
            	in.close();
            	return null; // To indicate that there was nothing there. 
            } 
            in.close();
            return time;
         }catch(IOException e){ 
             Alert alert = new Alert(Alert.AlertType.ERROR,"IO error " + e.getMessage());
             alert.showAndWait();
             return null;
         }
	}
	
    class ExitHandler implements EventHandler<WindowEvent>{ // For when the user exits the program overall. 
        @Override public void handle(WindowEvent event){
        	if (currentlyPlaying) {
                Alert alert = unsavedAlert(); // Call our own unsavedAlert() method. 
                Optional<ButtonType> result = alert.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.CANCEL) {
                	event.consume();
                } else if (result.isPresent() && result.get() == ButtonType.OK) {
                	// The timer runs concurrently so we need to cancel it. 
                	timer.purge();
                	currentlyPlaying = false;
                	timer.cancel();
                	Platform.exit();
                }
        	} else {
            	timer.purge();
            	currentlyPlaying = false;
            	timer.cancel();
            	Platform.exit();
        	}
        }
    }
    
	private Alert unsavedAlert() { // To call later to confirm unsaved changes. 
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "Unsaved changes, continue anyway?"); 
        alert.setTitle("Warning!"); 
        alert.setHeaderText(null);  
        return alert;
	}
	
	class ResetHandler implements EventHandler<MouseEvent> {

		@Override
		public void handle(MouseEvent arg0) {
			reset();
		}
	}
	
	private void reset() {
		currentlyPlaying = false;
		statesLeft = 50;
		statesLeftBox.setText(String.valueOf(statesLeft));
		wrongAnswerCounter = 0;
		wrongAnswerBox.setText(String.valueOf(wrongAnswerCounter));
		secondTimer = 0;
		minuteTimer = 0;
		hourTimer = 0;
		center.getChildren().clear();
	    center.getChildren().add(imageView); 
		loadStates();
		currentTimeString = String.valueOf(hourTimer + ":" + minuteTimer + ":" + secondTimer);
		timerBox.setText(String.valueOf(currentTimeString));
		
	    if (loadWrongGuesses() >= 0) {
	    	guessesHighScoreField.setText(String.valueOf(loadWrongGuesses()));
	    }
	    if (loadTime() != null) {
	    	timeHighScoreField.setText(loadTime());
	    }
	}
	
	private void loadStates() {
		loadState("Washington", 164, 60);
		loadState("Oregon", 129.6, 146.4);
		loadState("California", 96.0, 324.0);
		loadState("Nevada", 173.6, 271.2);
		loadState("Arizona", 256.8, 419.2);
		loadState("Idaho", 252.0, 177.6);
		loadState("Utah", 280.0, 295.2);
		loadState("Montana", 364.0, 100.8);
		loadState("Colorado", 404.8, 322.4);
		loadState("Wyoming", 377.6, 212);
		loadState("Texas", 530.4, 520.8);
		loadState("North Dakota", 521.6, 111.2);
		loadState("South Dakota", 518.4, 186.4);
		loadState("Nebraska", 524.8, 265.6);
		loadState("Kansas", 554.4, 343.2);
		loadState("Oklahoma", 569.6, 420.0);
		loadState("Louisiana", 693.6, 528.8);
		loadState("Arkansas", 684.8, 437.6);
		loadState("Missouri", 673.6, 342.4);
		loadState("Iowa", 653.6, 252);
		loadState("Minnesota", 634.4, 151.2);
		loadState("Wisconsin", 721.6, 186.4);
		loadState("Illinois", 741.6, 296.8);
		loadState("Tennessee", 819.2, 405.6);
		loadState("Alabama", 820.0, 473.6);
		loadState("Georgia", 894.4, 476.8);
		loadState("Florida", 962.4, 592.8);
		loadState("Indiana", 806.4, 292.0);
		loadState("Michigan", 828.0, 212);
		loadState("Kentucky", 840.8, 355.2);
		loadState("South Carolina", 950.4, 436.8);
		loadState("North Carolina", 977.6, 385.6);
		loadState("Ohio", 878.4, 280.0);
		loadState("Virginia", 983.2, 336.8);
		loadState("Pennsylvania", 980.0, 244);
		loadState("New York", 1013.6, 184.0);
		loadState("Maine", 1115.2, 103.2);
		loadState("Alaska", 142.4, 581.6);
		loadState("Hawaii", 350.4, 621.6);
		loadState("Rhode Island", 1099.2, 201.6);
		loadState("Connecticut", 1074.4, 208);
		loadState("New Jersey", 1051.2, 260.8);
		loadState("Delaware", 1040.8, 291.2);
		loadState("Maryland", 1008.8, 283.2);
		loadState("West Virginia", 926.4, 320.0);
		loadState("Vermount", 1058.4, 136.0);
		loadState("New Hampshire", 1082.4, 157.6);
		loadState("Massachusetts", 1084.8, 184.8);
		loadState("Mississippi", 754.4, 483.2);
		loadState("New Mexico", 380.8, 432.8);
	}
	
	private void loadState(String name, double x, double y) {
		State state = new State(name, x, y);
		center.getChildren().add(state);
	}
}

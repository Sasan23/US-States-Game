package usStatesGame;

import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

public class StateNameAlert extends Alert {
	
	private TextField stateField = new TextField();
	
	public StateNameAlert() {
		super(AlertType.CONFIRMATION);
		GridPane grid = new GridPane(); 
		grid.addRow(1, new Label("Name of the state:  "), stateField); 
		grid.setVgap(12); 
		getDialogPane().setContent(grid); 
		setTitle("Enter the name of the state"); 
		setHeaderText(null);
	}

	public String getName() {
		return stateField.getText();
	}
}


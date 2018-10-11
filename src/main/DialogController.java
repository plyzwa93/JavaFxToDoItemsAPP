package main;

import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import main.data.ToDoData;
import main.data.ToDoItem;

import java.time.LocalDate;

public class DialogController {
    @FXML
    private TextField short1;
    @FXML
    private TextArea long2;
    @FXML
    private DatePicker deadline3;

    public ToDoItem processResults() {
        String short1 = this.short1.getText().trim();
        String long2 = this.long2.getText().trim();
        LocalDate deadline3 = this.deadline3.getValue();

        ToDoItem newItem = new ToDoItem(short1, long2, deadline3);

        ToDoData.getInstance().addToDoItem(newItem);

        return newItem;
    }
}

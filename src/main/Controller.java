package main;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.util.Callback;
import main.data.ToDoData;
import main.data.ToDoItem;

import javax.swing.text.html.Option;
import java.io.IOException;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class Controller {

//    private List<ToDoItem> toDoItems;
    @FXML
    private ListView<ToDoItem> toDoListView;
    @FXML
    private TextArea text;
    @FXML
    private Label deadLineLabel;
    @FXML
    private BorderPane mainBorderPane;
    @FXML
    private ContextMenu listContextMenu;
    @FXML
    private ToggleButton filterButton;

    private FilteredList<ToDoItem> filteredList;

    private Predicate<ToDoItem> allItems;
    private Predicate<ToDoItem> todayItems;

    public void initialize() {

        listContextMenu = new ContextMenu();
        MenuItem deleteMenuItem = new MenuItem("Delete");
        deleteMenuItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                ToDoItem item = toDoListView.getSelectionModel().getSelectedItem();
                deleteItem(item);
            }
        });
        listContextMenu.getItems().addAll(deleteMenuItem);

        toDoListView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<ToDoItem>() {
            @Override
            public void changed(ObservableValue<? extends ToDoItem> observable, ToDoItem oldValue, ToDoItem newValue) {
                if (newValue != null) {
                    ToDoItem item = toDoListView.getSelectionModel().getSelectedItem();
                    text.setText(item.getLongDescription());
                    DateTimeFormatter df = DateTimeFormatter.ofPattern("MMMM d, yyyy");
                    deadLineLabel.setText(df.format(item.getDeadline()));
                }
            }
        });
        allItems = new Predicate<ToDoItem>() {
            @Override
            public boolean test(ToDoItem item) {
                return true;
            }
        };
        todayItems = new Predicate<ToDoItem>() {
            @Override
            public boolean test(ToDoItem item) {
                return (item.getDeadline().equals(LocalDate.now()));
            }
        };
        filteredList = new FilteredList<>(ToDoData.getInstance().getToDoItems(), allItems);

        SortedList<ToDoItem> sortedList = new SortedList<>(filteredList,
                new Comparator<ToDoItem>() {
                    @Override
                    public int compare(ToDoItem o1, ToDoItem o2) {
                        return o1.getDeadline().compareTo(o2.getDeadline());
                    }
                });

        //toDoListView.setItems(ToDoData.getInstance().getToDoItems());
        toDoListView.setItems(sortedList);
        toDoListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        toDoListView.getSelectionModel().selectFirst();

        toDoListView.setCellFactory(new Callback<ListView<ToDoItem>, ListCell<ToDoItem>>() {
            @Override
            public ListCell<ToDoItem> call(ListView<ToDoItem> param) {
                ListCell<ToDoItem> cell = new ListCell<>() {
                    @Override
                    protected void updateItem(ToDoItem item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setText(null);
                        } else {
                            setText(item.getShortDescription());
                            if (item.getDeadline().isBefore(LocalDate.now().plusDays(1))) {
                                setTextFill(Color.RED);
                            } else if (item.getDeadline().equals(LocalDate.now().plusDays(1))) {
                                setTextFill(Color.BROWN);
                            }
                        }
                    }
                };

                cell.emptyProperty().addListener(
                        (obs, wasEmpty, isNowEmpty) -> {
                            if (isNowEmpty) {
                                cell.setContextMenu(null);
                            } else {
                                cell.setContextMenu(listContextMenu);
                            }
                        }
                );

                return cell;
            }
        });

    }
    @FXML
    public void showNewItemDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initOwner(mainBorderPane.getScene().getWindow());
        dialog.setTitle("Add new ToDoItem");
        dialog.setHeaderText("Use this dialog to create new Item");
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("toDoItemDialog.fxml"));
        try {
            dialog.getDialogPane().setContent(fxmlLoader.load());
        } catch (IOException e) {
            System.out.println("Couldn't load the dialog");
            e.printStackTrace();
            return;
        }
        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            DialogController controller = fxmlLoader.getController();
            ToDoItem newItem = controller.processResults();
            toDoListView.getSelectionModel().select(newItem);
        }
    }
    public void deleteItem(ToDoItem item) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete this item?");
        alert.setHeaderText("Delete item: " + item.getShortDescription());
        alert.setContentText("Are you sore? Press OK or Cancel");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && (result.get() == ButtonType.OK)) {
            ToDoData.getInstance().deleteItem(item);
        }
    }
    @FXML
    public void handleKeypressed(KeyEvent keyEvent) {
        ToDoItem selectedItem = toDoListView.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            if (keyEvent.getCode().equals(KeyCode.DELETE)) {
                deleteItem(selectedItem);
            }
        }
    }
    @FXML
    public void handleFilterButton() {
        ToDoItem selectedItem = toDoListView.getSelectionModel().getSelectedItem();
        if (filterButton.isSelected()) {
            filteredList.setPredicate(todayItems);
            if (filteredList.isEmpty()) {
                text.clear();
                deadLineLabel.setText("");
            } else if (filteredList.contains(selectedItem)) {
                toDoListView.getSelectionModel().select(selectedItem);
            } else {
                toDoListView.getSelectionModel().selectFirst();
            }
        } else {
            filteredList.setPredicate(allItems);
            toDoListView.getSelectionModel().select(selectedItem);
        }
    }
    @FXML
    public void handleExit() {
        Platform.exit();
    }
}

package View.controllers;


import View.AView;
import View.MazeDisplayer;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Observable;
import java.util.ResourceBundle;

public class MazeViewController extends AView
{
    public int _rows;
    public int _cols;

    @FXML
    private MazeDisplayer mazeDisplayerFXML;

    StringProperty updatePlayerRow = new SimpleStringProperty();
    StringProperty updatePlayerCol = new SimpleStringProperty();

    //getters & setters
    public String getUpdatePlayerRow() { return updatePlayerRow.get(); }
    public void setUpdatePlayerRow(int updatePlayerRow) { this.updatePlayerRow.set(updatePlayerRow + ""); }
    public String getUpdatePlayerCol() { return updatePlayerCol.get(); }
    public void setUpdatePlayerCol(int updatePlayerCol) { this.updatePlayerCol.set(updatePlayerCol + ""); }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle)
    {
        Platform.runLater(() -> {
            viewModel.generateMaze(_rows, _cols);
            Media musicFile = new Media(new File("resources/music/PaintingTheRosesRed.mp3").toURI().toString());
            setMusic(musicFile);
        });
    }

    public void openFile(ActionEvent actionEvent) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Open maze");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Maze files (*.maze)", "*.maze"));
        fc.setInitialDirectory(new File("./resources"));
        File chosen = fc.showOpenDialog(null);
        //...
    }

    @FXML
    public void keyPressed(KeyEvent keyEvent)
    {
        viewModel.movePlayer(keyEvent);
        keyEvent.consume();
    }

    public void setPlayerPosition(int row, int col)
    {
        mazeDisplayerFXML.setPlayerPosition(row, col);
        setUpdatePlayerRow(row);
        setUpdatePlayerCol(col);
        if(row == viewModel.getMaze().getGoalPosition().getRowIndex() && col == viewModel.getMaze().getGoalPosition().getColumnIndex())
        {
            Stage MazeWindowStage = new Stage();
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource("View/FXMLs/FinishLineView.fxml"));
            Parent root = null;
            try
            {
                root = fxmlLoader.load();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            //MazeViewController controller = fxmlLoader.getController();
            Scene MazeWindowScene = new Scene(root);
            MazeWindowStage.setScene(MazeWindowScene);
            MazeWindowStage.show();
        }

    }


    @FXML
    public void mouseClicked(MouseEvent mouseEvent) { mazeDisplayerFXML.requestFocus(); }

    void initData(int rows, int cols)
    {
        _rows = rows;
        _cols = cols;
    }

    @FXML
    public void returnToMain(ActionEvent actionEvent) throws IOException
    {
        Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("View/FXMLs/MyView.fxml"));
        root.setId("mainWindow");
        Stage MainWindowStage = new Stage();
        Scene MainWindowScene = new Scene(root, 900, 650);
        MainWindowStage.setTitle("mainScene");
        MainWindowStage.setScene(MainWindowScene);
        MainWindowStage.show();

        ((Node)(actionEvent.getSource())).getScene().getWindow().setOnHidden(e -> mediaPlayer.stop());
        ((Node)(actionEvent.getSource())).getScene().getWindow().hide();


        Media musicFile = new Media(getClass().getClassLoader().getResource("music/AliceMainWindowMusic.mp3").toString());
        mediaPlayer = new MediaPlayer(musicFile);
        setMediaPlayer(mediaPlayer);
        if (!isOff)
        {
            mediaPlayer.setAutoPlay(true);
            mediaPlayer.setOnEndOfMedia( new Runnable() {
                @Override
                public void run() {
                    mediaPlayer.seek(Duration.ZERO);
                    mediaPlayer.play();
                }
            });
        }

        MainWindowStage.getScene().getWindow().setOnHidden(e -> mediaPlayer.stop());
    }

    @Override
    public void update(Observable observable, Object arg)
    {
        String change = (String) arg;
        switch (change)
        {
            case "maze generated" -> mazeGenerated();
            case "maze solved" -> mazeSolved();
            case "player moved" -> playerMoved();
            default -> System.out.println("Not implemented change: " + change);
        }
    }

    private void mazeGenerated()
    {
        mazeDisplayerFXML.drawMaze(viewModel.getMaze().getMazeArray());
        playerMoved();
    }

    private void playerMoved() { setPlayerPosition(viewModel.getPlayerRow(), viewModel.getPlayerCol()); }

    @FXML
    private void mazeSolved() { mazeDisplayerFXML.setSolution(viewModel.getSolution()); }

    @FXML
    public void solveMaze(ActionEvent actionEvent) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText("Solving maze...");
        alert.show();
        viewModel.solveMaze();
    }

    public void zoom(ScrollEvent scrollEvent) { mazeDisplayerFXML.zoom(scrollEvent); }
}

package com.sandbox.topdown.prediction.underscored;

import java.util.function.Consumer;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Maarten
 */
public class DebugController {

    private static final Logger LOG = LoggerFactory.getLogger(DebugController.class);

    @FXML
    private CheckBox inputNaive;

    @FXML
    private CheckBox inputPredict;

    @FXML
    private CheckBox inputSmoothing;

    @FXML
    private Spinner<Integer> inputSmoothingFactor;

    @FXML
    private CheckBox inputServerPosition;

    @FXML
    private CheckBox inputDestination;

    @FXML
    private Spinner<Integer> inputOffset;

    @FXML
    private Spinner<Integer> inputLag;

    private Parent view;

    public DebugController() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/debug.fxml"));
            loader.setController(this);
            this.view = loader.load();
        } catch (Exception ex) {
            throw new RuntimeException("Failed to load debug view", ex);
        }
    }

    public void initComponents(GameCore game) {
        this.inputSmoothingFactor.setDisable(true);

        bindSpinner(this.inputLag, (int) game.fake_lag, 0, 5000, (val) -> game.fake_lag = val);
        bindSpinner(this.inputOffset, game.net_offset, 0, 5000, (val) -> game.net_offset = val);

        bindCheckbox(inputNaive, game.naive_approach, (val) -> game.naive_approach = val);
        bindCheckbox(inputPredict, game.client_predict, (val) -> game.client_predict = val);
        bindCheckbox(inputSmoothing, game.client_smoothing, (val) -> game.client_smoothing = val);

        bindCheckbox(inputServerPosition, game.show_server_pos, (val) -> game.show_server_pos = val);
        bindCheckbox(inputDestination, game.show_dest, (val) -> game.show_dest = val);
    }

    private void bindCheckbox(CheckBox box, boolean initial, Consumer<Boolean> listener) {
        box.setSelected(initial);
        box.selectedProperty().addListener((obs, old, current) -> {
            listener.accept(current);
        });
    }

    private void bindSpinner(Spinner<Integer> spinner, int initial, int min, int max, Consumer<Integer> listener) {
        spinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(min, max, initial));
        spinner.valueProperty().addListener((obs, old, current) -> {
            listener.accept(current);
        });
    }

    public Parent getView() {
        return view;
    }

}

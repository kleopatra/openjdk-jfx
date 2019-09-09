/*
 * Created on 09.09.2019
 *
 */
package test.javafx.scene.control;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.sun.javafx.tk.Toolkit;

import static org.junit.Assert.*;
import static javafx.scene.input.KeyCode.*;
import static javafx.scene.input.KeyEvent.*;

import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.skin.ComboBoxPopupControl;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import test.com.sun.javafx.pgstub.StubToolkit;
import test.com.sun.javafx.scene.control.infrastructure.KeyEventFirer;

/**
 * Test to expose false green with KeyEventFirer.
 * <p>
 * The concrete issue is that a keyPressed filter on the editor is not notified.
 * It's a regression https://bugs.openjdk.java.net/browse/JDK-8229914.
 * It was fixed as https://bugs.openjdk.java.net/browse/JDK-8145515 (without tests).
 * It was re-introduced with fixing https://bugs.openjdk.java.net/browse/JDK-8149622.
 * <p>
 * First try to test the failure failed: firing the event on on the editor passes, which
 * is a false green (as can be reproduced by the example in the bug report).
 * Reason for the false green is that the editor is-a FakeFocusTextField which never is
 * the focusOwner. Firing the event on it produces a dispatch chain that contains the
 * eventFilter, such that the filter is indeed notified. Firing on its parent combo exposes
 * the issue.
 *
 */
public class ComboBoxEnterTest8229914 {

    private StackPane root;
    private Stage stage;
    private Scene scene;
    private ComboBox<String> comboBox;

    /**
     * Here we fire on the editor with support in KeyEventFirer to
     * use the scene as KeyEvent injector. With core version
     * of ComboBoxPopupControl, the test fails
     * as expected.
     */
    @Test
    public void testEnterPressedFilterEditorOnScene() {
        List<KeyEvent> keys = new ArrayList<>();
        comboBox.getEditor().addEventFilter(KEY_PRESSED, keys::add);
        KeyEventFirer keyboard = new KeyEventFirer(comboBox.getEditor(), true);
        keyboard.doKeyPress(ENTER);
        assertEquals("pressed ENTER filter on editor", 1, keys.size());
    }

    @Test
    public void testEnterPressedFilterEditorComboBox() {
        List<KeyEvent> keys = new ArrayList<>();
        comboBox.getEditor().addEventFilter(KEY_PRESSED, keys::add);
        KeyEventFirer keyboard = new KeyEventFirer(comboBox);
        keyboard.doKeyPress(ENTER);
        assertEquals("pressed ENTER filter on editor", 1, keys.size());
    }

    /**
     * Note: this is a false green!
     */
    @Test
    public void testEnterPressedFilterEditor() {
        List<KeyEvent> keys = new ArrayList<>();
        comboBox.getEditor().addEventFilter(KEY_PRESSED, keys::add);
        KeyEventFirer keyboard = new KeyEventFirer(comboBox.getEditor());
        keyboard.doKeyPress(ENTER);
        assertEquals("pressed ENTER filter on editor", 1, keys.size());
    }

    @After
    public void cleanup() {
        stage.hide();
    }

    @Before
    public void setup() {
        ComboBoxPopupControl c;
        // This step is not needed (Just to make sure StubToolkit is loaded into VM)
        Toolkit tk = (StubToolkit) Toolkit.getToolkit();
        root = new StackPane();
        scene = new Scene(root);
        stage = new Stage();
        stage.setScene(scene);
        comboBox = new ComboBox<>();
        comboBox.getItems().addAll("Test", "hello", "world");
        comboBox.setEditable(true);
        root.getChildren().addAll(comboBox);
        stage.show();
    }

}

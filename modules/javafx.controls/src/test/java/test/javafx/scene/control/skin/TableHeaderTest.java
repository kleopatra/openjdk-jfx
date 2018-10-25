/*
 * Created on 18.10.2018
 *
 */
package test.javafx.scene.control.skin;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.rules.TestRule;

import com.sun.javafx.scene.control.TableColumnBaseHelper;
import com.sun.javafx.tk.Toolkit;

import static org.junit.Assert.*;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.skin.NestedTableColumnHeader;
import javafx.scene.control.skin.TableColumnHeader;
import javafx.scene.shape.Rectangle;
import test.com.sun.javafx.scene.control.infrastructure.StageLoader;
import test.com.sun.javafx.scene.control.infrastructure.VirtualFlowTestUtils;
import test.com.sun.javafx.scene.control.test.Person;
/**
 * Contains tests around TableColumnHeader, NestedTableColumnHeader, TableRowHeader.
 * 
 * All these are added for testing properties/notifications/behaviour that
 * was not included in available core tests.
 */
public class TableHeaderTest {


    private static final double MIN_WIDTH = 35;
    private static final double MAX_WIDTH = 2000;
    private static final double PREF_WIDTH = 100;

    private TableColumn<Person,String> column;
    private TableView<Person> table;
    private ObservableList<Person> model;

//--------------------------- testing NestedTableColumnHeader    
    
    /**
     * Test that updating column text results in visible change of "label"
     * (== TableColumnHeader) if column is really nested.
     * 
     * 
     */
    @Test 
    public void testNestedColumnHeaderListeningToResizeText() {
        TableColumn<Person, String> toplevel = new TableColumn<>("TopLevel");
        TableColumn<Person, String> lastName = new TableColumn<>("LastName");
        lastName.setCellValueFactory(cc -> cc.getValue().lastNameProperty());
        column.setCellValueFactory(cc -> cc.getValue().firstNameProperty());
        toplevel.getColumns().addAll(lastName, column);
        table.getColumns().add(toplevel);
        new StageLoader(table);
        
        TableColumnHeader header = VirtualFlowTestUtils.getTableColumnHeader(table, toplevel);
        Node label = header.getChildrenUnmodifiable().get(0);
        assertTrue("label must be visible", label.isVisible());
        // hide header for nested
        toplevel.setText(null);
        assertFalse("label must be hidden", label.isVisible());

    }
    
    /**
     * weird (?) macroscopic effect: headers are removed and re-added?
     * 
     * but listeners not notified: newList.equals(oldList) because the 
     * same headers are re-added (?). same for children. Flying blind ...
     */
    @Test @Ignore
    public void testNestedColumnHeaderListeningToResizePolicy() {
        column.setCellValueFactory(cc -> cc.getValue().firstNameProperty());
        table.getColumns().add(column);
        new StageLoader(table);
        TableColumnHeader header = VirtualFlowTestUtils.getTableColumnHeader(table, column);
        NestedTableColumnHeader nested = (NestedTableColumnHeader) header.getParent();
        ObservableList<TableColumnHeader> headers = nested.getColumnHeaders();
        BooleanProperty changed = new SimpleBooleanProperty();
        headers.addListener((ListChangeListener) c -> {
            changed.set(true);
        });
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        // pulse doesn't help - update is immediate
        // Toolkit.getToolkit().firePulse();
        fail("verified by debugging that content is updated .. no way to actually test?");
        assertTrue("headers must have been updated", changed.get());
    }
//------------------------ testing TableColumnHeader    
    /**
     * How to test? even with protected dispose, cannot access dispose legally.
     * Trying indirectly, via column remove, doesn't work? Works when firing 
     * a pulse.
     */
    @Test
    public void testTableColumnHeaderListenerDispose() {
        column.setCellValueFactory(cc -> cc.getValue().firstNameProperty());
        table.getColumns().add(column);
        new StageLoader(table);
        
        TableColumnHeader header = VirtualFlowTestUtils.getTableColumnHeader(table, column);
        Node graphic = new Rectangle();
        Label label = (Label) header.lookup(".label");
        Node oldGraphic = label.getGraphic();
        // indirect testing: remove will force the headers removal during which its dispose is called
        table.getColumns().remove(column);
        // need to fire a pulse, removal happens lazily?
        Toolkit.getToolkit().firePulse();
        assertNull("header must be removed", VirtualFlowTestUtils.getTableColumnHeader(table, column));
        column.setGraphic(graphic);
        assertEquals("label must not have column graphic", oldGraphic, label.getGraphic());
    }
    
    @Test
    public void testTableColumnHeaderListeningToGraphic() {
        column.setCellValueFactory(cc -> cc.getValue().firstNameProperty());
        table.getColumns().add(column);
        new StageLoader(table);
        
        TableColumnHeader header = VirtualFlowTestUtils.getTableColumnHeader(table, column);
        Node graphic = new Rectangle();
        Label label = (Label) header.lookup(".label");
        column.setGraphic(graphic);
        assertEquals("label must have column graphic", column.getGraphic(), label.getGraphic());
    }
    
    @Test
    public void testTableColumnHeaderListeningToText() {
        column.setCellValueFactory(cc -> cc.getValue().firstNameProperty());
        table.getColumns().add(column);
        new StageLoader(table);
        
        TableColumnHeader header = VirtualFlowTestUtils.getTableColumnHeader(table, column);
        Label label = (Label) header.lookup(".label");
        String changed = "changed";
        column.setText(changed);
        assertEquals("label must have column text", column.getText(), label.getText());
    }
    
    @Test
    public void testTableColumnHeaderListeningToSortable() {
        column.setCellValueFactory(cc -> cc.getValue().firstNameProperty());
        table.getColumns().add(column);
        new StageLoader(table);
        Label sort = new Label("x");
        column.setSortNode(sort);
        table.getSortOrder().add(column);
        TableColumnHeader header = VirtualFlowTestUtils.getTableColumnHeader(table, column);
        column.setSortable(false);
        assertFalse("sortNode must be removed", header.getChildrenUnmodifiable().contains(sort));
    }
    
    @Test
    public void testTableColumnHeaderListeningToSortNode() {
        column.setCellValueFactory(cc -> cc.getValue().firstNameProperty());
        table.getColumns().add(column);
        new StageLoader(table);
        TableColumnHeader header = VirtualFlowTestUtils.getTableColumnHeader(table, column);
        table.getSortOrder().add(column);
        Label sort = new Label("x");
        column.setSortNode(sort);
        assertTrue("sortNode must be added", header.getChildrenUnmodifiable().contains(sort));
    }
    
    @Test
    public void testTableColumnHeaderListeningToVisible() {
        column.setCellValueFactory(cc -> cc.getValue().firstNameProperty());
        table.getColumns().add(column);
        new StageLoader(table);
        TableColumnHeader header = VirtualFlowTestUtils.getTableColumnHeader(table, column);
        column.setVisible(false);
        assertFalse("header must be invisible", header.isVisible());
    }
    
    /**
     * Tests whether the header is listening to the visible property.
     * 
     * Hmm ... doesn't make a difference whether or not the listener
     * is registered. why not?
     */
    @Test @Ignore
    public void testTableColumnHeaderListeningToWidth() {
        assertEquals("sanity: initial width is default", 80., column.getWidth(), 0.1);
        column.setCellValueFactory(cc -> cc.getValue().firstNameProperty());
        table.getColumns().add(column);
        new StageLoader(table);
        TableColumnHeader header = VirtualFlowTestUtils.getTableColumnHeader(table, column);
        double pref = column.getWidth() + 100;
        TableColumnBaseHelper.setWidth(column, pref);
        Toolkit.getToolkit().firePulse();
        assertEquals("autosizing happened", pref, header.getWidth(), 1.);
        fail("passes with/out listener to width property - why?");
    }
    @Test
    public void testTableColumnHeaderListeningToScene() {
        assertEquals("sanity: initial width is default", 80., column.getWidth(), 0.1);
        column.setCellValueFactory(cc -> cc.getValue().firstNameProperty());

        table.getColumns().add(column);
        String longName = "Henry the fifth with all his wives- or which was it?";
        
        Label label =  new Label(longName);
        new StageLoader(label);
        // rough measure for expected width
        double pref = label.prefWidth(-1);
        model.get(0).setFirstName(longName);
        assertEquals("sanity: changing data does not effect width", 80., column.getWidth(), .1);
        new StageLoader(table);
        assertEquals("autosizing happened", pref, column.getWidth(), pref/10);
        
    }
    @Before 
    public void setup() {
        column = new TableColumn<Person,String>("");
        model = FXCollections.observableArrayList(
                new Person("Humphrey McPhee", 76),
                new Person("Justice Caldwell", 30),
                new Person("Orrin Davies", 30),
                new Person("Emma Wilson", 8)
        );
        table = new TableView<Person>(model);
    }


}

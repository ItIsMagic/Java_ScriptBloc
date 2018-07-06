package GUI;

import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.*;


public class GUIController {

    private ArrayList<BlockDisplay> array_bd;

    @FXML
    private MenuItem menu_save;
    @FXML
    private GridPane Blockpane;
    @FXML
    private VBox Architecture;
    @FXML
    private Text description;
    @FXML
    private Label blockTitle;
    @FXML
    private GridPane argBox;

    private ArrayList<BlockDisplay> SourceList = new ArrayList<BlockDisplay>();
    private BlockDisplay tempBlock;
    private int cnt = 0;

    @FXML
    @MethodInfo(name = "initialize()", date = "05/07/18", arguments = "None", comments = "Function called on the opening of the window, initialize the component, call the BDD to get the blocks", returnValue="None" ,revision = 1)
    public void initialize() {
        array_bd = new ArrayList<BlockDisplay>();
        ApiCall api = new ApiCall("http://127.0.0.1:8080/");
        BlockDisplay source;
        try{
            JSONArray jsonObj = new JSONArray(api.ApiGetResponse("block/full?instructions=0"));
            int counter = 0;
            for (int i = 0; i < jsonObj.length(); i++) {
                source = new BlockDisplay(new Label((String)jsonObj.getJSONObject(i).get("name")),0);
                source.blockLabel.setStyle("-fx-background-color: slateblue;-fx-background-radius: 50;");
                source.blockLabel.setPrefWidth(100);
                source.blockLabel.setAlignment(Pos.CENTER);
                source.block = new Block((int)jsonObj.getJSONObject(i).get("id"),(String)jsonObj.getJSONObject(i).get("name"),(String)jsonObj.getJSONObject(i).get("description"),(String)jsonObj.getJSONObject(i).get("type"));
                if(!((String)jsonObj.getJSONObject(i).get("type")).equals("simple")){
                    source.setType(1);
                }
                JSONArray Obj = (JSONArray)jsonObj.getJSONObject(i).get("Arguments");

                if(Obj.length() != 0){
                    for(int j = 0; j < Obj.length(); j++){
                        String temp = (String)Obj.getJSONObject(j).get("keyValue");
                        String key = temp.substring(1, temp.length() - 1);
                        source.block.args.add(new Arguments((int)Obj.getJSONObject(j).get("id"), (String)Obj.getJSONObject(j).get("name"), key, (String)Obj.getJSONObject(j).get("description")));
                    }
                }
                blockLabelInit(source);
                if(counter %2 == 0){
                    Blockpane.add(source.getBlockLabel(),0,counter);
                    counter++;
                }else{
                    Blockpane.add(source.getBlockLabel(),1,counter-1);
                    counter++;
                }

                array_bd.add(source);
            }
        }catch(Exception e){
            System.out.println(e);
        }

        Label target = targetLabelCreation(0);
        target.setPrefHeight(535);
        target.setPrefWidth(438);
        Architecture.getChildren().addAll(target);

        menu_save.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN));
    }

    @MethodInfo(name = "refreshLabel(BlockDisplay selected, String position)", date = "05/07/18", arguments = "1: BlockDisplay selected, the block move on a target in the architecture, 2: String position, the position sent by the target", comments = "used to display the architecture when a block is added or when a script is loaded", returnValue="None" ,revision = 1)
    public void refreshLabel(BlockDisplay selected, String position){
        int counterPadding = 1;
        int counter_2 = 0;
        BlockDisplay endNew = null;
        ArrayList<BlockDisplay> container = new ArrayList<BlockDisplay>();

        ArrayList<Label> ResultList = new ArrayList<Label>();
        BlockDisplay newOne = new BlockDisplay(selected);

        selected.getBlockLabel().setTextFill(Color.BLACK);

        for (BlockDisplay temp : SourceList) {
            if(temp.getType() != 3 && temp.getType() != 2) {
                ResultList.add(targetLabelCreation(counter_2));
            }
            if(counter_2 == Integer.parseInt(position)) {
                newOne.getBlockLabel().setStyle("-fx-label-padding: 0 " + (10 * counterPadding) + ";");
                ResultList.add(newOne.getBlockLabel());
                if (newOne.getType() == 1) {
                    String[] insideContainer = newOne.block.getType().split("\\|");
                    counterPadding++;
                    container = new ArrayList<BlockDisplay>();
                    counter_2++;
                    for(String inside : insideContainer){
                        endNew = new BlockDisplay(new Label(inside),3);
                        endNew.getBlockLabel().setStyle("-fx-label-padding: 0 " + (10 * counterPadding) + ";");
                        container.add(endNew);
                        ResultList.add(endNew.getBlockLabel());
                        counter_2++;
                        ResultList.add(targetLabelCreation(counter_2));
                        endNew = new BlockDisplay(new Label("END"+inside),4);
                        endNew.getBlockLabel().setStyle("-fx-label-padding: 0 " + (10 * counterPadding) + ";");
                        container.add(endNew);
                        ResultList.add(endNew.getBlockLabel());
                        counter_2++;
                    }
                    counterPadding--;
                    endNew = new BlockDisplay(new Label("END"+selected.block.getName()),2);
                    endNew.getBlockLabel().setStyle("-fx-label-padding: 0 " + (10 * counterPadding) + ";");
                    ResultList.add(endNew.getBlockLabel());
                }
                counter_2++;
                ResultList.add(targetLabelCreation(counter_2));
            }
            if(temp.getType() == 2 || temp.getType() == 4){
                counterPadding--;
            }
            temp.getBlockLabel().setStyle("-fx-label-padding: 0 "+(10 * counterPadding)+";");
            if(temp.getType() == 1 || temp.getType() == 3){
                counterPadding++;
            }
            ResultList.add(temp.getBlockLabel());
            counter_2++;
        }

        if(SourceList.isEmpty() || counter_2 == Integer.parseInt(position)) {
            newOne.getBlockLabel().setStyle("-fx-label-padding: 0 "+(10 * counterPadding)+";");
            ResultList.add(targetLabelCreation(counter_2));
            ResultList.add(newOne.getBlockLabel());
            if (newOne.getType() == 1) {
                String[] insideContainer = newOne.block.getType().split("\\|");
                counterPadding++;
                container = new ArrayList<BlockDisplay>();
                counter_2++;
                for(String inside : insideContainer){
                    endNew = new BlockDisplay(new Label(inside),3);
                    endNew.getBlockLabel().setStyle("-fx-label-padding: 0 " + (10 * counterPadding) + ";");
                    container.add(endNew);
                    ResultList.add(endNew.getBlockLabel());
                    counter_2++;
                    ResultList.add(targetLabelCreation(counter_2));
                    endNew = new BlockDisplay(new Label("END"+inside),4);
                    container.add(endNew);
                    endNew.getBlockLabel().setStyle("-fx-label-padding: 0 " + (10 * counterPadding) + ";");
                    ResultList.add(endNew.getBlockLabel());
                    counter_2++;
                }
                counterPadding--;
                endNew = new BlockDisplay(new Label("END"+selected.block.getName()),2);
                endNew.getBlockLabel().setStyle("-fx-label-padding: 0 " + (10 * counterPadding) + ";");
                ResultList.add(endNew.getBlockLabel());
            }
            counter_2++;
        }
        addAction(newOne);
        SourceList.add(Integer.parseInt(position), newOne);
        if(newOne.getType() == 1){
            int counterContained = 1;
            for(BlockDisplay contained : container){
                SourceList.add(Integer.parseInt(position)+counterContained, contained);
                counterContained++;
            }
            SourceList.add(Integer.parseInt(position)+counterContained, endNew);
        }
        Label last = targetLabelCreation(counter_2);
        last.setPrefHeight(535);
        ResultList.add(last);

        Architecture.getChildren().clear();
        Architecture.getChildren().addAll(ResultList);
    }

    @MethodInfo(name = "targetLabelCreation(Integer number)", date = "05/07/18", arguments = "1: Integer number, the position of the target block", comments = "Initialize a Label that will be used as a target for any movement in the architecture", returnValue="Label target, the label initialized with all the handlers" ,revision = 1)
    public Label targetLabelCreation(Integer number){
        Label target = new Label(number.toString());
        target.setPrefWidth(438);
        target.setMinHeight(10);
        target.setFont(new Font(0));
        target.setOnDragOver(new EventHandler <DragEvent>() {
            public void handle(DragEvent event) {
                /* data is dragged over the target */
                //System.out.println("onDragOver");

                /* accept it only if it is  not dragged from the same node
                 * and if it has a string data */
                if (event.getGestureSource() != target &&
                        event.getDragboard().hasString()) {
                    /* allow for both copying and moving, whatever user chooses */
                    event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
                }

                event.consume();
            }
        });

        target.setOnDragEntered(new EventHandler <DragEvent>() {
            public void handle(DragEvent event) {
                /* the drag-and-drop gesture entered the target */
                //System.out.println("onDragEntered");
                /* show to the user that it is an actual gesture target */
                if (event.getGestureSource() != target &&
                        event.getDragboard().hasString()) {
                    target.setStyle("-fx-background-color: black;");
                }

                event.consume();
            }
        });

        target.setOnDragExited(new EventHandler <DragEvent>() {
            public void handle(DragEvent event) {
                /* mouse moved away, remove the graphical cues */
                target.setTextFill(Color.BLACK);
                target.setStyle("-fx-background-color: #505c7c;");
                event.consume();
            }
        });

        target.setOnDragDropped(new EventHandler <DragEvent>() {
            public void handle(DragEvent event) {
                /* data dropped */
                //System.out.println("onDragDropped");
                /* if there is a string data on dragboard, read it and use it */
                Dragboard db = event.getDragboard();
                boolean success = false;
                if (db.hasString()) {
                    //target.setText(db.getString());
                    if(db.getString().contains("#COPY&PASTE#")){
                        copyPaste(db.getString().split("\\|")[1],Integer.parseInt(target.getText()));
                    }else{
                        refreshLabel(tempBlock,target.getText());
                    }
                    success = true;
                }
                /* let the source know whether the string was successfully
                 * transferred and used */
                event.setDropCompleted(success);
                event.consume();
            }
        });
        return target;
    }

    @MethodInfo(name = "blockLabelInit(BlockDisplay source)", date = "05/07/18", arguments = "1: BlockDisplay source, the block on the left panel used to create the architecture", comments = "Initialize the BlockDisplay handlers", returnValue="None" ,revision = 1)
    public void blockLabelInit(BlockDisplay source) {
        source.getBlockLabel().setOnDragDetected(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent event) {
                /* drag was detected, start drag-and-drop gesture*/
                //System.out.println("onDragDetected");
                tempBlock = new BlockDisplay(source);
                /* allow any transfer mode */
                Dragboard db = source.getBlockLabel().startDragAndDrop(TransferMode.ANY);

                /* put a string on dragboard */
                ClipboardContent content = new ClipboardContent();
                content.putString(source.getBlockLabel().getText());
                db.setContent(content);

                event.consume();
            }
        });

        source.getBlockLabel().setOnDragDone(new EventHandler<DragEvent>() {
            public void handle(DragEvent event) {
                /* the drag-and-drop gesture ended */
                //System.out.println("onDragDone");
                /* if the data was successfully moved, clear it */
                    /*if (event.getTransferMode() == TransferMode.MOVE) {
                        source.setText("");
                    }*/
                event.consume();
            }
        });
        source.getBlockLabel().setTextFill(Color.WHITE);
    }

    @MethodInfo(name = "addAction(BlockDisplay newOne)", date = "05/07/18", arguments = "1: BlockDisplay newOne, the new block added in the architecture", comments = "Initialize the handlers for the new block in the architecture", returnValue="None" ,revision = 1)
    public void addAction(BlockDisplay newOne){
        newOne.getBlockLabel().setOnDragDetected(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent event) {
                tempBlock = new BlockDisplay(newOne);
                Dragboard db = newOne.getBlockLabel().startDragAndDrop(TransferMode.ANY);

                ClipboardContent content = new ClipboardContent();
                content.putString("#COPY&PASTE#|"+newOne);
                db.setContent(content);

                event.consume();
            }
        });

        newOne.getBlockLabel().setOnDragDone(new EventHandler<DragEvent>() {
            public void handle(DragEvent event) {
                event.consume();
            }
        });

        newOne.blockLabel.setOnMouseClicked(new EventHandler<MouseEvent>(){
            @Override
            public void handle(MouseEvent t) {
                description.setText(newOne.block.getDescription());
                blockTitle.setText(newOne.block.getName());
                Label tempLabel;
                TextField tempField;
                Tooltip tooltip;
                RowConstraints rc;
                argBox.getChildren().clear();
                argBox.getRowConstraints().clear();
                int counter = 0;
                for(Arguments argsObj : newOne.block.args){

                    tempLabel = new Label(argsObj.getName());
                    tooltip = new Tooltip();
                    tempField = new TextField();
                    tooltip.setText(argsObj.getDescription());
                    tempLabel.setTooltip(tooltip);
                    tempLabel.setTextFill(Color.web("#a0a2ab"));

                    tempField.setText(argsObj.getValue());
                    tempField.textProperty().addListener((observable, oldValue, newValue) -> {
                        argsObj.setValue(newValue);
                    });

                    argBox.add(tempLabel, 0,counter);
                    argBox.add(tempField, 1, counter);
                    rc = new RowConstraints();
                    rc.setMinHeight(40);
                    rc.setPrefHeight(40);
                    rc.setMaxHeight(40);
                    argBox.getRowConstraints().add(rc);
                    counter++;
                }
            }
        });

        ContextMenu contextMenu = new ContextMenu();

        MenuItem item1 = new MenuItem("Delete");
        item1.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                remove(newOne);
            }
        });

        contextMenu.getItems().add(item1);

        // When user right-click on Circle
        newOne.blockLabel.setOnContextMenuRequested(new EventHandler<ContextMenuEvent>() {

            @Override
            public void handle(ContextMenuEvent event) {

                contextMenu.show(newOne.blockLabel, event.getScreenX(), event.getScreenY());
            }
        });
    }

    private BlockDisplay findBlockById(int id) {
//        (Blockpane.getChildren()).forEach((bd) -> {
//            System.out.println(bd.getClass().getName());
//        });

        for(BlockDisplay bd : array_bd) {
            if(bd.block.getId() == id) {
//                System.out.println(bd.block.getName());
                return bd;
            }
        }

        return null;
    }

    public void setArchitecture (JSONArray json) throws JSONException {
        int pos_object;

        for(int i = 0; i < json.length(); i++) {
            JSONObject jso = json.getJSONObject(i);
            Iterator keys = jso.keys();
            int j = 0;
            int arg = 1;
            int id = (int)jso.get("id");
            BlockDisplay bd = findBlockById(id);

            refreshLabel(bd, ""+cnt);
            if(bd.getType() == 1) cnt+=2;
            else cnt++;


            for(pos_object = SourceList.size() - 1; pos_object >= 0; pos_object--) {
                if(SourceList.get(pos_object).getType() == 1 || SourceList.get(pos_object).getType() == 0) {
                    break;
                }
            }


            while(keys.hasNext()) {
                String key = (String) keys.next();
                if(jso.get(key) instanceof JSONObject) {
                    JSONObject value = (JSONObject) jso.get(key);
                    Iterator testo= value.keys();
                    while(testo.hasNext()) {
                        String k = (String) testo.next();
                        if(value.get(k) instanceof JSONArray) {
                            setArchitecture(value.getJSONArray(k));
                            cnt+=2;
                        } else {

                            for(int y = 0; y < SourceList.get(pos_object).block.args.size(); y++) {
                                if(SourceList.get(pos_object).block.args.get(y).getKey().equals(k)) {
                                    SourceList.get(pos_object).block.args.get(y).setValue(value.get(k).toString());
                                }
                            }
                        }
                    }
                }
            }
        }
    }


//    private JSONObject buildJSON(JSONObject jo, String keyName) {
//
//
//    }

    @FXML
    private void saveFile() {
        System.out.println("saveFile");
        JSONObject jo = new JSONObject();
        Stack stack = new Stack();
        try {
//            jo.append("blocks", new JSONArray());
            for(int x = 0; x < SourceList.size(); x++) {
                if(SourceList.get(x).block != null) {
                    JSONObject args = new JSONObject();
                    JSONObject obj = new JSONObject();
                    switch(SourceList.get(x).getType()) {
                        case 0:
                            args.put("id", SourceList.get(x).block.getId());
                            SourceList.get(x).block.args.forEach(el -> {
                                try {
                                    obj.put(el.getKey(), el.getValue());
                                } catch(JSONException e) {
                                    System.out.println(e);
                                }
                            });
                            break;

                        case 1:
                            stack.push(1);
                            break;
                    }
                    args.put("arguments", obj);
                    jo.append("blocks", args);
//                    JSONObject obj = new JSONObject(3);
//                    obj.put("id", SourceList.get(x).block.getId());
//                    JSONObject args = new JSONObject(3);
//                    SourceList.get(x).block.args.forEach((el) -> {
//                        try {
//                            args.put(el.getKey(), el.getValue());
//                        } catch (JSONException e) {
//                            System.out.println(e);
//                        }
//                    });
//                    obj.put("arguments", args);
//                    jo.append("blocks", obj);
//                    System.out.println(SourceList.get(x).block.getName());
                }
            }
            FileChooser fileChooser = new FileChooser();
            fileChooser.setInitialDirectory(new File("C:\\Users\\ycapel\\Documents\\ESGI_cours\\S2\\projet_annuel\\Java_ScriptBloc\\JAVASCRIPTBLOCKS"));
            fileChooser.setTitle("Save file");
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("SM files (*.sm)", "*.sm");
            fileChooser.getExtensionFilters().add(extFilter);
            File file = fileChooser.showSaveDialog(GUI.stage);
            try {
                PrintWriter writer = new PrintWriter(file.toString(), "UTF-8");
                writer.println(jo);
                writer.close();
            } catch (FileNotFoundException e) {
                System.out.println(e);
            } catch (UnsupportedEncodingException e) {
                System.out.println(e);
            }
        } catch(JSONException e) {
            System.out.println(e);
        }
    }

    @MethodInfo(name = "remove(BlockDisplay newOne)", date = "05/07/18", arguments = "1: BlockDisplay newOne, the block which the user choose to delete", comments = "Delete the block from SourceList", returnValue="None" ,revision = 1)
    public void remove(BlockDisplay newOne){
        int counter_1 = 0;
        for(int i = 0; i < SourceList.size() ;i++){
            if(newOne == SourceList.get(i)){
                if(SourceList.get(i).getType() == 1){
                    counter_1=1;
                    SourceList.remove(i);
                    while(true){
                        if(SourceList.size() == i){
                            break;
                        }
                        switch(SourceList.get(i).getType()){
                            case 1:
                                SourceList.remove(i);
                                counter_1++;
                                break;
                            case 2:
                                SourceList.remove(i);
                                counter_1--;
                                break;
                            default :
                                SourceList.remove(i);
                                break;
                        }
                        if(counter_1 == 0){
                            break;
                        }
                    }
                }else{
                    SourceList.remove(i);
                }
                break;
            }
        }
        reDisplay();
    }

    @MethodInfo(name = "reDisplay()", date = "05/07/18", arguments = "", comments = "Regenerate the architecture based on the SourceList", returnValue="None" ,revision = 1)
    public void reDisplay(){
        int counterPadding = 1;
        int counter_2 = 0;

        ArrayList<Label> ResultList = new ArrayList<Label>();

        for (BlockDisplay temp : SourceList) {
            if(temp.getType() != 3 && temp.getType() != 2) {
                ResultList.add(targetLabelCreation(counter_2));
            }

            if(temp.getType() == 2 || temp.getType() == 4){
                counterPadding--;
            }
            temp.getBlockLabel().setStyle("-fx-label-padding: 0 "+(10 * counterPadding)+";");
            if(temp.getType() == 1 || temp.getType() == 3){
                counterPadding++;
            }
            ResultList.add(temp.getBlockLabel());
            counter_2++;
        }

        Label last = targetLabelCreation(counter_2);
        last.setPrefHeight(535);
        ResultList.add(last);

        Architecture.getChildren().clear();
        Architecture.getChildren().addAll(ResultList);
    }

    @MethodInfo(name = "copyPaste(String newOne, int position)", date = "05/07/18", arguments = "1: String newOne, the block identifier, 2: int position, the position where the block will be moved", comments = "Move the block and is Children in the SourceList", returnValue="None" ,revision = 1)
    public void copyPaste(String newOne, int position){
        int start = 0;
        int end = 0;
        int counter_1;
        for(int i = 0; i < SourceList.size() ;i++){
            if(newOne .equals(SourceList.get(i).toString())){
                start = i;
                if(SourceList.get(i).getType() == 1){
                    counter_1=1;
                    while(true){
                        System.out.println("-- I ");
                        System.out.println(i);
                        i++;
                        if(SourceList.size() == i){
                            break;
                        }
                        switch(SourceList.get(i).getType()){
                            case 1:
                                counter_1++;
                                break;
                            case 2:
                                counter_1--;
                                break;
                        }
                        if(counter_1 == 0){
                            break;
                        }
                    }
                }
                end = i;
                break;
            }
        }
        counter_1 = 0;
        if(position == SourceList.size()){
            for(int j = start; j <= end ;j++) {
                SourceList.add(SourceList.remove(start));
            }
        }else{
            for(int i = 0; i < SourceList.size() ;i++) {
                if(counter_1 == position) {
                    if(position <= start) {
                        for (int j = start; j <= end; j++) {
                            SourceList.add(i, SourceList.remove(j));
                            i++;
                        }
                        break;
                    }else{
                        for (int j = start; j <= end; j++) {
                            SourceList.add(i-1, SourceList.remove(start));
                        }
                        break;
                    }
                }
                counter_1++;
            }
        }
        reDisplay();
    }
}

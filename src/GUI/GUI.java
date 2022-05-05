package GUI;

import API.API;
import Model.ModelObject;
import Model.Vendor;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;


public class GUI extends Application{
    private static API api;
    private static final int maxImgSize=100;
    private static final int minImgSize=50;
    private static Stage stage;
    private static Scene scene;
    private static FlowPane mainPane;
    ModelObject vendorContainer=new ModelObject(""){
        @Override
        public @Nullable String getName(){
            return "Vendors:";
        }
    };

    public static void main(String[] args){
        String token=API.getToken("");
        if(token!=null){
            api=API.getInstance(token);
            if (api!=null){
                launch(args);
            }
        }
    }

    public void init() {
        //stuff to do before application starts
    }

    @Override
    public void start(Stage stage) throws Exception{
        this.stage=stage;
        mainPane=new FlowPane();
        scene=new Scene(mainPane);
        stage.setScene(scene);

        update();
        stage.show();
        ArrayList<Vendor> vendors=new ArrayList<>();
        api.getLocations(vendors);
        vendorContainer.children.addAll(vendors);
        //TODO:
        //sort in alphabetical order
        //put objects in a gridpane
        //make objects buttons or clickable boxes
        //refactor so objectView is provided with the current focused object when an update happens
        //this starts as focused on the vendor view
        //separate vendors in the gridpane by a label for closed vendors
        //add sidebar at 15-25% size
        //make sidebar scrollable
        //sidebar shows common children
        //add back button
        //add actual code for api calls to expand out children when an item is selected

        update();
    }

    public void update(){
        mainPane.getChildren().clear();
        mainPane.getChildren().add(contentView(vendorContainer));
        stage.sizeToScene();
    }

    public BorderPane contentView(ModelObject o){
        BorderPane box=new BorderPane();
        VBox center=new VBox();

        if(o!=null){
            //set the clickable children in the center
            for (ModelObject child: o.children){
                HBox childBox=objectView(child);
                center.getChildren().add(childBox);
            }
        } else {
            center.getChildren().add(new Label("Loading..."));
        }

        box.centerProperty().set(center);

        HBox header=new HBox();
        String nameStr;
        if(o==null){
            nameStr="...";
        } else {
            nameStr=o.getName();
        }

        Label name=new Label(nameStr);

        header.getChildren().add(name);

        //view of parents to the left
        VBox parents=new VBox();

        //TODO

        box.leftProperty().set(parents);


        box.topProperty().set(header);

        return box;
    }

    public HBox objectView(ModelObject o){
        HBox main=new HBox();

        HBox img=new HBox();
        img.setMinSize(minImgSize,minImgSize);
        img.setMaxSize(maxImgSize,maxImgSize);

        VBox itemInfo=new VBox();

        Label itemName=new Label(o.getName());
        Label itemDesc=new Label(o.getDescription());

        itemInfo.getChildren().addAll(itemName,itemDesc);

        main.getChildren().addAll(img,itemInfo);



        return main;
    }





}

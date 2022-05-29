package com.example.moviebookingsystem;
import Classes.*;
import DatabaseServices.FinanceServices;
import DatabaseServices.MealServices;
import DatabaseServices.PurchaseServices;
import DatabaseServices.ReportServices;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.sql.SQLException;
import java.sql.Time;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.*;
public class AdminMainMenuController implements Initializable {
    Double totalBalanceSum = 0.0;
    String soldItemsStr = "";
    @FXML
    AnchorPane checkOutPane;
    Meal selectedMeal;
    @FXML
    ComboBox <String> categoryComboBox;
    @FXML
    TextField mealTitle,mealPrice,mealQuantity,soldMealQuantity;
    @FXML
    Pane dashboardPane,adminsMenuPane,usersMenuPane,ticketsMenuPane,foodMenuPane,addMealPane,mealsTable,sellMealPane;
    @FXML
    TextArea mealDescription;
    @FXML
    ListView<String> mealsList;
    @FXML
    TableColumn<Meal,String> MealTitleColumn,MealCategoryColumn,MealDescColumn;
    @FXML
    TableColumn<Meal,Double>MealPriceColumn;
    @FXML
    TableColumn<Meal,Integer>MealQuantityColumn;
    @FXML
    Button usersNavLink,dashboardNavLink,adminsNavLink,ticketsNavLink,foodNavLink,addMeal,viewAllMealsButton,sellMeal;
    @FXML
    Label errorMessage,totalBalance,availableBalance;
    ObservableList <String> categoryComboBoxItems = FXCollections.observableArrayList("Food","Drink");
    @FXML
    TableView<Meal> allMealsTable;
    @FXML
    VBox cartItems;
    @FXML
    public void onSellItems() throws IOException, JRException, SQLException {
        DatabaseConnection db = new DatabaseConnection();
        db.Connect();
        Date date = new Date();
        PurchaseID ID = new PurchaseID();
        Purchase purchase = new Purchase(soldItemsStr,totalBalanceSum, ID.generateUniqueId());
        PurchaseServices.sell(purchase);
        ReportServices.printInvoice("Blank_Letter.jrxml",purchase.getPurchaseId());
        FinanceServices.getTotalBalance();
        System.out.println(purchase.getPurchaseId());
    }
    @FXML
    public void onUsersReport() throws JRException, SQLException {
        DatabaseConnection db = new DatabaseConnection();
        db.Connect();
        ReportServices.ConnectReport("Invoice_4.jrxml");
    }
    @FXML
    public void onGoToCheckout() throws SQLException {

        for(Meal element : Cart.getCartItems()){
            soldItemsStr+= element.getMealTitle()+" ";
            double num = element.getPrice()*Integer.parseInt(soldMealQuantity.getText());
            totalBalanceSum+=num;
            MealServices.decrementMealQuantity(element.getMealTitle(),Double.parseDouble(soldMealQuantity.getText()));
            Label label = new Label(element.getMealTitle() + "  x"+soldMealQuantity.getText()+" "+num+"$");
            label.setTextFill(Color.WHITE);
            label.setFont(new Font(30));
            totalBalance.setText(String.valueOf(totalBalanceSum)+"$");
            cartItems.getChildren().add(label);
        }

        checkOutPane.toFront();

    }
    @FXML
    public  void onAddToCart() throws IOException {
        if(selectedMeal.getQuantity()<Integer.parseInt(soldMealQuantity.getText()))
        {
            errorMessage.setText("Quantity not available in storage!");
            return;
        }
        else if(Integer.parseInt(soldMealQuantity.getText())<=0)
        {
            errorMessage.setText("Please enter a number greater than 0!");
            return;
        }
        Cart cart = new Cart();
        Cart.addToCart(selectedMeal);
        System.out.println(Cart.getCartItems().size());
    }
    @FXML
    public void handleNavLinkClick(ActionEvent e){
        if(e.getSource() == usersNavLink)
            usersMenuPane.toFront();
        if(e.getSource() == dashboardNavLink)
            dashboardPane.toFront();
        if(e.getSource() == adminsNavLink)
            adminsMenuPane.toFront();
        if(e.getSource() == ticketsNavLink)
            ticketsMenuPane.toFront();
        if(e.getSource() == foodNavLink)
            foodMenuPane.toFront();

    }
    @FXML
    public void onViewAllMeals() throws SQLException {
        mealsTable.toFront();
        allMealsTable.setItems(MealServices.viewAllMeals());
        MealTitleColumn.setCellValueFactory(new PropertyValueFactory<Meal,String>("mealTitle"));
        MealCategoryColumn.setCellValueFactory(new PropertyValueFactory<Meal,String>("category"));
        MealPriceColumn.setCellValueFactory(new PropertyValueFactory<Meal,Double>("price"));
        MealQuantityColumn.setCellValueFactory(new PropertyValueFactory<Meal,Integer>("quantity"));
        MealDescColumn.setCellValueFactory(new PropertyValueFactory<Meal,String>("description"));

    }
//    @FXML
//    public void handleListClick(ActionEvent event){
////        System.out.println(mealsList.getSelectionModel().getSelectedItem());
//        System.out.println("hi");
//    }
    @FXML
    public void handleAdminMenuClick(ActionEvent e) throws SQLException {
        ObservableList<String> mealsListObsv = FXCollections.observableArrayList();
        if(e.getSource() == addMeal)
            addMealPane.toFront();
        if(e.getSource() == sellMeal)
        {
            sellMealPane.toFront();
            for(Meal element : MealServices.viewAllMeals()){
                mealsListObsv.add(element.getMealTitle());
            }
            mealsList.setItems(mealsListObsv);
            mealsList.setOnMouseClicked(event->{
                try {
                    for(Meal element:MealServices.viewAllMeals())
                    {
                        if(element.getMealTitle().equals(mealsList.getSelectionModel().getSelectedItem()))
                            selectedMeal = element;
                    }

                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            });
        }
    }

    @FXML
    private void onSubmitMealForm() throws SQLException, FileNotFoundException {
        DatabaseConnection db = new DatabaseConnection();
        db.Connect();
        MealServices.addMeal(mealTitle.getText(),mealDescription.getText(),categoryComboBox.getValue(),Double.parseDouble(mealPrice.getText()),Integer.parseInt(mealQuantity.getText()));
    }
    @FXML
    public void onViewAllUsers() throws IOException {
        Navigator navigator = new Navigator();
        navigator.Navigate("UsersTable.fxml","Users Table");
    }
    @FXML
    public void onSearchUsers() throws IOException {
        Navigator navigator = new Navigator();
        navigator.Navigate("SearchUsers.fxml","Search Users");
    }
    @FXML
    private void onDeleteUser() throws IOException {
        Navigator navigator = new Navigator();
        navigator.Navigate("deleteUserForm.fxml","Delete User");
    }
    @FXML
    public void onAddAdmin(){

    }
    @FXML
    public void onDeleteAdmin(){

    }
    @FXML
    public void onViewAllAdmins(){

    }
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        try {
            System.out.println(FinanceServices.getTotalBalance());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        categoryComboBox.setItems(categoryComboBoxItems);
        try {
            availableBalance.setText(FinanceServices.getTotalBalance());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
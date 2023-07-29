import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import net.proteanit.sql.DbUtils;

public class MealDiaryInfo extends javax.swing.JFrame implements MainInterface{

    //java.sql variables
    private Connection connection = null;
    private PreparedStatement pst = null;
    private ResultSet resultSet = null;
    
    private byte[] foodImg = null;

    //constructor
    public MealDiaryInfo() {
        initComponents();
        init();
        connection = dbConnect();
        DisplayUpdatedMealInfoTbl();
    }
    
    //Registering the Driver and getting the connection
    public Connection dbConnect(){
        try{
            Class.forName("org.sqlite.JDBC");
            //Registering the Driver and getting the connection
            connection= DriverManager.getConnection("jdbc:sqlite:EasyStat.sqlite");
            return connection;
        } catch (ClassNotFoundException | SQLException e) {
            JOptionPane.showMessageDialog(null, e);
            return null;
        }
    }
    
    //windows appearence
    @Override
    public void init() {
        //re-center windows
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        //disable resize windows
        setResizable(false);
    }

    //to close previous jframe on the opening of new jframe
    @Override
    public void close() {
        WindowEvent winClosingEvent = new WindowEvent(this, WindowEvent.WINDOW_CLOSING);
        Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(winClosingEvent);
    }

    //to display updated meal in tblMealInfo table
    @Override
    public void DisplayUpdatedMealInfoTbl() {
        try {
            //Query to retrieve records
            String sql = "select ID, Food_name, Food_group, Drinks, Date, time_of_Day from Meal_info";
            //PreparedStatement object that takes Query parameter
            pst = connection.prepareStatement(sql);
            //Executing the query
            resultSet = pst.executeQuery();
            //fetching data from database to tblMealInfo table
            tblMealInfo.setModel(DbUtils.resultSetToTableModel(resultSet));
        } catch (SQLException ex) {
            Logger.getLogger(MealDiaryInfo.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    //set fieldtext according to row of meal info
    @Override
    public void getValue() {
        try {
            //set fieldtext according to meal info
            txtFoodName.setText(resultSet.getString("Food_name"));
            comboFoodGroup.setSelectedItem(resultSet.getString("Food_group"));
            txtDrinks.setText(resultSet.getString("Drinks"));
            txtDate.setText(resultSet.getString("Date"));
            comboDay.setSelectedItem(resultSet.getString("time_of_Day"));

            byte[] imageData = resultSet.getBytes("Photo");
            ImageIcon format = new ImageIcon(scaledImage(imageData, labelImage.getWidth(), labelImage.getHeight()));
            labelImage.setIcon(format);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(rootPane, ex);
        }
    }
    
    //set jlabel according to row of meal info
    @Override
    public void getValueImage(){
        try {
            byte[] imageData = resultSet.getBytes("Photo");
            ImageIcon format = new ImageIcon(scaledImage(imageData, labelImage.getWidth(), labelImage.getHeight()));
            labelImage.setIcon(format);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(rootPane, ex);
        }
    }
    
    @Override
    public void signOut() {
        try {
            //to close previous jframe on the opening of new jframe
            close();
            LogIn obj = new LogIn();
            obj.setVisible(true);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(rootPane, e);
        } finally {
            try {
                //close sqlite database connection.
                resultSet.close();
                pst.close();
                connection.close();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(rootPane, e);
            }
        }
    }
    
    //whenever you click particular meal diary in table, it will show in Jtextfield, fieldtext
    @Override
    public void displayMealInfo(){
        try {
            int row = tblMealInfo.getSelectedRow();
            String tableClick = (tblMealInfo.getModel().getValueAt(row, 0).toString());
            //Query to retrieve records
            String sql = "select * from Meal_Info where ID = '" + tableClick + "'";
            //PreparedStatement object that takes tableClick input parameters
            pst = connection.prepareStatement(sql);
            //Executing the query
            resultSet = pst.executeQuery();
            //This method returns a boolean value.
            //If there are , else it returns true.
            if (resultSet.isBeforeFirst()) { //rows next to its current position this method
                getValue();
            }else{
                //no rows next to its current position this method returns false
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(rootPane, e);
        }
    }
    
    //to search by food name
    @Override
    public void searchByFoodName(){
        String sql = "select * from Meal_info where Food_Name = ?";
        try {
            //PreparedStatement object that takes Food_Name input parameters
            pst = connection.prepareStatement(sql);
            pst.setString(1, txtSearch.getText());
            //call an execute statement to execute a PreparedStatement object
            resultSet = pst.executeQuery();
            //isBeforeFirst is used to solve the bug of next().
            if (resultSet.isBeforeFirst()) {
                getValueImage();
                tblMealInfo.setModel(DbUtils.resultSetToTableModel(resultSet));
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(rootPane, e);
        }
    }
    
    //to search by food group
    @Override
    public void searchByFoodGroup(){
        String sql = "select * from Meal_info where Food_group = ?";
        try {
            //PreparedStatement object that takes Food_group input parameters
            pst = connection.prepareStatement(sql);
            pst.setString(1, txtSearch.getText());
            //call an execute statement to execute a PreparedStatement object
            resultSet = pst.executeQuery();
            //determine whether the cursor is at the default position of the ResultSet.
            //isBeforeFirst is used to solve the bug of next().
            if (resultSet.isBeforeFirst()) {
                getValueImage();
                tblMealInfo.setModel(DbUtils.resultSetToTableModel(resultSet));
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(rootPane, e);
        }
    }
    
    //to search by food time of the day
    @Override
    public void searchByTimeOfTheDay(){
        //Query to retrieve records
        String sql2 = "select * from Meal_info where time_of_Day = ?";
        try {
            //PreparedStatement object that takes time_of_Day input parameters
            pst = connection.prepareStatement(sql2);
            pst.setString(1, txtSearch.getText());
            //Executing the query
            resultSet = pst.executeQuery();
            //Verifying whether the cursor is at the end of the ResultSet.
            //isBeforeFirst is used to solve the bug of next().
            if (resultSet.isBeforeFirst()) {
                getValueImage();
                tblMealInfo.setModel(DbUtils.resultSetToTableModel(resultSet));
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(rootPane, e);
        }
    }
    
    //to search by ID
    @Override
    public void searchByID(){
        //Query to retrieve records
        String sql2 = "select * from Meal_info where id = ?";
        try {
            //PreparedStatement object that takes id input parameters
            pst = connection.prepareStatement(sql2);
            pst.setString(1, txtSearch.getText());
            //Executing the query
            resultSet = pst.executeQuery();
            //Verifying whether the cursor is at the end of the ResultSet
            //isBeforeFirst is used to solve the bug of next()..
            if (resultSet.isBeforeFirst()) {
                getValue();
                tblMealInfo.setModel(DbUtils.resultSetToTableModel(resultSet));
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(rootPane, e);
        }
    }
    
    //to search by drinks
    @Override
    public void searchByDrinks(){
        //Query to retrieve records
        String sql2 = "select * from Meal_info where Drinks = ?";
        try {
            //PreparedStatement object that takes Drinks = ? input parameters
            pst = connection.prepareStatement(sql2);
            pst.setString(1, txtSearch.getText());
            //Executing the query
            resultSet = pst.executeQuery();
            //Verifying whether the cursor is at the end of the ResultSet.
            //isBeforeFirst is used to solve the bug of next().
            if (resultSet.isBeforeFirst()) {
                getValueImage();
                tblMealInfo.setModel(DbUtils.resultSetToTableModel(resultSet));
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(rootPane, e);
        }
    }
    
    //to search by date
    @Override
    public void searchByDate(){
        //Query to retrieve records
        String sql2 = "select * from Meal_info where Date = ?";
        try {
            //PreparedStatement object that takes Date = ? input parameters
            pst = connection.prepareStatement(sql2);
            pst.setString(1, txtSearch.getText());
            //Executing the query
            resultSet = pst.executeQuery();
            //Verifying whether the cursor is at the end of the ResultSet.
            //isBeforeFirst is used to solve the bug of next().
            if (resultSet.isBeforeFirst()) {
                getValueImage();
                tblMealInfo.setModel(DbUtils.resultSetToTableModel(resultSet));
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(rootPane, e);
        }
    }
    
    // Search query can involve two fields (or more) at a time
    @Override
    public void searchFilterByEverything(){
        String sql = "select * from Meal_info where Food_name= ? and id= ?";
        try {
            //PreparedStatement object that takes Food_name= ? and id= ? input parameters
            pst = connection.prepareStatement(sql);
            pst.setString(1, txtSearch.getText());
            pst.setString(2, txtSearch2.getText());
            //call an execute statement to execute a PreparedStatement object
            resultSet = pst.executeQuery();
            //isBeforeFirst is used to solve the bug of next().
            if (resultSet.isBeforeFirst()) {
                getValueImage();
                tblMealInfo.setModel(DbUtils.resultSetToTableModel(resultSet));
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(rootPane, e);
        }

        String sql2 = "select * from Meal_info where Food_name= ? and Food_group= ?";
        try {
            //PreparedStatement object that takes Food_name= ? and Food_group= ? input parameters
            pst = connection.prepareStatement(sql2);
            pst.setString(1, txtSearch.getText());
            pst.setString(2, txtSearch2.getText());
            //call an execute statement to execute a PreparedStatement object
            resultSet = pst.executeQuery();
            //isBeforeFirst is used to solve the bug of next().
            if (resultSet.isBeforeFirst()) {
                getValueImage();
                tblMealInfo.setModel(DbUtils.resultSetToTableModel(resultSet));
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(rootPane, e);
        }

        String sql3 = "select * from Meal_info where Food_name= ? and Drinks= ?";
        try {
            //PreparedStatement object that takes Food_name= ? and Drinks= ? input parameters
            pst = connection.prepareStatement(sql3);
            pst.setString(1, txtSearch.getText());
            pst.setString(2, txtSearch2.getText());
            //call an execute statement to execute a PreparedStatement object
            resultSet = pst.executeQuery();
            //isBeforeFirst is used to solve the bug of next().
            if (resultSet.isBeforeFirst()) {
                getValueImage();
                tblMealInfo.setModel(DbUtils.resultSetToTableModel(resultSet));
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(rootPane, e);
        }

        String sql4 = "select * from Meal_info where Food_name= ? and Date= ?";
        try {
            //PreparedStatement object that takes Food_name= ? and Date= ? input parameters
            pst = connection.prepareStatement(sql4);
            pst.setString(1, txtSearch.getText());
            pst.setString(2, txtSearch2.getText());
            //call an execute statement to execute a PreparedStatement object
            resultSet = pst.executeQuery();
            //isBeforeFirst is used to solve the bug of next().
            if (resultSet.isBeforeFirst()) {
                getValueImage();
                tblMealInfo.setModel(DbUtils.resultSetToTableModel(resultSet));
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(rootPane, e);
        }

        String sql5 = "select * from Meal_info where Food_name= ? and time_of_Day= ?";
        try {
            //PreparedStatement object that takes Food_name= ? and time_of_Day= ? input parameters
            pst = connection.prepareStatement(sql5);
            pst.setString(1, txtSearch.getText());
            pst.setString(2, txtSearch2.getText());
            //call an execute statement to execute a PreparedStatement object
            resultSet = pst.executeQuery();
            //isBeforeFirst is used to solve the bug of next().
            if (resultSet.isBeforeFirst()) {
                getValueImage();
                tblMealInfo.setModel(DbUtils.resultSetToTableModel(resultSet));
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(rootPane, e);
        }

        String sql6 = "select * from Meal_info where Food_group= ? and id= ?";
        try {
            //PreparedStatement object that takes Food_group= ? and id= ? input parameters
            pst = connection.prepareStatement(sql6);
            pst.setString(1, txtSearch.getText());
            pst.setString(2, txtSearch2.getText());
            //call an execute statement to execute a PreparedStatement object
            resultSet = pst.executeQuery();
            //isBeforeFirst is used to solve the bug of next().
            if (resultSet.isBeforeFirst()) {
                getValueImage();
                tblMealInfo.setModel(DbUtils.resultSetToTableModel(resultSet));
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(rootPane, e);
        }

        String sql7 = "select * from Meal_info where Food_group= ? and Food_name= ?";
        try {
            //PreparedStatement object that takes Food_group= ? and Food_name= ? input parameters
            pst = connection.prepareStatement(sql7);
            pst.setString(1, txtSearch.getText());
            pst.setString(2, txtSearch2.getText());
            //call an execute statement to execute a PreparedStatement object
            resultSet = pst.executeQuery();
            //isBeforeFirst is used to solve the bug of next().
            if (resultSet.isBeforeFirst()) {
                getValueImage();
                tblMealInfo.setModel(DbUtils.resultSetToTableModel(resultSet));
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(rootPane, e);
        }

        String sql8 = "select * from Meal_info where Food_group= ? and Drinks= ?";
        try {
            //PreparedStatement object that takes Food_group= ? and Drinks= ? input parameters
            pst = connection.prepareStatement(sql8);
            pst.setString(1, txtSearch.getText());
            pst.setString(2, txtSearch2.getText());
            //call an execute statement to execute a PreparedStatement object
            resultSet = pst.executeQuery();
            //isBeforeFirst is used to solve the bug of next().
            if (resultSet.isBeforeFirst()) {
                getValueImage();
                tblMealInfo.setModel(DbUtils.resultSetToTableModel(resultSet));
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(rootPane, e);
        }

        String sql9 = "select * from Meal_info where Food_group= ? and Date= ?";
        try {
            //PreparedStatement object that takes Food_group= ? and Date= ? input parameters
            pst = connection.prepareStatement(sql9);
            pst.setString(1, txtSearch.getText());
            pst.setString(2, txtSearch2.getText());
            //call an execute statement to execute a PreparedStatement object
            resultSet = pst.executeQuery();
            //isBeforeFirst is used to solve the bug of next().
            if (resultSet.isBeforeFirst()) {
                getValueImage();
                tblMealInfo.setModel(DbUtils.resultSetToTableModel(resultSet));
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(rootPane, e);
        }

        String sql10 = "select * from Meal_info where Food_group= ? and time_of_Day= ?";
        try {
            //PreparedStatement object that takes Food_group= ? and time_of_Day= ? input parameters
            pst = connection.prepareStatement(sql10);
            pst.setString(1, txtSearch.getText());
            pst.setString(2, txtSearch2.getText());
            //call an execute statement to execute a PreparedStatement object
            resultSet = pst.executeQuery();
            //isBeforeFirst is used to solve the bug of next().
            if (resultSet.isBeforeFirst()) {
                getValueImage();
                tblMealInfo.setModel(DbUtils.resultSetToTableModel(resultSet));
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(rootPane, e);
        }

        String sql11 = "select * from Meal_info where Drinks= ? and id= ?";
        try {
            //PreparedStatement object that takes Drinks= ? and id= ? input parameters
            pst = connection.prepareStatement(sql11);
            pst.setString(1, txtSearch.getText());
            pst.setString(2, txtSearch2.getText());
            //call an execute statement to execute a PreparedStatement object
            resultSet = pst.executeQuery();
            //isBeforeFirst is used to solve the bug of next().
            if (resultSet.isBeforeFirst()) {
                getValueImage();
                tblMealInfo.setModel(DbUtils.resultSetToTableModel(resultSet));
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(rootPane, e);
        }

        String sql12 = "select * from Meal_info where Drinks= ? and Food_name= ?";
        try {
            //PreparedStatement object that takes Drinks= ? and Food_name= ? input parameters
            pst = connection.prepareStatement(sql12);
            pst.setString(1, txtSearch.getText());
            pst.setString(2, txtSearch2.getText());
            //call an execute statement to execute a PreparedStatement object
            resultSet = pst.executeQuery();
            //isBeforeFirst is used to solve the bug of next().
            if (resultSet.isBeforeFirst()) {
                getValueImage();
                tblMealInfo.setModel(DbUtils.resultSetToTableModel(resultSet));
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(rootPane, e);
        }

        String sql13 = "select * from Meal_info where Drinks= ? and Food_group= ?";
        try {
            //PreparedStatement object that takes Drinks= ? and Food_group= ? input parameters
            pst = connection.prepareStatement(sql13);
            pst.setString(1, txtSearch.getText());
            pst.setString(2, txtSearch2.getText());
            //call an execute statement to execute a PreparedStatement object
            resultSet = pst.executeQuery();
            //isBeforeFirst is used to solve the bug of next().
            if (resultSet.isBeforeFirst()) {
                getValueImage();
                tblMealInfo.setModel(DbUtils.resultSetToTableModel(resultSet));
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(rootPane, e);
        }

        String sql14 = "select * from Meal_info where Drinks= ? and Date= ?";
        try {
            //PreparedStatement object that takes Drinks= ? and Date= ? input parameters
            pst = connection.prepareStatement(sql14);
            pst.setString(1, txtSearch.getText());
            pst.setString(2, txtSearch2.getText());
            //call an execute statement to execute a PreparedStatement object
            resultSet = pst.executeQuery();
            //isBeforeFirst is used to solve the bug of next().
            if (resultSet.isBeforeFirst()) {
                getValueImage();
                tblMealInfo.setModel(DbUtils.resultSetToTableModel(resultSet));
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(rootPane, e);
        }

        String sql15 = "select * from Meal_info where Drinks= ? and time_of_Day= ?";
        try {
            //PreparedStatement object that takes Drinks= ? and time_of_Day= ? input parameters
            pst = connection.prepareStatement(sql15);
            pst.setString(1, txtSearch.getText());
            pst.setString(2, txtSearch2.getText());
            //call an execute statement to execute a PreparedStatement object
            resultSet = pst.executeQuery();
            //isBeforeFirst is used to solve the bug of next().
            if (resultSet.isBeforeFirst()) {
                getValueImage();
                tblMealInfo.setModel(DbUtils.resultSetToTableModel(resultSet));
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(rootPane, e);
        }

        String sql16 = "select * from Meal_info where Date= ? and id= ?";
        try {
            //PreparedStatement object that takes Date= ? and id= ? input parameters
            pst = connection.prepareStatement(sql16);
            pst.setString(1, txtSearch.getText());
            pst.setString(2, txtSearch2.getText());
            //call an execute statement to execute a PreparedStatement object
            resultSet = pst.executeQuery();
            //isBeforeFirst is used to solve the bug of next().
            if (resultSet.isBeforeFirst()) {
                getValueImage();
                tblMealInfo.setModel(DbUtils.resultSetToTableModel(resultSet));
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(rootPane, e);
        }

        String sql17 = "select * from Meal_info where Date= ? and Food_name= ?";
        try {
            //PreparedStatement object that takes Date= ? and Food_name= ? input parameters
            pst = connection.prepareStatement(sql17);
            pst.setString(1, txtSearch.getText());
            pst.setString(2, txtSearch2.getText());
            //call an execute statement to execute a PreparedStatement object
            resultSet = pst.executeQuery();
            //isBeforeFirst is used to solve the bug of next().
            if (resultSet.isBeforeFirst()) {
                getValueImage();
                tblMealInfo.setModel(DbUtils.resultSetToTableModel(resultSet));
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(rootPane, e);
        }

        String sql18 = "select * from Meal_info where Date= ? and Food_group= ?";
        try {
            //PreparedStatement object that takes Date= ? and Food_group= ? input parameters
            pst = connection.prepareStatement(sql18);
            pst.setString(1, txtSearch.getText());
            pst.setString(2, txtSearch2.getText());
            //call an execute statement to execute a PreparedStatement object
            resultSet = pst.executeQuery();
            //isBeforeFirst is used to solve the bug of next().
            if (resultSet.isBeforeFirst()) {
                getValueImage();
                tblMealInfo.setModel(DbUtils.resultSetToTableModel(resultSet));
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(rootPane, e);
        }

        String sql19 = "select * from Meal_info where Date= ? and Drinks= ?";
        try {
            //PreparedStatement object that takes Date= ? and Drinks= ? input parameters
            pst = connection.prepareStatement(sql19);
            pst.setString(1, txtSearch.getText());
            pst.setString(2, txtSearch2.getText());
            //call an execute statement to execute a PreparedStatement object
            resultSet = pst.executeQuery();
            //isBeforeFirst is used to solve the bug of next().
            if (resultSet.isBeforeFirst()) {
                getValueImage();
                tblMealInfo.setModel(DbUtils.resultSetToTableModel(resultSet));
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(rootPane, e);
        }

        String sql20 = "select * from Meal_info where Date= ? and time_of_Day= ?";
        try {
            //PreparedStatement object that takes Date= ? and time_of_Day= ? input parameters
            pst = connection.prepareStatement(sql20);
            pst.setString(1, txtSearch.getText());
            pst.setString(2, txtSearch2.getText());
            //call an execute statement to execute a PreparedStatement object
            resultSet = pst.executeQuery();
            //isBeforeFirst is used to solve the bug of next().
            if (resultSet.isBeforeFirst()) {
                getValueImage();
                tblMealInfo.setModel(DbUtils.resultSetToTableModel(resultSet));
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(rootPane, e);
        }

        String sql21 = "select * from Meal_info where time_of_Day= ? and id= ?";
        try {
            //PreparedStatement object that takes time_of_Day= ? and id= ? input parameters
            pst = connection.prepareStatement(sql21);
            pst.setString(1, txtSearch.getText());
            pst.setString(2, txtSearch2.getText());
            //call an execute statement to execute a PreparedStatement object
            resultSet = pst.executeQuery();
            //isBeforeFirst is used to solve the bug of next().
            if (resultSet.isBeforeFirst()) {
                getValueImage();
                tblMealInfo.setModel(DbUtils.resultSetToTableModel(resultSet));
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(rootPane, e);
        }

        String sql22 = "select * from Meal_info where time_of_Day= ? and Food_name= ?";
        try {
            //PreparedStatement object that takes time_of_Day= ? and Food_name= ? input parameters
            pst = connection.prepareStatement(sql22);
            pst.setString(1, txtSearch.getText());
            pst.setString(2, txtSearch2.getText());
            //call an execute statement to execute a PreparedStatement object
            resultSet = pst.executeQuery();
            //isBeforeFirst is used to solve the bug of next().
            if (resultSet.isBeforeFirst()) {
                getValueImage();
                tblMealInfo.setModel(DbUtils.resultSetToTableModel(resultSet));
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(rootPane, e);
        }

        String sql23 = "select * from Meal_info where time_of_Day= ? and Food_group= ?";
        try {
            //PreparedStatement object that takes time_of_Day= ? and Food_group= ? input parameters
            pst = connection.prepareStatement(sql23);
            pst.setString(1, txtSearch.getText());
            pst.setString(2, txtSearch2.getText());
            //call an execute statement to execute a PreparedStatement object
            resultSet = pst.executeQuery();
            //isBeforeFirst is used to solve the bug of next().
            if (resultSet.isBeforeFirst()) {
                getValueImage();
                tblMealInfo.setModel(DbUtils.resultSetToTableModel(resultSet));
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(rootPane, e);
        }

        String sql24 = "select * from Meal_info where time_of_Day= ? and Drinks= ?";
        try {
            //PreparedStatement object that takes time_of_Day= ? and Drinks= ? input parameters
            pst = connection.prepareStatement(sql24);
            pst.setString(1, txtSearch.getText());
            pst.setString(2, txtSearch2.getText());
            //call an execute statement to execute a PreparedStatement object
            resultSet = pst.executeQuery();
            //isBeforeFirst is used to solve the bug of next().
            if (resultSet.isBeforeFirst()) {
                getValueImage();
                tblMealInfo.setModel(DbUtils.resultSetToTableModel(resultSet));
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(rootPane, e);
        }

        String sql25 = "select * from Meal_info where time_of_Day= ? and Date= ?";
        try {
            //PreparedStatement object that takes time_of_Day and Date input parameters
            pst = connection.prepareStatement(sql25);
            pst.setString(1, txtSearch.getText());
            pst.setString(2, txtSearch2.getText());
            //call an execute statement to execute a PreparedStatement object
            resultSet = pst.executeQuery();
            //isBeforeFirst is used to solve the bug of next().
            if (resultSet.isBeforeFirst()) {
                getValueImage();
                tblMealInfo.setModel(DbUtils.resultSetToTableModel(resultSet));
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(rootPane, e);
        }
    }

    //to clear textdfiled
    @Override
    public void clearInfo(){
        txtFoodName.setText(null);
        comboFoodGroup.setSelectedItem("Fruits");
        txtDrinks.setText(null);
        txtDate.setText(null);
        comboDay.setSelectedItem("Breakfast");
        txtSearch.setText("Search...");
        txtSearch2.setText("Search with second variable");
        labelImage.setIcon(null);
        txtImageUpld.setText(null);
        DisplayUpdatedMealInfoTbl();
    }
    
    //to add textfield info into sqlite database and display in table form
    @Override
    public void addDiary(){
        //Query to insert records
        String sql = "insert into Meal_info (Food_name, food_group, drinks, Date, time_of_Day) values (?, ?, ? ,? ,?)";
        try {
            //PreparedStatement object that takes 5 input parameters
            pst = connection.prepareStatement(sql);
            pst.setString(1, txtFoodName.getText());
            pst.setString(2, (String) comboFoodGroup.getSelectedItem());
            pst.setString(3, txtDrinks.getText());
            pst.setString(4, txtDate.getText());
            pst.setString(5, (String) comboDay.getSelectedItem());
            //Executing the query
            pst.execute();
            JOptionPane.showMessageDialog(rootPane, "Saved.");

        } catch (Exception e) {
            JOptionPane.showMessageDialog(rootPane, e);
        }
        DisplayUpdatedMealInfoTbl();
    }
    
    //to update database from textfield and display updated meal diary in table
    @Override
    public void updateDiary(){
        DefaultTableModel RecordTable = (DefaultTableModel) tblMealInfo.getModel();
        int SelectedRows = tblMealInfo.getSelectedRow();
        //Query to update records
        String sql = "update Meal_info set Food_name= ?, food_group= ?, drinks= ?, Date= ?, time_of_Day=? where id= ?";
        try {
            int id = Integer.parseInt(RecordTable.getValueAt(SelectedRows, 0).toString());
            //PreparedStatement object that takes 6 input parameters
            pst = connection.prepareStatement(sql);
            pst.setString(1, txtFoodName.getText());
            pst.setString(2, (String) comboFoodGroup.getSelectedItem());
            pst.setString(3, txtDrinks.getText());
            pst.setString(4, txtDate.getText());
            pst.setString(5, (String) comboDay.getSelectedItem());
            pst.setInt(6, id);
            //Executing the query
            pst.execute();
            JOptionPane.showMessageDialog(rootPane, "Updated.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(rootPane, e);
        }
        DisplayUpdatedMealInfoTbl();
    }
    
    //to delete selected row meal diary from database then display in table
    @Override
    public void deleteDiary(){
        DefaultTableModel RecordTable = (DefaultTableModel) tblMealInfo.getModel();
        int SelectedRows = tblMealInfo.getSelectedRow();
        int p = JOptionPane.showConfirmDialog(rootPane, "Do you really wan to delete?", "Delete", JOptionPane.YES_NO_OPTION);
        if (p == 0) {
            //Query to delete records
            String sql = "delete from meal_info where id= ?";
            try {
                int id = Integer.parseInt(RecordTable.getValueAt(SelectedRows, 0).toString());
                //PreparedStatement object that takes id input parameters
                pst = connection.prepareStatement(sql);
                pst.setInt(1, id);
                pst.executeUpdate();

                JOptionPane.showMessageDialog(rootPane, "Deleted!");
                clearInfo();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(rootPane, e);
            }
            DisplayUpdatedMealInfoTbl();
        }
    }
    
    //to display meal diary from database and filter or order by food group.
    @Override
    public void filterByFoodGroup(){
        try {
            //Query to retrive records
            String sql = "select ID, Food_name, Food_group, Drinks, Date, time_of_Day from Meal_info order by Food_group";
            //PreparedStatement object that takes input parameters
            pst = connection.prepareStatement(sql);
            //Executing the query
            resultSet = pst.executeQuery();
            //from import net.proteanit.sql.DbUtils; to display executed the query
            tblMealInfo.setModel(DbUtils.resultSetToTableModel(resultSet));
        } catch (SQLException ex) {
            Logger.getLogger(MealDiaryInfo.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    //to update selected row meal photo into database then display in JLabel, foodImg .
    @Override
    public void imageSave(){
        DefaultTableModel RecordTable = (DefaultTableModel) tblMealInfo.getModel();
        int SelectedRows = tblMealInfo.getSelectedRow();
        //Query to update records
        String sql = "update Meal_Info set Photo = ? where id= ?";
        try {
            int id = Integer.parseInt(RecordTable.getValueAt(SelectedRows, 0).toString());
            //PreparedStatement object that takes Photo and id input parameters
            pst = connection.prepareStatement(sql);
            pst.setBytes(1, foodImg);
            pst.setInt(2, id);
            //Executing the query
            pst.executeUpdate();
            JOptionPane.showMessageDialog(rootPane, "Image saved.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(rootPane, e);
        }
    }
    
    //convert buffered image to byte array
    @Override
    public void imageUpload(){
        JFileChooser chooser = new JFileChooser();
        chooser.showOpenDialog(null);

        File f = chooser.getSelectedFile();
        String fileName = f.getAbsolutePath();
        txtImageUpld.setText(fileName);

        try {
            FileInputStream fIS = new FileInputStream(f);
            ByteArrayOutputStream bAOS = new ByteArrayOutputStream();
            byte[] buf = new byte[1024];
            for (int readNum; (readNum = fIS.read(buf)) != -1;) {
                bAOS.write(buf, 0, readNum);
            }
            foodImg = bAOS.toByteArray();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(rootPane, e);
        }
    }
    
    //convert byte array back to buffered image
    @Override
    public Image scaledImage(byte[] img, int w, int h) {
        BufferedImage resizedImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        try {
            Graphics2D g2 = resizedImage.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

            //convert byte array back to buffered image
            ByteArrayInputStream in = new ByteArrayInputStream(img);
            BufferedImage bImageFromConvert = ImageIO.read(in);

            g2.drawImage(bImageFromConvert, 0, 0, w, h, null);
            g2.dispose();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(rootPane, e);
        }
        return resizedImage;
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        btnSignOut = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblMealInfo = new javax.swing.JTable();
        PanelMealInfo = new javax.swing.JPanel();
        lblFoodName = new javax.swing.JLabel();
        txtFoodName = new javax.swing.JTextField();
        lblFoodGroup = new javax.swing.JLabel();
        lblDrinks = new javax.swing.JLabel();
        txtDrinks = new javax.swing.JTextField();
        lblDate = new javax.swing.JLabel();
        txtDate = new javax.swing.JTextField();
        lblTimeOfTheDay = new javax.swing.JLabel();
        comboFoodGroup = new javax.swing.JComboBox<>();
        comboDay = new javax.swing.JComboBox<>();
        PanelImage = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        labelImage = new javax.swing.JLabel();
        btnImgSave = new javax.swing.JButton();
        btnImgUpload = new javax.swing.JButton();
        txtImageUpld = new javax.swing.JTextField();
        btnClear = new javax.swing.JButton();
        btnUpdate = new javax.swing.JButton();
        btnDelete = new javax.swing.JButton();
        btnAdd = new javax.swing.JButton();
        PanelWelcom = new javax.swing.JPanel();
        lblWelcome4 = new javax.swing.JLabel();
        txtSearch = new javax.swing.JTextField();
        lblWelcome1 = new javax.swing.JLabel();
        lblWelcome2 = new javax.swing.JLabel();
        lblWelcome3 = new javax.swing.JLabel();
        lblLogo = new javax.swing.JLabel();
        btnFilter = new javax.swing.JButton();
        txtSearch2 = new javax.swing.JTextField();
        btnShowAll = new javax.swing.JButton();
        jMenuBar1 = new javax.swing.JMenuBar();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        btnSignOut.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        btnSignOut.setText("Sign Out");
        btnSignOut.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSignOutActionPerformed(evt);
            }
        });

        tblMealInfo.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        tblMealInfo.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        tblMealInfo.setRowHeight(25);
        tblMealInfo.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblMealInfoMouseClicked(evt);
            }
        });
        tblMealInfo.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                tblMealInfoKeyReleased(evt);
            }
        });
        jScrollPane1.setViewportView(tblMealInfo);

        PanelMealInfo.setBackground(new java.awt.Color(0, 255, 255));
        PanelMealInfo.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), "Meal info", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 18), new java.awt.Color(0, 0, 153))); // NOI18N

        lblFoodName.setFont(new java.awt.Font("Tahoma", 1, 20)); // NOI18N
        lblFoodName.setText("Food Name");

        txtFoodName.setFont(new java.awt.Font("Tahoma", 1, 20)); // NOI18N

        lblFoodGroup.setFont(new java.awt.Font("Tahoma", 1, 20)); // NOI18N
        lblFoodGroup.setText("Food Group");

        lblDrinks.setFont(new java.awt.Font("Tahoma", 1, 20)); // NOI18N
        lblDrinks.setText("Drinks");

        txtDrinks.setFont(new java.awt.Font("Tahoma", 1, 20)); // NOI18N

        lblDate.setFont(new java.awt.Font("Tahoma", 1, 20)); // NOI18N
        lblDate.setText("Date");

        txtDate.setFont(new java.awt.Font("Tahoma", 1, 20)); // NOI18N

        lblTimeOfTheDay.setFont(new java.awt.Font("Tahoma", 1, 20)); // NOI18N
        lblTimeOfTheDay.setText("Time of the Day");

        comboFoodGroup.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        comboFoodGroup.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Fruits", "Vegetables", "Grains", "Protein Foods", "Dairy" }));

        comboDay.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        comboDay.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Breakfast", "Lunch", "Dinner" }));

        javax.swing.GroupLayout PanelMealInfoLayout = new javax.swing.GroupLayout(PanelMealInfo);
        PanelMealInfo.setLayout(PanelMealInfoLayout);
        PanelMealInfoLayout.setHorizontalGroup(
            PanelMealInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(PanelMealInfoLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(PanelMealInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(PanelMealInfoLayout.createSequentialGroup()
                        .addGroup(PanelMealInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblDate)
                            .addComponent(lblDrinks)
                            .addComponent(lblFoodGroup)
                            .addComponent(lblFoodName))
                        .addGap(60, 60, 60))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, PanelMealInfoLayout.createSequentialGroup()
                        .addComponent(lblTimeOfTheDay)
                        .addGap(18, 18, 18)))
                .addGroup(PanelMealInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(txtDate, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtDrinks, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(comboFoodGroup, javax.swing.GroupLayout.Alignment.LEADING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(txtFoodName, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(comboDay, javax.swing.GroupLayout.PREFERRED_SIZE, 350, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(33, Short.MAX_VALUE))
        );
        PanelMealInfoLayout.setVerticalGroup(
            PanelMealInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(PanelMealInfoLayout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addGroup(PanelMealInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblFoodName)
                    .addComponent(txtFoodName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(PanelMealInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(comboFoodGroup, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblFoodGroup))
                .addGap(18, 18, 18)
                .addGroup(PanelMealInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblDrinks)
                    .addComponent(txtDrinks, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(PanelMealInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblDate)
                    .addComponent(txtDate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(PanelMealInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(comboDay, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblTimeOfTheDay))
                .addContainerGap(100, Short.MAX_VALUE))
        );

        PanelImage.setBackground(new java.awt.Color(0, 255, 255));
        PanelImage.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(labelImage, javax.swing.GroupLayout.PREFERRED_SIZE, 162, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(labelImage, javax.swing.GroupLayout.PREFERRED_SIZE, 218, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        btnImgSave.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        btnImgSave.setText("Save");
        btnImgSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnImgSaveActionPerformed(evt);
            }
        });

        btnImgUpload.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        btnImgUpload.setText("Upload");
        btnImgUpload.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnImgUploadActionPerformed(evt);
            }
        });

        txtImageUpld.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N

        javax.swing.GroupLayout PanelImageLayout = new javax.swing.GroupLayout(PanelImage);
        PanelImage.setLayout(PanelImageLayout);
        PanelImageLayout.setHorizontalGroup(
            PanelImageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(PanelImageLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(PanelImageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(PanelImageLayout.createSequentialGroup()
                        .addComponent(txtImageUpld, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnImgUpload)))
                .addContainerGap())
            .addGroup(PanelImageLayout.createSequentialGroup()
                .addGap(61, 61, 61)
                .addComponent(btnImgSave)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        PanelImageLayout.setVerticalGroup(
            PanelImageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(PanelImageLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(28, 28, 28)
                .addGroup(PanelImageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtImageUpld, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnImgUpload))
                .addGap(18, 18, 18)
                .addComponent(btnImgSave)
                .addContainerGap(20, Short.MAX_VALUE))
        );

        btnClear.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        btnClear.setText("Clear");
        btnClear.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnClearActionPerformed(evt);
            }
        });

        btnUpdate.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        btnUpdate.setText("Update");
        btnUpdate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUpdateActionPerformed(evt);
            }
        });

        btnDelete.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        btnDelete.setText("Delete");
        btnDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDeleteActionPerformed(evt);
            }
        });

        btnAdd.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        btnAdd.setText("Add");
        btnAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddActionPerformed(evt);
            }
        });

        PanelWelcom.setBackground(new java.awt.Color(0, 255, 255));
        PanelWelcom.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        lblWelcome4.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        lblWelcome4.setForeground(new java.awt.Color(0, 51, 153));
        lblWelcome4.setText("Software System");

        txtSearch.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        txtSearch.setForeground(new java.awt.Color(102, 102, 102));
        txtSearch.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtSearch.setText("Search...");
        txtSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtSearchActionPerformed(evt);
            }
        });
        txtSearch.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtSearchKeyReleased(evt);
            }
        });

        lblWelcome1.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        lblWelcome1.setForeground(new java.awt.Color(0, 51, 153));
        lblWelcome1.setText("Welcome to ");

        lblWelcome2.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        lblWelcome2.setForeground(new java.awt.Color(0, 51, 153));
        lblWelcome2.setText("Potatoes Group");

        lblWelcome3.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        lblWelcome3.setForeground(new java.awt.Color(0, 51, 153));
        lblWelcome3.setText("Meal Diary");

        lblLogo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/image/potato (2).png"))); // NOI18N

        btnFilter.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        btnFilter.setText("Filter by Food Group");
        btnFilter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnFilterActionPerformed(evt);
            }
        });

        txtSearch2.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        txtSearch2.setForeground(new java.awt.Color(102, 102, 102));
        txtSearch2.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtSearch2.setText("Search with second variable");
        txtSearch2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtSearch2ActionPerformed(evt);
            }
        });
        txtSearch2.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtSearch2KeyReleased(evt);
            }
        });

        btnShowAll.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        btnShowAll.setText("Show all");
        btnShowAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnShowAllActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout PanelWelcomLayout = new javax.swing.GroupLayout(PanelWelcom);
        PanelWelcom.setLayout(PanelWelcomLayout);
        PanelWelcomLayout.setHorizontalGroup(
            PanelWelcomLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, PanelWelcomLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(PanelWelcomLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(PanelWelcomLayout.createSequentialGroup()
                        .addGap(20, 20, 20)
                        .addComponent(lblWelcome1))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, PanelWelcomLayout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(lblLogo)
                        .addGap(52, 52, 52))
                    .addGroup(PanelWelcomLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(lblWelcome4)
                        .addGroup(PanelWelcomLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblWelcome2, javax.swing.GroupLayout.PREFERRED_SIZE, 154, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(PanelWelcomLayout.createSequentialGroup()
                                .addGap(23, 23, 23)
                                .addComponent(lblWelcome3)))))
                .addGap(67, 67, 67))
            .addGroup(PanelWelcomLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(PanelWelcomLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(txtSearch, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(btnFilter, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(txtSearch2)
                    .addComponent(btnShowAll, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(13, Short.MAX_VALUE))
        );
        PanelWelcomLayout.setVerticalGroup(
            PanelWelcomLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(PanelWelcomLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblWelcome1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblWelcome2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblWelcome3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblWelcome4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblLogo)
                .addGap(7, 7, 7)
                .addComponent(txtSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(txtSearch2, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 11, Short.MAX_VALUE)
                .addComponent(btnShowAll)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnFilter)
                .addContainerGap())
        );

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(PanelImage, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(PanelMealInfo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane1))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 85, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(btnSignOut, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnUpdate, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnAdd, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnDelete, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnClear, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(76, 76, 76))
                    .addGroup(layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(PanelWelcom, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(20, Short.MAX_VALUE))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(PanelMealInfo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(PanelImage, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(PanelWelcom, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btnAdd)
                        .addGap(28, 28, 28)
                        .addComponent(btnDelete)
                        .addGap(28, 28, 28)
                        .addComponent(btnUpdate)
                        .addGap(26, 26, 26)
                        .addComponent(btnClear)
                        .addGap(31, 31, 31)
                        .addComponent(btnSignOut))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnSignOutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSignOutActionPerformed
        // TODO add your handling code here:
        signOut();
    }//GEN-LAST:event_btnSignOutActionPerformed

    private void tblMealInfoMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblMealInfoMouseClicked
        // TODO add your handling code here:
        displayMealInfo();
    }//GEN-LAST:event_tblMealInfoMouseClicked

    private void tblMealInfoKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tblMealInfoKeyReleased
        // TODO add your handling code here:
        if (evt.getKeyCode() == KeyEvent.VK_UP || evt.getKeyCode() == KeyEvent.VK_DOWN) {
            try {
                int row = tblMealInfo.getSelectedRow();
                String tableClick = (tblMealInfo.getModel().getValueAt(row, 0).toString());
                String sql = "select * from Meal_Info where ID = '" + tableClick + "'";
                pst = connection.prepareStatement(sql);
                resultSet = pst.executeQuery();
                if (resultSet.isBeforeFirst()) {
                    getValue();
                } else {
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(rootPane, e);
            }
        }
    }//GEN-LAST:event_tblMealInfoKeyReleased

    private void txtSearchKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtSearchKeyReleased
        // TODO add your handling code here:
        searchByFoodName();
        searchByFoodGroup();
        searchByTimeOfTheDay();
        searchByID();
        searchByDrinks();
        searchByDate();
    }//GEN-LAST:event_txtSearchKeyReleased

    private void btnClearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnClearActionPerformed
        // TODO add your handling code here:
        clearInfo();
    }//GEN-LAST:event_btnClearActionPerformed

    private void btnAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddActionPerformed
        // TODO add your handling code here:
        addDiary();
        clearInfo();
    }//GEN-LAST:event_btnAddActionPerformed

    private void btnUpdateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUpdateActionPerformed
        // TODO add your handling code here:
        updateDiary();
    }//GEN-LAST:event_btnUpdateActionPerformed

    private void btnDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteActionPerformed
        // TODO add your handling code here:
        deleteDiary();
    }//GEN-LAST:event_btnDeleteActionPerformed

    private void txtSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtSearchActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtSearchActionPerformed

    private void btnShowAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnShowAllActionPerformed
        // TODO add your handling code here:
        DisplayUpdatedMealInfoTbl();
    }//GEN-LAST:event_btnShowAllActionPerformed

    private void btnFilterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnFilterActionPerformed
        filterByFoodGroup();
    }//GEN-LAST:event_btnFilterActionPerformed

    private void btnImgSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnImgSaveActionPerformed
        // TODO add your handling code here:
        imageSave();
        displayMealInfo();
    }//GEN-LAST:event_btnImgSaveActionPerformed

    private void btnImgUploadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnImgUploadActionPerformed
        // TODO add your handling code here:
        //convert buffered image to byte array
        imageUpload();
        
    }//GEN-LAST:event_btnImgUploadActionPerformed

    private void txtSearch2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtSearch2ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtSearch2ActionPerformed

    private void txtSearch2KeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtSearch2KeyReleased
        // TODO add your handling code here:
        searchFilterByEverything();
    }//GEN-LAST:event_txtSearch2KeyReleased

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel PanelImage;
    private javax.swing.JPanel PanelMealInfo;
    private javax.swing.JPanel PanelWelcom;
    private javax.swing.JButton btnAdd;
    private javax.swing.JButton btnClear;
    private javax.swing.JButton btnDelete;
    private javax.swing.JButton btnFilter;
    private javax.swing.JButton btnImgSave;
    private javax.swing.JButton btnImgUpload;
    private javax.swing.JButton btnShowAll;
    private javax.swing.JButton btnSignOut;
    private javax.swing.JButton btnUpdate;
    private javax.swing.JComboBox<String> comboDay;
    private javax.swing.JComboBox<String> comboFoodGroup;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel labelImage;
    private javax.swing.JLabel lblDate;
    private javax.swing.JLabel lblDrinks;
    private javax.swing.JLabel lblFoodGroup;
    private javax.swing.JLabel lblFoodName;
    private javax.swing.JLabel lblLogo;
    private javax.swing.JLabel lblTimeOfTheDay;
    private javax.swing.JLabel lblWelcome1;
    private javax.swing.JLabel lblWelcome2;
    private javax.swing.JLabel lblWelcome3;
    private javax.swing.JLabel lblWelcome4;
    private javax.swing.JTable tblMealInfo;
    private javax.swing.JTextField txtDate;
    private javax.swing.JTextField txtDrinks;
    private javax.swing.JTextField txtFoodName;
    private javax.swing.JTextField txtImageUpld;
    private javax.swing.JTextField txtSearch;
    private javax.swing.JTextField txtSearch2;
    // End of variables declaration//GEN-END:variables
}

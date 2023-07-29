import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.event.WindowEvent;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class LogIn extends javax.swing.JFrame implements LogInInterface{

    //java.sql variables
    private Connection connection= null;
    private PreparedStatement pst= null;
    private ResultSet resultSet= null;
    
    //constructor
    public LogIn() {
        initComponents();
        init();
        connection= dbConnect(); 
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
    public void init(){
        //re-center windows
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        //disable resize windows
        setResizable(false);
    }
    
    //to close previous jframe on the opening of new jframe
    @Override
    public void close(){
        WindowEvent winClosingEvnt = new WindowEvent (this, WindowEvent.WINDOW_CLOSING);
        Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(winClosingEvnt);
    }
    
    //to exit program
    @Override
    public void exit(){
        System.exit(0);
    }
    
    //validate username and password
    @Override
    public void btnLogIn(){
        //Query to retrieve records
        String sql = "select * from Log_In where user_name=? and password=?";
        try{
            //PreparedStatement object that takes user_name and password input parameters
            pst= connection.prepareStatement(sql);
            pst.setString(1, txtuserName.getText());
            pst.setString(2, txtPass.getText());
            
            //Executing the query
            resultSet= pst.executeQuery();
            
            //This method returns a boolean value.
            //If there are no rows next to its current position this method returns false, else it returns true.
            //if username and password exist in preparedStatement object that takes user_name and password input parameters
            if(resultSet.next()){       
                //to close previous jframe on the opening of new jframe
                close();
                
                MealDiaryInfo obj = new MealDiaryInfo();
                obj.setVisible(true);
                try{
                    //close sqlite database connection
                    resultSet.close();
                    pst.close();
                    connection.close();
                }
                catch(Exception e){
                    JOptionPane.showMessageDialog(rootPane, e);
                }
            }else{                      //if input username and password doesnt exist in preparedStatement object that takes user_name and password input parameters
                JOptionPane.showMessageDialog(rootPane, "Username and Password is incorrect");
            }
        }
        catch(SQLException | HeadlessException e){
            JOptionPane.showMessageDialog(null, e);
        }
    }
 
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        PanelLogin = new javax.swing.JPanel();
        lblUserName = new javax.swing.JLabel();
        txtuserName = new javax.swing.JTextField();
        lblPass = new javax.swing.JLabel();
        txtPass = new javax.swing.JPasswordField();
        btnLogOn = new javax.swing.JButton();
        btnExit = new javax.swing.JButton();
        lblLogo = new javax.swing.JLabel();
        lblTitle = new javax.swing.JLabel();
        lblGroup = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        PanelLogin.setBackground(new java.awt.Color(0, 255, 255));
        PanelLogin.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "LogIn", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 16), new java.awt.Color(51, 51, 255))); // NOI18N
        PanelLogin.setToolTipText("");

        lblUserName.setFont(new java.awt.Font("Tahoma", 1, 16)); // NOI18N
        lblUserName.setText("UserName");

        txtuserName.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtuserNameActionPerformed(evt);
            }
        });

        lblPass.setFont(new java.awt.Font("Tahoma", 1, 16)); // NOI18N
        lblPass.setText("Password");

        btnLogOn.setFont(new java.awt.Font("Tahoma", 1, 16)); // NOI18N
        btnLogOn.setText("Enter");
        btnLogOn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLogOnActionPerformed(evt);
            }
        });

        btnExit.setFont(new java.awt.Font("Tahoma", 1, 16)); // NOI18N
        btnExit.setText("Exit");
        btnExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnExitActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout PanelLoginLayout = new javax.swing.GroupLayout(PanelLogin);
        PanelLogin.setLayout(PanelLoginLayout);
        PanelLoginLayout.setHorizontalGroup(
            PanelLoginLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(PanelLoginLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(PanelLoginLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(PanelLoginLayout.createSequentialGroup()
                        .addComponent(lblPass)
                        .addGroup(PanelLoginLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(PanelLoginLayout.createSequentialGroup()
                                .addGap(35, 35, 35)
                                .addComponent(txtPass, javax.swing.GroupLayout.PREFERRED_SIZE, 145, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(PanelLoginLayout.createSequentialGroup()
                                .addGap(93, 93, 93)
                                .addGroup(PanelLoginLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(btnExit, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(btnLogOn, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(PanelLoginLayout.createSequentialGroup()
                        .addComponent(lblUserName)
                        .addGap(29, 29, 29)
                        .addComponent(txtuserName)))
                .addContainerGap())
        );
        PanelLoginLayout.setVerticalGroup(
            PanelLoginLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(PanelLoginLayout.createSequentialGroup()
                .addGap(68, 68, 68)
                .addGroup(PanelLoginLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblUserName)
                    .addComponent(txtuserName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(27, 27, 27)
                .addGroup(PanelLoginLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblPass)
                    .addComponent(txtPass, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(34, 34, 34)
                .addComponent(btnLogOn)
                .addGap(18, 18, 18)
                .addComponent(btnExit)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        lblLogo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/image/potato (3).png"))); // NOI18N

        lblTitle.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        lblTitle.setText("MEAL DIARY");

        lblGroup.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        lblGroup.setText("Group of Potatoes");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(lblGroup))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(41, 41, 41)
                                .addComponent(lblTitle)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addGap(62, 62, 62)
                        .addComponent(lblLogo)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                .addComponent(PanelLogin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(PanelLogin, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addGap(47, 47, 47)
                .addComponent(lblTitle)
                .addGap(18, 18, 18)
                .addComponent(lblLogo)
                .addGap(18, 18, 18)
                .addComponent(lblGroup)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void txtuserNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtuserNameActionPerformed

    }//GEN-LAST:event_txtuserNameActionPerformed

    private void btnLogOnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLogOnActionPerformed
        btnLogIn();
    }//GEN-LAST:event_btnLogOnActionPerformed

    private void btnExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExitActionPerformed
        exit();
    }//GEN-LAST:event_btnExitActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel PanelLogin;
    private javax.swing.JButton btnExit;
    private javax.swing.JButton btnLogOn;
    private javax.swing.JLabel lblGroup;
    private javax.swing.JLabel lblLogo;
    private javax.swing.JLabel lblPass;
    private javax.swing.JLabel lblTitle;
    private javax.swing.JLabel lblUserName;
    private javax.swing.JPasswordField txtPass;
    private javax.swing.JTextField txtuserName;
    // End of variables declaration//GEN-END:variables
}

package com.mobilepulse.gestioncine.connection;

import android.util.Log;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionSQL {
    protected static String db = "cine_prueba";
    protected static String ip = "10.0.2.2";
    protected static String port = "3306";
    protected static String user = "root";
    protected static String pass = "usuario";

    public Connection newConnection() {
        Connection connection = null;

        try{
            Class.forName("com.mysql.jdbc.Driver");
            String connectionURL = "jdbc:mysql://" + ip + ":" + port + "/" + db;
            connection = DriverManager.getConnection(connectionURL, user, pass);

        } catch (ClassNotFoundException e) {
            Log.e("ConnectionSQL", "ClassNotFoundException: " + e.getMessage());
        } catch (SQLException e) {
            Log.e("ConnectionSQL", "SQLException: " + e.getMessage());
        } catch (Exception e) {
            Log.e("ConnectionSQL", "Exception: " + e.getMessage());
        }

        return connection;
    }


}

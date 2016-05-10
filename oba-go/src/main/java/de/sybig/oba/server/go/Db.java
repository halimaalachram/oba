/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.sybig.oba.server.go;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Vector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Halima
 */
public class Db {

    private static Connection connection;
    private static Logger log = LoggerFactory.getLogger(Db.class);

    public static void connect(String urlDB, String driver) throws SQLException {
        try {
            System.out.println("Loading driver...");
            Class.forName(driver).newInstance();
            System.out.println("Driver loaded!");
        } catch (ClassNotFoundException e) {
            log.error("Cannot find the driver in the classpath!");
            e.printStackTrace();

        } catch (InstantiationException e) {
            log.error("InstantiationException");
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            log.error("IllegalAccessException");
            e.printStackTrace();
        }
        System.out.println("Connecting to database...");
        try {
            connection = DriverManager.getConnection(urlDB);
        } catch (SQLException e) {
            log.error("Failed to connect to Database!!");
            e.printStackTrace();
        }
        System.out.println("Database connected!");
    }

    public static ResultSet doQuery(String query) throws SQLException {
        Statement statement = connection.createStatement();
        ResultSet result = statement.executeQuery(query);
        return result;
    }

    public static Vector ResultSetToTable(ResultSet res) throws SQLException {
        Vector result;
        ResultSetMetaData rsmd = res.getMetaData();
        int columnsNumber = rsmd.getColumnCount();
        result = new Vector<Vector<String>>();
        while (res.next()) {
            Vector<String> line = new Vector<String>();
            for (int i = 1; i <= columnsNumber; i++) {
                line.add(res.getString(i));
            }
            result.add(line);
        }
        return result;
    }

    public static Vector<String> fetchRow(Vector<Vector<String>> vector, int rowNum) {
        Vector<String> row = vector.elementAt(rowNum);
        return row;
    }
  public static String findSpecieFromID(String ensID)
    {
        if(ensID.startsWith("ENSG"))
            return "hs";
        if(ensID.startsWith("ENSMUSG"))
            return "mm";
        if(ensID.startsWith("ENSRNOG"))
            return "rn";
        return null;
    }
    public static boolean isValidEnsID(String ensID, String sp) throws SQLException {
        boolean valid = false;
        ResultSet result = doQuery("SELECT * from `" + sp + "` where col_2='" + ensID + "';");
        Vector<Vector<String>> rows = ResultSetToTable(result);
        if (rows.size() != 0) {
            valid = true;
        }
        return valid;
    }

    public static Vector getGOID(String ensID, String sp) throws SQLException {
        Vector list_temp = new Vector<Vector<String>>();
        Vector list = new Vector<String>();
        String query = "SELECT * from `" + sp + "` where col_2='" + ensID + "';";
        list_temp = ResultSetToTable(doQuery(query));
        for (int i = 0; i < list_temp.size(); i++) {
            list.add(fetchRow(list_temp, i).elementAt(2));
        }

        return list;
    }

    public static void close() throws SQLException {
        connection.close();
    }

    //getters and setters
    public static Connection getConnection() {
        return connection;
    }

    public static void setConnection(Connection connection) {
        Db.connection = connection;
    }
}

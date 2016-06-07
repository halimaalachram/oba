package de.sybig.oba.server.go;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Vector;
import javax.ws.rs.WebApplicationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Halima
 */
class Db {

    private Connection connection;
    private Logger log = LoggerFactory.getLogger(Db.class);

    /**
     * A class providing connection for OBA database . A database that includes
     * the mappings of ENSEMBL gene identifiers to gene ontology identifiers as
     * three tables for three different species: human(hs), mouse(mm) and rat
     * rn). Each table includes two tables: one for ENSEMBL IDs(ENS_ID) and one
     * for GO IDs(GO_ID).
     *
     */
    public void connect(String urlDB, String driver) throws SQLException {
        try {
            log.info("Loading driver...");
            Class.forName(driver).newInstance();
            log.info("Driver loaded!");

        } catch (ClassNotFoundException e) {
            log.error("Cannot find the driver in the classpath!");


        } catch (InstantiationException e) {
            log.error("InstantiationException");

        } catch (IllegalAccessException e) {
            log.error("IllegalAccessException");

        }
        log.info("Connecting to database...");
        try {
            connection = DriverManager.getConnection(urlDB);
        } catch (SQLException e) {
            log.error("Failed to connect to Database!!");

        }
        log.info("Database connected!");
    }

    private ResultSet doQuery(String query) throws SQLException {
        Statement statement = connection.createStatement();
        ResultSet result = statement.executeQuery(query);
        return result;
    }

    private Vector resultSetToTable(ResultSet res) throws SQLException {
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

    private Vector<String> fetchRow(Vector<Vector<String>> vector, int rowNum) {
        Vector<String> row = vector.elementAt(rowNum);
        return row;
    }

    String findSpecieFromID(String ensID) {
        if (ensID.startsWith("ENSG")) {
            return "hs";
        }
        if (ensID.startsWith("ENSMUSG")) {
            return "mm";
        }
        if (ensID.startsWith("ENSRNOG")) {
            return "rn";
        }
        return null;
    }
    /*a function to detect if a particular ENSEMBL ID for a particular specie exists 
     * in the database.
     */

    private boolean isValidEnsID(String ensID, String sp) throws SQLException {
        boolean valid = false;
        ResultSet result = doQuery("SELECT * from `" + sp + "` where ENS_ID='" + ensID + "';");
        Vector<Vector<String>> rows = resultSetToTable(result);
        if (rows.size() != 0) {
            valid = true;
        }
        return valid;
    }
    /*A function to get the GO ID of a particular ENSEMBL ID for a particular specie.
     * Each table represents the mappings as two columns, the first one for Esembl IDs 
     * and the second for GO IDs.
     * 
     */

    Vector getGOID(String ensID, String sp) throws SQLException {
        Vector list_temp = new Vector<Vector<String>>();
        Vector list = new Vector<String>();
        String query = "SELECT * from `" + sp + "` where ENS_ID='" + ensID + "';";
        list_temp = resultSetToTable(doQuery(query));
        for (int i = 0; i < list_temp.size(); i++) {
            list.add(fetchRow(list_temp, i).elementAt(2));
        }

        return list;
    }
    /*A function to get the list of GO IDs of a particular ENSEMBL ID.
     * 
     */

    Vector retrieveGeneByID(String geneEnsID) throws SQLException {
        Vector<String> list = new Vector<String>();

        String sp = findSpecieFromID(geneEnsID);
        if (sp == null) {
            log.error("INVALID SPECIES");
            throw new WebApplicationException(404);

        }


        if (!isValidEnsID(geneEnsID, sp)) {
            throw new WebApplicationException(404);
            //return null;
        } else {
            list = getGOID(geneEnsID, sp);
        }
        close();
        return list;

    }

    public void close() throws SQLException {
        connection.close();
    }

    //getters and setters
    public Connection getConnection() {
        return connection;
    }

}

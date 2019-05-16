/*
 * Copyright 2019 DZB Leipzig
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dzb.util;

import java.sql.*;

import org.jboss.logging.Logger;

import java.util.*;

public class MSSQLDatabaseAdapter {


    private Connection connection;
    private Statement statement;
    private Properties properties;

    private static final Logger logger = Logger.getLogger(MSSQLDatabaseAdapter.class);

    MSSQLDatabaseAdapter(Properties p) {

        this.properties = p;

        try {

            // The parent project where this module will be used is requiring Java 8 or higher but
            // the project needs a connection to a customer database which is a ms sql server 2005.
            // To connect this database it will be normally the microsoft JDBC driver 4.1 using!
            // The culprit is the driver only supports Java 7. -> https://docs.microsoft.com/de-de/sql/connect/jdbc/
            // release-notes-for-the-jdbc-driver?view=sql-server-2017#support-for-jdk-7.
            //
            // So at the moment the only way to connect ms sql server 2005 via Java 8 is the open source jtds driver.
            // -> http://jtds.sourceforge.net
            Class.forName("net.sourceforge.jtds.jdbc.Driver");

//            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }


    public MSSQLDatabaseAdapter getConnection() throws Exception {
        String connectionUrl =
                "jdbc:jtds:sqlserver://" + this.properties.getProperty("serverName") + ":1433;"
                        + "database=" + this.properties.getProperty("db") + ";"
                        + "user=" + this.properties.getProperty("user") + ";"
                        + "password=" + this.properties.getProperty("password") + ";";
//                        + "loginTimeout=30;"

        logger.info("Connecting to database...");
        connection = DriverManager.getConnection(connectionUrl);
        logger.info("Successful connected");

        return this;
    }


    public int findByUsername(String username) throws Exception {

        return getCount(getUserByUsername(username));
    }

    public ResultSet getUserByUsername(String username) throws Exception {
        return execQuery("SELECT Pers_Nachname From tblPerson WHERE Pers_Nachname =' + username + '");
    }


    public void addUsers(Properties p) throws Exception {

//        String query = "IF EXISTS ( SELECT * FROM " + p.getProperty("table") + " WITH (UPDLOCK) WHERE Pers_Ident_EMail='"
//                + p.getProperty("username") + "') UPDATE " + p.getProperty("table") + "  " +
//                "SET Pers_Nachname = '" + p.getProperty("lastname") + "', Pers_Vorname ='" +
//                "" + p.getProperty("firstname") + "', Pers_Ident_PW= '" + p.getProperty("password") + "' WHERE Pers_Ident_EMail='"
//                + p.getProperty("username") + "'; " +
//                "ELSE INSERT " + p.getProperty("table") + " ( Pers_Ident_EMail, Pers_Nachname, Pers_Vorname, Pers_Ident_PW ) " +
//                "VALUES ( " + p.getProperty("username") + ", " + p.getProperty("lastname") + " , " +
//                "" + p.getProperty("firstname") + ", " + p.getProperty("password") + "); ";

        String query = "IF NOT EXISTS ( SELECT * FROM " + p.getProperty("table") + " WITH (UPDLOCK) WHERE Pers_Ident_EMail='"
                + p.getProperty("Pers_Ident_EMail") + "')" +
                "BEGIN INSERT " + p.getProperty("table") + " (";

        Set<String> keys = p.stringPropertyNames();
        keys.remove("table");

        query +=  String.join(",", keys);
        query += ") VALUES ( ";

        List<String> values = new ArrayList<>();
        for (String key : keys)
            values.add("'"+ p.getProperty(key) + "'");

        query +=  String.join(",", values);
        query += "); END";

        System.out.println(query);
        statement = connection.createStatement();
        statement.executeUpdate(query);
        statement.close();
    }

    private ResultSet execQuery(String query) throws Exception {

        statement = connection.createStatement();
//            String selectSql = "SELECT session_id, encrypt_option FROM sys.dm_exec_connections";
        return statement.executeQuery(query);

    }

    public int getUserCount() throws Exception {
        return getCount(execQuery("SELECT * FROM tblPerson"));
    }


    private int getCount(ResultSet r) throws SQLException {
        return r.last() ? r.getRow() : 0;
    }

//    public void outputResultSet() throws Exception {
//
//        ResultSetMetaData rsmd = rs.getMetaData();
//        int columnsNumber = rsmd.getColumnCount();
//
//        while (rs.next()) {
//
//            for (int i = 1; i <= columnsNumber; i++) {
//
//                if (i > 1) System.out.print(",  ");
//                String columnValue = rs.getString(i);
//                System.out.print(rsmd.getColumnName(i) + ": " + columnValue);
//            }
//            System.out.println("");
//        }
//    }
}

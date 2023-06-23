package io.github.realguystuff.ChattServer;
import java.lang.reflect.InvocationTargetException;
import java.sql.*;
public class Database {
    public ResultSet call(String request) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException, SQLException {
        Class.forName("com.mysql.cj.jdbc.Driver").getConstructor().newInstance();
        Connection conn = DriverManager.getConnection("jdbc:mysql://db1.falix.cc:3306/928237_025747233ae1", "u928237_oqIDJMfPV5", "OD9rtdqw70Aa7s2uLG+PQWkh");
        Statement stmt = conn.createStatement();
        ResultSet result = stmt.executeQuery(request);
        conn.close();
        return result;
    }
}

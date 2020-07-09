package common.utils;


import lombok.Data;
import pojo.TestJDBC;

import java.sql.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcUtils {
    private static Connection con;
    private static Statement statement ;

    private static final Logger logger = LoggerFactory.getLogger(JdbcUtils.class);

    private static void setConnection(TestJDBC jdbc) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            logger.debug("数据库驱动加载成功");
        } catch(ClassNotFoundException e){
            logger.error("数据库驱动加载失败");
            e.printStackTrace();
        }
        try {
            con = DriverManager.getConnection(jdbc.getUrl(),jdbc.getUsername(),jdbc.getPassword());
            if(!con.isClosed()) {
                logger.debug("数据库连接成功");
            }
        } catch (SQLException e) {
            logger.error("数据库连接失败");
            e.printStackTrace();
        }
    }

    private static Statement setStatement(TestJDBC jdbc){
        try {
            if(con==null||con.isClosed()){
                setConnection(jdbc);
            }
            statement = con.createStatement();
            logger.debug("数据库statement对象获取成功");
        }catch (SQLException e){
            logger.error("数据库statement对象获取失败");
            e.printStackTrace();
        }
        return statement;
    }

    public static ResultSet getResult(String sql,TestJDBC jdbc){
        ResultSet resultSet =null;
        try {
            //ResultSet类，用来存放获取的结果集！！
            if(statement==null||statement.isClosed()){
                setStatement(jdbc);
            }
            statement.execute(sql);

            logger.debug("数据库操作语句执行成功");

        }catch (SQLException e){
            e.printStackTrace();
            logger.error("数据库操作语句执行失败");
        }
        return  resultSet;
    }


    public static void closeConn(){
        try {
            closeStatement();
            if(con!=null&&!con.isClosed()) {
                con.close();
                logger.debug("数据库连接关闭成功");
            }
        }catch (SQLException e){
            logger.error("数据库连接关闭失败");
            e.printStackTrace();
        }
    }

    private static void closeStatement(){
        try {
            if (statement != null && !statement.isClosed()) {
                statement.close();
                logger.debug("statement 关闭成功");
            }
        }catch (SQLException e){
            logger.error("statement 关闭失败");
            e.printStackTrace();
        }
    }
}

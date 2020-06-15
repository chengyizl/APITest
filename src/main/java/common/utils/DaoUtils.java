package common.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by liugumin on 2019/8/28.
 */
public class DaoUtils {
    private static SqlSession sqlSession;

    public  static <T> T getMapper(Class<T> tClass,String db) {
        InputStream in = null;
        try {
            if(db.equals("tenant")){
                in = Resources.getResourceAsStream("testNg/mybatisConfig/mybatis-config.xml");
//            }else if(db.equals("alipaybill")) {
//                in = Resources.getResourceAsStream("mybatisConfig/mybatis-config-Alipaybill.xml");
//            }else if(db.equals("tags")){
//                in = Resources.getResourceAsStream("mybatisConfig/mybatis-config-tags.xml");
//            }else if(db.equals("billing")){
//                in = Resources.getResourceAsStream("mybatisConfig/mybatis-config-billing.xml");
//            }else if(db.equals("operation")){
//                in = Resources.getResourceAsStream("mybatisConfig/mybatis-config-operation.xml");
//            }else if(db.equals("wechat")){
//                in = Resources.getResourceAsStream("mybatisConfig/mybatis-config-wechat.xml");

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(in);
        sqlSession = sqlSessionFactory.openSession();

        T Mapper = sqlSession.getMapper(tClass);
        return Mapper;
    }

    public static void closeSession(){
        if(sqlSession!=null){
            sqlSession.commit();
            sqlSession.close();
        }

    }

    public static void commit(){
        sqlSession.commit();
    }

    public static void closeSession(SqlSession sqlSession){
        if(sqlSession!=null){
            sqlSession.commit();
            sqlSession.close();
        }

    }

    @SuppressWarnings("unchecked")
    public static SqlSession getSession(Class tClass,String db){
        getMapper(tClass,db);
        return sqlSession;
    }


    public static SqlSession getSqlSession(){
        return sqlSession;
    }


}

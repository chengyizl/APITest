package data;

import common.utils.IOUtils;
import org.testng.ITestContext;
import org.testng.annotations.DataProvider;
import pojo.JsonData;
import pojo.TestCase;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

/**
 * Created by liugumin on 2019/8/28.
 *
 * 提供测试数据
 */
public class DataProviders {

    /**
     * 读取文件内容作为测试数据
     * @return
     */
    @DataProvider(name = "dataFromJson")
    public static Iterator<Object> createData(Method method/*ITestContext context*/){

        Parameter[] parameters = method.getParameters();

        List<Object> jsonDatas = new LinkedList<Object>();
        //        String testParam = context.getCurrentXmlTest().getParameter("test_param");
        try {
            jsonDatas = IOUtils.readFileToList("/Users/liugumin/IdeaProjects/company/APITest/src/main/resources/testNg/1.txt", null, JsonData.class);
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            return jsonDatas.iterator();
        }
    }


    @DataProvider(name = "dataFromArray")
    public static Iterator<Object> dataFromArray(Method method/*ITestContext context*/){
       List<Object> mock = new LinkedList<>();
       mock.add("nihao");
       mock.add("asdfa");

       return mock.iterator();

    }

    @DataProvider(name = "Collection")
    public static Iterator<Object> Collection(ITestContext context){
        List<Object> mock = new LinkedList<>();
        mock.add(context.getAttribute(""));
        mock.add(context.getAttribute(""));

        return mock.iterator();

    }

}

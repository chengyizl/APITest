package data;

import common.utils.DateUtils;
import common.utils.IOUtils;
import org.testng.ITestContext;
import org.testng.annotations.DataProvider;
import pojo.JsonData;
import pojo.LimaTrainline;
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

    @DataProvider(name = "Collection2")
    public static Iterator<Object> Collection2(ITestContext context){
        JsonData jsonData = new JsonData();
        jsonData.setDes("sdsds");
        List<Object> mock = new LinkedList<>();
        mock.add(jsonData);

        return mock.iterator();

    }
    @DataProvider(name = "LimaTrainline")
    public static Iterator<Object> lima_trainline(ITestContext context){
        LimaTrainline limaTrainline = new LimaTrainline();
        limaTrainline.setBookers("[{\"ticketType\": 1,\"bookerName\": \"王云\",\"idcardType\": 0,\"idcardNo\": \"420621199503270621\",\"bookerPhone\": \"18727083743\",\"seatType\": 0}]");
        limaTrainline.setContactName("王云");
        limaTrainline.setContactTel("18727083743");
        limaTrainline.setDate(DateUtils.dateToStr(DateUtils.rollDay(DateUtils.getNow(),1),"yyyy-MM-dd"));
        limaTrainline.setEndTime("19:31");
        limaTrainline.setFrom("杭州东");
        limaTrainline.setMemberId("712708494783938560");
        limaTrainline.setRunTimeDays("0");
        limaTrainline.setRunTimeHour("0");
        limaTrainline.setRunTimeMinutes("9");
        limaTrainline.setStartTime("19:22");
        limaTrainline.setTenantId("519142041838419968");
        limaTrainline.setTo("余杭");
        limaTrainline.setTrainNumber("D3132");

        LimaTrainline limaTrainline1 = new LimaTrainline();
        limaTrainline1.setBookers("[{\"ticketType\": 1,\"bookerName\": \"王云\",\"idcardType\": 0,\"idcardNo\": \"420621199503270621\",\"bookerPhone\": \"18727083743\",\"seatType\": 0}]");
        limaTrainline1.setContactName("云云");
        limaTrainline1.setContactTel("18727083743");
        limaTrainline1.setDate(DateUtils.dateToStr(DateUtils.rollDay(DateUtils.getNow(),1),"yyyy-MM-dd"));
        limaTrainline1.setEndTime("21:17");
        limaTrainline1.setFrom("杭州东");
        limaTrainline1.setMemberId("712708494783938560");
        limaTrainline1.setRunTimeDays("0");
        limaTrainline1.setRunTimeHour("0");
        limaTrainline1.setRunTimeMinutes("9");
        limaTrainline1.setStartTime("21:08");
        limaTrainline1.setTenantId("519142041838419968");
        limaTrainline1.setTo("余杭");
        limaTrainline1.setTrainNumber("D2286");

        /*
        LimaTrainline limaTrainline2 = new LimaTrainline();
        limaTrainline2.setBookers("[{\"bookerName\":\"王云\",\"idcardType\":0,\"idcardNo\":\"420621199503270621\",\"bookerPhone\":\"18727083743\",\"ticketType\":1,\"typeName\":\"成人票\",\"isNameError\":false,\"isIDError\":false,\"isPhoneError\":false,\"seatType\":\"5\"}]");
        limaTrainline2.setContactName("云云");
        limaTrainline2.setContactTel("18727083743");
        limaTrainline2.setDate(DateUtils.dateToStr(DateUtils.rollDay(DateUtils.getNow(),3),"yyyy-MM-dd"));
        limaTrainline2.setEndTime("17:37");
        limaTrainline2.setFrom("太原");
        limaTrainline2.setMemberId("712708494783938560");
        limaTrainline2.setRunTimeDays("0");
        limaTrainline2.setRunTimeHour("0");
        limaTrainline2.setRunTimeMinutes("11");
        limaTrainline2.setStartTime("17:26");
        limaTrainline2.setTenantId("519142041838419968");
        limaTrainline2.setTo("太原南");
        limaTrainline2.setTrainNumber("4643");
         */
        List<Object> Trainline = new LinkedList<>();
        Trainline.add(limaTrainline);
        Trainline.add(limaTrainline1);
        //Trainline.add(limaTrainline2);

        return Trainline.iterator();
    }



}

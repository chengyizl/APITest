package TestCase;

import common.utils.IOUtils;
import common.utils.JdbcUtils;
import common.utils.JsonUtils;
import data.DataProviders;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Step;
import io.qameta.allure.Story;
import io.restassured.http.Cookie;

import io.restassured.http.Method;
import io.restassured.response.Response;

import static org.hamcrest.Matchers.*;
import static io.restassured.RestAssured.*;


import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import pojo.JsonData;
import pojo.TestCase;
import pojo.TestSuit;
import redis.clients.jedis.Jedis;

import java.sql.ResultSet;
import java.util.*;

@Epic("一级标题")
@Feature("二级标题")
@Listeners()
public class Demo2TestCase {

    TestSuit suit;
    ArrayList<TestCase> caseList;
    Map<String,Object> params;

    @BeforeTest
    public void preConditions() {
        String path = "TestCaseData/testDemo1.json";
        String jsondata = IOUtils.readFiletoString(path,"utf-8");
        suit = JsonUtils.parseJsonData(jsondata,TestSuit.class);
        caseList = suit.getCaseList();
        params = suit.getParams();
        System.out.println((String) params.get("params1"));
    }

//    @Test(dataProvider = "dataFromJson",dataProviderClass = DataProviders.class)
    @Story("关联接口测试-登录")
    @Test(description = "先登录，然后将登录的响应传递给ITestContest")
    public void testCaseLogin(ITestContext context){
        TestCase testCase = suit.getCaseList().get(0);
        baseURI = suit.getBaseurl();
        Response response =
                given()
                         .contentType("application/json; charset=UTF-8")
                         .body("{\"username\":\""+testCase.getRequest().getUsername()+"\",\"password\":\""+testCase.getRequest().getPassword()+"\"}")
                         .request(Method.POST,"/login/v2")
                         .then()
                         .body("data.user_name",equalTo(testCase.getRequest().getUsername()))
                        .extract().
                        response();
        // 将响应放到上下文中 ITestContext
        context.setAttribute("LoginSponse",response);

    }
    @Story("关联接口测试-登录后查询")
    @Test(dependsOnMethods = "testCaseLogin",description = "通过ITestContest获取到")
    public void testCaseAfterLogin(ITestContext context){
        // 从上下文中ITestContext获取登录后的响应
        Response response =(Response) context.getAttribute("LoginSponse");
        String token = response.path("data.token");// response body 如下
//        {
//            "code":8200,
//                "data":{
//            "id":"1185109714373382146",
//                    "image":"",
//                    "token":"3b1bd0b7-6bac-482d-86e6-62d6e1bb5a7c",
//                    "user_name":"liangshuihe"
//        },
//            "message":"请求成功",
//                "success":true
//        }

        String LSSESSIONID = response.getHeader("cookie");

        given()
                .cookie("LSSESSIONID", LSSESSIONID)
                .cookie("token", token)
                .request(Method.POST, "/mapInfo/getList")
                .then()
                .body("message", equalTo("查询成功"));
    }

    @Story("数据库测试")
    @Test(description = "通过sql语句查询数据库信息，进行断言")
    public void testCase2(){
        try {
            String tem="";
            TestCase testCase = caseList.get(0);

            ResultSet resultSet = JdbcUtils.getResult(testCase.getDBsql().getSqlList().get(0), testCase.getDBsql().getJdbc());
            while (resultSet.next()){
                tem = resultSet.getString("name");
            }
            Assert.assertEquals("刘成成", tem);
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    @Story("单接口测试")
    @Step()
    @Test(testName ="hahah" ,description = "单接口测试，对返回的json内容内层字段进行断言")
    public void testCase3() {
        baseURI = "https://lmyxtest-web.limachufa.com";
//        Response response =
                given()
                        .formParam("bookers", "[{\"ticketType\": 1,\"bookerName\": \"王云\",\"idcardType\": 0,\"idcardNo\": \"420621199503270621\",\"bookerPhone\": \"18727083743\",\"seatType\": 0}]")
                        .formParam("contactName", "阿云")
                        .formParam("contactTel", "18727083743")
                        .formParam("date", "2020-06-01")
                        .formParam("endTime", "15:50")
                        .formParam("from", "汉口")
                        .formParam("memberId", "712708494783938560")
                        .formParam("runTimeDays", "1")
                        .formParam("runTimeHour", "14")
                        .formParam("runTimeMinutes", "850")
                        .formParam("startTime", "11:09")
                        .formParam("tenantId", "519142041838419968")
                        .formParam("to", "杭州东")
                        .formParam("trainNumber", "D2194")
                        .request(Method.POST, "/web/trainlineOrder/bookTrainTickets")
                        .then()
                        .body("message", equalTo("出票失败"));



    }
    /*public void testCase4(){
           baseURI = "https://lmyxtest-web.limachufa.com";
           given()
              .formParam(s:"",)

           ;

    }*/

    @Story("mock数据进行多次测试")
    @Test(description = "通过mock数据进行多次测试时",dataProviderClass = DataProviders.class,dataProvider = "Collection2")
    public void testCase5(Object object){
        Assert.assertNotNull(((JsonData)object).getDes());
    }




    @Story("操作redis")
    @Test(description = "通过mock数据进行多次测试时")
    public void testCase6(Object object){
        //连接redis服务器，121.43.167.127:6379
        Jedis jedis = new Jedis("121.43.167.127", 6379);
        //权限认证 输入密码
        jedis.auth("Lishi@s127");

        //###############################################################字符串操作######################################################
        // 存储字符串
        jedis.set("TestString","xinxin");//向key-->TestString中放入了value-->xinxin
        //拼接 字符串
        jedis.append("TestString", " is my lover");
        // 获取制定key的字符串
        String message = jedis.get("TestString");
        // 删除指定的字符串类型key
        jedis.del("TestString");

        //###############################################################Map操作######################################################
        Map<String,String>  map = new HashMap<>();
        map.put("name", "xinxin");
        map.put("age", "22");
        map.put("qq", "123456");
        // 存储map
        jedis.hmset("user",map);;
        //取出user中的name，执行结果:[minxr]-->注意结果是一个泛型的List
        //第一个参数是存入redis中map对象的key，后面跟的是放入map中的对象的key，后面的key可以跟多个，是可变参数
        List<String> rsmap = jedis.hmget("user", "name", "age", "qq");
        //删除map中的某个键值
        jedis.hdel("user","age");
        // 判断是否存在 key为user的map
        boolean exit = jedis.exists("user");
        //返回map对象中的所有key
        Set<String> keys = jedis.hkeys("user");
        //返回map对象中的所有value
        List<String> values = jedis.hvals("user");




        //###############################################################list操作######################################################
        //开始前，先移除所有的内容
        jedis.del("java framework");
        // 插入key为java framework 起始下标为0，不限长度的list
        jedis.lrange("java framework",0,-1);
        //先向key java framework中从左存放三条数据
        jedis.lpush("java framework","spring");
        jedis.lpush("java framework","struts");
        jedis.lpush("java framework","hibernate");
        //再取出所有数据jedis.lrange是按范围取出，
        // 第一个是key，第二个是起始位置，第三个是结束位置，jedis.llen获取长度 -1表示取得所有
        List<String> list = jedis.lrange("java framework",0,-1);
        jedis.del("java framework");
        //向key java framework中从右存放三条数据
        jedis.rpush("java framework","spring");
        jedis.rpush("java framework","struts");
        jedis.rpush("java framework","hibernate");
        //再取出所有数据jedis.lrange是按范围取出，
        // 第一个是key，第二个是起始位置，第三个是结束位置，jedis.llen获取长度 -1表示取得所有
        jedis.lrange("java framework",0,-1);



        //###############################################################set操作######################################################

        //添加
        jedis.sadd("user","liuling");
        jedis.sadd("user","xinxin");
        jedis.sadd("user","ling");
        jedis.sadd("user","zhangxinxin");
        jedis.sadd("user","who");
        //移除noname,可指定多个value
        jedis.srem("user","who");
        //获取所有加入的value
        jedis.smembers("user");
        //判断 who 是否是user集合的元素
        jedis.sismember("user", "who");
        jedis.srandmember("user");
        //返回集合的元素个数
        jedis.scard("user");
        //删除set中随机多个元素
        jedis.spop("user",jedis.scard("user"));

    }

    @AfterClass
    public void clean(){
        JdbcUtils.closeConn();
    }

}

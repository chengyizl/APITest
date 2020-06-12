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
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import pojo.TestCase;
import pojo.TestSuit;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
@Epic("一级标题")
@Feature("二级标题")
@Listeners()
public class Demo2TestCase {

    TestSuit suit;

    List<Cookie> cookies;
    ArrayList<TestCase> caseList;

    @BeforeTest
    public void preConditions() {
        String path = "TestCaseData/testDemo1.json";
        String jsondata = IOUtils.readFiletoString(path,"utf-8");
        suit = JsonUtils.parseJsonData(jsondata,TestSuit.class);
        caseList = suit.getCaseList();
    }

//    @Test(dataProvider = "dataFromJson",dataProviderClass = DataProviders.class)
    @Story("关联接口测试")
    @Test(description = "先登录，然后通过登录接口信息进行下面的测试")
    public void testCase(){
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
        }finally {
            JdbcUtils.closeConn();
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

    @Story("mock数据进行多次测试")
    @Test(description = "通过mock数据进行多次测试时",dataProviderClass = DataProviders.class,dataProvider = "dataFromArray")
    public void testCase4(Object object){
        Assert.assertNotNull(object);
    }






}

package TestCase;

import common.utils.IOUtils;
import common.utils.JdbcUtils;
import common.utils.JsonUtils;
import data.DataProviders;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Step;
import io.qameta.allure.Story;
import io.restassured.http.Method;
import io.restassured.response.Response;
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

import static io.restassured.RestAssured.baseURI;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

@Epic("一级标题")
@Feature("二级标题")
@Listeners()
public class RobotTestCase {

    TestSuit suit;
    ArrayList<TestCase> caseList;
    Map<String,Object> params;

    @BeforeTest
    public void preConditions() {
        String path = "TestCaseData/Robot.json";
        String jsondata = IOUtils.readFiletoString(path,"utf-8");
        suit = JsonUtils.parseJsonData(jsondata,TestSuit.class);
        caseList = suit.getCaseList();
        params = suit.getParams();
//        System.out.println((String) params.get("params1"));
    }

//    @Test(dataProvider = "dataFromJson",dataProviderClass = DataProviders.class)
    @Story("力小知后台登录-获取token")
    @Test(description = "先登录，然后将登录的响应传递给ITestContest")
    public void testCaseLogin(ITestContext context){
        TestCase testCase = suit.getCaseList().get(0);
        baseURI = suit.getBaseurl();
        System.out.println(params);
        Response response =
                 given()
                         .contentType("application/x-www-form-urlencoded")
                         .formParam("appid",params.get("appid"))
                         .formParam("secret",params.get("secret"))
                         .request(Method.POST, "/api/token")
                         .then()
                         .extract().
                         response();
        // 将响应放到上下文中 ITestContext
        context.setAttribute("LoginSponse",response);
        response.body().print();
    }
    @Story("调取力小知-问答接口")
    @Test(dependsOnMethods = "testCaseLogin",description = "获取模拟对话接口数据")
    public void chatAnswer(ITestContext context){
        // 从上下文中ITestContext获取登录后的响应
        Response response =(Response) context.getAttribute("LoginSponse");
//        String question1 = (String) params.get("querstion");
//        System.out.println(question1);
        String token = response.path("token");// response body 如下
//        {
//            "code":8200,
//                "data":{
//                       "id":"1185109714373382146",
//                    "image":"",
//                    "token":"3b1bd0b7-6bac-482d-86e6-62d6e1bb5a7c",
//                    "user_name":"liangshuihe"
//        },
//            "message":"请求成功",
//                "success":true
//        }
        Response response1 =

        given()
                .contentType("application/x-www-form-urlencoded;charset=UTF-8")
//                .cookie("token", token)
                .header("token", token)

                .formParam("question","曹操介绍" )
                .request(Method.POST, "/api/chat")
                .then()
                .body("data.answer", equalTo("中国古代杰出的政治家、军事家、文学家、书法家、诗人 。东汉末年权相，太尉曹嵩之子，曹魏的奠基者")).
                extract().response();
        response1.body().print();
    }


}

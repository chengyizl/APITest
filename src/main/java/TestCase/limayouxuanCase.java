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
import static org.hamcrest.Matchers.equalTo;


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
public class limayouxuanCase {
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
    //@Test(dataProvider = "dataFromJson",dataProviderClass = DataProviders.class)
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

    //分页查询攻略-tenantId为空
    @Test(testName ="攻略" ,description = "分页查询攻略-tenantId为空")
    public void testCase1() {
        baseURI = "https://lmyxtest-web.limachufa.com";
        given()
                .formParam("tenantId", "")
                .formParam("pageSize", "10")
                .formParam("currentPage", "1")
                .formParam("count", "0")
                .formParam("loading", " ")
                .formParam("scenicspotIds", " ")
                .formParam("days", " ")
                .formParam("title", " ")
                .request(Method.POST, "/web/office/queryPage")
                .then()
                .body("message", equalTo("租户id不能为空"))
                .body("resultCode",equalTo(8500));

    }
    //分页查询攻略-列表查询成功
    @Test(testName ="攻略" ,description = "分页查询攻略-列表查询成功")
    public void testCase2() {
        baseURI = "https://lmyxtest-web.limachufa.com";
        given()
                .formParam("tenantId", "519142041838419968")
                .formParam("pageSize", "10")
                .formParam("currentPage", "1")
                .formParam("count", "0")
                .formParam("loading", " ")
                .formParam("scenicspotIds", " ")
                .formParam("days", " ")
                .formParam("title", " ")
                .request(Method.POST, "/web/office/queryPage")
                .then()
                .body("message", equalTo("官方攻略列表查询成功"));

    }
    //分页查询攻略-列表查询成功
    @Test(testName ="攻略" ,description = "分页查询攻略-列表查询成功")
    public void testCase3() {
        baseURI = "https://lmyxtest-web.limachufa.com";
        given()
                .formParam("tenantId", "519142041838419968")
                .formParam("pageSize", "10")
                .formParam("currentPage", "1")
                .formParam("count", "0")
                .formParam("loading", " ")
                .formParam("scenicspotIds", " ")
                .formParam("days", " ")
                .formParam("title", " ")
                .request(Method.POST, "/web/office/queryPage")
                .then()
                .body("list", hasSize(10));

    }




}

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
    /*
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
     */

/*
    @Story("h5登录")
    @Test(description = "先登录，然后通过登录接口信息进行下面的测试")
    public void login(){
        TestCase testCase = suit.getCaseList().get(0);
           baseURI =suit.getBaseurl();
           Response response =
                   given()
                           .contentType("application/json; charset=UTF-8")
                           .body("{\"phone\":\""+testCase.getRequest().getPhone()+"\",\"captcha\":\""+testCase.getRequest().getCaptcha()+"\"}")
                           .request(Method.POST,"/web/wechat/login")
                           .then()
                           .body("phone",equalTo(testCase.getRequest().getPhone()))
                           //.body("captcha",equalTo(testCase.getRequest().getCaptcha()))
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
                //mapInfo/getList
                .request(Method.POST, "mapInfo/getList")
                .then()
                .body("resultCode", equalTo(null));
    }

 */

    //首页未传入tanantid、cityCode
    @Test(testName ="首页" ,description = "首页-未传入tanantid、cityCode")
    public void Homepage_null() {
        baseURI = "https://lmyxtest-web.limachufa.com";
        given()
                .formParam("tenantId", "")
                .formParam("cityCode", "")
                .formParam("provinceCode","")
                .request(Method.POST, "/web/wechat/getHomepage")
                .then()
                .body("message", equalTo(""));
                //.body("resultCode",equalTo(8200));
    }
    //首页传入tanantid、cityCode
    @Test(testName ="首页" ,description = "首页-传入tanantid、cityCode")
    public void Homepage() {
        baseURI = "https://lmyxtest-web.limachufa.com";
        given()
                .formParam("tenantId", "519142041838419968")
                .formParam("cityCode", "330100000000")
                .formParam("provinceCode","330100000000")
                .request(Method.POST, "/web/wechat/getHomepage")
                .then()
                //.body("message", equalTo(""));
                .body("resultCode",equalTo(8200));
    }

    //广告位-tenantId为空
    @Test(testName ="广告位" ,description = "广告位-tenantId为空/")
    public void adsense_null() {
        baseURI = "https://lmyxtest-web.limachufa.com";
        given()
               // .formParam("tenantId", "")
               // .formParam("areaNumbers", "")
                .request(Method.POST, "/web/sowing/querySowingProduct")
                .then()
                .body("message", equalTo("租户id不能为空"))
                .body("resultCode",equalTo(8500));
    }
    //广告位输入正确的tenantId
    @Test(testName ="广告位" ,description = "广告位-输入正确的tenantId")
    public void adsense() {
        baseURI = "https://lmyxtest-web.limachufa.com";
        given()
                .formParam("tenantId", "519142041838419968")
                .formParam("areaNumbers", "banner001")
                .request(Method.POST, "/web/sowing/querySowingProduct")
                .then()
                //查询成功
                .body("message", equalTo("查询成功!"));
                   //banner001下有2条数据
                //.body("data.banner001",hasSize(2));
    }
    //广告位随意输入tenantId
    @Test(testName ="广告位" ,description = "广告位-随意输入tenantId")
    public void adsense_random() {
        baseURI = "https://lmyxtest-web.limachufa.com";
        given()
                .formParam("tenantId", "1")
                .formParam("areaNumbers", "1")
                .request(Method.POST, "/web/sowing/querySowingProduct")
                .then()
                //.body("message", equalTo("查询成功"))
                //返回code为8200
                .body("resultCode",equalTo(8200));
    }
    //门票列表为空
    @Test(testName = "门票",description = "景区列表为空")
    public void scenic_null() {
        baseURI = "https://lmyxtest-web.limachufa.com";
        given()
                /*.formParam("tenantId","")
                .formParam("scenicName","")
                .formParam("districtCode","")
                .formParam("scenicLevel","")
                .formParam("provinceCode","")
                .formParam("sortType","")
                .formParam("pageSize","")
                .formParam("currentPage","")
                .formParam("count","")
                .formParam("loading","")
                .formParam("finished","")
                .formParam("cityCode","")
                .formParam("memberCityCode","")
                .formParam("memberLatitude","")
                .formParam("memberLongitude","")
                .formParam("isSelling","")
                 */
                .request(Method.POST, "/web/ticket/queryScenicPage")
                .then()
                .body("resultCode", equalTo(8200));

    }
    //门票列表1条数据
    @Test(testName = "门票",description = "景区列表1条数据")
    public void scenic_one() {
        baseURI = "https://lmyxtest-web.limachufa.com";
        given()
                .formParam("tenantId","519142041838419968")
                .formParam("scenicName","")
                .formParam("districtCode","")
                .formParam("scenicLevel","")
                .formParam("provinceCode","")
                .formParam("sortType","")
                .formParam("pageSize","1")
                .formParam("currentPage","1")
                .formParam("count","0")
                .formParam("loading","")
                .formParam("finished","")
                .formParam("cityCode","330100000000")
                .formParam("memberCityCode","330100000000")
                .formParam("memberLatitude","30.279872")
                .formParam("memberLongitude","120.015659")
                .formParam("isSelling","0")
                .request(Method.POST, "/web/ticket/queryScenicPage")
                .then()
                .body("resultCode", equalTo(8200));

    }
    //门票列表15条数据
    @Test(testName = "门票",description = "景区列表15条数据")
    public void scenic_15() {
        baseURI = "https://lmyxtest-web.limachufa.com";
        given()
                .formParam("tenantId","519142041838419968")
                .formParam("scenicName","")
                .formParam("districtCode","")
                .formParam("scenicLevel","")
                .formParam("provinceCode","")
                .formParam("sortType","")
                .formParam("pageSize","15")
                .formParam("currentPage","1")
                .formParam("count","0")
                .formParam("loading","")
                .formParam("finished","")
                .formParam("cityCode","330100000000")
                //或者输入错误的城市编码
                //.formParam("cityCode","3301000000009")
                .formParam("memberCityCode","330100000000")
                .formParam("memberLatitude","30.279872")
                .formParam("memberLongitude","120.015659")
                .formParam("isSelling","0")
                .request(Method.POST, "/web/ticket/queryScenicPage")
                .then()
                .body("resultCode", equalTo(8200));

    }

    //门景区详情未传入id
    @Test(testName = "门票",description = "景区详情未传入id")
    public void ScenicDetailById_null() {
        baseURI = "https://lmyxtest-web.limachufa.com";
        given()
                .formParam("tenantId","")
                .formParam("memberId","")
                .formParam("scenicId","")
                .formParam("currentPage","")
                .formParam("pageSize","")
                .request(Method.POST, "/web/ticket/queryScenicDetailById")
                .then()
                .body("message",equalTo("景区id不能为空"));
              //  .body("resultCode", equalTo(9001));

    }

    //门景区详情传入id，tenantId/memberId可传可不传
    @Test(testName = "门票",description = "景区详情传入id")
    public void ScenicDetailById() {
        baseURI = "https://lmyxtest-web.limachufa.com";
        given()
                .formParam("tenantId","519142041838419968")
                .formParam("memberId","712708494783938560")
                .formParam("scenicId","692326891465474048")
                .formParam("currentPage","")
                .formParam("pageSize","")
                .request(Method.POST, "/web/ticket/queryScenicDetailById")
                .then()
                .body("resultCode", equalTo(8200));

    }
    //景区详情传入错误id
    @Test(testName = "门票",description = "景区详情输入错误id")
    public void ScenicDetailById_errors() {
        baseURI = "https://lmyxtest-web.limachufa.com";
        given()
                .formParam("tenantId","519142041838419968")
                .formParam("memberId","712708494783938560")
                .formParam("scenicId","6923268914654740481")
                .formParam("currentPage","")
                .formParam("pageSize","")
                .request(Method.POST, "/web/ticket/queryScenicDetailById")
                .then()
                .body("resultCode", equalTo(8500));

    }
    //门票，景区门票id为空692335480447959040
    @Test(testName = "门票",description = "门票id为空")
    public void ticket_id_null() {
        baseURI = "https://lmyxtest-web.limachufa.com";
        given()
                .formParam("id","")
                .formParam("tenantId","")
                .request(Method.POST, "/web/ticket/queryTicketDetail")
                .then()
                //或者景区门票不能为空
               // .body("message",equalTo("门票id不能为空"));
                .body("resultCode", equalTo(9002));

    }

    //门票-景区id不为空692335480447959040
    @Test(testName = "门票",description = "输入正确的景区id")
    public void ticket() {
        baseURI = "https://lmyxtest-web.limachufa.com";
        given()
                .formParam("id","692335480447959040")
                .formParam("tenantId","")
                .request(Method.POST, "/web/ticket/queryTicketDetail")
                .then()
                //或者景区门票不能为空
                // .body("message",equalTo("门票id不能为空"));
                .body("resultCode", equalTo(8200));

    }

    //景区门票错误id
    @Test(testName = "门票",description = "门票输入错误的id")
    public void ticket_errors() {
        baseURI = "https://lmyxtest-web.limachufa.com";
        given()
                .formParam("id","692335480447959040")
                .formParam("tenantId","")
                .request(Method.POST, "/web/ticket/queryTicketDetail")
                .then()
                //或者景区门票不能为空
                // .body("message",equalTo("门票id不能为空"));
                .body("resultCode", equalTo(8200));

    }
    //门票日历价格查询
    @Test(testName = "门票",description = "日历-门票id未输入")
    public void queryTicketDayPrice_null() {
        baseURI = "https://lmyxtest-web.limachufa.com";
        given()
                .formParam("id","")
                .formParam("startDate","")
                .formParam("endDate","")
                .request(Method.POST, "/web/ticket/queryTicketDayPrice")
                .then()
                //或者景区门票不能为空
                // .body("message",equalTo("门票id不能为空"));
                .body("resultCode", equalTo(9002));

    }
    //门票日历价格-输入id
    @Test(testName = "门票",description = "日历-门票传入id")
    public void queryTicketDayPrice() {
        baseURI = "https://lmyxtest-web.limachufa.com";
        given()
                .formParam("id","692332412155199488")
                .formParam("startDate","2020-06-22")
                .formParam("endDate","2020-06-22")
                .request(Method.POST, "/web/ticket/queryTicketDayPrice")
                .then()
                //或者景区门票不能为空
                // .body("message",equalTo("门票id不能为空"));
                .body("resultCode", equalTo(8200));

    }



    //分页查询攻略-tenantId为空
    @Test(testName ="攻略" ,description = "分页查询攻略-tenantId为空")
    public void strategy_null() {
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
    @Test(testName ="攻略" ,description = "分页查询攻略-官方攻略列表查询成功")
    public void strategy() {
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
    //分页查询攻略-列表显示15条数据
    @Test(testName ="攻略" ,description = "分页查询攻略-列表显示15条数据")
    public void strategy_ten() {
        baseURI = "https://lmyxtest-web.limachufa.com";
        given()
                .formParam("tenantId", "519142041838419968")
                .formParam("pageSize", "15")
                .formParam("currentPage", "1")
                .formParam("count", "0")
                .formParam("loading", " ")
                .formParam("scenicspotIds", " ")
                .formParam("days", " ")
                .formParam("title", " ")
                .request(Method.POST, "/web/office/queryPage")
                .then()
                .body("list", hasSize(15));

    }





}

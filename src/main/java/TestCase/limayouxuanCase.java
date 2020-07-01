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
import redis.clients.jedis.Jedis;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

@Epic("一级标题")
@Feature("二级标题")
@Listeners()
public class limayouxuanCase {
    TestSuit suit;

    // 操作redis
    private Jedis jedis;

    // 短时间内可以多次使用的验证码
    private String code;

    List<Cookie> cookies;
    ArrayList<TestCase> caseList;

    @BeforeTest
    public void preConditions() {
        String path = "TestCaseData/limayouxuan.json";
        String jsondata = IOUtils.readFiletoString(path, "utf-8");
        suit = JsonUtils.parseJsonData(jsondata, TestSuit.class);
        caseList = suit.getCaseList();

        //连接redis服务器，121.43.167.127:6379
        jedis = new Jedis("121.43.167.127", 6379);
        //权限认证
        jedis.auth("Lishi@s127");
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

    //h5登录~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    //正常登录成功
    @Test(testName = "登录", description = "正常登录成功")
    public void login() {
        baseURI = "https://lmyxtest-web.limachufa.com";
        // 通过redis 获取验证码
        if(code==null||code.isEmpty()) {
            //TODO  调用获取验证码

            // redis 的key格式是 OTS_3.1.0_DEV_PREFIX_+tenantId+_phone
            String codeMessage = jedis.get("OTS_3.1.0_DEV_PREFIX_557254765256376320_13588283723");
            code = codeMessage.substring(codeMessage.length() - 6);
        }

        given()
                .formParam("tenantId", "519142041838419968")
                .formParam("phone", "18727083743")
                .formParam("captcha", code)
                .formParam("openId", "")
                .formParam("type", "")
                .request(Method.POST, "/web/wechat/login")
                .then()
                //.body("message", equalTo(""));
                .body("resultCode", equalTo(8200));
    }

    //未传入租户id
    @Test(testName = "登录", description = "租户id为空")
    public void login_error() {
        baseURI = "https://lmyxtest-web.limachufa.com";
        given()
                .formParam("tenantId", "")
                .formParam("phone", "")
                .formParam("captcha", "")
                .formParam("openId", "")
                .formParam("type", "")
                .request(Method.POST, "/web/wechat/login")
                .then()
                //.body("message", equalTo("租户ID不能为空"));
                .body("resultCode", equalTo(8500));
    }

    //是否为新用户
    @Test(testName = "登录", description = "是否为新用户")
    public void login_isNewMember() {
        try {
            String tem = "";
            TestCase testCase = caseList.get(0);

            ResultSet resultSet = JdbcUtils.getResult(testCase.getDBsql().getSqlList().get(0), testCase.getDBsql().getJdbc());
//            while (resultSet.next()){
//                tem = resultSet.getString("name");
//            }
            baseURI = "https://lmyxtest-web.limachufa.com";

            // 通过redis 获取验证码
            if(code==null||code.isEmpty()) {
                //TODO  调用获取验证码


                // redis 的key格式是 OTS_3.1.0_DEV_PREFIX_+tenantId+_phone
                String codeMessage = jedis.get("OTS_3.1.0_DEV_PREFIX_557254765256376320_13588283723");
                code = codeMessage.substring(codeMessage.length() - 6);
            }

            Response response =
                    given()
                            .contentType("application/x-www-form-urlencoded;charset=UTF-8")
                            .formParam("tenantId", "519142041838419968")
                            .formParam("phone", "18727083743")
                            .formParam("captcha", code)
                            .formParam("openId", "")
                            .formParam("type", "")
                            .request(Method.POST, "/web/wechat/login")
                            .then()
                            .body("data.isNewMember", equalTo(true))
                            .extract().
                            response();
        } finally {
            JdbcUtils.closeConn();
        }
        Response response1 =
                given()
                        .contentType("application/x-www-form-urlencoded;charset=UTF-8")
                        .formParam("tenantId", "519142041838419968")
                        .formParam("phone", "18727083743")
                        .formParam("captcha", "653771")
                        .formParam("openId", "")
                        .formParam("type", "")
                        .request(Method.POST, "/web/wechat/login")
                        .then()
                        .body("data.isNewMember", equalTo(false))
                        .extract().
                        response();

    }

    //首页~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    //首页未传入tanantid、cityCode
    @Test(testName = "首页", description = "首页-未传入tanantid、cityCode")
    public void Homepage_null() {
        baseURI = "https://lmyxtest-web.limachufa.com";
        given()
                .formParam("tenantId", "")
                .formParam("cityCode", "")
                .formParam("provinceCode", "")
                .request(Method.POST, "/web/wechat/getHomepage")
                .then()
                .body("message", equalTo(""));
        //.body("resultCode",equalTo(8200));
    }

    //首页传入tanantid、cityCode
    @Test(testName = "首页", description = "首页-传入tanantid、cityCode")
    public void Homepage() {
        baseURI = "https://lmyxtest-web.limachufa.com";
        given()
                .formParam("tenantId", "519142041838419968")
                .formParam("cityCode", "330100000000")
                .formParam("provinceCode", "330100000000")
                .request(Method.POST, "/web/wechat/getHomepage")
                .then()
                //.body("message", equalTo(""));
                .body("resultCode", equalTo(8200));
    }

    //广告位~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    //广告位-tenantId为空
    @Test(testName = "广告位", description = "广告位-tenantId为空")
    public void adsense_null() {
        baseURI = "https://lmyxtest-web.limachufa.com";
        given()
                // .formParam("tenantId", "")
                // .formParam("areaNumbers", "")
                .request(Method.POST, "/web/sowing/querySowingProduct")
                .then()
                .body("message", equalTo("租户id不能为空"))
                .body("resultCode", equalTo(8500));
    }

    //广告位输入正确的tenantId
    @Test(testName = "广告位", description = "广告位-输入正确的tenantId")
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
    @Test(testName = "广告位", description = "广告位-随意输入tenantId")
    public void adsense_random() {
        baseURI = "https://lmyxtest-web.limachufa.com";
        given()
                .formParam("tenantId", "1")
                .formParam("areaNumbers", "1")
                .request(Method.POST, "/web/sowing/querySowingProduct")
                .then()
                //.body("message", equalTo("查询成功"))
                //返回code为8200
                .body("resultCode", equalTo(8200));
    }

    //门票~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    //门票列表为空
    @Test(testName = "门票", description = "景区列表为空")
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
    @Test(testName = "门票", description = "景区列表1条数据")
    public void scenic_one() {
        baseURI = "https://lmyxtest-web.limachufa.com";
        given()
                .formParam("tenantId", "519142041838419968")
                .formParam("scenicName", "")
                .formParam("districtCode", "")
                .formParam("scenicLevel", "")
                .formParam("provinceCode", "")
                .formParam("sortType", "")
                .formParam("pageSize", "1")
                .formParam("currentPage", "1")
                .formParam("count", "0")
                .formParam("loading", "")
                .formParam("finished", "")
                .formParam("cityCode", "330100000000")
                .formParam("memberCityCode", "330100000000")
                .formParam("memberLatitude", "30.279872")
                .formParam("memberLongitude", "120.015659")
                .formParam("isSelling", "0")
                .request(Method.POST, "/web/ticket/queryScenicPage")
                .then()
                .body("resultCode", equalTo(8200));

    }

    //门票列表15条数据
    @Test(testName = "门票", description = "景区列表15条数据")
    public void scenic_15() {
        baseURI = "https://lmyxtest-web.limachufa.com";
        given()
                .formParam("tenantId", "519142041838419968")
                .formParam("scenicName", "")
                .formParam("districtCode", "")
                .formParam("scenicLevel", "")
                .formParam("provinceCode", "")
                .formParam("sortType", "")
                .formParam("pageSize", "15")
                .formParam("currentPage", "1")
                .formParam("count", "0")
                .formParam("loading", "")
                .formParam("finished", "")
                .formParam("cityCode", "330100000000")
                //或者输入错误的城市编码
                //.formParam("cityCode","3301000000009")
                .formParam("memberCityCode", "330100000000")
                .formParam("memberLatitude", "30.279872")
                .formParam("memberLongitude", "120.015659")
                .formParam("isSelling", "0")
                .request(Method.POST, "/web/ticket/queryScenicPage")
                .then()
                .body("resultCode", equalTo(8200));

    }

    //门景区详情未传入id
    @Test(testName = "门票", description = "景区详情未传入id")
    public void ScenicDetailById_null() {
        baseURI = "https://lmyxtest-web.limachufa.com";
        given()
                .formParam("tenantId", "")
                .formParam("memberId", "")
                .formParam("scenicId", "")
                .formParam("currentPage", "")
                .formParam("pageSize", "")
                .request(Method.POST, "/web/ticket/queryScenicDetailById")
                .then()
                .body("message", equalTo("景区id不能为空"));
        //  .body("resultCode", equalTo(9001));

    }

    //门景区详情传入id，tenantId/memberId可传可不传
    @Test(testName = "门票", description = "景区详情传入id")
    public void ScenicDetailById() {
        baseURI = "https://lmyxtest-web.limachufa.com";
        given()
                .formParam("tenantId", "519142041838419968")
                .formParam("memberId", "712708494783938560")
                .formParam("scenicId", "692326891465474048")
                .formParam("currentPage", "")
                .formParam("pageSize", "")
                .request(Method.POST, "/web/ticket/queryScenicDetailById")
                .then()
                .body("resultCode", equalTo(8200));

    }

    //景区详情传入错误id
    @Test(testName = "门票", description = "景区详情输入错误id")
    public void ScenicDetailById_errors() {
        baseURI = "https://lmyxtest-web.limachufa.com";
        given()
                .formParam("tenantId", "519142041838419968")
                .formParam("memberId", "712708494783938560")
                .formParam("scenicId", "6923268914654740481")
                .formParam("currentPage", "")
                .formParam("pageSize", "")
                .request(Method.POST, "/web/ticket/queryScenicDetailById")
                .then()
                .body("resultCode", equalTo(8500));

    }

    //门票，景区门票id为空692335480447959040
    @Test(testName = "门票", description = "门票id为空")
    public void ticket_id_null() {
        baseURI = "https://lmyxtest-web.limachufa.com";
        given()
                .formParam("id", "")
                .formParam("tenantId", "")
                .request(Method.POST, "/web/ticket/queryTicketDetail")
                .then()
                //或者景区门票不能为空
                // .body("message",equalTo("门票id不能为空"));
                .body("resultCode", equalTo(9002));

    }

    //门票-景区id不为空692335480447959040
    @Test(testName = "门票", description = "输入正确的景区id")
    public void ticket() {
        baseURI = "https://lmyxtest-web.limachufa.com";
        given()
                .formParam("id", "692335480447959040")
                .formParam("tenantId", "")
                .request(Method.POST, "/web/ticket/queryTicketDetail")
                .then()
                //或者景区门票不能为空
                // .body("message",equalTo("门票id不能为空"));
                .body("resultCode", equalTo(8200));

    }

    //景区门票错误id
    @Test(testName = "门票", description = "门票输入错误的id")
    public void ticket_errors() {
        baseURI = "https://lmyxtest-web.limachufa.com";
        given()
                .formParam("id", "692335480447959040")
                .formParam("tenantId", "")
                .request(Method.POST, "/web/ticket/queryTicketDetail")
                .then()
                //或者景区门票不能为空
                // .body("message",equalTo("门票id不能为空"));
                .body("resultCode", equalTo(8200));

    }

    //门票日历价格查询
    @Test(testName = "门票", description = "日历-门票id未输入")
    public void queryTicketDayPrice_null() {
        baseURI = "https://lmyxtest-web.limachufa.com";
        given()
                .formParam("id", "")
                .formParam("startDate", "")
                .formParam("endDate", "")
                .request(Method.POST, "/web/ticket/queryTicketDayPrice")
                .then()
                //或者景区门票不能为空
                // .body("message",equalTo("门票id不能为空"));
                .body("resultCode", equalTo(9002));

    }

    //门票日历价格-输入id
    @Test(testName = "门票", description = "日历-门票传入id")
    public void queryTicketDayPrice() {
        baseURI = "https://lmyxtest-web.limachufa.com";
        given()
                .formParam("id", "692332412155199488")
                .formParam("startDate", "2020-06-22")
                .formParam("endDate", "2020-06-22")
                .request(Method.POST, "/web/ticket/queryTicketDayPrice")
                .then()
                //或者景区门票不能为空
                // .body("message",equalTo("门票id不能为空"));
                .body("resultCode", equalTo(8200));

    }

    //酒店~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    //酒店列表为空
    @Test(testName = "酒店", description = "酒店列表为空")
    public void hotel_null() {
        baseURI = "https://lmyxtest-web.limachufa.com";
        given()
                .formParam("tenantId", "")
                .formParam("district", "")
                .formParam("starRating", "")
                .formParam("type", "")
                .formParam("pageSize", "")
                .formParam("currentPage", "")
                .formParam("count", "")
                .formParam("loading", "")
                .formParam("status", "")
                .formParam("beginDate", "")
                .formParam("endDate", "")
                .formParam("city", "")
                .formParam("userInCity", "")
                .formParam("lnt", "")
                .formParam("lat", "")
                .request(Method.POST, "/web/hotel/queryPage")
                .then()
                .body("resultCode", equalTo(8500));

    }

    //酒店列表显示1条数据
    @Test(testName = "酒店", description = "酒店列表显示1条数据")
    public void hotel_one() {
        baseURI = "https://lmyxtest-web.limachufa.com";
        given()
                .formParam("tenantId", "")
                .formParam("district", "")
                .formParam("starRating", "")
                .formParam("type", "")
                .formParam("pageSize", "1")
                .formParam("currentPage", "1")
                .formParam("count", "0")
                .formParam("loading", "true")
                .formParam("status", "1")
                .formParam("beginDate", "2020-06-23")
                .formParam("endDate", "2020-06-24")
                .formParam("city", "330100000000")
                .formParam("userInCity", "330100000000")
                .formParam("lnt", "120.015599")
                .formParam("lat", "30.279756")
                .request(Method.POST, "/web/hotel/queryPage")
                .then()
                .body("resultCode", equalTo(8200));

    }

    //酒店列表显示10条数据
    @Test(testName = "酒店", description = "酒店列表显示10条数据")
    public void hotel_ten() {
        baseURI = "https://lmyxtest-web.limachufa.com";
        given()
                .formParam("tenantId", "")
                .formParam("district", "")
                .formParam("starRating", "")
                .formParam("type", "")
                .formParam("pageSize", "10")
                .formParam("currentPage", "1")
                .formParam("count", "0")
                .formParam("loading", "true")
                .formParam("status", "1")
                .formParam("beginDate", "2020-06-23")
                .formParam("endDate", "2020-06-24")
                .formParam("city", "330100000000")
                .formParam("userInCity", "330100000000")
                .formParam("lnt", "120.015599")
                .formParam("lat", "30.279756")
                .request(Method.POST, "/web/hotel/queryPage")
                .then()
                .body("resultCode", equalTo(8200));

    }

    //酒店列表传入错误日期
    @Test(testName = "酒店", description = "酒店列表输入错误日期")
    public void hotel_errors() {
        baseURI = "https://lmyxtest-web.limachufa.com";
        given()
                .formParam("tenantId", "")
                .formParam("district", "")
                .formParam("starRating", "")
                .formParam("type", "")
                .formParam("pageSize", "10")
                .formParam("currentPage", "1")
                .formParam("count", "0")
                .formParam("loading", "true")
                .formParam("status", "1")
                .formParam("beginDate", "2020-06-23")
                .formParam("endDate", "2020-06-24")
                .formParam("city", "")
                .formParam("userInCity", "")
                .formParam("lnt", "")
                .formParam("lat", "")
                .request(Method.POST, "/web/hotel/queryPage")
                .then()
                .body("resultCode", equalTo(8200));
    }

    //酒店详情为空
    @Test(testName = "酒店", description = "酒店详情为空")
    public void hotel_queryById() {
        baseURI = "https://lmyxtest-web.limachufa.com";
        given()
                .formParam("tenantId", "")
                .formParam("district", "")
                .formParam("starRating", "")
                .formParam("type", "")
                .formParam("pageSize", "10")
                .formParam("currentPage", "1")
                .formParam("count", "0")
                .formParam("loading", "true")
                .formParam("status", "1")
                .formParam("beginDate", "2020-06-23")
                .formParam("endDate", "2020-06-24")
                .formParam("city", "")
                .formParam("userInCity", "")
                .formParam("lnt", "")
                .formParam("lat", "")
                .request(Method.POST, "/web/hotel/queryPage")
                .then()
                .body("resultCode", equalTo(8200));
    }

    //酒店日态详情为空
    @Test(testName = "酒店", description = "酒店日态详情为空")
    public void queryBatchBySkuId_null() {
        baseURI = "https://lmyxtest-web.limachufa.com";
        given()
                .formParam("tenantId", "")
                .formParam("beginDate", "")
                .formParam("endData", "")
                .formParam("skuId", "")
                .request(Method.POST, "/web/hotel/queryBatchBySkuId")
                .then()
                .body("resultCode", equalTo(8500));
    }

    //酒店日态详情传入id
    @Test(testName = "酒店", description = "酒店日态详情传入id")
    public void queryBatchBySkuId() {
        baseURI = "https://lmyxtest-web.limachufa.com";
        given()
                .formParam("tenantId", "519142041838419968")
                .formParam("beginDate", "2020-06-23")
                .formParam("endData", "2020-06-24")
                .formParam("skuId", "722853931939528706")
                .request(Method.POST, "/web/hotel/queryBatchBySkuId")
                .then()
                .body("resultCode", equalTo(8500));
    }

    //酒店日态详情传入id
    @Test(testName = "酒店", description = "酒店日态详情传入随意id")
    public void queryBatchBySkuId_errors() {
        baseURI = "https://lmyxtest-web.limachufa.com";
        given()
                .formParam("tenantId", "5191420418384199681")
                .formParam("beginDate", "2020-06-23")
                .formParam("endData", "2020-06-24")
                .formParam("skuId", "7228539319395287061")
                .request(Method.POST, "/web/hotel/queryBatchBySkuId")
                .then()
                .body("resultCode", equalTo(8500));
    }

    //攻略~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    //分页查询攻略-tenantId为空
    @Test(testName = "攻略", description = "分页查询攻略-tenantId为空")
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
                .body("resultCode", equalTo(8500));

    }

    //分页查询攻略-列表查询成功
    @Test(testName = "攻略", description = "分页查询攻略-官方攻略列表查询成功")
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
    @Test(testName = "攻略", description = "分页查询攻略-列表显示15条数据")
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

    //力马快充~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    //缴费记录为空
    @Test(testName = "力马快充", description = "力马快充，缴费记录为空")
    public void MemberOrderPage_null() {
        baseURI = "https://lmyxtest-web.limachufa.com";
        given()
                /*
                .formParam("tenantId", "519142041838419968")
                .formParam("pageSize", "10")
                .formParam("currentPage", "1")
                .formParam("count", "0")
                .formParam("loading", " ")
                .formParam("memberId", " ")
                 */
                .request(Method.POST, "/web/serveOrder/queryMemberOrderPage")
                .then()
                .body("message", equalTo("用户id不能为空"));

    }

    //缴费记录显示1条
    @Test(testName = "力马快充", description = "力马快充，缴费记录1条数据")
    public void MemberOrderPage_one() {
        baseURI = "https://lmyxtest-web.limachufa.com";
        given()
                .formParam("tenantId", "519142041838419968")
                .formParam("pageSize", "1")
                .formParam("currentPage", "1")
                .formParam("count", "0")
                .formParam("loading", " ")
                .formParam("memberId", " ")
                .request(Method.POST, "/web/serveOrder/queryMemberOrderPage")
                .then()
                .body("message", equalTo(""));

    }

    //缴费记录显示10条
    @Test(testName = "力马快充", description = "力马快充，缴费记录10条数据")
    public void MemberOrderPage_ten() {
        baseURI = "https://lmyxtest-web.limachufa.com";
        given()
                .formParam("tenantId", "519142041838419968")
                .formParam("pageSize", "10")
                .formParam("currentPage", "1")
                .formParam("count", "0")
                .formParam("loading", "")
                .formParam("memberId", "725081578018963456")
                .request(Method.POST, "/web/serveOrder/queryMemberOrderPage")
                .then()
                .body("message", equalTo(""));

    }

    //缴费记录-错误id
    @Test(testName = "力马快充", description = "力马快充，缴费记录输入错误的menberid")
    public void MemberOrderPage_error() {
        baseURI = "https://lmyxtest-web.limachufa.com";
        given()
                .formParam("tenantId", "519142041838419968")
                .formParam("pageSize", "10")
                .formParam("currentPage", "1")
                .formParam("count", "0")
                .formParam("loading", "")
                .formParam("memberId", "72508157801896345611111111111")
                .request(Method.POST, "/web/serveOrder/queryMemberOrderPage")
                .then()
                .body("message", equalTo(""));

    }

    //缴费记录详情，根据订单编码查询订单详情：
    //缴费记录-订单详情页-订单为空
    @Test(testName = "力马快充", description = "力马快充，订单号为空")
    public void OrderCode_null() {
        baseURI = "https://lmyxtest-web.limachufa.com";
        given()
                .formParam("orderCode", "")
                .request(Method.POST, "/web/serveOrder/queryDetailByOrderCode")
                .then()
                .body("message", equalTo("订单编号不能为空"));

    }

    //缴费记录-订单详情页-输入错误的订单号
    @Test(testName = "力马快充", description = "力马快充，错误订单号")
    public void OrderCode_error() {
        baseURI = "https://lmyxtest-web.limachufa.com";
        given()
                .formParam("orderCode", "1234567890")
                .request(Method.POST, "/web/serveOrder/queryDetailByOrderCode")
                .then()
                .body("message", equalTo("订单不存在"));

    }

    //缴费记录-订单详情页-正确的订单编号
    @Test(testName = "力马快充", description = "力马快充，正确订单编号")
    public void OrderCode() {
        baseURI = "https://lmyxtest-web.limachufa.com";
        given()
                .formParam("orderCode", "SDM703919762966577153")
                .request(Method.POST, "/web/serveOrder/queryDetailByOrderCode")
                .then()
                .body("message", equalTo(""));

    }

    //话费-直接充值
    @Test(testName = "力马快充", description = "力马快充，话费直接充值")
    public void mobileOrder_null() {
        baseURI = "https://lmyxtest-web.limachufa.com";
        given()
                .formParam("tenantId", "")
                .formParam("memberId", " ")
                .formParam("mobileNo", "")
                .formParam("rechargeAmount", "")
                .request(Method.POST, "/web/serveOrder/mobileOrder")
                .then()
                .body("message", equalTo("手机号不能为空"));

    }

    //话费-输入手机号充值，充值10元
    @Test(testName = "力马快充", description = "力马快充，输入手机号充值，充值10元")
    public void mobileOrder_tenyaun() {
        baseURI = "https://lmyxtest-web.limachufa.com";
        given()
                .formParam("tenantId", "519142041838419968")
                .formParam("memberId", "725081578018963456")
                .formParam("mobileNo", "18727083743")
                .formParam("rechargeAmount", "10")
                .request(Method.POST, "/web/serveOrder/mobileOrder")
                .then()
                .body("message", equalTo(""));

    }

    //话费-输入手机号充值，充值100元
    @Test(testName = "力马快充", description = "力马快充，输入手机号充值，充值100元")
    public void mobileOrder_100yuan() {
        baseURI = "https://lmyxtest-web.limachufa.com";
        given()
                .formParam("tenantId", "519142041838419968")
                .formParam("memberId", "725081578018963456")
                .formParam("mobileNo", "18727083743")
                .formParam("rechargeAmount", "100")
                .request(Method.POST, "/web/serveOrder/mobileOrder")
                .then()
                .body("message", equalTo(""));
    }

    //话费-输入手机号充值，充值500元
    @Test(testName = "力马快充", description = "力马快充，输入手机号充值，充值500元")
    public void mobileOrder_500yuan() {
        baseURI = "https://lmyxtest-web.limachufa.com";
        given()
                .formParam("tenantId", "519142041838419968")
                .formParam("memberId", "725081578018963456")
                .formParam("mobileNo", "18727083743")
                .formParam("rechargeAmount", "500")
                .request(Method.POST, "/web/serveOrder/mobileOrder")
                .then()
                .body("message", equalTo(""));
    }

    //话费-输入手机号充值，充值1000元
    @Test(testName = "力马快充", description = "力马快充，输入手机号充值，充值1000元")
    public void mobileOrder_1000yuan() {
        baseURI = "https://lmyxtest-web.limachufa.com";
        given()
                .formParam("tenantId", "519142041838419968")
                .formParam("memberId", "725081578018963456")
                .formParam("mobileNo", "18727083743")
                .formParam("rechargeAmount", "1000")
                .request(Method.POST, "/web/serveOrder/mobileOrder")
                .then()
                .body("message", equalTo(""));
    }


    //火车票~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    //火车票预订，购买当前过去的日期
    @Test(testName = "火车票", description = "火车票，购买当前过去的日期")
    public void TrainTickets_after() {
        baseURI = "https://lmyxtest-web.limachufa.com";
        given()
                .formParam("bookers", "[{\"ticketType\": 1,\"bookerName\": \"王云\",\"idcardType\": 0,\"idcardNo\": \"420621199503270621\",\"bookerPhone\": \"18727083743\",\"seatType\": 0}]")
                .formParam("contactName", "云")
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
                //断言resultCode=7115
                //.body("resultCode",equalTo("7115"));
                .body("message", equalTo("出票失败"));

    }

    //火车票预订，购票人信息为空时下单
    @Test(testName = "火车票", description = "火车票，购票人信息为空下单")
    public void TrainTickets_null() {
        baseURI = "https://lmyxtest-web.limachufa.com";
        given()
                .formParam("bookers", "")
                .formParam("contactName", "")
                .formParam("contactTel", "")
                .formParam("date", "")
                .formParam("endTime", "")
                .formParam("from", "")
                .formParam("memberId", "")
                .formParam("runTimeDays", "")
                .formParam("runTimeHour", "")
                .formParam("runTimeMinutes", "")
                .formParam("startTime", "")
                .formParam("tenantId", "")
                .formParam("to", "")
                .formParam("trainNumber", "")
                .request(Method.POST, "/web/trainlineOrder/bookTrainTickets")
                .then()
                //断言resultCode=7115
                //.body("resultCode",equalTo("7115"));
                .body("message", equalTo("请求参数不能为空"));

    }
    /*
    //火车票预订，购买一个成人票和一个儿童票
    @Test(testName ="火车票" ,description = "火车票购买一个成人票和一个儿童票")
    public void TrainTickets_child() {
        baseURI = "https://lmyxtest-web.limachufa.com";
        given()
                .formParam("bookers", "[{\"ticketType\": 1,\"bookerName\": \"王云\",\"idcardType\": 0,\"idcardNo\": \"420621199503270621\",\"bookerPhone\": \"18727083743\",\"seatType\": 0},{\"ticketType\": 2,\"bookerName\": \"王云\",\"idcardType\": 0,\"idcardNo\": \"420621199503270621\",\"bookerPhone\": \"18727083743\",\"seatType\": 0}]")
                .formParam("contactName", "王云")
                .formParam("contactTel", "18727083743")
                .formParam("date", "2020-07-22")
                .formParam("endTime", "07:20")
                .formParam("from", "太原")
                .formParam("memberId", "712708494783938560")
                .formParam("runTimeDays", "0")
                .formParam("runTimeHour", "0")
                .formParam("runTimeMinutes", "11")
                .formParam("startTime", "07:09")
                .formParam("tenantId", "519142041838419968")
                .formParam("to", "太原南")
                .formParam("trainNumber", "4611")
                .request(Method.POST, "/web/trainlineOrder/bookTrainTickets")
                .then()
                //断言resultCode=7115
                //.body("resultCode",equalTo("7115"));
                .body("message",equalTo("出票失败"));

    }
     */

    //火车票预订，单独购买一张儿童票
    @Test(testName = "火车票", description = "火车票，单独购买一张儿童票")
    public void TrainTickets_child() {
        baseURI = "https://lmyxtest-web.limachufa.com";
        given()
                .formParam("bookers", "[{\"bookerName\":\"王云\",\"idcardType\":0,\"idcardNo\":\"420621199503270621\",\"bookerPhone\":\"18727083743\",\"ticketType\":2,\"typeName\":\"成人票\",\"isNameError\":false,\"isIDError\":false,\"isPhoneError\":false,\"seatType\":\"5\"}]")
                .formParam("contactName", "云")
                .formParam("contactTel", "18727083743")
                .formParam("date", "2020-07-21")
                .formParam("endTime", "07:20")
                .formParam("from", "太原")
                .formParam("memberId", "725081578018963456")
                .formParam("runTimeDays", "0")
                .formParam("runTimeHour", "0")
                .formParam("runTimeMinutes", "11")
                .formParam("startTime", "07:09")
                .formParam("tenantId", "519142041838419968")
                .formParam("to", "太原南")
                .formParam("trainNumber", "4611")
                .request(Method.POST, "/web/trainlineOrder/bookTrainTickets")
                .then()
                //断言resultCode=7115
                //.body("resultCode",equalTo("7115"));
                .body("message", equalTo("出票失败"));

    }


}

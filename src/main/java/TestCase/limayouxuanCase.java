package TestCase;


import common.utils.DateUtils;
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


import io.restassured.specification.RequestSpecification;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.*;
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

    //收藏id
    private String Collectionid;

    //意见反馈id
    private String publishid;

    List<Cookie> cookies;
    ArrayList<TestCase> caseList;

    @BeforeClass
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

    //h5登录~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    //正常登录成功
    @Test(testName = "正常登录成功", description = "正常登录成功")
    public void login(ITestContext context) {
        baseURI = suit.getBaseurl();
        // 通过redis 获取验证码
        if(!jedis.exists("OTS_CLOUD_LIMAYOUXUAN_TEST_PREFIX_519142041838419968_18727083743")) {
            //TODO  调用获取验证码
            given()
                    .formParam("tenantId", "519142041838419968")
                    .formParam("phone", "18727083743")
                    .request(Method.POST, "/web/wechat/sendCode")
                    .then();
        }
            // redis 的key格式是 OTS_3.1.0_DEV_PREFIX_+tenantId+_phone
            String codeMessage = jedis.get("OTS_CLOUD_LIMAYOUXUAN_TEST_PREFIX_519142041838419968_18727083743");
            code = codeMessage.substring(codeMessage.length() - 6);
        given()
                .formParam("tenantId", "519142041838419968")
                .formParam("phone", "18727083743")
                .formParam("captcha", code)
                .formParam("openId", "")
                .formParam("type", "")
                .request(Method.POST, "/web/wechat/login")
                .then()
                .body("message", equalTo(""))
                .body("resultCode", equalTo(8200));
        }

    //未传入租户id
    @Test(testName = "租户id为空", description = "租户id为空")
    public void login_error() {
        baseURI = suit.getBaseurl();
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
    @Test(testName = "是否为新用户", description = "是否为新用户")
    public void login_isNewMember() {
        try {
            String tem = "";
            TestCase testCase = caseList.get(0);

            ResultSet resultSet = JdbcUtils.getResult(testCase.getDBsql().getSqlList().get(0), testCase.getDBsql().getJdbc());
            baseURI = suit.getBaseurl();

            // 通过redis 获取验证码
            if(!jedis.exists("OTS_CLOUD_LIMAYOUXUAN_TEST_PREFIX_519142041838419968_18727083743")) {
                //TODO  调用获取验证码
                given()
                        .formParam("tenantId", "519142041838419968")
                        .formParam("phone", "18727083743")
                        .request(Method.POST, "/web/wechat/sendCode")
                        .then();
            }
            // redis 的key格式是 OTS_3.1.0_DEV_PREFIX_+tenantId+_phone
            String codeMessage = jedis.get("OTS_CLOUD_LIMAYOUXUAN_TEST_PREFIX_519142041838419968_18727083743");
            code = codeMessage.substring(codeMessage.length() - 6);

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
        }catch (Exception e){
            e.printStackTrace();
        }
            Response response1 =
                    given()
                        .contentType("application/x-www-form-urlencoded;charset=UTF-8")
                        .formParam("tenantId", "519142041838419968")
                        .formParam("phone", "18727083743")
                        .formParam("captcha", code)
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
    @Test(testName = "首页-未传入tanantid、cityCode", description = "首页-未传入tanantid、cityCode")
    public void Homepage_null() {
        baseURI = suit.getBaseurl();
        given()
                .formParam("tenantId", "")
                .formParam("cityCode", "")
                .formParam("provinceCode", "")
                .request(Method.POST, "/web/wechat/getHomepage")
                .then()
                .body("message", equalTo(""))
                .body("resultCode",equalTo(8200));
    }

    //首页传入tanantid、cityCode
    @Test(testName = "首页-传入tanantid、cityCode", description = "首页-传入tanantid、cityCode")
    public void Homepage() {
        baseURI = suit.getBaseurl();
        given()
                .formParam("tenantId", "519142041838419968")
                .formParam("cityCode", "330100000000")
                .formParam("provinceCode", "330100000000")
                .request(Method.POST, "/web/wechat/getHomepage")
                .then()
                .body("message", equalTo(""))
                .body("resultCode", equalTo(8200));
    }

    //广告位~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    //广告位-tenantId为空
    @Test(testName = "广告位-tenantId为空", description = "广告位-tenantId为空")
    public void adsense_null() {
        baseURI = suit.getBaseurl();
        given()
                // .formParam("tenantId", "")
                // .formParam("areaNumbers", "")
                .request(Method.POST, "/web/sowing/querySowingProduct")
                .then()
                .body("message", equalTo("租户id不能为空"))
                .body("resultCode", equalTo(8500));
    }

    //广告位输入正确的tenantId
    @Test(testName = "广告位-输入正确的tenantId", description = "广告位-输入正确的tenantId")
    public void adsense() {
        baseURI = suit.getBaseurl();
        given()
                .formParam("tenantId", "519142041838419968")
                .formParam("areaNumbers", "banner001")
                .request(Method.POST, "/web/sowing/querySowingProduct")
                .then()
                .body("message", equalTo("查询成功!"))
                //banner001下有2条数据
               .body("data.banner001",hasSize(2));
    }

    //广告位随意输入tenantId
    @Test(testName = "广告位-随意输入tenantId", description = "广告位-随意输入tenantId")
    public void adsense_random() {
        baseURI = suit.getBaseurl();
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
    //门票list列表为空
    @Test(testName = "景区列表为空", description = "景区列表为空")
    public void scenic_null() {
        baseURI = suit.getBaseurl();
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

    //景区list列表显示1条数据
    @Test(testName = "景区list列表显示1条数据", description = "景区list列表显示1条数据")
    public void scenic_one() {
        baseURI = suit.getBaseurl();
        given()
                .formParam("tenantId","519142041838419968")
                .formParam("scenicName","11111111111")
                .formParam("districtCode","11111111")
                .formParam("scenicLevel","1111111111")
                .formParam("provinceCode","11111")
                .formParam("sortType","")
                .formParam("pageSize","100")
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

    //景区列表15条数据
    @Test(testName = "景区列表15条数据", description = "景区列表15条数据")
    public void scenic_15() {
        baseURI = suit.getBaseurl();
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

    //景区详情未传入景区scenicId
    @Test(testName = "景区详情未传入景区scenicId", description = "景区详情未传入景区scenicId")
    public void ScenicDetailById_null() {
        baseURI = suit.getBaseurl();
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

    //景区详情传入景区scenicId，tenantId/memberId可传可不传
    @Test(testName = "景区详情传入景区scenicId", description = "景区详情传入景区scenicId")
    public void ScenicDetailById() {
        baseURI = suit.getBaseurl();
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

    //景区详情输入错误的景区scenicId
    @Test(testName = "景区详情输入错误的景区scenicId", description = "景区详情输入错误的景区scenicId")
    public void ScenicDetailById_errors() {
        baseURI = suit.getBaseurl();
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

    //景区门票id为空
    @Test(testName = "门票id为空", description = "门票id为空")
    public void ticket_id_null() {
        baseURI = suit.getBaseurl();
        given()
                .formParam("id", "")
                .formParam("tenantId", "")
                .request(Method.POST, "/web/ticket/queryTicketDetail")
                .then()
                //或者景区门票不能为空
                // .body("message",equalTo("门票id不能为空"));
                .body("resultCode", equalTo(9002));

    }

    //输入正确的景区id
    @Test(testName = "门票-输入正确的景区id", description = "输入正确的景区id")
    public void ticket() {
        baseURI = suit.getBaseurl();
        given()
                .formParam("id", "692335480447959040")
                .formParam("tenantId", "")
                .request(Method.POST, "/web/ticket/queryTicketDetail")
                .then()
                //或者景区门票不能为空
                // .body("message",equalTo("门票id不能为空"));
                .body("resultCode", equalTo(8200));

    }

    //输入错误的景区id
    @Test(testName = "门票-输入错误的景区id", description = "输入错误的景区id")
    public void ticket_errors() {
        baseURI = suit.getBaseurl();
        given()
                .formParam("id", "692335480447959040")
                .formParam("tenantId", "")
                .request(Method.POST, "/web/ticket/queryTicketDetail")
                .then()
                //或者景区门票不能为空
                // .body("message",equalTo("门票id不能为空"));
                .body("resultCode", equalTo(8200));

    }

    //门票日历价格查询-未传入景区id
    @Test(testName = "门票日历价格查询-未传入景区id", description = "门票日历价格查询-未传入景区id")
    public void queryTicketDayPrice_null() {
        baseURI = suit.getBaseurl();
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

    //门票日历价格-输入景区id
    @Test(testName = "门票日历价格-输入景区id", description = "门票日历价格-输入景区id")
    public void queryTicketDayPrice() {
        baseURI = suit.getBaseurl();
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
    @Test(testName = "酒店列表为空", description = "酒店列表为空")
    public void hotel_null() {
        baseURI = suit.getBaseurl();
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
    @Test(testName = "酒店列表显示1条数据", description = "酒店列表显示1条数据")
    public void hotel_one() {
        baseURI = suit.getBaseurl();
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

    //酒店列表显示11条数据
    @Test(testName = "酒店列表显示11条数据", description = "酒店列表显示11条数据")
    public void hotel_ten() {
        baseURI = suit.getBaseurl();
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
        baseURI = suit.getBaseurl();
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
/*
    //酒店详情为空
    @Test(testName = "酒店", description = "酒店详情为空")
    public void hotel_queryById_null() {
        baseURI = suit.getBaseurl();
        given()
                .formParam("tenantId", "")
                .formParam("memberId", "")
                .formParam("hotelId", "")
                .formParam("stayNight", "")
                .formParam("beginDate", "")
                .formParam("endDate", "")
                .request(Method.POST, "")
                .then()
                .body("resultCode", equalTo("null"));
    }
*/
    /*
    //酒店详情-传入正确的酒店id
    @Test(testName = "酒店", description = "酒店详情-传入正确的酒店id")
    public void hotel_queryById() {
        baseURI = suit.getBaseurl();
        given()
                .formParam("tenantId", "")
                .formParam("memberId", "")
                .formParam("hotelId", "")
                .formParam("stayNight", "")
                .formParam("beginDate", "")
                .formParam("endDate", "")
                .request(Method.POST, "")
                .then()
                .body("resultCode", equalTo(8500));
    }
     */

    //酒店日态详情-房型id为空
    @Test(testName = "酒店日态详情为空", description = "酒店日态详情为空")
    public void queryBatchBySkuId_null() {
        baseURI = suit.getBaseurl();
        given()
                .formParam("tenantId", "")
                .formParam("beginDate", "")
                .formParam("endData", "")
                .formParam("skuId", "")
                .request(Method.POST, "/web/hotel/queryBatchBySkuId")
                .then()
                .body("resultCode", equalTo(8500));
    }

    //酒店日态详情传入skuid
    @Test(testName = "酒店日态详情传入skuid", description = "酒店日态详情传入skuid")
    public void queryBatchBySkuId() {
        baseURI = suit.getBaseurl();
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
    @Test(testName = "酒店日态详情传入任意id", description = "酒店日态详情传入任意id")
    public void queryBatchBySkuId_errors() {
        baseURI = suit.getBaseurl();
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
    @Test(testName = "攻略，tenantId为空", description = "分页查询攻略-tenantId为空")
    public void strategy_null() {
        baseURI = suit.getBaseurl();
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
    @Test(testName = "攻略，官方攻略列表查询成功", description = "分页查询攻略-官方攻略列表查询成功")
    public void strategy() {
        baseURI = suit.getBaseurl();
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
    @Test(testName = "攻略，列表显示15条数据", description = "分页查询攻略-列表显示15条数据")
    public void strategy_ten() {
        baseURI = suit.getBaseurl();
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
    @Test(testName = "力马快充,缴费记录为空", description = "力马快充，缴费记录为空")
    public void MemberOrderPage_null() {
        baseURI = suit.getBaseurl();
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
    @Test(testName = "缴费记录1条", description = "力马快充，缴费记录1条数据")
    public void MemberOrderPage_one() {
        baseURI = suit.getBaseurl();
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
    @Test(testName = "缴费记录10条", description = "力马快充，缴费记录10条数据")
    public void MemberOrderPage_ten() {
        baseURI = suit.getBaseurl();
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
    @Test(testName = "力马快充，缴费记录输入错误的menberid", description = "力马快充，缴费记录输入错误的menberid")
    public void MemberOrderPage_error() {
        baseURI = suit.getBaseurl();
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
    @Test(testName = "缴费记录-订单号为空", description = "力马快充，订单号为空")
    public void OrderCode_null() {
        baseURI = suit.getBaseurl();
        given()
                .formParam("orderCode", "")
                .request(Method.POST, "/web/serveOrder/queryDetailByOrderCode")
                .then()
                .body("message", equalTo("订单编号不能为空"));

    }

    //缴费记录-订单详情页-输入错误的订单号
    @Test(testName = "缴费记录-错误订单号", description = "力马快充，错误订单号")
    public void OrderCode_error() {
        baseURI = suit.getBaseurl();
        given()
                .formParam("orderCode", "1234567890")
                .request(Method.POST, "/web/serveOrder/queryDetailByOrderCode")
                .then()
                .body("message", equalTo("订单不存在"));

    }

    //缴费记录-订单详情页-正确的订单编号
    @Test(testName = "缴费记录-正确订单编号", description = "力马快充，正确订单编号")
    public void OrderCode() {
        baseURI = suit.getBaseurl();
        given()
                .formParam("orderCode", "SDM703919762966577153")
                .request(Method.POST, "/web/serveOrder/queryDetailByOrderCode")
                .then()
                .body("message", equalTo(""));

    }

    //话费-直接充值
    @Test(testName = "话费，不填手机号直接充值", description = "力马快充，话费直接充值")
    public void mobileOrder_null() {
        baseURI = suit.getBaseurl();
        given()
                .formParam("tenantId", "")
                .formParam("memberId", " ")
                .formParam("mobileNo", "")
                .formParam("rechargeAmount", "")
                .request(Method.POST, "/web/serveOrder/mobileOrder")
                .then()
                .body("message", equalTo("手机号不能为空"));

    }

    //话费-输入手机号充值，充值0元
    @Test(testName = "话费充值0元", description = "力马快充，输入手机号充值，充值0元")
    public void mobileOrder_tenyaun() {
        baseURI = suit.getBaseurl();
        given()
                .formParam("tenantId", "519142041838419968")
                .formParam("memberId", "725081578018963456")
                .formParam("mobileNo", "18727083743")
                .formParam("rechargeAmount", "0")
                .request(Method.POST, "/web/serveOrder/mobileOrder")
                .then()
                .body("message", equalTo(""));

    }

    //话费-输入手机号充值，充值0.01元
    @Test(testName = "话费充值0.01", description = "力马快充，输入手机号充值，充值0.01元")
    public void mobileOrder() {
        baseURI = suit.getBaseurl();
        given()
                .formParam("tenantId", "519142041838419968")
                .formParam("memberId", "725081578018963456")
                .formParam("mobileNo", "18727083743")
                .formParam("rechargeAmount", "0.01")
                .request(Method.POST, "/web/serveOrder/mobileOrder")
                .then()
                .body("message", equalTo(""));
    }


    //火车票~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    //火车票预订，正常下单
    @Test(testName = "火车票正常占座成功", description = "火车票正常占座成功")
    public void TrainTickets() {
        baseURI = suit.getBaseurl();
        given()
                .header("Content-Type","application/x-www-form-urlencoded;charset=UTF-8")
                .formParam("bookers", "[{\"ticketType\": 1,\"bookerName\": \"王云\",\"idcardType\": 0,\"idcardNo\": \"420621199503270621\",\"bookerPhone\": \"18727083743\",\"seatType\": 0}]")
                .formParam("contactName", "王云")
                .formParam("contactTel", "18727083743")
                .formParam("date", DateUtils.dateToStr(DateUtils.rollDay(DateUtils.getNow(),2),"yyyy-MM-dd"))
                .formParam("endTime", DateUtils.dateToStr(DateUtils.rollMinute(DateUtils.getNow(),90),"hh:mm"))
                .formParam("from", "杭州东")
                .formParam("memberId", "712708494783938560")
                .formParam("runTimeDays", "0")
                .formParam("runTimeHour", "0")
                .formParam("runTimeMinutes", "9")
                .formParam("startTime", "19:22")
                .formParam("tenantId", "519142041838419968")
                .formParam("to", "余杭")
                .formParam("trainNumber", "D3132")
                .request(Method.POST, "/web/trainlineOrder/bookTrainTickets")
                .then()
                //断言resultCode=7115
                //.body("resultCode",equalTo("7115"));
                .body("message", equalTo("成功"));

    }
    //火车票预订，购买当前过去的日期
    @Test(testName = "火车票，购买当前过去的日期", description = "火车票，购买当前过去的日期")
    public void TrainTickets_after() {
        baseURI = suit.getBaseurl();
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
    @Test(testName = "火车票，购票人信息为空下单", description = "火车票，购票人信息为空下单")
    public void TrainTickets_null() {
        baseURI = suit.getBaseurl();
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


    //火车票预订，单独购买一张儿童票
    @Test(testName = "火车票，单独购买一张儿童票", description = "火车票，单独购买一张儿童票")
    public void TrainTickets_child() {
        baseURI = suit.getBaseurl();
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
    //智能导览
    @Test(testName = "智能导览详情", description = "智能导览")
    public void getAround() {
        baseURI = suit.getBaseurl();
        given()
                .formParam("tenantId", "111")
                .formParam("type", "111")
                .formParam("id", "11")
                .formParam("pageSize", "1")
                .formParam("currentPage", "1")
                .formParam("count", "1")
                .formParam("loading", "1")
                .request(Method.POST, "/web/selfcenter/myCollectionList")
                .then()
                .body("list",hasSize(0))
                .body("resultCode", equalTo(8200));
    }
    //收藏~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    //收藏列表为空
    @Test(testName = "收藏", description = "收藏列表为空")
    public void myCollectionlist_null() {
        baseURI = suit.getBaseurl();
        given()
                .formParam("tenantId", "111")
                .formParam("type", "111")
                .formParam("id", "11")
                .formParam("pageSize", "1")
                .formParam("currentPage", "1")
                .formParam("count", "1")
                .formParam("loading", "1")
                .request(Method.POST, "/web/selfcenter/myCollectionList")
                .then()
                .body("list",hasSize(0))
                .body("resultCode", equalTo(8200));
    }
    //不传入id收藏
    @Test(testName = "收藏", description = "收藏_不传入id收藏")
    public void Collection_id() {
        baseURI = suit.getBaseurl();
        given()
                .formParam("tenantId", "519142041838419968")
                .formParam("id", "")
                .formParam("contentId", "")
                .formParam("type", "2")
                .request(Method.POST, "/web/selfcenter/collect")
                .then()
                .body("message", equalTo("微信会员未授权"));
    }
    /*
    //收藏
    @Test(testName = "收藏", description = "收藏")
    public void Collection() {
        baseURI = suit.getBaseurl();
        given()
                .formParam("tenantId", "519142041838419968")
                .formParam("id", "Collectionid")
                .formParam("contentId", "")
                .formParam("type", "2")
                .request(Method.POST, "/web/selfcenter/collect")
                .then()
                .body("message", equalTo("成功"));

    }
    //收藏列表
    @Test(testName = "收藏", description = "收藏列表" ,dependsOnMethods = "Collection")
    public void myCollectionlist() {
        baseURI = suit.getBaseurl();
        given()
                .formParam("tenantId", "519142041838419968")
                .formParam("type", "2")
                .formParam("id", "Collectionid")
                .formParam("pageSize", "10")
                .formParam("currentPage", "2")
                .formParam("count", "0")
                .formParam("loading", "true")
                .request(Method.POST, "/web/selfcenter/myCollectionList")
                .then()
                .body("message", equalTo(""));
    }

    //取消收藏
    @Test(testName = "取消收藏", description = "取消收藏")
    public void canceCollection() {
        baseURI = suit.getBaseurl();
        given()
                .formParam("tenantId", "519142041838419968")
                .formParam("id", "Collectionid")
                .formParam("contentId", "692326891465474048")
                .formParam("type", "2")
                .request(Method.POST, "/web/selfcenter/cancelCollect")
                .then()
                .body("message", equalTo("成功"));
    }
    //取消收藏列表
    @Test(testName = "收藏列表", description = "收藏列表" ,dependsOnMethods = "canceCollection")
    public void cancemyCollectionlist() {
        baseURI = suit.getBaseurl();
        given()
                .formParam("tenantId", "519142041838419968")
                .formParam("type", "2")
                .formParam("id", "Collectionid")
                .formParam("pageSize", "10")
                .formParam("currentPage", "2")
                .formParam("count", "0")
                .formParam("loading", "true")
                .request(Method.POST, "/web/selfcenter/myCollectionList")
                .then()
                .body("message", equalTo(""));

    }
     */

    @Test(testName = "收藏、取消、查看列表", description = "收藏、取消、查看列表")
    public void Collection1() {
        try {
            String tem = "";
            TestCase testCase = caseList.get(0);

            ResultSet resultSet = JdbcUtils.getResult(testCase.getDBsql().getSqlList().get(1), testCase.getDBsql().getJdbc());
            baseURI = suit.getBaseurl();
            Response response =
                    given()
                            .formParam("tenantId", "519142041838419968")
                            .formParam("id", "Collectionid")
                            .formParam("contentId", "")
                            .formParam("type", "2")
                            .request(Method.POST, "/web/selfcenter/collect")
                            .then()
                            .body("message", equalTo("成功"))
                            .extract()
                            .response();

        } finally {
        }
        Response response1 =
                given()
                        .formParam("tenantId", "519142041838419968")
                        .formParam("type", "2")
                        .formParam("id", "Collectionid")
                        .formParam("pageSize", "10")
                        .formParam("currentPage", "2")
                        .formParam("count", "0")
                        .formParam("loading", "true")
                        .request(Method.POST, "/web/selfcenter/myCollectionList")
                        .then()
                        .body("message", equalTo(""))
                        .extract()
                        .response();
        Response response2 =
                given()
                        .formParam("tenantId", "519142041838419968")
                        .formParam("id", "Collectionid")
                        .formParam("contentId", "692326891465474048")
                        .formParam("type", "2")
                        .request(Method.POST, "/web/selfcenter/cancelCollect")
                        .then()
                        .body("message", equalTo("成功"))
                        .extract()
                        .response();
        Response response3 =
                given()
                        .formParam("tenantId", "519142041838419968")
                        .formParam("type", "2")
                        .formParam("id", "Collectionid")
                        .formParam("pageSize", "10")
                        .formParam("currentPage", "2")
                        .formParam("count", "0")
                        .formParam("loading", "true")
                        .request(Method.POST, "/web/selfcenter/myCollectionList")
                        .then()
                        .body("message", equalTo(""))
                        .extract()
                        .response();
    }


    //意见反馈~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    //意见反馈
    /*
    @Test(testName = "意见反馈-新增", description = "新增、撤销、查看" )
        public void suggestions_add() {
            baseURI = suit.getBaseurl();
            given()
                    .formParam("tenantId", "519142041838419968")
                    .formParam("publishId", "publishid")
                    .formParam("contactName", "王云")
                    .formParam("phoneNum", "18727083743")
                    .formParam("type", "2")
                    .formParam("count", "0")
                    .formParam("context","新增意见")
                    .request(Method.POST, "/web/complaint/add")
                    .then()
                    .body("resultCode", equalTo("8200"));

        }
    @Test(testName = "意见反馈-撤销", description = "新增、撤销、查看" ,dependsOnMethods = "suggestions_add")
         public void suggestions_updata() {
            baseURI = suit.getBaseurl();
            given()
                    .formParam("tenantId", "519142041838419968")
                    .formParam("publishId", "publishid")
                    .request(Method.POST, "/web/complaint/updateCancel")
                    .then()
                    .body("message", equalTo(""));

    }
    @Test(testName = "意见反馈-查看", description = "新增、撤销、查看" )
    public void queryByConditions() {
        baseURI = suit.getBaseurl();
        given()
                .formParam("tenantId", "519142041838419968")
                .formParam("memberId", "")
                .formParam("pageSize", "王云")
                .formParam("currentPage", "18727083743")
                .formParam("count", "1")
                .formParam("loading","true")
                .formParam("finished","true")
                .request(Method.POST, "/web/complaint/add")
                .then()
                .body("resultCode", equalTo("8200"));

    }


     */

    @AfterClass
    public void clean(){
        JdbcUtils.closeConn();
        String tem = "";
        TestCase testCase = caseList.get(0);
        ResultSet resultSet = JdbcUtils.getResult(testCase.getDBsql().getSqlList().get(2), testCase.getDBsql().getJdbc());
        ResultSet resultSet1 = JdbcUtils.getResult(testCase.getDBsql().getSqlList().get(3), testCase.getDBsql().getJdbc());
    }


}
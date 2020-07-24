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
import pojo.JsonData;
import pojo.LimaTrainline;
import pojo.TestCase;
import pojo.TestSuit;
import redis.clients.jedis.Jedis;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Epic("力马优选")
@Feature("接口名称")
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

    //门票主体id
    private String bodyId;

    //门票产品id
    private  String productId;

    //门票规格id
    private  String skuId;

    List<Cookie> cookies;
    ArrayList<TestCase> caseList;
    Map<String,Object> params;

    @BeforeClass
    public void preConditions() {
        String path = "TestCaseData/limayouxuan.json";
        String jsondata = IOUtils.readFiletoString(path, "utf-8");
        suit = JsonUtils.parseJsonData(jsondata, TestSuit.class);
        caseList = suit.getCaseList();

        params = suit.getParams();
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
                    .formParam("tenantId", params.get("tenantId"))
                    .formParam("phone", params.get("phone"))
                    .request(Method.POST, "/web/wechat/sendCode")
                    .then();
        }
            // redis 的key格式是 OTS_3.1.0_DEV_PREFIX_+tenantId+_phone
            String codeMessage = jedis.get("OTS_CLOUD_LIMAYOUXUAN_TEST_PREFIX_519142041838419968_18727083743");
            code = codeMessage.substring(codeMessage.length() - 6);
        given()
                .formParam("tenantId", params.get("tenantId"))
                .formParam("phone", params.get("phone"))
                .formParam("captcha", code)
                .formParam("openId", "")
                .formParam("type", "")
                .request(Method.POST, "/web/wechat/login")
                .then()
                .body("message", equalTo(""))
                .body("resultCode", equalTo(8200));
        }

    //登录时租户id为空
    @Test(testName = "租户id为空", description = "租户id为空")
    public void login_error() {
        baseURI = suit.getBaseurl();
        given()
                .formParam("tenantId", "")
                .formParam("phone", params.get("phone"))
                .formParam("captcha", "12121")
                .formParam("openId", "")
                .formParam("type", "")
                .request(Method.POST, "/web/wechat/login")
                .then()
                .body("message", equalTo("租户ID不能为空"))
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
                        .formParam("tenantId", params.get("tenantId"))
                        .formParam("phone", params.get("phone"))
                        .request(Method.POST, "/web/wechat/sendCode")
                        .then();
            }
            // redis 的key格式是 OTS_3.1.0_DEV_PREFIX_+tenantId+_phone
            String codeMessage = jedis.get("OTS_CLOUD_LIMAYOUXUAN_TEST_PREFIX_519142041838419968_18727083743");
            code = codeMessage.substring(codeMessage.length() - 6);

            Response response =
                    given()
                            .contentType("application/x-www-form-urlencoded;charset=UTF-8")
                            .formParam("tenantId", params.get("tenantId"))
                            .formParam("phone", params.get("phone"))
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
                            .formParam("tenantId", params.get("tenantId"))
                            .formParam("phone", params.get("phone"))
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
                .formParam("tenantId", params.get("tenantId"))
                .formParam("cityCode", "330100000000")
                .formParam("provinceCode", "330100000000")
                .request(Method.POST, "/web/wechat/getHomepage")
                .then()
                .body("message", equalTo(""))
                .body("resultCode", equalTo(8200));
    }

    //首页传入tanantid、cityCode，任意输入
    @Test(testName = "首页-传入tanantid、cityCode", description = "首页-传入tanantid、cityCode")
    public void Homepage_error() {
        baseURI = suit.getBaseurl();
        given()
                .formParam("tenantId", "14441423434344")
                .formParam("cityCode", "43143413")
                .formParam("provinceCode", "330100000000")
                .request(Method.POST, "/web/wechat/getHomepage")
                .then()
                .body("message", equalTo(""))
                .body("resultCode", equalTo(8200));
    }


    //广告位~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
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
                .formParam("tenantId", params.get("tenantId"))
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

    //门票~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
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
                .formParam("tenantId", params.get("tenantId"))
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
                .formParam("tenantId", params.get("tenantId"))
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
    @Test(testName = "景区scenicId为空", description = "景区scenicId为空")
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
                .body("message", equalTo("景区id不能为空"))
                .body("resultCode", equalTo(9001));

    }

    //景区详情传入景区scenicId，tenantId/memberId可传可不传
    @Test(testName = "景区详情-传入正确的景区scenicId", description = "景区详情传入正确的景区scenicId")
    public void ScenicDetailById() {
        baseURI = suit.getBaseurl();
        given()
                .formParam("tenantId", params.get("tenantId"))
                .formParam("memberId", params.get("memberId"))
                .formParam("scenicId", "692326891465474048")
                .formParam("currentPage", "")
                .formParam("pageSize", "")
                .request(Method.POST, "/web/ticket/queryScenicDetailById")
                .then()
                .body("resultCode", equalTo(8200));

    }

    //输入不存在的景区scenicId（任意输入英文+数字）
    @Test(testName = "输入不存在的景区scenicId（任意输入英文+数字）", description = "输入不存在的景区scenicId（任意输入英文+数字）")
    public void ScenicDetailById_errors() {
        baseURI = suit.getBaseurl();
        given()
                .formParam("tenantId", params.get("tenantId"))
                .formParam("memberId", params.get("memberId"))
                .formParam("scenicId", "mp6923268914654740481测试")
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
                .body("message",equalTo("门票id不能为空"))
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
                .body("resultCode", equalTo(8200));

    }

    //输入不存在的景区id（任意输入英文+数字）
    @Test(testName = "输入不存在的景区id（任意输入英文+数字）", description = "输入不存在的景区id（任意输入英文+数字）")
    public void ticket_errors() {
        baseURI = suit.getBaseurl();
        given()
                .formParam("id", "mp692335480447959040")
                .formParam("tenantId", "")
                .request(Method.POST, "/web/ticket/queryTicketDetail")
                .then()
                .body("resultCode", equalTo(8200));

    }

    //门票日历价格查询-门票id为空
    @Test(testName = "门票日历价格查询-门票id为空", description = "门票日历价格查询-门票id为空")
    public void queryTicketDayPrice_null() {
        baseURI = suit.getBaseurl();
        given()
                .formParam("id", "")
                .formParam("startDate", "")
                .formParam("endDate", "")
                .request(Method.POST, "/web/ticket/queryTicketDayPrice")
                .then()
                .body("resultCode", equalTo(9002));

    }

    //门票日历价格-输入正确的门票id
    @Test(testName = "门票日历价格-输入正确的门票id", description = "门票日历价格-输入正确的门票id")
    public void queryTicketDayPrice() {
        baseURI = suit.getBaseurl();
        given()
                .formParam("id", "692332412155199488")
                .formParam("startDate", "2020-06-22")
                .formParam("endDate", "2020-06-22")
                .request(Method.POST, "/web/ticket/queryTicketDayPrice")
                .then()
                .body("resultCode", equalTo(8200));

    }
    //门票日历价格-输入不存在的门票id
    @Test(testName = "门票日历价格-输入不存在的门票id", description = "门票日历价格-输入不存在的门票id")
    public void queryTicketDayPrice_error() {
        baseURI = suit.getBaseurl();
        given()
                .formParam("id", "mp692332412155199488")
                .formParam("startDate", "2020-06-22")
                .formParam("endDate", "2020-06-22")
                .request(Method.POST, "/web/ticket/queryTicketDayPrice")
                .then()
                .body("resultCode", equalTo(8200));

    }

    //门票下单，数据为空时下单
    @Test(testName = "门票下单时数据为空", description = "门票下单时数据为空")
    public void order_null() {
        baseURI = suit.getBaseurl();
        given()
                .formParam("tenantId", "")
                .formParam("bodyId", "")
                .formParam("memberId", "")
                .formParam("orderType", "")
                .formParam("orderUserParamVoList", "")
                .formParam("productId", "")
                .formParam("skuId", "")
                .formParam("totalQuantity", "")
                .formParam("userDateStart", "")
                .formParam("userDateEnd", "")
                .formParam("couponCode", "")
                .request(Method.POST, "/web/order/order")
                .then()
                .body("resultCode", equalTo(8500));
                //.body("message", equalTo("产品id不能为空"));
                //.body("message", equalTo("主体id不能为空"))
                //.body("message", equalTo("使用结束日期不能为空"))
                //.body("message", equalTo("租户id不能为空"))
                //.body("message", equalTo("用户id不能为空"));

    }
    /*
    //门票下单，门票正常下单
    @Test(testName = "门票正常下单", description = "门票正常下单")
    public void order() {
        baseURI = suit.getBaseurl();
        given()
                .formParam("tenantId","519142041838419968")
                .formParam("bodyId", "692326942245912576")
                .formParam("memberId", params.get("memberId"))
                .formParam("orderType", "1")
                .formParam("orderUserParamVoList", "[{\"isLiaison\":1,\"orderUserName\":\"\",\"orderUserPhone\":\"18727083743\"},{\"isLiaison\":0,\"orderUserName\":\"云\",\"enName\":\"\",\"orderUserPhone\":\""+params.get("phone")+"\",\"cardCode\":\"\",\"email\":\"\"}]")
                .formParam("productId", "692332299005460480")
                .formParam("skuId", "skuId")
                .formParam("totalQuantity", "1")
                .formParam("userDateStart", "2020-07-31")
                .formParam("userDateEnd", "2020-07-31")
                .formParam("couponCode", "")
                .request(Method.POST, "/web/order/order")
                .then()
                .body("resultCode", equalTo(8200));
                //.body("message", equalTo("总数量不能为空"));
    }
*/

    //酒店~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
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
    //酒店列表显示11条数据
    @Test(testName = "酒店列表传入过去日期", description = "酒店列表显示11条数据")
    public void hotel_errordate() {
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

    //酒店详情为空-酒店id为空
    @Test(testName = "酒店详情页-酒店id为空", description = "酒店详情页-酒店id为空")
    public void hotel_queryById_null() {
        baseURI = suit.getBaseurl();
        given()
                .formParam("tenantId", "")
                .formParam("memberId", "")
                .formParam("hotelId", "")
                .formParam("stayNight", "")
                .formParam("beginDate", "")
                .formParam("endDate", "")
                .request(Method.POST, "/web/hotel/queryById")
                .then()
                .body("resultCode", equalTo(8500));
    }

    //酒店详情-正确的酒店id
    @Test(testName = "酒店详情页-正确的酒店id", description = "酒店详情页-正确的酒店id")
    public void hotel_queryById() {
        baseURI = suit.getBaseurl();
        given()
                .formParam("tenantId", params.get("tenantId"))
                .formParam("memberId", params.get("memberId"))
                .formParam("hotelId", "700746662380830720")
                .formParam("stayNight", "1")
                .formParam("beginDate", "2020-7-22")
                .formParam("endDate", "2020-7-23")
                .request(Method.POST, "/web/hotel/queryById")
                .then()
                .body("resultCode", equalTo(8200));
    }

    //酒店详情-正确的酒店id
    @Test(testName = "酒店详情页-输入任意id", description = "酒店详情页-输入任意id")
    public void hotel_queryByidr_random() {
        baseURI = suit.getBaseurl();
        given()
                .formParam("tenantId", params.get("tenantId"))
                .formParam("memberId", params.get("memberId"))
                .formParam("hotelId", "11")
                .formParam("stayNight", "1")
                .formParam("beginDate", "2020-7-22")
                .formParam("endDate", "2020-7-23")
                .request(Method.POST, "/web/hotel/queryById")
                .then()
                .body("resultCode", equalTo(8500));
    }

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
                .formParam("tenantId", params.get("tenantId"))
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
                .formParam("tenantId", params.get("tenantId"))
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
                .formParam("tenantId", params.get("tenantId"))
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

    //分页查询攻略-列表显示11条数据
    @Test(testName = "攻略，列表显示11条数据", description = "分页查询攻略-列表显示11条数据")
    public void strategy_ten() {
        baseURI = suit.getBaseurl();
        given()
                .formParam("tenantId", params.get("tenantId"))
                .formParam("pageSize", "11")
                .formParam("currentPage", "1")
                .formParam("count", "0")
                .formParam("loading", " ")
                .formParam("scenicspotIds", " ")
                .formParam("days", " ")
                .formParam("title", " ")
                .request(Method.POST, "/web/office/queryPage")
                .then()
                .body("list", hasSize(11));

    }
    //官方攻略详情
    @Test(testName = "攻略详情id为空", description = "分页查询攻略-列表显示11条数据")
    public void queryById_null() {
        baseURI = suit.getBaseurl();
        given()
                .formParam("tenantId", "")
                .formParam("id", "")
                .request(Method.POST, "/web/office/queryById")
                .then()
                .body("message", equalTo("官方攻略id不能为空"));

    }
    //官方攻略详情-输入不存在的id
    @Test(testName = "攻略详情输入不存在id", description = "攻略详情输入不存在id")
    public void queryById_no() {
        baseURI = suit.getBaseurl();
        given()
                .formParam("tenantId", "")
                .formParam("id", "1111")
                .request(Method.POST, "/web/office/queryById")
                .then()
                .body("message", equalTo("根据id查询官方攻略详情成功"));

    }

    //力马快充~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    //缴费记录为空
    @Test(testName = "力马快充,缴费记录为空", description = "力马快充，缴费记录为空")
    public void MemberOrderPage_null() {
        baseURI = suit.getBaseurl();
        given()
                    /*
                .formParam("tenantId", params.get("tenantId"))
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
                .formParam("tenantId", params.get("tenantId"))
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
                .formParam("tenantId", params.get("tenantId"))
                .formParam("pageSize", "10")
                .formParam("currentPage", "1")
                .formParam("count", "0")
                .formParam("loading", "")
                .formParam("memberId", params.get("memberId"))
                .request(Method.POST, "/web/serveOrder/queryMemberOrderPage")
                .then()
                .body("message", equalTo(""));

    }

    //缴费记录-缴费记录输入错误的menberid
    @Test(testName = "力马快充，缴费记录输入错误的menberid", description = "力马快充，缴费记录输入错误的menberid")
    public void MemberOrderPage_error() {
        baseURI = suit.getBaseurl();
        given()
                .formParam("tenantId", params.get("tenantId"))
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
    @Test(testName = "缴费记录-输入错误订单号", description = "力马快充，输入错误订单号")
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
    @Test(testName = "话费，不输入手机号直接充值", description = "力马快充，话费直接充值")
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
    @Test(testName = "话费充值1元", description = "力马快充，输入手机号充值，充值1元")
    public void mobileOrder_tenyaun() {
        baseURI = suit.getBaseurl();
        given()
                .formParam("tenantId", params.get("tenantId"))
                .formParam("memberId", params.get("memberId"))
                .formParam("mobileNo", "18727083743")
                .formParam("rechargeAmount", "1")
                .request(Method.POST, "/web/serveOrder/mobileOrder")
                .then()
                .body("message", equalTo(""));

    }

    //话费-输入不存在的手机号充值
    @Test(testName = "话费-输入不存在的手机号充值", description = "话费-输入不存在的手机号充值")
    public void mobileOrder() {
        baseURI = suit.getBaseurl();
        given()
                .formParam("tenantId", params.get("tenantId"))
                .formParam("memberId", params.get("memberId"))
                .formParam("mobileNo", "12313213")
                .formParam("rechargeAmount", "0.01")
                .request(Method.POST, "/web/serveOrder/mobileOrder")
                .then()
                .body("message", equalTo(""))
                .body("resultCode",equalTo(8200));
    }

    //加油卡，列表为空
    @Test(testName = "加油卡商品列表为空", description = "力马快充，加油卡列表为空")
    public void getGasList_null() {
        baseURI = suit.getBaseurl();
        given()
                .formParam("tenantId", "")
                .formParam("type", "")
                .request(Method.POST, "/web/serveOrder/getGasList")
                .then()
                .body("message", equalTo("加油卡类型不能为空"))
                .body("resultCode", equalTo(8500));

    }
    //加油卡，商品列表正常查询成功
    @Test(testName = "商品列表正常查询成功", description = "力马快充，加油卡列表正常显示")
    public void getGasList() {
        baseURI = suit.getBaseurl();
        given()
                .formParam("tenantId", params.get("tenantId"))
                .formParam("type", "2")
                .request(Method.POST, "/web/serveOrder/getGasList")
                .then()
                .body("message", equalTo("查询加油卡类标准商品列表查询成功"))
                .body("resultCode", equalTo(8200));

    }
    //加油卡，输入不存在的类型（type=20）
    @Test(testName = "输入不存在的类型（type=20）", description = "输入不存在的类型（type=20）")
    public void getGasList_no() {
        baseURI = suit.getBaseurl();
        given()
                .formParam("tenantId", params.get("tenantId"))
                .formParam("type", "20")
                .request(Method.POST, "/web/serveOrder/getGasList")
                .then()
                .body("message", equalTo("查询加油卡类标准商品列表查询成功"))
                .body("resultCode", equalTo(8200));
    }

    //加油卡订单接口，列表为空时立即充值
    @Test(testName = "加油卡订单接口，必填项为空时立即充值", description = "加油卡订单接口，列表为空时立即充值")
    public void gasCardOrder_null() {
        baseURI = suit.getBaseurl();
        given()
                .formParam("tenantId", "")
                .formParam("gasCardNo", "")
                .formParam("gasCardName", "")
                .formParam("gasCardTel", "")
                .formParam("isStock", "")
                .formParam("itemName", "")
                .formParam("rechargAmount", "")
                .formParam("memberId", "")
                .formParam("type", "")
                .request(Method.POST, "/web/serveOrder/gasCardOrder")
                .then()
                .body("message", equalTo("手机号不能为空"))
                .body("resultCode", equalTo(8500));

    }

    /*
    //加油卡订单接口，列表为空时立即充值
    @Test(testName = "加油卡订单接口，正常充值", description = "加油卡订单接口，正常充值")
    public void gasCardOrder() {
        baseURI = suit.getBaseurl();
        given()
                .formParam("tenantId", params.get("tenantId"))
                .formParam("gasCardNo", "")
                .formParam("gasCardName", "")
                .formParam("gasCardTel", params.get(""))
                .formParam("isStock", "")
                .formParam("itemName", "")
                .formParam("rechargAmount", "")
                .formParam("memberId", params.get("memberId"))
                .formParam("type", "1")
                .request(Method.POST, "/web/serveOrder/gasCardOrder")
                .then()
                .body("message", equalTo("手机号不能为空"))
                .body("resultCode", equalTo(8500));
    }
     */
    //加油卡订单接口，输入不存在的卡号和名称
    @Test(testName = "加油卡订单接口，输入不存在的卡号和名称", description = "加油卡订单接口，输入不存在的卡号和名称")
    public void gasCardOrder() {
        baseURI = suit.getBaseurl();
        given()
                .formParam("tenantId", params.get("tenantId"))
                .formParam("gasCardNo", "111")
                .formParam("gasCardName", "111")
                .formParam("gasCardTel", "1111")
                .formParam("isStock", "1")
                .formParam("itemName", "yun")
                .formParam("rechargAmount", "10")
                .formParam("memberId", params.get("memberId"))
                .formParam("type", "1")
                .request(Method.POST, "/web/serveOrder/gasCardOrder")
                .then()
                .body("message", equalTo("充值金额不能为空"))
                .body("resultCode", equalTo(8500));

    }

    //水电煤接口，商品必填项全部不填写
    @Test(testName = "水电煤，必填项为空", description = "水电煤接口，商品必填项全部不填写")
    public void serveOrdergetItemList_null() {
        baseURI = suit.getBaseurl();
        given()
                /*
                .formParam("tenantId", "")
                .formParam("currentPage", "")
                .formParam("pageSize", "")
                .formParam("province", "")
                .formParam("projectId", "")
                .formParam("city", "")
                 */
                .request(Method.POST, "/web/serveOrder/getItemList")
                .then()
                .body("message", equalTo("查询水电煤类标准商品列表查询成功"))
                .body("resultCode", equalTo(8200));

    }

    //水电煤接口，商品列表正常显示
    @Test(testName = "水电煤，商品列表正常显示", description = "水电煤接口，商品必填项全部不填写")
    public void serveOrdergetItemList() {
        baseURI = suit.getBaseurl();
        given()
                .formParam("tenantId", params.get("tenantId"))
                .formParam("currentPage", "1")
                .formParam("pageSize", "100")
                .formParam("province", "浙江")
                .formParam("projectId", "1")
                .formParam("city", "杭州")
                .request(Method.POST, "/web/serveOrder/getItemList")
                .then()
                .body("message", equalTo("查询水电煤类标准商品列表查询成功"))
                .body("resultCode", equalTo(8200));

    }

    //水电煤接口，输入不存在的省份(默认查询成功，但无省份数据)
    @Test(testName = "水电煤，输入不存在的省份", description = "输入不存在的省份")
    public void serveOrdergetItemList_no() {
        baseURI = suit.getBaseurl();
        given()
                .formParam("tenantId", params.get("tenantId"))
                .formParam("currentPage", "1")
                .formParam("pageSize", "100")
                .formParam("province", "1")
                .formParam("projectId", "1")
                .formParam("city", "1")
                .request(Method.POST, "/web/serveOrder/getItemList")
                .then()
                .body("message", equalTo("查询水电煤类标准商品列表查询成功"))
                .body("resultCode", equalTo(8200));

    }

    //水电煤订单接口，
    @Test(testName = "水电煤，订单必填项为空时立即充值", description = "水电煤，订单必填项为空时立即充值")
    public void waterOrder_null() {
        baseURI = suit.getBaseurl();
        given()
                .formParam("tenantId", "")
                .formParam("memberId", "")
                .formParam("city", "")
                .formParam("projectId", "")
                .formParam("province", "")
                .formParam("rechargeAccount", "")
                .formParam("itemId", "")
                .formParam("itemName", "")
                .formParam("type", "")
                .formParam("rechargeAmount", "")
                .request(Method.POST, "/web/serveOrder/waterOrder")
                .then()
                .body("message", equalTo("商品名称不能为空"))
                .body("resultCode", equalTo(8500));

    }

    //水电煤订单接口，输入不存在的租户id
    @Test(testName = "水电煤，输入不存在的租户id", description = "水电煤，订单必填项为空时立即充值")
    public void waterOrder_notenantid() {
        baseURI = suit.getBaseurl();
        given()
                .formParam("tenantId", "1")
                .formParam("memberId", "1")
                .formParam("city", "1")
                .formParam("projectId", "1")
                .formParam("province", "1")
                .formParam("rechargeAccount", "1")
                .formParam("itemId", "1")
                .formParam("itemName", "1")
                .formParam("type", "1")
                .formParam("rechargeAmount", "1")
                .request(Method.POST, "/web/serveOrder/waterOrder")
                .then()
                .body("message", equalTo("当前租户没有分销商信息"))
                .body("resultCode", equalTo(8500));

    }
    //水电煤订单接口，输入存在的租户id
    @Test(testName = "水电煤，输入存在的租户id", description = "水电煤，订单必填项为空时立即充值")
    public void waterOrder_id() {
        baseURI = suit.getBaseurl();
        given()
                .formParam("tenantId", params.get("tenantId"))
                .formParam("memberId", params.get("memberId"))
                .formParam("city", "1")
                .formParam("projectId", "1")
                .formParam("province", "1")
                .formParam("rechargeAccount", "1")
                .formParam("itemId", "1")
                .formParam("itemName", "1")
                .formParam("type", "1")
                .formParam("rechargeAmount", "1")
                .request(Method.POST, "/web/serveOrder/waterOrder")
                .then()
                .body("message", equalTo(""))
                .body("resultCode", equalTo(8200));

    }

    //火车票~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
       /*
    //火车票预订，正常下单
           //.formParam("date", DateUtils.dateToStr(DateUtils.rollDay(DateUtils.getNow(),2),"yyyy-MM-dd"))
            //.formParam("endTime", DateUtils.dateToStr(DateUtils.rollMinute(DateUtils.getNow(),90),"hh:mm"))
    @Test(testName = "火车票正常占座成功", description = "火车票正常占座成功")
    public void TrainTickets() {
        baseURI = suit.getBaseurl();
        given()
                .header("Content-Type","application/x-www-form-urlencoded;charset=UTF-8")
                .formParam("bookers", "[{\"ticketType\": 1,\"bookerName\": \"王云\",\"idcardType\": 0,\"idcardNo\": \""+params.get("idcardNo")+"\",\"bookerPhone\": \""+params.get("phone")+"\",\"seatType\": 0}]")
                .formParam("contactName", "王云")
                .formParam("contactTel", "18727083743")
                .formParam("date", DateUtils.dateToStr(DateUtils.rollDay(DateUtils.getNow(),2),"yyyy-MM-dd"))
                .formParam("endTime", "19:31")
                .formParam("from", "杭州东")
                .formParam("memberId", params.get("memberId"))
                .formParam("runTimeDays", "0")
                .formParam("runTimeHour", "0")
                .formParam("runTimeMinutes", "9")
                .formParam("startTime", "19:22")
                .formParam("tenantId", params.get("tenantId"))
                .formParam("to", "余杭")
                .formParam("trainNumber", "D3132")
                .request(Method.POST, "/web/trainlineOrder/bookTrainTickets")
                .then()
                //断言resultCode=7115
                //.body("resultCode",equalTo("7115"));
                .body("message", equalTo("成功"));

    }


    @Test(testName = "火车票预订多张票", description = "火车票预订多张票",dataProviderClass = DataProviders.class,dataProvider = "LimaTrainline")
    public void TrainTickets1(Object object) {
        baseURI = suit.getBaseurl();
        RequestSpecification requestSpecification = given()
                .header("Content-Type","application/x-www-form-urlencoded;charset=UTF-8")
                .formParam("bookers", ((LimaTrainline)object).getBookers())
                .formParam("contactName", ((LimaTrainline)object).getContactName())
                .formParam("contactTel", ((LimaTrainline)object).getContactTel())
                .formParam("date", ((LimaTrainline)object).getDate())
                .formParam("endTime", ((LimaTrainline)object).getEndTime())
                .formParam("from", ((LimaTrainline)object).getFrom())
                .formParam("memberId", ((LimaTrainline)object).getMemberId())
                .formParam("runTimeDays",((LimaTrainline)object).getRunTimeDays())
                .formParam("runTimeHour", ((LimaTrainline)object).getRunTimeHour())
                .formParam("runTimeMinutes", ((LimaTrainline)object).getRunTimeMinutes())
                .formParam("startTime",((LimaTrainline)object).getStartTime() )
                .formParam("tenantId",((LimaTrainline)object).getTenantId() )
                .formParam("to", ((LimaTrainline)object).getTo())
                .formParam("trainNumber", ((LimaTrainline)object).getTrainNumber());
        requestSpecification
                .request(Method.POST, "/web/trainlineOrder/bookTrainTickets")
                .then()
                .body("message", equalTo("成功"));

    }
        */

    //火车票预订，购买当前过去的日期
    @Test(testName = "火车票，购买当前过去的日期", description = "火车票，购买当前过去的日期")
    public void TrainTickets_after() {
        baseURI = suit.getBaseurl();
        given()
                .formParam("bookers", "[{\"ticketType\": 1,\"bookerName\": \"王云\",\"idcardType\": 0,\"idcardNo\": \""+params.get("idcardNo")+"\",\"bookerPhone\": \""+params.get("phone")+"\",\"seatType\": 0}]")
                .formParam("contactName", "云")
                .formParam("contactTel", "18727083743")
                .formParam("date", "2020-06-01")
                .formParam("endTime", "15:50")
                .formParam("from", "汉口")
                .formParam("memberId", params.get("memberId"))
                .formParam("runTimeDays", "1")
                .formParam("runTimeHour", "14")
                .formParam("runTimeMinutes", "850")
                .formParam("startTime", "11:09")
                .formParam("tenantId", params.get("tenantId"))
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
                .formParam("bookers", "[{\"bookerName\":\"王云\",\"idcardType\":0,\"idcardNo\":\""+params.get("idcardNo")+"\",\"bookerPhone\":\""+params.get("phone")+"\",\"ticketType\":2,\"typeName\":\"成人票\",\"isNameError\":false,\"isIDError\":false,\"isPhoneError\":false,\"seatType\":\"5\"}]")
                .formParam("contactName", "云")
                .formParam("contactTel", "18727083743")
                .formParam("date", "2020-07-21")
                .formParam("endTime", "07:20")
                .formParam("from", "太原")
                .formParam("memberId", params.get("memberId"))
                .formParam("runTimeDays", "0")
                .formParam("runTimeHour", "0")
                .formParam("runTimeMinutes", "11")
                .formParam("startTime", "07:09")
                .formParam("tenantId", params.get("tenantId"))
                .formParam("to", "太原南")
                .formParam("trainNumber", "4611")
                .request(Method.POST, "/web/trainlineOrder/bookTrainTickets")
                .then()
                //断言resultCode=7115
                //.body("resultCode",equalTo("7115"));
                .body("message", equalTo("出票失败"));



    }
    //智能导览
    @Test(testName = "参数为空时默认获取周边位置", description = "智能导览")
    public void getAround() {
        baseURI = suit.getBaseurl();
        given()
                .formParam("tenantId", "")
                .formParam("keywords", "")
                .formParam("offset", "")
                .formParam("page", "")
                .formParam("types", "")
                .formParam("city", "")
                .formParam("children", "")
                .formParam("extensions", "")
                .request(Method.POST, "/web/cms/around/getAroundInfo")
                .then()
                .body("resultCode", equalTo(8200))
                .body("message",equalTo("周边位置信息获取成功"));
    }

    //智能导览
    @Test(testName = "参数为空时默认获取周边位置-type=住宿", description = "智能导览")
    public void getAround_hotel() {
        baseURI = suit.getBaseurl();
        given()
                .formParam("tenantId", "")
                .formParam("keywords", "")
                .formParam("offset", "")
                .formParam("page", "")
                .formParam("types", "住宿")
                .formParam("city", "")
                .formParam("children", "")
                .formParam("extensions", "")
                .request(Method.POST, "/web/cms/around/getAroundInfo")
                .then()
                .body("resultCode", equalTo(8200))
                .body("message",equalTo("周边位置信息获取成功"));
    }

    //个人中心-根据会员id查询会员信息~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    //会员id为空
    @Test(testName = "会员id为空", description = "会员id为空")
    public void member_null() {
        baseURI = suit.getBaseurl();
        given()
                .formParam("tenantId", params.get("tenantId"))
                .formParam("id", "")
                .request(Method.POST, "/web/selfcenter/queryById")
                .then()
                .body("resultCode",equalTo(8500))
                .body("message", equalTo("微信会员未授权"));
    }
    //输入任意会员id
    @Test(testName = "输入任意会员id", description = "会员id为空")
    public void member_error() {
        baseURI = suit.getBaseurl();
        given()
                .formParam("tenantId", params.get("tenantId"))
                .formParam("id", "111")
                .request(Method.POST, "/web/selfcenter/queryById")
                .then()
                .body("resultCode",equalTo(8200));
    }
    //输入正确的会员id
    @Test(testName = "输入任意会员id", description = "会员id为空")
    public void member_id() {
        baseURI = suit.getBaseurl();
        given()
                .formParam("tenantId", params.get("tenantId"))
                .formParam("id", params.get("memberId"))
                .request(Method.POST, "/web/selfcenter/queryById")
                .then()
                .body("resultCode",equalTo(8200));
    }

    //收藏~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    //收藏列表为空
    @Test(testName = "收藏列表为空", description = "收藏列表为空")
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
    @Test(testName = "收藏_不传入id收藏", description = "收藏_不传入id收藏")
    public void Collection_id() {
        baseURI = suit.getBaseurl();
        given()
                .formParam("tenantId", params.get("tenantId"))
                .formParam("id", "")
                .formParam("contentId", "")
                .formParam("type", "2")
                .request(Method.POST, "/web/selfcenter/collect")
                .then()
                .body("message", equalTo("微信会员未授权"));
    }

    //不传入id取消收藏
    @Test(testName = "收藏_不传入id取消收藏", description = "收藏_不传入id收藏")
    public void canceCollection() {
        baseURI = suit.getBaseurl();
        given()
                .formParam("tenantId", params.get("tenantId"))
                .formParam("id", "")
                .formParam("contentId", "")
                .formParam("type", "2")
                .request(Method.POST, "/web/selfcenter/cancelCollect")
                .then()
                .body("message", equalTo("微信会员未授权"));
    }

    /*
    //收藏
    @Test(testName = "收藏", description = "收藏")
    public void Collection() {
        baseURI = suit.getBaseurl();
        given()
                .formParam("tenantId", params.get("tenantId"))
                .formParam("id", params.get("memberId"))
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
                .formParam("tenantId", params.get("tenantId"))
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
                .formParam("tenantId", params.get("tenantId"))
                .formParam("id", params.get("memberId"))
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
                .formParam("tenantId", params.get("tenantId"))
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
                            .formParam("tenantId", params.get("tenantId"))
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
                        .formParam("tenantId", params.get("tenantId"))
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
                        .formParam("tenantId", params.get("tenantId"))
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
                        .formParam("tenantId", params.get("tenantId"))
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

    //我的订单~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    //待使用订单列表
    @Test(testName = "订单查询-必填项未填写", description = "订单查询-必填项未填写" )
    public void myOrderList1() {
        baseURI = suit.getBaseurl();
        given()
                .formParam("tenantId","")
                .formParam("orderStatus", "")
                .formParam("id", "")
                .formParam("pageSize", "")
                .formParam("currentPage", "")
                .request(Method.POST, "/web/selfcenter/myOrderList")
                .then()
                .body("resultCode", equalTo(8500));

    }
    //待使用订单列表
    @Test(testName = "订单查询-全部必填项填写", description = "订单查询-必填项未填写orderStatus=3000/4000/5000" )
    public void myOrderList() {
        baseURI = suit.getBaseurl();
        given()
                .formParam("tenantId",params.get("tenantId"))
                .formParam("orderStatus", "")
                .formParam("id", params.get("memberId"))
                .formParam("pageSize", "10")
                .formParam("currentPage", "1")
                .request(Method.POST, "/web/selfcenter/myOrderList")
                .then()
                .body("resultCode", equalTo(8200));

    }



    //意见反馈~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    //根据父节点code查询子节点信息（意见反馈填写）
    @Test(testName = "进入意见反馈填写为空", description = "进入意见反馈填写为空" )
    public void ChildrenByParentCode_null() {
        baseURI = suit.getBaseurl();
        given()
                .formParam("tenantId","")
                .formParam("code", "")
                .formParam("isEnable", "")
                .request(Method.POST, "/web/dictionary/queryChildrenByParentCode")
                .then()
                .body("resultCode", equalTo(8200));

    }
    //根据父节点code查询子节点信息（意见反馈填写）
    @Test(testName = "正常进入填写页面", description = "正常进入填写页面" )
    public void ChildrenByParentCode() {
        baseURI = suit.getBaseurl();
        given()
                .formParam("tenantId",params.get("tenantId"))
                .formParam("code", "COMPLAIN_TYPE")
                .formParam("isEnable", "1")
                .request(Method.POST, "/web/dictionary/queryChildrenByParentCode")
                .then()
                .body("resultCode", equalTo(8200));

    }

    //意见反馈-新增
    @Test(testName = "意见反馈-新增成功（填写信息项都填写时，点击添加）", description = "新增" )
        public void suggestions_add() {
            baseURI = suit.getBaseurl();
        Response response = given()
                    .header("Content-Type","application/x-www-form-urlencoded;charset=UTF-8")
                    .formParam("tenantId", params.get("tenantId"))
                    .formParam("publishId", params.get("memberId"))
                    .formParam("contactName", "王云")
                    .formParam("phoneNum", params.get("phone"))
                    .formParam("type", "2")
                    .formParam("count", "0")
                    .formParam("context","新增意见自动化测试")
                    .request(Method.POST, "/web/complaint/add")
                    .then()
                    .body("resultCode", equalTo(8200))
                    .extract()
                    .response();
              String id = response.path("data");
              params.put("suggestions_id",id);

        }

        //意见反馈-查看
    @Test(testName = "意见反馈-正常查看", description = "查看" ,dependsOnMethods = "suggestions_add")
    public void queryByConditions() {
        baseURI = suit.getBaseurl();
        given()
                .formParam("tenantId", params.get("tenantId"))
                .formParam("memberId", params.get("memberId"))
                .formParam("pageSize", "10")
                .formParam("currentPage", "1")
                .formParam("count", "1")
                .formParam("loading","true")
                .formParam("finished","true")
                .request(Method.POST, "/web/complaint/queryByConditions")
                .then()
                .body("resultCode", equalTo(8200))
                .body("message",equalTo("投诉管理列表查询成功"))
                .body("list.id",hasItem(params.get("suggestions_id")));

    }
    //意见反馈-撤销
    @Test(testName = "意见反馈-待处理状态时撤销", description = "撤销" ,dependsOnMethods = "queryByConditions")
    public void suggestions_updata() {
        baseURI = suit.getBaseurl();
        given()
                .formParam("tenantId", params.get("tenantId"))
                .formParam("id",params.get("suggestions_id"))
                .request(Method.POST, "/web/complaint/updateCancel")
                .then()
                .body("message", equalTo("撤销成功"));


    }

    //意见反馈-撤销
    @Test(testName = "意见反馈-id未填时点击撤销", description = "撤销" )
    public void suggestions_null() {
        baseURI = suit.getBaseurl();
        given()
                .formParam("tenantId", "")
                .formParam("id","")
                .request(Method.POST, "/web/complaint/updateCancel")
                .then()
                .body("message", equalTo("投诉管理id不能为空"));


    }
    //意见反馈-撤销
    @Test(testName = "意见反馈-输入不存在的id点击撤销", description = "撤销" )
    public void suggestions_error() {
        baseURI = suit.getBaseurl();
        given()
                .formParam("tenantId", "")
                .formParam("id","11111")
                .request(Method.POST, "/web/complaint/updateCancel")
                .then()
                .body("message", equalTo(""));


    }

    //意见反馈，填写信息项为空时新增
    @Test(testName = "意见反馈-填写信息项为空时", description = "新增" )
    public void suggestions_addnull() {
        baseURI = suit.getBaseurl();
        given()
                .header("Content-Type","application/x-www-form-urlencoded;charset=UTF-8")
                .formParam("tenantId", "")
                .formParam("publishId", "")
                .formParam("contactName", "")
                .formParam("phoneNum", "")
                .formParam("type", "")
                .formParam("count", "")
                .formParam("context","")
                .request(Method.POST, "/web/complaint/add")
                .then()
                .body("resultCode", equalTo(8500));


    }

    //意见反馈-查看
    @Test(testName = "意见反馈,列表为空", description = "查看" )
    public void queryByConditions_null() {
        baseURI = suit.getBaseurl();
        given()
                .formParam("tenantId", params.get("tenantId"))
                .formParam("memberId", params.get("memberId"))
                .formParam("pageSize", "10")
                .formParam("currentPage", "1")
                .formParam("count", "1")
                .formParam("loading","true")
                .formParam("finished","true")
                .request(Method.POST, "/web/complaint/queryByConditions")
                .then()
                .body("resultCode", equalTo(8200));

    }
    //意见反馈-查看
    @Test(testName = "意见反馈-查看-必填项未填写时", description = "查看" )
    public void queryByConditions_error() {
        baseURI = suit.getBaseurl();
        given()
                .formParam("tenantId", "")
                .formParam("memberId", "")
                .formParam("pageSize", "")
                .formParam("currentPage", "")
                .formParam("count", "")
                .formParam("loading","")
                .formParam("finished","")
                .request(Method.POST, "/web/complaint/queryByConditions")
                .then()
                .body("resultCode", equalTo(8500));

    }

    @AfterClass
    public void clean(){
        JdbcUtils.closeConn();
        String tem = "";
        TestCase testCase = caseList.get(0);
        ResultSet resultSet = JdbcUtils.getResult(testCase.getDBsql().getSqlList().get(2), testCase.getDBsql().getJdbc());
        ResultSet resultSet1 = JdbcUtils.getResult(testCase.getDBsql().getSqlList().get(3), testCase.getDBsql().getJdbc());
    }


}
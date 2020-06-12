# APITest
接口测试工具


### 使用说明
#### 目录结构说明
- common  公共类目录
- - listener  测试结果监听类，后续根据需求实现
- - utils     一些通用工具类
- data   数据提取器
- pojo   基础实例类
- TestCase  测试类，用于运行的测试用例
- resources     资源文件夹
- - TeseCaseData  存放测试数据文件，为json格式
- - testNg        存放testNg配置文件，每个xml文件可以配置运行指定的那些测试类等一些配置

#### 模板测试用例Demo2TestCase.java 运行步骤
- 1.TestCase包中查看模板测试用例文件Demo2TestCase.java 文件，查看测试用例编写
- 2.确认Demo2TestCase.java中 preConditions方法中读取的数据文件TestCaseData/testDemo1.json是存在
- 3.在resource中查看测试类中读取的数据，确认无误
- 4.在resource/testNg/testng.xml 文件中查看class配置，确认配置的测试类是Demo2TestCase(可以配置多个class)
- 5.查看pom文件中suiteXmlFile 配置的内容，确认是读取resource/testNg/testng.xml内容(只能有一条该数据)
- 6.项目根目录运行命令 mvn clean test
- 7.项目根目录 运行allure serve target或allure-results  查看结果报告


#### 测试用例使用的json文件编写
1. 字段内容参考resources/TestCaseData/testDemo1.json
2. 字段类型参考resources/TestCaseData/testDemo1.json

#### 测试用例java编写
##### 设置请求信息
````
// 设置请求表单数据和路径信息
given().
       formParam("formParamName", "value1").  
       queryParam("queryParamName", "value2").
when().
       post("/something");

````
##### 获取响应
````
//获取响应_links.next.href字段内容
String nextTitleLink =
given().
        param("param_name", "param_value").
when().
        get("/title").
then().
        contentType(JSON).
        body("title", equalTo("My Title")).
extract().
        path("_links.next.href")


//获取整个响应对象
Response response = 
given().
        param("param_name", "param_value").
when().
        get("/title").
then().
        contentType(JSON).
        body("title", equalTo("My Title")).
extract().
        response(); 

//获取响应头信息
Headers allHeaders = response.getHeaders();
String headerName = response.getHeader("headerName");

//获取响应cookie
Map<String,String> cookies = response.getCookies();
String cookie = response.getCookie("cookiename");

````
##### 响应头字段内容断言
````
// 响应cookie断言
get("/x").then().assertThat().cookie("cookieName", "cookieValue"). ..
get("/x").then().assertThat().cookies("cookieName1", "cookieValue1", "cookieName2", "cookieValue2")
get("/x").then().assertThat().cookies("cookieName1", "cookieValue1", "cookieName2", containsString("Value2"))

// 响应状态码断言
get("/x").then().assertThat().statusCode(200)
get("/x").then().assertThat().statusLine("something")
get("/x").then().assertThat().statusLine(containsString("some"))

// 响应头断言
get("/x").then().assertThat().header("headerName", "headerValue")
get("/x").then().assertThat().headers("headerName1", "headerValue1", "headerName2", "headerValue2")
get("/x").then().assertThat().headers("headerName1", "headerValue1", "headerName2", containsString("Value2"))
````

##### 响应时间断言
````
// 获取响应时间
long timeInMs = get("/lotto").time()

//以指定单位获取响应时间
long timeInSeconds = get("/lotto").timeIn(SECONDS);

// 响应时间断言，默认单位是毫秒
when().
      get("/lotto").
then().
      time(lessThan(2000L)); // Milliseconds

// 响应时间断言，不能大于指定值
when().
      get("/lotto").
then().
      time(lessThan(2L), SECONDS);

````

##### 响应body的字段内容断言
```
返回json内容如下:
{
"lotto":{
 "lottoId":5,
 "winning-numbers":[2,45,34,23,7,5,3],
 "winners":[{
   "winnerId":23,
   "numbers":[2,45,34,23,3,5]
 },{
   "winnerId":54,
   "numbers":[52,3,12,11,18,22]
 }]
}
}

对lottoId进行断言
get("/lotto").then().body("lotto.lottoId", equalTo(5));
对值的范围区间断言
get("/lotto").then().body("lotto.winners.winnerId", hasItems(23, 54));

```




项目接口测试框架使用的是rest-assured  更多功能[点击此处跳转官网](https://github.com/rest-assured/rest-assured/wiki/Usage)
### 其他注意事项
#### mybatis 自动生成代码
- resources目录下修改 generatorConfig.xml 文件
- 在idea的右侧栏点击Maven,选中添加的Mybatis-generator插件并运行









#### testng 使用testng.xml
- mvn clean test -Dsurefire.suiteXmlFiles=src/test/java/testSuit/testng.xml




#### allure

1. 安装 brew install allure (mac 环境)

2. 安装 npm install allure (node 环境)

3. 参见https://www.cnblogs.com/wsy1103/p/10530397.html  （windows环境）

3. 通过allure --version  查看是否安装成功

##### java 用例中的注释
- @Epic
敏捷的术语，定义史诗，可以理解为一级标题，往下再分Feature和Story。
- @Feature
敏捷的术语，定义功能模块，可以理解为二级标题，往下是Story。
- @Story
敏捷的术语，定义用户故事。可以理解为三级标题
- @Title(当前版本不支持)
定义用例名称。
- @Description
定义用例描述。
- @Issue
定义缺陷链接。可结合@Link使用，也可以结合配置文件使用。配置文件放到resource目录下，Allure 会替换{}为对应注解的值。
- @TmsLink
与@Issue类似用法，定义案例链接。
- @Link
定义一个链接，在测试报告展现。
- @Severity
定义用例的级别，主要有BLOCKER,CRITICAL,MINOR,NORMAL,TRIVIAL等几种类型，默认是NORMAL。使用方法如下。
> @Severity(SeverityLevel.TRIVIAL)
- @Attachment
添加已有附件或者新建附件后添加至测试报告。




#### mvn 运行测试用例 
1.1.pom 文件配置 或 命令行配置
1.2.命令行配置 mvn clean test -Dsurefire.suiteXmlFiles=src/test/java/testSuit/testng.xml(pom 未配置命令行指定运行的xml文件)
2.mvn clean test 运行测试用例(pom 已经配置了运行的xml文件)
3.打开报告结果（使用4.1 或者4.2)
- 4.1 allure serve allure-results  或者  allure serve target/allure-results (生成临时目录并打开报告)
- 4.2.1 allure generate target/allure-results  先生成报告到目录
- 4.2.2  allure open allure-results  然后再执行打开命令





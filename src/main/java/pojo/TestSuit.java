package pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import groovyjarjarantlr.collections.impl.LList;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
public class TestSuit {

	/**
	 * 测试用例集合的基础域名
	 */
	@JsonProperty("baseurl")
	private String baseurl;

	/**
	 * 测试用例集合
	 */
	@JsonProperty("caseList")
	private ArrayList<TestCase> caseList;

	/**
	 * 参数容器
	 */
	@JsonProperty("params")
	private Map<String,Object> params;

	

}

package common.utils;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class JsonUtils {
    private static final Logger logger = LoggerFactory.getLogger(JsonUtils.class);

    private static ObjectMapper objectMapper = new ObjectMapper();

    static{
        /**重写objectMapper 将null转换为"" **/
        objectMapper.getSerializerProvider().setNullValueSerializer(
                new JsonSerializer<Object>() {
                    @Override
                    public void serialize(Object value, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException, JsonProcessingException {
                        jsonGenerator.writeString("");
                    }
                }
        );
    }


    /**
     * Json 字符串转为对象
     * @param JSONStr
     * @param claz
     * @param <T>
     * @return
     */
    @SuppressWarnings("unchecked")
    public static  <T extends Object> T parseJsonData(String JSONStr,Class<T> claz){
        Object object = null;
        try {
            object = objectMapper.readValue(JSONStr, claz);
        } catch (JsonGenerationException e) {
            e.printStackTrace();
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();

        }
        return object != null ? (T) object : null;

    }

    /**
     * 方法名称: JSON2List
     * 方法描述:  JSON 转换为List:
     * 返回类型: List<T>
     * @throws
     */
    @SuppressWarnings("unchecked")
    public static <T> List<T> JSON2List(String jsonStr, Class<T> claz) {
        List<T> result = null;
        try{
            JavaType t = objectMapper.getTypeFactory().
                    constructParametricType(List.class, claz);
            result = objectMapper.readValue(jsonStr, t);
        } catch (JsonGenerationException e) {
            e.printStackTrace();
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (IOException e) {
         e.printStackTrace();
        }
        return result==null?new ArrayList<T>():result;
    }


}

package common.utils;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;

/**
 * Created by liugumin on 2019/8/28.
 */
public class IOUtils {

    private static final Logger logger = LoggerFactory.getLogger(IOUtils.class);

    /**
     * 读取文件内容作为字符串对象
     * @param file  文件地址
     * @param encode 编码方式,默认使用utf-8
     * @return
     * @throws Exception
     */
    public static String readFiletoString(String file, String encode)  {
        URL classpath = IOUtils.class.getResource("/");
        file = classpath.getPath()+file;
        File file1 = new File(file);
        StringBuffer sBuffer = null;

        try {
            if (!file1.exists()) {
                logger.error("{}文件不存在",file);
                return null;
            }

            FileInputStream fileInput = new FileInputStream(file);
            InputStreamReader reader = (encode == null || "".equals(encode)) ? new InputStreamReader(fileInput, "utf-8") : new InputStreamReader(fileInput, encode);
            sBuffer = new StringBuffer();
            int charCount = 0;
            char[] charBuffer = new char[1024];
            while ((charCount = reader.read(charBuffer)) > 0) {
                sBuffer.append(charBuffer, 0, charCount);
            }
            reader.close();
            fileInput.close();
        }catch (IOException e){

        }
        return sBuffer.toString();
    }


    /**
     /**
     * 读取文件内容作为pojo的list对象
     * @param file  文件地址
     * @param encode 编码，默认使用utf-8
     * @param T  pojo 的类型。默认是String
     * @return
     * @throws Exception
     */
    public static List<Object> readFileToList(String file, String encode,Class T) throws  Exception {
        File file1 = new File(file);
        ObjectMapper mapper = new ObjectMapper();

        if(!file1.exists()){
            throw  new Exception("文件不存在");
        }

        if(T==null){
            T= String.class;
       }

        String  congtent = readFiletoString(file,encode);
        JavaType javaType = mapper.getTypeFactory().constructParametricType(List.class, T);
        List<Object> pojoList = mapper.readValue(congtent, javaType);

        return pojoList;
    }


}

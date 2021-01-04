package github.genelin.common.util;

import java.io.*;
import java.util.Properties;
import lombok.extern.slf4j.Slf4j;

/**
 * @author gene lin
 * @createTime 2020/12/25 16:13
 */
@Slf4j
public final class PropertiesFileUtils {

    private PropertiesFileUtils(){}

    // 基于classloader进行配置读取
    public static Properties getPropertiesByFileName(String fileName){
        log.info(PropertiesFileUtils.class.getClassLoader().toString());
        InputStream is = PropertiesFileUtils.class.getClassLoader().getResourceAsStream(fileName);
        Properties properties = new Properties();
        try {
            properties.load(is);
        } catch (IOException e) {
            log.error("Exception occurs when reads properties file: {}", e.getMessage());
        }
        return properties;
    }
}

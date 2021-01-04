import github.genelin.common.util.PropertiesFileUtils;
import java.util.Properties;
import org.junit.jupiter.api.Test;

/**
 * @author gene lin
 * @createTime 2020/12/25 16:52
 */
public class PropertiesFileUtilTest {

    @Test
    public void foo(){
        Properties propertiesByFileName = PropertiesFileUtils.getPropertiesByFileName("rpc.properties");
        System.out.println(propertiesByFileName.getProperty("rpc.zookeeper.address"));
    }
}

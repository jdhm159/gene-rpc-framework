import github.genelin.common.util.PropertiesFileUtils;
import github.genelin.remoting.constants.RpcConstants;
import java.net.*;
import java.util.Properties;
import org.junit.jupiter.api.Test;
import service.HelloService;

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

    @Test
    public void bar() throws UnknownHostException {
        System.out.println(HelloService.class.getName());
        System.out.println(new InetSocketAddress(InetAddress.getLocalHost().getHostAddress()
            , RpcConstants.DEFAULT_PORT).toString());
    }
}

import github.genelin.common.extension.ExtensionLoader;
import github.genelin.common.extension.SPI;
import github.genelin.registry.ServiceRegistry;
import org.junit.Assert;
import org.junit.Test;
import service.HelloService;

/**
 * @author gene lin
 * @createTime 2021/1/4 16:48
 */
public class ExtensionsLoaderTest {

    @Test
    public void getExtensionTest() {
        ExtensionLoader<HelloService> extensionLoader = ExtensionLoader.getExtensionLoader(HelloService.class);
        HelloService impl1 = extensionLoader.getExtension("impl1");
        Assert.assertEquals(impl1.hello(), "This is impl1");
        HelloService impl2 = extensionLoader.getExtension("impl2");
        Assert.assertEquals(impl2.hello(), "This is impl2");
    }

    @Test
    public void getDefaultExtensionTest() {
        ExtensionLoader<HelloService> extensionLoader = ExtensionLoader.getExtensionLoader(HelloService.class);
        HelloService defaultExtension = extensionLoader.getDefaultExtension();

        Assert.assertEquals(defaultExtension.hello(), "This is impl2");
    }
}

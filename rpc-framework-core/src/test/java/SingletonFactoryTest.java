import github.genelin.common.util.factory.SingletonFactory;
import github.genelin.remoting.transport.ServiceProviderImpl;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

/**
 * @author gene lin
 * @createTime 2021/1/7 13:40
 */
public class SingletonFactoryTest {

    @Test
    public void getSingletonInstanceTest(){
        Object singletonObject = SingletonFactory.getSingletonObject(Object.class);
        Object singletonObject2 = SingletonFactory.getSingletonObject(Object.class);
        Assert.assertEquals(singletonObject, singletonObject2);
    }

    @Test
    public void getSingletonServiceProvider(){
        ServiceProviderImpl serviceProvider = SingletonFactory.getSingletonObject(ServiceProviderImpl.class);
        ServiceProviderImpl serviceProvider2 = SingletonFactory.getSingletonObject(ServiceProviderImpl.class);
        Assert.assertNotNull(serviceProvider);
        Assert.assertEquals(serviceProvider, serviceProvider2);
    }
}

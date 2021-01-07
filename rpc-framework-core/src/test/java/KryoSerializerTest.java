import github.genelin.serialization.kryo.KryoSerializer;
import org.junit.jupiter.api.Test;


/**
 * @author gene lin
 * @createTime 2020/12/19 16:43
 */
public class KryoSerializerTest {

    @Test
    public void foo() {
        MyObject myObject = new MyObject();
        myObject.name = "gene";

        KryoSerializer kryoSerializer = new KryoSerializer();
        byte[] b = kryoSerializer.serialize(myObject);

        MyObject deserializedObject = kryoSerializer.deserialize(b, MyObject.class);
        System.out.println(deserializedObject.name);

    }
    public static class MyObject{
        String name;
    }
}

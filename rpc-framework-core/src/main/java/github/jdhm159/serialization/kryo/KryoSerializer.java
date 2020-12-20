package github.jdhm159.serialization.kryo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Kryo.DefaultInstantiatorStrategy;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.pool.KryoPool;
import github.jdhm159.remoting.dto.RPCRequest;
import github.jdhm159.remoting.dto.RPCResponse;
import github.jdhm159.serialization.SerializeException;
import github.jdhm159.serialization.Serializer;
import java.io.*;
import org.objenesis.strategy.StdInstantiatorStrategy;

/**
 * Kryo序列化器
 * @author gene lin
 * @createTime 2020/12/18 16:36
 */
public class KryoSerializer implements Serializer {

    private final static KryoPool kryoPool = new KryoPool.Builder(() -> {
        Kryo kryo = new Kryo();
        // configure here
        // 要确保注册顺序一致
        kryo.register(RPCRequest.class);
        kryo.register(RPCResponse.class);
        // 显示指定实例化器，首先使用默认无参构造策略DefaultInstantiatorStrategy，若创建对象失败再采用StdInstantiatorStrategy
        kryo.setInstantiatorStrategy(new DefaultInstantiatorStrategy(new StdInstantiatorStrategy()));
        return kryo;
    }).softReferences().build();

    @Override
    public byte[] serialize(Object obj) {
        Kryo kryo = kryoPool.borrow();
        try (ByteArrayOutputStream os = new ByteArrayOutputStream();
            Output output = new Output(os);) {

            kryo.writeObject(output, obj);
            return output.toBytes();
        } catch (Exception e) {
            throw new SerializeException("Kryo序列化失败");
        } finally {
            kryoPool.release(kryo);
        }
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        Kryo kryo = kryoPool.borrow();
        try (ByteArrayInputStream is = new ByteArrayInputStream(bytes);
            Input input = new Input(is)) {

            return kryo.readObject(input, clazz);
        } catch (Exception e) {
            e.printStackTrace();
            throw new SerializeException("Kryo反序列化失败");
        } finally {
            kryoPool.release(kryo);
        }
    }
}

package github.genelin.serialization;

/**
 * 所有的序列化类都要实现该接口
 * @author gene lin
 * @createTime 2020/12/7 23:57
 */
public interface Serializer {

    /**
     * 序列化
     * @param obj 需要进行序列化的对象
     * @return 字节数组
     */
    byte[] serialize(Object obj);

    /**
     * 反序列化
     * @param bytes 需要进行反序列化的字节数组
     * @param clazz 在rpc场景，类型都是可知的
     * @param <T> 反序列化得到的对象类型
     * @return
     */
    <T> T deserialize(byte[] bytes, Class<T> clazz);

}

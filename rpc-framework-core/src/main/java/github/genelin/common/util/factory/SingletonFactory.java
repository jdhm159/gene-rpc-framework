package github.genelin.common.util.factory;

import github.genelin.common.entity.Holder;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 单例模式 结合 工厂模式
 * 统一管理单例对象，进行解耦，简化单例类的编写（创建及暴露单例统一由工厂来负责，无需在每个单例类中编写）
 * 用于构建单例对象的工厂类（本身也是单例的，使用静态内部类方式）
 * @author gene lin
 * @createTime 2020/12/18 19:45
 */
public final class SingletonFactory {

    private final ConcurrentHashMap<String, Holder<Object>> OBJECT_MAP;

    // 私有化构造方法
    private SingletonFactory(ConcurrentHashMap<String, Holder<Object>> map){
        this.OBJECT_MAP = map;
    }

    // 静态内部类
    private static class FactorySingletonHolder{
        private static SingletonFactory singletonFactory = new SingletonFactory(new ConcurrentHashMap<>());
    }

    private static SingletonFactory getInstance(){
        return FactorySingletonHolder.singletonFactory;
    }

    public static <T> T getSingletonObject(Class<T> clazz){
        String key = clazz.getName();
        ConcurrentHashMap<String, Holder<Object>> holderMap = getInstance().OBJECT_MAP;
        Holder<Object> holder = holderMap.get(key);
        if (holder == null){
            holderMap.putIfAbsent(key, new Holder<>());
            holder = holderMap.get(key);
        }

        Object obj = holder.get();
        if (obj == null){
            synchronized (holder){
                obj = holder.get();
                if (obj == null){
                    try {
                        Constructor<T> c = clazz.getDeclaredConstructor();
                        c.setAccessible(true);
                        obj = c.newInstance();
                        holder.set(obj);
                    } catch (InstantiationException | IllegalAccessException e) {
                        throw new RuntimeException(e);
                    } catch (NoSuchMethodException | InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return clazz.cast(obj);
    }
}

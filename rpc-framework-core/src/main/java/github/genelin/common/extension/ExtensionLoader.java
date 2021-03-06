package github.genelin.common.extension;

import github.genelin.common.entity.Holder;
import java.io.*;
import java.net.*;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;

/**
 * refer to dubbo spi: https://dubbo.apache.org/zh/docs/v2.7/dev/source/dubbo-spi/
 */

@Slf4j
public final class ExtensionLoader<T> {

    private static final String SERVICES_DIRECTORY = "META-INF/extensions/";
    private static final Pattern NAME_SEPARATOR = Pattern.compile("\\s*[,]+\\s*");
    private static final ConcurrentMap<Class<?>, ExtensionLoader<?>> EXTENSION_LOADERS = new ConcurrentHashMap<>(64);
    private static final ConcurrentMap<Class<?>, Object> EXTENSION_INSTANCES = new ConcurrentHashMap<>(64);

    private final Class<?> type;
    private final Holder<Map<String, Class<?>>> cachedClasses = new Holder<>();
    private final ConcurrentMap<String, Holder<Object>> cachedInstances = new ConcurrentHashMap<>();

    private String cachedDefaultName;

    private ExtensionLoader(Class<?> type) {
        this.type = type;
    }

    public static <T> ExtensionLoader<T> getExtensionLoader(Class<T> type) {
        if (type == null) {
            throw new IllegalArgumentException("Extension type == null");
        }
        if (!type.isInterface()) {
            throw new IllegalArgumentException("Extension type (" + type + ") is not an interface!");
        }
        if (!withExtensionAnnotation(type)) {
            throw new IllegalArgumentException("Extension type (" + type +
                ") is not an extension, because it is NOT annotated with @SPI");
        }

        ExtensionLoader<T> loader = (ExtensionLoader<T>) EXTENSION_LOADERS.get(type);
        if (loader == null) {
            EXTENSION_LOADERS.putIfAbsent(type, new ExtensionLoader<T>(type));
            loader = (ExtensionLoader<T>) EXTENSION_LOADERS.get(type);
        }
        return loader;
    }

    private static <T> boolean withExtensionAnnotation(Class<T> type) {
        return type.isAnnotationPresent(SPI.class);
    }

    public T getExtension(String name) {
        if (name == null || name.length() == 0) {
            throw new IllegalArgumentException("Extension name should not be null or empty");
        }
        // Holder??????????????????????????????????????????
        Holder<Object> holder = cachedInstances.get(name);
        if (holder == null) {
            cachedInstances.putIfAbsent(name, new Holder<Object>());
            holder = cachedInstances.get(name);
        }
        Object instance = holder.get();
        // ????????????
        if (instance == null) {
            synchronized (holder) {
                instance = holder.get();
                if (instance == null) {
                    // ??????????????????
                    instance = createExtension(name);
                    // ??????????????? holder ???
                    holder.set(instance);
                }
            }
        }
        return (T) instance;
    }

    /**
     * Return default extension, return <code>null</code> if it's not configured.
     */
    public T getDefaultExtension() {
        getExtensionClasses();
        if ("".equals(cachedDefaultName)) {
            log.error("Fail to get default extension of type[{}], because the default name is not configured", type);
            throw new RuntimeException("Fail to get default extension of type[" + type
                + "], because the default name is not configured");
        }
        return getExtension(cachedDefaultName);
    }

    private T createExtension(String name) {
        // ???????????????????????????????????????????????????????????????????????????????????????????????????????????????
        Class<?> clazz = getExtensionClasses().get(name);
        if (clazz == null) {
            throw new RuntimeException("No such extension of name - " + name);
        }
        T instance = (T) EXTENSION_INSTANCES.get(clazz);
        try {
            if (instance == null) {
                // ????????????????????????
                EXTENSION_INSTANCES.putIfAbsent(clazz, clazz.newInstance());
                instance = (T) EXTENSION_INSTANCES.get(clazz);
            }
            // ????????? ioc ??? aop ??????...
        } catch (Exception e) {
            log.error("Fail to create instance of extension[{}]", clazz, e);
            throw new RuntimeException("Fail to create instance of extension " + name);
        }
        return instance;
    }

    private Map<String, Class<?>> getExtensionClasses() {
        // ???????????????????????????????????????
        Map<String, Class<?>> classes = cachedClasses.get();
        // ????????????
        if (classes == null) {
            synchronized (cachedClasses) {
                classes = cachedClasses.get();
                if (classes == null) {
                    // ???????????????
                    classes = loadExtensionClasses();
                    cachedClasses.set(classes);
                }
            }
        }
        return classes;
    }

    private Map<String, Class<?>> loadExtensionClasses() {
        cacheDefaultExtensionName();

        Map<String, Class<?>> extensionClasses = new HashMap<String, Class<?>>();
        // ???????????????????????????????????????
        loadDirectory(extensionClasses, SERVICES_DIRECTORY);
        return extensionClasses;
    }

    /**
     * extract and cache default extension name if exists
     */
    private void cacheDefaultExtensionName() {
        // ?????? SPI ?????????????????? type ?????????????????? getExtensionLoader ??????????????????
        final SPI defaultAnnotation = type.getAnnotation(SPI.class);
        if (defaultAnnotation == null) {
            return;
        }

        String value = defaultAnnotation.value();
        if ((value = value.trim()).length() > 0) {
            // ??? SPI ????????????????????????
            String[] names = NAME_SEPARATOR.split(value);
            // ?????? SPI ???????????????????????????????????????????????????
            if (names.length > 1) {
                throw new IllegalStateException("More than 1 default extension name on extension " + type.getName()
                    + ": " + Arrays.toString(names));
            }
            // ??????????????????????????? getDefaultExtension ??????
            if (names.length == 1) {
                cachedDefaultName = names[0];
            }
        }
    }

    private void loadDirectory(Map<String, Class<?>> extensionClasses, String dir) {
        // fileName = ??????????????? + type ????????????
        String fileName = dir + type.getName();
        try {
            Enumeration<URL> urls;
            ClassLoader classLoader = ExtensionLoader.class.getClassLoader();
            urls = classLoader.getResources(fileName);
            if (urls != null) {
                while (urls.hasMoreElements()) {
                    java.net.URL resourceURL = urls.nextElement();
                    // ????????????
                    loadResource(extensionClasses, classLoader, resourceURL);
                }
            }
        } catch (Exception e) {
            log.error("Exception occurs while loading resource[{}]", fileName, e);
            throw new RuntimeException("Fail to load directory");
        }
    }

    private void loadResource(Map<String, Class<?>> extensionClasses,
        ClassLoader classLoader, java.net.URL resourceURL) {
        try {
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(resourceURL.openStream(), "utf-8"));
            try {
                String line;
                // ????????????????????????
                while ((line = reader.readLine()) != null) {
                    // ?????? # ??????
                    final int ci = line.indexOf('#');
                    if (ci >= 0) {
                        // ?????? # ?????????????????????# ???????????????????????????????????????
                        line = line.substring(0, ci);
                    }
                    line = line.trim();
                    if (line.length() > 0) {
                        String name = null;
                        int i = line.indexOf('=');
                        if (i > 0) {
                            // ???????????? = ????????????????????????
                            name = line.substring(0, i).trim();
                            line = line.substring(i + 1).trim();
                        }
                        if (line.length() > 0) {
                            Class<?> c = extensionClasses.get(name);
                            if (c == null) {
                                // ?????????????????????????????????
                                c = classLoader.loadClass(line);
                                // ??????????????? Class ???????????????
                                extensionClasses.put(name, c);
                            }
                        }
                    }
                }
            } finally {
                reader.close();
            }
        } catch (ClassNotFoundException e) {
            log.error("Failed to load extension class...", e);
            throw new RuntimeException("Failed to load extension class...");
        } catch (Exception t) {
            log.error("Exception occurs when load extension class...", t);
            throw new RuntimeException("Exception when load extension class...");
        }
    }

}
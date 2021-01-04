package github.genelin.common.extension;

/**
 * Helper Class for hold a value.
 * @author gene lin
 * @createTime 2021/1/4 0:07
 */
public class Holder<T> {

    private volatile T value;

    public void set(T value) {
        this.value = value;
    }

    public T get() {
        return value;
    }

}
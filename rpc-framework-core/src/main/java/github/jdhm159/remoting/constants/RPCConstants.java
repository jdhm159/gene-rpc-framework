package github.jdhm159.remoting.constants;

/**
 * @author gene lin
 * @createTime 2020/12/7 9:14
 */
public interface RPCConstants {

    // magic_num
    byte[] MAGIC_CODE = {(byte) 'G', (byte) 'e', (byte) 'n', (byte) 'e'};       // 使用 "Gene" 作为魔数，校验传输格式

    // framework_version
    byte FRAMEWORK_VERSION = 1;
}

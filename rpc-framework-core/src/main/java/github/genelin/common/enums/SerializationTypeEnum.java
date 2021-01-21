package github.genelin.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author gene lin
 * @createTime 2020/12/8 0:00
 */

@AllArgsConstructor
@Getter
public enum SerializationTypeEnum {

    KRYO((byte) 0x01, "kryo"),
    PROTOSTUFF((byte) 0x02, "protostuff");

    private final byte id;
    private final String name;

    public static String getName(byte id){
        for(SerializationTypeEnum s: SerializationTypeEnum.values()){
            if(id == s.id){
                return s.name;
            }
        }
        return null;
    }

}

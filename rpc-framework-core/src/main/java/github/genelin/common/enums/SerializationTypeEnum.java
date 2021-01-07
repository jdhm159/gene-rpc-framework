package github.genelin.common.enums;

import lombok.AllArgsConstructor;

/**
 * @author gene lin
 * @createTime 2020/12/8 0:00
 */

@AllArgsConstructor
public enum SerializationTypeEnum {
    KRYO((byte) 0x01, "Kryo"),
    PROTOSTUFF((byte) 0x02, "ProtoStuff");

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

package com.jerryoops.eurika.transmission.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@Getter
@Setter
@Builder
@ToString
public class RpcMessage implements Serializable {
    private static final long serialVersionUID = 8504992780438466802L;

    /**
     * 整条RpcMessage的长度，包括定长的header及变长的body，以Byte为单位。
     */
    private Integer length;
    /**
     * 魔数。
     */
    private Byte magic;
    /**
     * RPC通讯协议版本。
     */
    private Byte version;
    /**
     * body的压缩方式。
     */
    private Byte compression;
    /**
     * body的编码方式。
     */
    private Byte serialization;
    /**
     * 代表本RpcMessage的类型，对应携带不同类型的body。
     */
    private Byte type;
    /**
     * 唯一对应本次RPC调用的UUID值。
     */
    private Long requestId;
    /**
     * body
     */
    private Object body;
}

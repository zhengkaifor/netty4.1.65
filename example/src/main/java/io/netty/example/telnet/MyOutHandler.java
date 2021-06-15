package io.netty.example.telnet;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;

import java.util.List;

/**
 * MyOutHandler
 *
 * @author zhengkai
 * @date 2021/6/15
 */
public class MyOutHandler extends MessageToMessageEncoder<String> {

    @Override
    protected void encode(ChannelHandlerContext ctx, String msg, List<Object> out) throws Exception {
        msg = "MyOutHandler:" + msg;
        System.out.println(msg);
        out.add(msg);
    }
}

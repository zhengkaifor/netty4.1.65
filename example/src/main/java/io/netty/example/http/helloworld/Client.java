/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package io.netty.example.http.helloworld;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;

/**
 * An HTTP server that sends back the content of the received HTTP request
 * in a pretty plaintext form.
 */
public final class Client {

    static final boolean SSL = System.getProperty("ssl") != null;
    static final int PORT = Integer.parseInt(System.getProperty("port", SSL ? "8443" : "8080"));

    public static void main(String[] args) throws Exception {
        Selector selector = Selector.open();
        SocketChannel chan = SocketChannel.open();
        chan.connect(new InetSocketAddress("localhost", 8080));
        chan.configureBlocking(false);
        chan.register(selector, SelectionKey.OP_READ);
        while (!chan.finishConnect()){

        }
        System.out.println("已连接！");


        ByteBuffer byteBuffer = ByteBuffer.allocate(256);
        new Avca(selector,chan).start();
        while (true){
            Scanner scanner = new Scanner(System.in);
            String word = scanner.nextLine();
            byteBuffer.put(word.getBytes());
            byteBuffer.flip();
            chan.write(byteBuffer);
            byteBuffer.clear();
        }

    }

    static class Avca extends Thread{
        private Selector selector;
        private SocketChannel clntChan;

        public Avca(Selector selector,SocketChannel clntChan){
            this.selector = selector;
            this.clntChan = clntChan;
        }

        @Override
        public void run(){
            try {
                while (true){
                    selector.select();
                    Set<SelectionKey> keys = selector.selectedKeys();
                    Iterator<SelectionKey> keyIterator = keys.iterator();
                    ByteBuffer byteBuffer = ByteBuffer.allocate(256);
                    while (keyIterator.hasNext()){
                        SelectionKey selectionKey = keyIterator.next();
                        if (selectionKey.isValid()){
                            if (selectionKey.isReadable()){
                                SocketChannel socketChannel = (SocketChannel)selectionKey.channel();
                                socketChannel.read(byteBuffer);
                                byteBuffer.flip();
                                byte[] bytes = new byte[byteBuffer.remaining()];
                                byteBuffer.get(bytes);
                                System.out.println(new String(bytes));
                                byteBuffer.clear();
                            }
                        }
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}

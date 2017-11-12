package org.suntabu.Server;


import org.nanohttpd.protocols.http.IHTTPSession;
import org.nanohttpd.protocols.http.NanoHTTPD;
import org.nanohttpd.protocols.http.request.Method;
import org.nanohttpd.protocols.http.response.IStatus;
import org.nanohttpd.protocols.http.response.Response;
import org.nanohttpd.protocols.http.response.Status;
import org.nanohttpd.protocols.websockets.WebSocketFrame;
import org.suntabu.Server.SimpleWebSocketServer;
import org.suntabu.commands.Console;
import org.suntabu.websockets.WebsocketListener;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by gouzhun on 2016/11/22.
 */

public class ConsoleServer extends NanoHTTPD {
    private static final String ASSET_BASE = "console_html";

    private static final String MIME_JSON = "application/json";
    private static final String MIME_CSS = "text/css";
    private HashMap<String, String> fileTypes = new HashMap<>();
    private Pattern pattern = null;
    private int port = 0;

    private static String HTML_ROOT_DIR = "../html/";
    SimpleWebSocketServer socketServer;
    private Console console;

    /**
     * Starts as a standalone file server and waits for Enter.
     */
    public static void main(String[] args) {
        int port = 8081;

        final ConsoleServer consoleServer = new ConsoleServer(port);



        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    consoleServer.start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);

                } catch (IOException e) {
                    e.printStackTrace();
                }

                System.out.println("Server started\n");
            }
        }).start();
    }

    public ConsoleServer(int port) {
        super(port);

        initPattern();
        this.port = port;

        console = new Console();
    }

    private void initPattern() {
        fileTypes.put("js", "application/javascript");
        fileTypes.put("json", "application/json");
        fileTypes.put("jpg", "image/jpeg");
        fileTypes.put("jpeg", "image/jpeg");
        fileTypes.put("gif", "image/gif");
        fileTypes.put("png", "image/png");
        fileTypes.put("css", "text/css");
        fileTypes.put("htm", "text/html");
        fileTypes.put("html", "text/html");
        fileTypes.put("ico", "image/x-icon");


        String patternstr = "^/(.*\\.(";
        for (Map.Entry<String, String> key : fileTypes.entrySet()) {
            patternstr += key.getKey() + "|";
        }
        patternstr = patternstr.substring(0, patternstr.length() - 1) + "))$";

        pattern = Pattern.compile(patternstr);
    }


    @Override
    public Response serve(IHTTPSession session) {

        Method method = session.getMethod();
        String uri = session.getUri();

  /*          Map<String, String> files = new HashMap<String, String>();
            session.parseBody(files);
            if (files.containsKey("a.png")) {
                SunLog.Log("SUNTABU", files.get("a.png"));
            }

            if (files.containsKey("head")) {
                SunLog.Log("SUNTABU", files.get("head"));
            }*/

        if (uri.equalsIgnoreCase("/")) {
            uri += "index.html";
        }

        if (uri.endsWith("index.html")) {
            if (socketServer == null)
                socketServer = new SimpleWebSocketServer(8083, new WebsocketListener() {
                    @Override
                    public void onOpen() {

                    }

                    @Override
                    public void onClose() {
                    }

                    @Override
                    public void onMessage(WebSocketFrame message, SimpleWebSocketServer simpleWebSocket) {
                        String msg = message.getTextPayload();
                        String result = console.console_run(msg);
                        simpleWebSocket.send(result);
                    }
                });

            if (!socketServer.isAlive()) {
                try {
                    socketServer.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        Matcher matcher = pattern.matcher(uri);
        if (matcher.find()) {
            String key = matcher.group();
            System.out.println("Key:" + key);
            String[] strs = key.split("\\.");
            if (strs.length >= 1) {
                String mimeType = strs[strs.length - 1];
                if (fileTypes.containsKey(mimeType)) {
                    File file = new File(HTML_ROOT_DIR + key);
                    Response res = mimeFileResponse(file, mimeType);
                    res.addHeader("Expires", new Date(System.currentTimeMillis() + 86400000).toGMTString());
                    return res;
                } else {
                    return newFixedLengthResponse(Status.INTERNAL_ERROR, mimeTypes().get("md"), "error for " + key);
                }

            }


        }

        String responseString = "";
        return newFixedLengthResponse(Status.OK, MIME_JSON, responseString);
    }


    private Response mimeFileResponse(File file, String mimeType) {
        if (file.exists()) {
            InputStream inputStream = null;
            try {
                inputStream = new FileInputStream(file);
                return Response.newFixedLengthResponse(Status.OK, fileTypes.get(mimeType), inputStream, inputStream.available());
            } catch (FileNotFoundException e) {
                return newFixedLengthResponse(Status.INTERNAL_ERROR, mimeTypes().get("md"), "FileNotFoundException " + file.getAbsolutePath());
            } catch (IOException e) {
                return newFixedLengthResponse(Status.INTERNAL_ERROR, mimeTypes().get("md"), "IOException " + file.getAbsolutePath());
            }


        } else {
            file = new File(HTML_ROOT_DIR + "/404.html");
            if (!file.exists()) {
                return newFixedLengthResponse(Status.INTERNAL_ERROR, mimeTypes().get("md"), "FileNotFoundException " + file.getAbsolutePath());
            }
            return mimeFileResponse(file, "html");
        }
    }


    public static Response newFixedLengthResponse(IStatus status, String mimeType, String message) {
        Response response = Response.newFixedLengthResponse(status, mimeType, message);
        response.addHeader("Accept-Ranges", "bytes");
        return response;
    }

    DatagramSocket socket = null;
    boolean isReporterRunning = true;


}
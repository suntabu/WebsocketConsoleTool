package org.suntabu.commands;

/*
 * #%L
 * NanoHttpd-Webserver
 * %%
 * Copyright (C) 2012 - 2017 nanohttpd
 * %%
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * 3. Neither the name of the nanohttpd nor the names of its contributors
 *    may be used to endorse or promote products derived from this software without
 *    specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.nanohttpd.protocols.http.NanoHTTPD;
import org.nanohttpd.protocols.http.response.Response;
import org.nanohttpd.protocols.http.response.Status;
import org.suntabu.Server.ConsoleServer;
import org.suntabu.commands.anno.Command;
import org.suntabu.commands.anno.CommandProcessor;

/**
 * Created by gouzhun on 2016/11/22.
 */

public class Commands {

    private CommandProcessor processor;

    private static final int JSON_INDENT = 2;

    public Commands() {
        processor = new CommandProcessor(this);
    }

    @Command(value = "hello",description = "say hello")
    private void hello(String[] args){
        ConsoleManager.append("\t\tHi  ^ ^");
    }


    @Command(value = "clear", description = "clear console")
    private void clearConsole(String[] args) {
        ConsoleManager.clear();
    }

    @Command(value = "help", description = "command info for help")
    private void help(String[] args) {
        processor.help();
    }

    @Command(value = "cc", description = "clear cache")
    public void clearCache(String[] args) {
        try {
            // do clean things


            ConsoleManager.append("\t\tdone!");
        } catch (Exception e) {
            ConsoleManager.append("error: " + e.getMessage());

        }
    }

    public String handle(String command) {
        ConsoleManager.append("> " + command);

        return processor.handle(command);
    }

}

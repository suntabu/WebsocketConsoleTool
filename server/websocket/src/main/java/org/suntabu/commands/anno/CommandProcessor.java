package org.suntabu.commands.anno;

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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import org.nanohttpd.protocols.http.response.Response;
import org.suntabu.commands.Commands;
import org.suntabu.commands.ConsoleManager;

/**
 * Created by Administrator on 2016/11/24.
 */

public class CommandProcessor {

    public CommandProcessor(Commands commands) {
        registeredCommand = new ArrayList<>();
        registerCommands();
        commandsInstance = commands;
    }

    private Commands commandsInstance;

    private ArrayList<CommandWrapper> registeredCommand;

    private void registerCommands() {
        Method[] ms = Commands.class.getDeclaredMethods();
        for (Method method : ms) {
            if (method.isAnnotationPresent(Command.class)) {
                if (!method.isAccessible()) {
                    method.setAccessible(true);
                }
                Command command = method.getAnnotation(Command.class);
                if (command != null) {
                    CommandWrapper cw = new CommandWrapper(command, method);
                    registeredCommand.add(cw);
                }
            }
        }
    }

    public void help() {
        for (int i = 0; i < registeredCommand.size(); i++) {
            CommandWrapper cw = registeredCommand.get(i);
            ConsoleManager.append(String.format("  %-15s", cw.command.value()) + " ---" + cw.command.description());
        }
    }

    public class CommandWrapper {

        public Command command;

        public Method method;

        public CommandWrapper(Command command, Method method) {
            this.command = command;
            this.method = method;
        }
    }

    public String handle(String command) {
        CommandWrapper commandWrapper = null;
        String[] strings = command.split(" ");

        if (strings.length > 0) {

            String commandStr = strings[0].trim();

            for (int i = 0; i < registeredCommand.size(); i++) {
                CommandWrapper cw = registeredCommand.get(i);
                if (cw.command.value().equalsIgnoreCase(commandStr)) {
                    commandWrapper = cw;

                }
            }

            if (commandWrapper != null) {
                try {
                    Object obj = commandWrapper.method.invoke(commandsInstance, new Object[]{
                        command.replace(commandStr, "").trim().split(" ")
                    });
                    String response = (String) obj;
                    if (response != null) {
                        return response;
                    }
                } catch (IllegalAccessException e) {
                    ConsoleManager.append(e.getMessage());
                } catch (InvocationTargetException e) {
                    ConsoleManager.append(e.getMessage());
                }
            } else {
                ConsoleManager.append("not found " + strings[0]);
            }
        } else {
            ConsoleManager.append("nothing to show...");
        }

        return ConsoleManager.logStr();
    }
}

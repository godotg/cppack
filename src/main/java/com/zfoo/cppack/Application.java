/*
 * Copyright (C) 2020 The zfoo Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package com.zfoo.cppack;

import com.zfoo.monitor.util.OSUtils;
import com.zfoo.protocol.collection.ArrayUtils;
import com.zfoo.protocol.util.FileUtils;
import com.zfoo.protocol.util.StringUtils;

import java.io.File;
import java.util.stream.Collectors;

/**
 * @author jaysunxiao
 * @version 1.0
 * @since 2021-01-20 16:00
 */
public class Application {


    public static void main(String[] args) {
        if (ArrayUtils.length(args) != 1) {
            System.out.println(StringUtils.format("语法格式[java -jar cppack.jar main|test]"));
            return;
        }

        var arg = args[0].equals("test") ? "main_test" : args[0];

        System.out.println("------------------------------------------------------");
        System.out.println(StringUtils.format("arg = [{}]", arg));
        System.out.println("------------------------------------------------------");

        FileUtils.createDirectory("bin");

        FileUtils.deleteFile(new File((StringUtils.format("bin/{}.exe", arg))));

        var proPath = new File(FileUtils.getProAbsPath()).getAbsolutePath();
        System.out.println(StringUtils.format("proPath = [{}]", proPath));


        var files = FileUtils.getAllReadableFiles(new File(proPath))
                .stream()
                .filter(it -> it.getName().endsWith(".h") || it.getName().endsWith(".cpp"))
                .map(it -> StringUtils.substringAfterFirst(it.getAbsolutePath(), proPath))
                .map(it -> {
                    if (it.startsWith("/")) {
                        return StringUtils.substringAfterFirst(it, "/");
                    }
                    if (it.startsWith("\\")) {
                        return StringUtils.substringAfterFirst(it, "\\");
                    }
                    return it;
                })
                .filter(it -> !arg.equals("main") || !it.startsWith("test"))
                .filter(it -> it.contains("/") || it.contains("\\"))
                .filter(it -> !it.startsWith("cmake-build-debug"))
                .collect(Collectors.toList());

        var cppFiles = StringUtils.joinWith(StringUtils.SPACE, files.toArray());
        var gppCommand = StringUtils.format("g++ -I ./ {}.cpp {} -o bin/{}", arg, cppFiles, arg);

        System.out.println(StringUtils.format("[{}]", gppCommand));
        System.out.println("------------------------------------------------------");

        var result = OSUtils.execCommand(gppCommand);
        System.out.println(result);
        System.out.println("------------------------------------------------------");

        result = OSUtils.execCommand(StringUtils.format("bin/{}.exe", arg));
        System.out.println(result);

        System.exit(0);
    }

}

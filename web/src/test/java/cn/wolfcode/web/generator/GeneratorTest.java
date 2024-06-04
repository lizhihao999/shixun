/*
 * Copyright (c) 2011-2020, baomidou (jobob@qq.com).
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * <p>
 * https://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package cn.wolfcode.web.generator;

import com.baomidou.mybatisplus.annotation.DbType;
import link.ahsj.generator.utils.GeneratorUtils;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.stereotype.Service;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.FileSystemUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

//import org.omg.CORBA.Environment;


/**
 * 代码生成器演示
 *
 * @author hubin
 * @since 2016-12-01
 */
@Service
@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
public class GeneratorTest {
//    @Autowired
//    private TbDeleFileMapper delefileMapper;

    /**
     * MySQL 生成演示
     */
//
    public  String base = "visit";
    @Test
    public  void generator() throws IOException {


        //删除createFile目录下的所有文件
        FileSystemUtils.deleteRecursively(new File(pathsConfig.createFile));
        //生成模板文件
        GeneratorUtils.generator(
                "web",
                "cn.wolfcode.web.modules",
                base,
                DbType.MYSQL,
                pathsConfig.createFile,
                // 页面上的父上下文
                // 自己的上下文
                "1111",
                "联系人走访管理",
                null,
                base,
                base + "_info",
                "辉将",
                "127.0.0.1",
                "3306",
                "nojoke",
                "root",
                "123456",
                new String[]{"bmd_", "mp_", "SYS_"},
                new String[]{"tb_" + base}, false
        );


        Map<String, String> map = new HashMap<>();
        map.put(pathsConfig.s_modules, pathsConfig.tg_modules);
        map.put(pathsConfig.s_html, pathsConfig.tg_html);
        map.put(pathsConfig.s_js, pathsConfig.tg_js);


        //文件复制
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (hasDuplicateFolderName(Paths.get(entry.getValue()), base)) {
                System.out.println("文件存在");
                return;
            }
            File srcDir = new File(entry.getKey());
            File destDir = new File(entry.getValue());
            FileUtils.copyDirectory(srcDir, destDir);
            System.out.println("文件夹复制成功！");

        }

        DeleteErroCode();
//        //运行mysql 创建
        runMysql(pathsConfig.sqlPath, pathsConfig.url, "root", "123456");


    }

    /**
     * 检查指定文件夹下是否存在与给定名称相同的文件夹
     *
     * @param parentDir  父文件夹的路径
     * @param targetName 要查找的文件夹名称
     * @return 如果找到重名的文件夹，则返回true；否则返回false
     * @throws IOException 如果在遍历目录时发生I/O错误
     */
    public  boolean hasDuplicateFolderName(Path parentDir, String targetName) throws IOException {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(parentDir)) {
            for (Path path : stream) {
                if (Files.isDirectory(path) && path.getFileName().toString().equals(targetName)) {
                    // 如果找到与targetName相同的文件夹名称，则返回true
                    return true;
                }
            }
        }
        // 如果没有找到，则返回false
        return false;
    }


    public  void runMysql(String sqlPath, String url, String username, String password) {
        try (Connection conn = DriverManager.getConnection(url, username, password);
             BufferedReader reader = new BufferedReader(new FileReader(sqlPath))) {

            Statement stmt = conn.createStatement();
            String sqlLine;
            StringBuilder sql = new StringBuilder();

            while ((sqlLine = reader.readLine()) != null) {
                // 去除行首和行尾的空白字符
                String trimmedLine = sqlLine.trim();

                // 检查当前行是否是注释（以--开头）
                if (trimmedLine.startsWith("--")) {
                    // 是注释，不执行任何操作
                    continue;
                }

                // 如果当前行不是注释，则添加到SQL语句中
                sql.append(trimmedLine);

                // 检查当前行是否是一个完整的SQL语句（以;结尾）
                if (trimmedLine.endsWith(";")) {
                    // 移除末尾的分号
                    sql.setLength(sql.length() - 1);

                    // 执行SQL语句
                    String fullSql = sql.toString();
                    if (!fullSql.isEmpty()) {
                        stmt.execute(fullSql); // 或者使用 stmt.executeUpdate(fullSql) 对于DML语句
                        System.out.println("执行SQL:");
                        System.out.println(fullSql);
                    }

                    // 重置StringBuilder以准备处理下一个SQL语句
                    sql.setLength(0);
                }
            }

            // 如果SQL文件以一个不完整的SQL语句结束（没有分号），并且该语句不是注释
            if (sql.length() > 0) {
//                TbDeleFile df=new TbDeleFile();
//                df.setName("tb_"+base);
//                df.setStr(sql.toString());
//                System.out.println(df);
//                System.out.println(sql.toString().length());
//
//                delefileMapper.insert(df);


                // 这里你可能需要处理或记录一个不完整的SQL语句
                // 例如，你可以抛出一个异常或记录一个错误
                System.err.println("警告：文件以一个不完整的SQL语句结束：" + sql.toString());

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public  String capitalizeAfterUnderscoresAndRemove(String original) {
        if (original == null || original.length() == 0) {
            return original; // 或者返回一个空字符串或抛出异常
        }

        StringBuilder sb = new StringBuilder();
        boolean capitalizeNext = true; // 标记是否应该大写下一个字母

        for (int i = 0; i < original.length(); i++) {
            char c = original.charAt(i);

            // 如果遇到'_'，不添加到结果中，并设置下一个字符为大写
            if (c == '_') {
                capitalizeNext = true;
                continue;
            }

            // 根据是否应该大写来添加字符
            if (capitalizeNext) {
                sb.append(Character.toUpperCase(c));
                capitalizeNext = false;
            } else {
                sb.append(Character.toLowerCase(c));
            }
        }

        // 因为首字母总是大写，所以不需要额外处理
        return sb.toString();
    }

    public  void DeleteErroCode() {
        //替换掉list.ftl 首代码
        Path ftlPath = Paths.get(pathsConfig.tg_html + "//" + base + "//" + base + "_info//list.ftl"); // 替换为你的.ftl文件路径

        try {
            String content = new String(Files.readAllBytes(ftlPath));
            String modifiedContent = content.replace("<#assign sec=JspTaglibs[\"http://http://www.gdyuhui.net/security/tags\"]/>",
                    "<#assign sec=JspTaglibs[\"http://http://www.ahsj.link/security/tags\"]/>");

            Files.write(ftlPath, modifiedContent.getBytes());
            System.out.println("File modified successfully!");
        } catch (IOException e) {
            e.printStackTrace();
        }

        Path filePath = Paths.get(pathsConfig.newController + "//" + base +"//"+ "controller//Tb" + capitalizeAfterUnderscoresAndRemove(base) + "Controller.java"); // 指定你的.java文件路径
        System.out.println(filePath);
        String[] stringsToRemove = {"import com.nebula.commons.modules.log.LogModules;",
                "import com.nebula.web.modules.BaseController;",
                "import com.nebula.web.commons.utils.SystemCheckUtils;",
                 "import com.nebula.web.commons.utils.LayuiTools;",
                 "import com.nebula.web.commons.entity.LayuiPage;"}; // 要删除的字符串列表

        try {
            String content = new String(Files.readAllBytes(filePath), StandardCharsets.UTF_8);
            for (String strToRemove : stringsToRemove) {
                content = content.replace(strToRemove, ""); // 逐个删除字符串
            }

            // 将修改后的内容写回到原文件（或你可以指定另一个文件）
            Files.write(filePath, content.getBytes(StandardCharsets.UTF_8));

            System.out.println("Strings removed successfully!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //Id生成策略
    @Test
    public void saveTest(){
//        TbDeleFile df=new TbDeleFile();
////        df.setId(3);
//        df.setName("tb");
//        df.setStr("abcd");
//        System.out.println(df);
//
//
//       delefileMapper.insert(df);

    }
    @Test
    public void runMysqlStr(){
//        TbDeleFile df=new TbDeleFile();
////        df.setId(3);
//        df.setName("tb");
//        df.setStr("abcd");
//        System.out.println(df);
//
//
//        delefileMapper.insert(df);

    }





}
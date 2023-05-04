<h1 align="center"><a href="https://github.com/shalousun/smart-doc-maven-plugin" target="_blank">Smart-Doc Maven Plugin</a></h1>

![maven](https://img.shields.io/maven-central/v/com.github.shalousun/smart-doc-maven-plugin)
[![License](https://img.shields.io/badge/license-Apache%202-green.svg)](https://www.apache.org/licenses/LICENSE-2.0)
![closed pull requests](https://img.shields.io/github/issues-pr-closed-raw/shalousun/smart-doc-maven-plugin)
![java version](https://img.shields.io/badge/JAVA-1.8+-green.svg)

## Introduce

`smart-doc-maven-plugin`是`smart-doc`官方团队开发的`maven`插件，该插件从`smart-doc 1.7.9`版本开始提供，
使用`smart-doc-maven-plugin`更方便用户集成到自己的项目中，集成也更加轻量，你不再需要在项目中编写单元测试来
启动`smart-doc`扫描代码分析生成接口文档。可以直接运行`maven`命令
或者是`IDE`中点击`smart-doc-maven-plugin`预设好的`goal`即可生成接口文档。
`smart-doc-maven-plugin`底层完全依赖于官方开源的`smart-doc`解析库，
因此整个使用过程中遇到问题或者是想查看完整解决方案请前往码云`smart-doc`的仓库查看`wiki`文档。

[关于smart-doc](https://gitee.com/smart-doc-team/smart-doc)

## Best Practice

smart-doc + [Torna](http://torna.cn) 组成行业领先的文档生成和管理解决方案，使用`smart-doc`无侵入完成`Java`源代码分析和提取注释生成`API`文档，自动将文档推送到`Torna`企业级接口文档管理平台。

![smart-doc+torna](https://gitee.com/smart-doc-team/smart-doc/raw/master/images/smart-doc-torna.png)

[smart-doc+Torna文档自动化](https://gitee.com/smart-doc-team/smart-doc/wikis/smart-doc与torna对接?sort_id=3695028)

## Getting started

### Add plugin

```
<plugin>
    <groupId>com.github.shalousun</groupId>
    <artifactId>smart-doc-maven-plugin</artifactId>
    <version>【最新版】</version>
    <configuration>
        <!--指定生成文档的使用的配置文件,配置文件放在自己的项目中-->
        <configFile>./src/main/resources/smart-doc.json</configFile>
        <!--指定项目名称-->
        <projectName>测试</projectName>
        <!--smart-doc实现自动分析依赖树加载第三方依赖的源码，如果一些框架依赖库加载不到导致报错，这时请使用excludes排除掉-->
        <excludes>
            <!--格式为：groupId:artifactId;参考如下-->
            <!--1.0.7版本开始你还可以用正则匹配排除,如：poi.* -->
            <exclude>com.alibaba:fastjson</exclude>
        </excludes>
        <!--自1.0.8版本开始，插件提供includes支持,配置了includes后插件会按照用户配置加载而不是自动加载，因此使用时需要注意-->
        <!--smart-doc能自动分析依赖树加载所有依赖源码，原则上会影响文档构建效率，因此你可以使用includes来让插件加载你配置的组件-->
        <includes>
            <!--格式为：groupId:artifactId;参考如下-->
            <include>com.alibaba:fastjson</include>
            <!-- 如果配置了includes的情况下， 使用了mybatis-plus的分页需要include所使用的源码包 -->
            <include>com.baomidou:mybatis-plus-extension</include>
            <!-- 如果配置了includes的情况下， 使用了jpa的分页需要include所使用的源码包 -->
            <include>org.springframework.data:spring-data-commons</include>
        </includes>
    </configuration>
    <executions>
        <execution>
            <!--如果不需要在执行编译时启动smart-doc，则将phase注释掉-->
            <phase>compile</phase>
            <goals>
                <goal>html</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

### Create a json config

在自己的项目中创建一个`json`配置文件，`smart-doc-maven-plugin`插件会根据这个配置生成项目的接口文档。
例如在项目中创建`/src/main/resources/smart-doc.json`。配置内容参考如下。

**最小配置单元:**

```
{
   "outPath": "D://md2" //指定文档的输出路径 相对路径时请写 ./ 不要写 / eg:./src/main/resources/static/doc
}
```

> 如果你想把html文档也打包到应用中随着服务一起访问，则建议你配置路径为：src/main/resources/static/doc。
[服务访问配置参考](https://gitee.com/smart-doc-team/smart-doc/wikis/smart-doc常见问题解决方法?sort_id=2457284)

仅仅需要上面一行配置就能启动`smart-doc-maven-plugin`插件，根据自己项目情况更多详细的配置参考下面。

`smart-doc`提供很多配置项，详细配置请参考[官方文档](https://smart-doc-group.github.io/#/zh-cn/diy/config?id=allconfig)

### Generated document

#### Run plugin with maven command

```
//生成html
mvn -Dfile.encoding=UTF-8 smart-doc:html
//生成markdown
mvn -Dfile.encoding=UTF-8 smart-doc:markdown
//生成adoc
mvn -Dfile.encoding=UTF-8 smart-doc:adoc
//生成postmanjson数据
mvn -Dfile.encoding=UTF-8 smart-doc:postman
// 生成 Open Api 3.0+,Since smart-doc-maven-plugin 1.1.5
mvn -Dfile.encoding=UTF-8 smart-doc:openapi
// 生成文档推送到Torna平台
mvn -Dfile.encoding=UTF-8 smart-doc:torna-rest

// Apache Dubbo Rpc文档
// Generate html
mvn -Dfile.encoding=UTF-8 smart-doc:rpc-html
// Generate markdown
mvn -Dfile.encoding=UTF-8 smart-doc:rpc-markdown
// Generate adoc
mvn -Dfile.encoding=UTF-8 smart-doc:rpc-adoc
```

**注意：** 尤其在`window`系统下，如果实际使用`maven`命令行执行文档生成，可能会出现乱码，因此需要在执行时指定`-Dfile.encoding=UTF-8`。

查看maven的编码

```
# mvn -version
Apache Maven 3.3.3 (7994120775791599e205a5524ec3e0dfe41d4a06; 2015-04-22T19:57:37+08:00)
Maven home: D:\ProgramFiles\maven\bin\..
Java version: 1.8.0_191, vendor: Oracle Corporation
Java home: D:\ProgramFiles\Java\jdk1.8.0_191\jre
Default locale: zh_CN, platform encoding: GBK
OS name: "windows 10", version: "10.0", arch: "amd64", family: "dos"
```

#### Run plugin in IDEA

当你使用`Idea`时，可以通过`Maven Helper`插件选择生成何种文档。

![idea中smart-doc-maven插件使用](https://gitee.com/smart-doc-team/smart-doc-maven-plugin/raw/master/images/idea.png "maven_plugin_tasks.png")

### Generated document example

[点击查看文档生成文档效果图](https://gitee.com/smart-doc-team/smart-doc/wikis/文档效果图?sort_id=1652819)

## Building

如果你需要自己构建，那可以使用下面命令，构建需要依赖`Java 1.8`。

```
mvn clean install -Dmaven.test.skip=true
```

## Releases

[发布记录](https://gitee.com/smart-doc-team/smart-doc-maven-plugin/blob/master/CHANGELOG.md)

## License

smart-doc-maven-plugin is under the Apache 2.0 license. See
the [LICENSE](https://gitee.com/smart-doc-team/smart-doc/blob/master/license.txt) file for details.

**注意：** smart-doc源代码文件全部带有版权注释，使用关键代码二次开源请保留原始版权，否则后果自负！

## Who is using

>
排名不分先后，更多接入公司，欢迎在[https://gitee.com/smart-doc-team/smart-doc/issues/I1594T](https://gitee.com/smart-doc-team/smart-doc/issues/I1594T)
登记（仅供开源用户参考）

![IFLYTEK](https://raw.githubusercontent.com/shalousun/smart-doc/master/images/known-users/iflytek.png)
&nbsp;&nbsp;<img src="https://raw.githubusercontent.com/smart-doc-group/smart-doc/master/images/known-users/oneplus.png" title="OnePlus" width="83px" height="83px"/>
&nbsp;&nbsp;<img src="https://raw.githubusercontent.com/smart-doc-group/smart-doc/master/images/known-users/xiaomi.png" title="Xiaomi" width="170px" height="83px"/>
&nbsp;&nbsp;<img src="https://raw.githubusercontent.com/smart-doc-group/smart-doc/master/images/known-users/neusoft.png" title="东软集团" width="170px" height="83px"/>
&nbsp;&nbsp;<img src="https://www.hand-china.com/static/img/hand-logo.svg" title="上海汉得信息技术股份有限公司" width="260px" height="83px"/>
&nbsp;&nbsp;<img src="https://raw.githubusercontent.com/smart-doc-group/smart-doc/master/images/known-users/shunfeng.png" title="顺丰" width="83px" height="83px"/>
<img src="https://raw.githubusercontent.com/smart-doc-group/smart-doc/master/images/known-users/zhongkezhilian.png" title="zhongkezhilian" width="272px" height="83px"/>
&nbsp;&nbsp;<img src="https://gitee.com/smart-doc-team/smart-doc/raw/master/images/known-users/mafenwo.png" title="马蜂窝" width="150px" height="83px"/>
<img src="https://raw.githubusercontent.com/smart-doc-group/smart-doc/master/images/known-users/yuanmengjiankang.png" title="yuanmengjiankang" width="260px" height="83px"/>
&nbsp;&nbsp;
<img src="https://raw.githubusercontent.com/smart-doc-group/smart-doc/master/images/known-users/tianbo-tech.png" title="tianbo tech" width="127px" height="70px"/>

## Contact

愿意参与构建`smart-doc`或者是需要交流问题可以加入qq群：

<img src="https://gitee.com/smart-doc-team/smart-doc/raw/master/images/smart-doc-qq.png" title="qq群" width="200px" height="200px"/>

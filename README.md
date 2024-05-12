<h1 align="center">Smart-Doc Maven Plugin</h1>

![maven](https://img.shields.io/maven-central/v/com.ly.smart-doc/smart-doc-maven-plugin)
[![License](https://img.shields.io/badge/license-Apache%202-green.svg)](https://www.apache.org/licenses/LICENSE-2.0)
![number of issues closed](https://img.shields.io/github/issues-closed-raw/smart-doc-group/smart-doc-maven-plugin)
![closed pull requests](https://img.shields.io/github/issues-pr-closed-raw/smart-doc-group/smart-doc-maven-plugin)
![java version](https://img.shields.io/badge/JAVA-1.8+-green.svg)
[![chinese](https://img.shields.io/badge/chinese-中文文档-brightgreen)](https://github.com/smart-doc-group/smart-doc-maven-plugin/blob/master/README_CN.md)

## Introduce

smart-doc-maven-plugin is a `maven` plugin developed by the smart-doc official team.
This plugin is available from smart-doc 1.7.9.
Using smart-doc-maven-plugin makes it easier to integrate smart-doc into your project, and integration is more
lightweight.
You no longer need to write unit tests in your project to
Start smart-doc to scan source code analysis and generate API documents.
You can run the `maven` command directly or click on the preset` goal` of the smart-doc-maven-plugin in the IDE to
generate API documentation.
smart-doc-maven-plugin will also make smart-doc's ability to generate API documentation more powerful.
[About smart-doc](https://smart-doc-group.github.io/#/)

## Best Practice

smart-doc + [Torna](http://torna.cn) form an industry-leading document generation and management solution, using
smart-doc to complete Java source code analysis and extract annotations to generate API documents without intrusion, and
automatically push the documents to the Torna enterprise-level interface document management platform.

![smart-doc+torna](https://raw.githubusercontent.com/smart-doc-group/smart-doc/master/images/smart-doc-torna-en.png)

## Getting started

### Add plugin

```
<plugin>
    <groupId>com.ly.smart-doc</groupId>
    <artifactId>smart-doc-maven-plugin</artifactId>
    <version>[latest version]</version>
    <configuration>
        <!--skip option is used to disable plugin in child module-->
        <!--<skip>true</skip>-->
        <!--Specify the configuration file used to generate the document-->
        <configFile>./src/main/resources/smart-doc.json</configFile>
        <!--smart-doc implements automatic analysis of the dependency tree to load the source code of third-party dependencies. If some framework dependency libraries are not loaded, an error is reported, then use excludes to exclude-->
        <excludes>
            <!--The format is: groupId: artifactId; refer to the following-->
            <!--since 1.0.7 version you can also use regular matching to exclude, such as: poi. *-->
            <exclude>com.google.guava:guava</exclude>
        </excludes>
        <!--Since version 1.0.8, the plugin provides includes support-->
        <!--smart-doc can automatically analyze the dependency tree to load all dependent source code. In principle, it will affect the efficiency of document construction, so you can use includes to let the plugin load the components you configure.-->
        <includes>
            <!--The format is: groupId: artifactId; refer to the following-->
            <include>com.alibaba:fastjson</include>
        </includes>
    </configuration>
    <executions>
        <execution>
            <!--Comment out phase if you don't need to start smart-doc when compiling-->
            <phase>compile</phase>
            <goals>
                <!--smart-doc provides html, openapi, markdown, adoc and other goals-->
                <goal>html</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

### Create a json config

Create a json configuration file in your project. The smart-doc-maven-plugin plugin will use this configuration
information.
For example, create `/src/main/resources/smart-doc.json` in the project.
The configuration contents are as follows.

**Minimize configuration:**

```
{
   "allInOne": true, // whether to merge documents into one file, generally recommended as true
   "isStrict": false,//If the strict mode is set to true, Smart-doc forces that the public method in each interface in the code has a comment.
   "outPath": "/src/main/resources" //Set the api document output path.
}
```

Only three configurations items are needed to generate API documentation using smart-doc-maven-plugin. In fact, only
outPath must be configured.

**Detailed configuration content:**

smart-doc provides a lot of configuration options. For more configuration options,
please refer to the [official documentation](https://smart-doc-group.github.io/#/diy/config?id=allconfig)

### Generated document

#### Run plugin with maven command

```
mvn -Dfile.encoding=UTF-8 smart-doc:html
//  Generate document output to Markdown
mvn -Dfile.encoding=UTF-8 smart-doc:markdown
// Generate document output to Adoc
mvn -Dfile.encoding=UTF-8 smart-doc:adoc
// Generate Postman.
mvn -Dfile.encoding=UTF-8 smart-doc:postman
// build Open Api 3.0+,Since smart-doc-maven-plugin 1.1.5
mvn -Dfile.encoding=UTF-8 smart-doc:openapi
// Generate document and push to torna
mvn -Dfile.encoding=UTF-8 smart-doc:torna-rest
// Generate document output to Word.
mvn -Dfile.encoding=UTF-8 smart-doc:word
// Generate Jmeter performance pressure test scripts.
mvn -Dfile.encoding=UTF-8 smart-doc:


// Apache Dubbo RPC
// Generate html
mvn -Dfile.encoding=UTF-8 smart-doc:rpc-html
// Generate markdown
mvn -Dfile.encoding=UTF-8 smart-doc:rpc-markdown
// Generate adoc
mvn -Dfile.encoding=UTF-8 smart-doc:rpc-adoc
```

**Note:** Under the window system, if you use the maven command line to perform document generation,
non-English characters may be garbled, so you need to specify `-Dfile.encoding = UTF-8` during execution.

View maven's coding

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

On Use IntelliJ IDE, if you have added smart-doc-maven-plugin to the project,
you can directly find the plugin smart-doc plugin and click to generate API documentation.

![smart-doc-maven-plugin](https://raw.githubusercontent.com/smart-doc-group/smart-doc-maven-plugin/master/images/idea.png)

### Generated document example

#### Interface header rendering

![header](https://images.gitee.com/uploads/images/2019/1231/223538_be45f8a9_144669.png "header.png")

#### Request parameter example rendering

![request-params](https://images.gitee.com/uploads/images/2019/1231/223710_88933f55_144669.png "request.png")

#### Response parameter example renderings

![response-fields](https://images.gitee.com/uploads/images/2019/1231/223817_32bea6dc_144669.png "response.png")

## Building
- `Maven` 3.8+
- `JDK` 1.8+

you can build with the following commands.

```
mvn clean install -Dmaven.test.skip=true
```

## Who is using

These are only part of the companies using smart-doc, for reference only. If you are using smart-doc,
please [add your company here](https://github.com/smart-doc-group/smart-doc/issues/12) to tell us your scenario to make
smart-doc better.

![IFLYTEK](https://raw.githubusercontent.com/smart-doc-group/smart-doc/master/images/known-users/iflytek.png)
&nbsp;&nbsp;<img src="https://raw.githubusercontent.com/smart-doc-group/smart-doc/master/images/known-users/oneplus.png" title="一加" >
&nbsp;&nbsp;<img src="https://raw.githubusercontent.com/smart-doc-group/smart-doc/master/images/known-users/xiaomi.png" title="小米" >
&nbsp;&nbsp;&nbsp;<img src="https://raw.githubusercontent.com/smart-doc-group/smart-doc/master/images/known-users/shunfeng.png" title="顺丰">
&nbsp;&nbsp;&nbsp;<img src="https://raw.githubusercontent.com/smart-doc-group/smart-doc/master/images/known-users/ly.jpeg" title="同程旅行" width="160px" height="70px"/>
&nbsp;&nbsp;&nbsp;<img src="https://raw.githubusercontent.com/smart-doc-group/smart-doc/master/images/known-users/kuishou.png" title="快手">
&nbsp;&nbsp;&nbsp;<img src="https://raw.githubusercontent.com/smart-doc-group/smart-doc/master/images/known-users/mafengwo.png" title="马蜂窝">
&nbsp;&nbsp;<img src="https://raw.githubusercontent.com/smart-doc-group/smart-doc/master/images/known-users/yunda.png" title="韵达速递" width="192px" height="64px">
&nbsp;&nbsp;<img src="https://raw.githubusercontent.com/smart-doc-group/smart-doc/master/images/known-users/zhongtongzhiyun.png" title="中通智运">
&nbsp;&nbsp;<img src="https://raw.githubusercontent.com/smart-doc-group/smart-doc/master/images/known-users/tcsklogo.jpeg" title="同程数科" width="170px" height="64px"/>
&nbsp;&nbsp;<img src="https://raw.githubusercontent.com/smart-doc-group/smart-doc/master/images/known-users/flipboard.png" title="红板报">
&nbsp;&nbsp;<img src="https://raw.githubusercontent.com/smart-doc-group/smart-doc/master/images/known-users/dianxin.png" title="中国电信">
&nbsp;&nbsp;<img src="https://raw.githubusercontent.com/smart-doc-group/smart-doc/master/images/known-users/yidong.png" title="中国移动">
&nbsp;&nbsp;<img src="https://raw.githubusercontent.com/smart-doc-group/smart-doc/master/images/known-users/neusoft.png" title="东软集团">
&nbsp;&nbsp;<img src="https://raw.githubusercontent.com/smart-doc-group/smart-doc/master/images/known-users/zhongkezhilian.png" title="中科智链" width="240px" height="64px"/>
&nbsp;&nbsp;<img src="https://www.hand-china.com/static/img/hand-logo.svg" title="上海汉得信息技术股份有限公司" width="240px" height="64px"/>
&nbsp;&nbsp;<img src="https://raw.githubusercontent.com/smart-doc-group/smart-doc/master/images/known-users/yuanmengjiankang.png" title="远盟健康" width="230px" height="64px"/>


## License

smart-doc-maven-plugin is under the Apache 2.0 license. See
the [LICENSE](https://raw.githubusercontent.com/smart-doc-group/smart-doc-maven-plugin/master/LICENSE) file for details.

## Contact

Email： opensource@ly.com

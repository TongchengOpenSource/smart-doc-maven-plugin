<h1 align="center"><a href="https://github.com/shalousun/smart-doc-maven-plugin" target="_blank">Smart-Doc Maven Plugin</a></h1>

![maven](https://img.shields.io/maven-central/v/com.github.shalousun/smart-doc-maven-plugin)
[![License](https://img.shields.io/badge/license-Apache%202-green.svg)](https://www.apache.org/licenses/LICENSE-2.0)
![closed pull requests](https://img.shields.io/github/issues-pr-closed-raw/shalousun/smart-doc-maven-plugin)
![java version](https://img.shields.io/badge/JAVA-1.8+-green.svg)

## Introduce
smart-doc-maven-plugin是smart-doc官方团队开发的`maven`插件，该插件从smart-doc 1.7.9版本开始提供，
使用smart-doc-maven-plugin更方便用户集成到自己的项目中，集成也更加轻量，你不再需要在项目中编写单元测试来
启动smart-doc扫描代码分析生成接口文档。可以直接运行`maven`命令
或者是IDE中点击smart-doc-maven-plugin预设好的`goal`即可生成接口文档。

[关于smart-doc](https://gitee.com/smart-doc-team/smart-doc)

## Getting started
### Add plugin
```
<plugin>
    <groupId>com.github.shalousun</groupId>
    <artifactId>smart-doc-maven-plugin</artifactId>
    <version>【最新版】</version>
    <configuration>
        <!--对于maven多模块项目，插件在parent中时，触发插件会在各模块中执行一遍-->
        <!--因此在不需要执行文档任务的模块中,可以添加插件后配置skip让maven跳过smart-doc插件 -->
        <!--看不懂的同学不建议设置skip,因为报错不影响文档生成-->
        <!--<skip>true</skip>-->
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
        <!--自1.0.8版本开始，插件提供includes支持-->
        <!--smart-doc能自动分析依赖树加载所有依赖源码，原则上会影响文档构建效率，因此你可以使用includes来让插件加载你配置的组件-->
        <includes>
            <!--格式为：groupId:artifactId;参考如下-->
            <include>com.alibaba:fastjson</include>
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
在自己的项目中创建一个json配置文件，smart-doc-maven-plugin插件会根据这个配置生成项目的接口文档。
例如在项目中创建`/src/main/resources/smart-doc.json`。配置内容参考如下。

**最小配置单元:**
```
{
   "outPath": "D://md2" //指定文档的输出路径 相对路径时请写 ./ 不要写 / eg:./src/main/resources/static/doc
}
```
**详细配置说明:**
```
{
  "serverUrl": "http://127.0.0.1", //设置服务器地址,非必须
  "isStrict": false, //是否开启严格模式
  "allInOne": true,  //是否将文档合并到一个文件中，一般推荐为true
  "outPath": "D://md2", //指定文档的输出路径
  "coverOld": true,  //是否覆盖旧的文件，主要用于mardown文件覆盖
  "packageFilters": "",//controller包过滤，多个包用英文逗号隔开
  "md5EncryptedHtmlName": false,//只有每个controller生成一个html文件是才使用
  "projectName": "smart-doc",//配置自己的项目名称
  "skipTransientField": true,//目前未实现
  "sortByTitle":false,//接口标题排序，默认为false,@since 1.8.7版本开始
  "requestFieldToUnderline":true, //自动将驼峰入参字段在文档中转为下划线格式,//@since smart-doc 1.8.7 版本开始
  "responseFieldToUnderline":true,//自动将驼峰入参字段在文档中转为下划线格式,//@since  smart-doc 1.8.7 版本开始
  "inlineEnum":true,//设置为true会将枚举详情展示到参数表中，默认关闭，//@since smart-doc 1.8.8版本开始
  "recursionLimit":7,//设置允许递归执行的次数用于避免一些对象解析卡主，默认是7，正常为3次以内，//@since smart-doc 1.8.8版本开始
  "allInOneDocFileName":"index.html",//自定义设置输出文档名称, @since smart-doc 1.9.0
  "requestExample":"true",//是否将请求示例展示在文档中，默认true，@since smart-doc 1.9.0
  "responseExample":"true",//是否将响应示例展示在文档中，默认为true，@since  smart-doc 1.9.0
  "ignoreRequestParams":[ //忽略请求参数对象，把不想生成文档的参数对象屏蔽掉，@since smart-doc 1.9.2
     "org.springframework.ui.ModelMap"
  ],
  "dataDictionaries": [ //配置数据字典，没有需求可以不设置
    {
      "title": "订单状态", //数据字典的名称
      "enumClassName": "com.power.doc.enums.OrderEnum", //数据字典枚举类名称
      "codeField": "code",//数据字典字典码对应的字段名称
      "descField": "desc"//数据字典对象的描述信息字典
    }
  ],

  "errorCodeDictionaries": [{ //错误码列表，没有需求可以不设置
    "title": "title",
    "enumClassName": "com.power.doc.enums.ErrorCodeEnum", //错误码枚举类
    "codeField": "code",//错误码的code码字段名称
    "descField": "desc"//错误码的描述信息对应的字段名
  }],

  "revisionLogs": [ //设置文档变更记录，没有需求可以不设置
    {
      "version": "1.0", //文档版本号
      "status": "update", //变更操作状态，一般为：创建、更新等
      "author": "author", //文档变更作者
      "remarks": "desc" //变更描述
    }
  ],
  "customResponseFields": [ //自定义添加字段和注释，api-doc后期遇到同名字段则直接给相应字段加注释，非必须
    {
      "name": "code",//覆盖响应码字段
      "desc": "响应代码",//覆盖响应码的字段注释
      "value": "00000"//设置响应码的值
    }
  ],
  "apiObjectReplacements": [{ // 自smart-doc 1.8.5开始你可以使用自定义类覆盖其他类做文档渲染，非必须
      "className": "org.springframework.data.domain.Pageable",
      "replacementClassName": "com.power.doc.model.PageRequestDto" //自定义的PageRequestDto替换Pageable做文档渲染
  }],
  "rpcApiDependencies":[{ // 项目开放的dubbo api接口模块依赖，配置后输出到文档方便使用者集成
      "artifactId":"SpringBoot2-Dubbo-Api",
      "groupId":"com.demo",
      "version":"1.0.0"
  }],
  "rpcConsumerConfig": "src/main/resources/consumer-example.conf",//文档中添加dubbo consumer集成配置，用于方便集成方可以快速集成
  "requestHeaders": [ //设置请求头，没有需求可以不设置
    {
      "name": "token",
      "type": "string",
      "desc": "desc",
      "required": false,
      "since": "-"
    }
  ],
  "apiConstants": [{//从1.8.9开始配置自己的常量类，smart-doc在解析到常量时自动替换为具体的值
        "constantsClassName": "com.power.doc.constants.RequestParamConstant"
  }],
  "sourceCodePaths": [ //设置代码路径，默认加载src/main/java, 没有需求可以不设置
    {
      "path": "src/main/java",
      "desc": "测试"
    }
  ]
}
```
**注意：** 上面的json配置完全使用smart-doc的`ApiConfig`转化成json而来。因此项目配置也可以参考smart-doc的介绍。
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
mvn -Dfile.encoding = UTF-8 smart-doc:openapi

// Apache Dubbo Rpc文档
// Generate html
mvn -Dfile.encoding = UTF-8 smart-doc:rpc-html
// Generate markdown
mvn -Dfile.encoding = UTF-8 smart-doc:rpc-markdown
// Generate adoc
mvn -Dfile.encoding = UTF-8 smart-doc:rpc-adoc
```
**注意：** 尤其在window系统下，如果实际使用maven命令行执行文档生成，可能会出现乱码，因此需要在执行时指定`-Dfile.encoding=UTF-8`。

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
当你使用Idea时，可以通过maven Helper插件选择生成何种文档。

![idea中smart-doc-maven插件使用](https://gitee.com/smart-doc-team/smart-doc-maven-plugin/raw/master/images/idea.png "maven_plugin_tasks.png")

### Generated document example
[点击查看文档生成文档效果图](https://gitee.com/smart-doc-team/smart-doc/wikis/文档效果图?sort_id=1652819)

## Building
如果你需要自己构建，那可以使用下面命令，构建需要依赖Java 1.8。
```
mvn clean install -Dmaven.test.skip=true
```
## Releases
[发布记录](https://gitee.com/smart-doc-team/smart-doc-maven-plugin/blob/master/CHANGELOG.md)
## Other reference
- [smart-doc功能使用介绍](https://my.oschina.net/u/1760791/blog/2250962)
- [smart-doc官方wiki](https://gitee.com/smart-doc-team/smart-doc/wikis/Home?sort_id=1652800)
## License
smart-doc-maven-plugin is under the Apache 2.0 license.  See the [LICENSE](https://gitee.com/smart-doc-team/smart-doc/blob/master/license.txt) file for details.

**注意：** smart-doc源代码文件全部带有版权注释，使用关键代码二次开源请保留原始版权，否则后果自负！
## Who is using
> 排名不分先后，更多接入公司，欢迎在[https://gitee.com/smart-doc-team/smart-doc/issues/I1594T](https://gitee.com/smart-doc-team/smart-doc/issues/I1594T)登记（仅供开源用户参考）

![iFLYTEK](https://gitee.com/smart-doc-team/smart-doc/raw/master/images/known-users/iflytek.png)
&nbsp;&nbsp;<img src="https://gitee.com/smart-doc-team/smart-doc/raw/master/images/known-users/oneplus.png" title="一加" width="83px" height="83px"/>
&nbsp;&nbsp;<img src="https://gitee.com/smart-doc-team/smart-doc/raw/master/images/known-users/xiaomi.png" title="小米" width="170px" height="83px"/>
<img src="https://gitee.com/smart-doc-team/smart-doc/raw/master/images/known-users/yuanmengjiankang.png" title="远盟健康" width="260px" height="83px"/>
<img src="https://gitee.com/smart-doc-team/smart-doc/raw/master/images/known-users/zhongkezhilian.png" title="中科智链" width="272px" height="83px"/>
<img src="https://gitee.com/smart-doc-team/smart-doc/raw/master/images/known-users/puqie_gaitubao_100x100.jpg" title="普切信息科技" width="83px" height="83px"/>

## Contact
愿意参与构建smart-doc或者是需要交流问题可以加入qq群：

<img src="https://gitee.com/smart-doc-team/smart-doc/raw/master/images/smart-doc-qq.png" title="qq群" width="200px" height="200px"/>

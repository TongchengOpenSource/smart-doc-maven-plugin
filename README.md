# smart-doc-maven-plugin
## Description 
##### 基于smart-doc项目开发的maven插件

--------------------------------------------------------------------------------------------------------------
## 使用说明
### 在pom.xml 添加该插件 指定configFile位置和projectName项目名称
```
 <plugin>
            <groupId>com.smartdoc.plugin</groupId>
            <artifactId>smartdoc-maven-plugin</artifactId>
            <version>1.0</version>
            <configuration>
               <configFile>./src/main/resources/smart-doc.json</configFile>
                <projectName>测试</projectName>
            </configuration>
           <executions>
               <execution>
      <!--goal参数表示默认构建 html 类型文档 支持的goal参数有：
      <goal>html</goal>
      <goal>markDown</goal> 注意D大写
      <goal>postman</goal>
      不是必须项 详情看注意事项 2-->
                   <goals>
                       <goal>html</goal>
                   </goals>
               </execution>
           </executions>
        </plugin>
```

------------------------------------------------------------------------------------------------------------------
## ApiConfig配置项
##### 采用json格式配置<br> json文件位置为pom中configFile中配置的路径

### smart.json 文件内容：
```
//smart.json
{
   //项目地址  --> config.setServerUrl("http://localhost:8080");
  "serverUrl": "http://127.0.0.1",
  
  /*开启严格检查 开启严格检查后 要求public method必须加注释 默认false --> config.setStrict(true);
   If the strict mode is set to true, Smart-doc forces that the public method in each interface in the code has a comment.
  */
  "isStrict": false,
  
  /*设置所有文档在一个文件 --> config.setAllInOne(true);
  When AllInOne is set to true, the document generation of all interfaces is merged into a Markdown or AsciiDoc document,
   and the error code list is output to the bottom of the document.
   */
  "allInOne": true,
  
  /*文档保存路径--> config.setOutPath("d:\\md");*/
  "outPath": "D://md2",
  
  /*是否覆盖原来的md文档 allinone：true有效 --> config.setCoverOld(true);*/
  "coverOld": true,
  
  /*当有多个module时使用 非必须 --> config.setPackageFilters("com.power.doc.controller");*/
  "packageFilters": "",
  
  /*生成html时加密文档名不暴露controller的名称 默认暴露 非必要情况不要设置true */
  "md5EncryptedHtmlName": false,
  
  /*项目名称 --> config.setProjectName("Your project name");*/
  "projectName": "smart-doc",
  
  /*跳过 transient 字段 默认true 非必须*/
  "skipTransientField": true,
  
  /*数据字典 非必须*/
  "dataDictionaries": [
    {
      //字典名称
      "title": "title",
      //字典包含的枚举类型 参考（） https://github.com/shalousun/api-doc-test/blob/master/src/main/java/com/power/doc/enums/ErrorCodeEnum.java
      "enumClassName": "com.power.doc.enums.ErrorCodeEnum",
      //枚举的value
      "codeField": "code",
      //枚举的desc
      "descField": "desc"
    }
  ],
  
  /*错误码字典  配置同上 非必须*/
  "errorCodeDictionaries": [{
    "title": "title",
    "enumClassName": "com.power.doc.enums.ErrorCodeEnum",
    "codeField": "codeField",
    "descField": "descField"
  }],
  
  /*allinone：true时 有效 设置更改日志 非必须*/
  "revisionLogs": [
   { 
      //版本号
      "version": "1.0",
      //状态
      "status": "use",
      //作者
      "author": "author",
      //更改时间
      "revisionTime": "2019-10-12",
     //描述信息
      "remarks": "desc"
    }
  ],
 
 /*通用返回示例  非必须*/
  "customResponseFields": [
    {
      "name": "name",
      "desc": "desc",
      "ownerClassName": "ownerClassName",
      "value": "value"
    }
  ],
  
  /*全局请求头设置 非必须*/
  "requestHeaders": [
    {
      //请求头名称
      "name": "token",
      //数据类型
      "type": "string",
      //描述
      "desc": "desc",
      //是否必须
      "required": false,
      //版本号
      "since": "-"
    }
  ],
  
  /*扫描路径 默认扫描本项目src/main/java 多个module时需要添加path*/
  "sourceCodePaths": [
    {
       //项目加载路径 本项目为src/main/java 其他项目为全路径 example： C://program/com/test/program/src/main/java
      "path": "src/main/java",
      "desc": "测试"
    }
  ]
}
```
## 注意事项
* ### 1.当设置ErrorCodeDictionary 和 ApiDataDictionary时 保证项目target文件里包含所需的枚举类型字节码文件，否则会抛出classNotFoundException。
* ### 2.goal不是必须项，当你使用Idea时，可以通过maven Helper插件选择生成何种文档，如下图：
![image.png](https://upload-images.jianshu.io/upload_images/5118042-9cefab064ee3ea93.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
* ### 3.对于配置文件不理解的地方可以参照[smart-doc](https://gitee.com/sunyurepository/smart-doc) 项目的配置文档。


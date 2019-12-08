# smart-doc-maven-plugin
## Description 
### 基于smart-doc项目开发的maven插件
## ApiConfig配置项
### 采用json格式配置
```
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
  
  /*当生成html文件时 是否加密请求的url 默认不加密 */
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
      //字典包含的枚举类型  https://github.com/shalousun/api-doc-test/blob/master/src/main/java/com/power/doc/enums/ErrorCodeEnum.java
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
      "version": "1.0",
      "status": "use",
      "author": "author",
      "revisionTime": "2019-10-12",
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
      "name": "token",
      "type": "string",
      "desc": "desc",
      "required": false,
      "since": "-"
    }
  ],
  
  /*扫描路径 默认扫描本项目src/main/java 多个module时需要添加path*/
  "sourceCodePaths": [
    {
      "path": "src/main/java",
      "desc": "测试"
    }
  ]
}
```
##使用说明
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
                   <goals>
                       <goal>html</goal>
                   </goals>
               </execution>
           </executions>
        </plugin>
```
##注意事项
* ### 当设置ErrorCodeDictionary 和 ApiDataDictionary时 保证项目target文件里包含所需的枚举类型字节码文件，否则会抛出classNotFoundException

# 基础配置

> **logLevel**: String

Log4j的输出等级 可选项如下
- `ALL`
- `TRACE`
- `DEBUG`
- `INFO`
- `WARN`
- `ERROR`
- `FATAL`
- `OFF`

---

> **inputPath**: String

输入Jar的路径

---

> **outputPath**: String

输出Jar的路径

---

> **jdkPath**: String

编译此Jar的JDK路径 示例如下
- `C:\\Program Files\\jdk-21`
- `C:\\Program Files\\jdk-1.8`
- `/usr/local/java/jdk21`

---

> **javaVersion**: Integer

此JDK的Java版本 示例如下
- `22`
- `21`
- `17`
- `11`
- `8`
- `6`

---

> **libraries**: String[]

包含库文件的目录或库文件本身  
若填写的是目录则将会搜索此目录以及所有子目录的.jar文件  
示例如下
- `C:\\Workspace\\MySecretProject\\libs\\`
- `C:\\Workspace\\MySecretProject\\lib.jar`
- `/home/potato/Workspace/libs/`
- `/home/potato/Workspace/lib.jar`

> **注意:** 不需要添加Java的库, 已自动从**jdkPath**搜索

---

> **skipClasses**: String[]

哪些类将直接写出, 如同资源  
语法与**Exclusion.classes**相同

---

> **libraryClasses**: String[]

匹配Jar内的类, 如果匹配成功, 将会把它当作外部类  
语法与**Exclusion.classes**相同

---

> **dictionary**: DictionarySetting

字典设置

### DictionarySetting
> **valueType**: String

输入的值的类型, 默认为`preset`  
可选项如下:
- `chars` value为String, 字典将从value随机取值
- `range` value为String[] 输入的字符串应为`一个字符`或`x--x`的形式, 示例如下
  - a--z
  - A--Z
  - \u4E00--\u9FFF
  - a
  - \uFF4A
- `preset` 使用预设, 可选value如下
  - `English` 使用英文字符表
  - `Number` 使用阿拉伯数字
  - `Arabic` 使用阿拉伯文字符表
  - `Chinese` 使用汉字表
  - `ThaiPhoSym` 使用泰文的发音符号
  - `Spaces` 使用空格
> **value**: (String | String[])

输入的值, 默认为`English`

> **baseLength**: Integer

起始长度, 默认为`1`

> **blackList**: String[]

字典不会生成里面的字符串

---

> **transformers**: TransformersObject
### TransformersObject
Key应为对应的Transformer名称, Value为TransformerSetting, 只要存在, 若不在Transformer设置`disable: true`
则它默认为启用状态

### TransformerSetting
> **disable**: Boolean

默认为`true`

### NameObfuscation
混淆类名及其成员名

> **exclude**: ExclusionSetting

排除设置

> **mappingPath**: String

写出Mapping的位置 默认为`./mapping.txt`

> **renameClass**: Boolean

混淆类名 默认为`true`

> **renameField**: Boolean

混淆字段名 默认为`true`

> **renameMethod**: Boolean

混淆方法名 默认为`true`

> **multithreading**: Boolean

启用多线程 默认为`true`

> **threadPoolSize**: Integer

线程池的大小, 默认为CPU的线程数

### StringEncryption
字符串加密

### NumberEncryption
数字加密

> **doInt**: Boolean

处理Integer 默认为`true`

> **doLong**: Boolean

处理Long 默认为`true`

> **doFloat**: Boolean

处理Float 默认为`true`

> **doDouble**: Boolean

处理Double 默认为`true`

### FlowObfuscation
混淆指令流程

### GotoReplacer
替换GOTO指令

### InvokeProxy
为每一个字段和方法的调用都生成一个方法去代理

---

# 排除 Exclusion
可用`*`指代所有  
特别的, `*`默认只会指代该包下的类, 不会指定子包的类, 可以使用`**`来进行此操作

> classes: String[]

要排除的类 格式应为`"packageName/className"`  
示例如下
- `org/g0to/*` 排除`org/g0to`这个包下的所有类
- `org/g0to/**` 排除`org/g0to`这个包以及它的子包下的所有类
- `org/g0to/Main` 只排除`org/g0to/Main`这一个类

> fields: String[]

要排除的字段 格式应为`packageName/className fieldName fieldDescriptor`  
// 其实fieldDescriptor是不需要的, 但是为了整齐只能这么做...(懒)  
示例如下
- `org/g0to/Main field *` 排除`org/g0to/Main.field`这个字段
- `org/g0to/Main * *` 排除`org/g0to/Main`这个类的所有字段
- `org/g0to/* * *` 排除`org/g0to`这个包下的所有字段

> methods: String[]

要排除的方法 格式应为`packageName/className methodName methodDescriptor`
示例如下
- `org/g0to/Main method (I)V` 排除`org/g0to/Main.method(I)`这个方法
- `org/g0to/Main method **` 排除`org/g0to/Main`这个类的所有名为`method`的方法
- `org/g0to/Main * (I)J` 排除`org/g0to/Main`这个类的所有描述符为`(I)J`的方法
- `org/g0to/* * *` 排除`org/g0to`这个包下的所有方法

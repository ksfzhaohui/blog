**前言**  
最近一款游戏开发中，客户端基于lua语言开发，为了方便客户端调用原型数据，所有的原型数据都以lua表格的形式存放；对于服务器端的java语言就需要解析出lua中的数据， 转换为java对象。

**桥接的选择**  
网上大致搜索了一下，用的比较广泛的是下面这两款：  
1.luajava 官网：[http://luaforge.net/projects/luajava/](http://luaforge.net/projects/luajava/)  
2.luaj 官网：[http://www.luaj.org/luaj/3.0/README.html](http://www.luaj.org/luaj/3.0/README.html)

看luajava的官网上支持的lua写的是lua5，lua现在的最新版本已经到5.3了，应该是好久没有更新了，同时也可以看github上的源码[luajava](https://github.com/jasonsantos/luajava)，最近的一次改动是在3年前；相比较luaj虽然没有提供最新的lua5.3支持，但是已经提供到了lua5.2的支持，明显更加活跃。  
另外一点就是luajava在使用中是依赖于dll(动态链接库)的，而luaj是纯java语言

综上最终选择luaj作为桥接器  
maven引入：

```
<dependency>
    <groupId>org.luaj</groupId>
    <artifactId>luaj-jse</artifactId>
    <version>2.0.3</version>
</dependency>
```

**实例**  
1.准备一个存放数据的lua文件TbTest.lua，存放路径D:/Data

```
TbTest = {
      [1] = {1,[[name1]],{1,2},
      },
      [2] = {2,[[name2]],
      },
}
```

准备了一张表数据，里面准备了int类型，string类型以及table类型

2.提供lua对外的接口LuaToJavaBridge.lua

```
﻿LuaToJavaBridge = {}

function LuaToJavaBridge.getData(dbName,dataId,fieldIndex)
     require(dbName)
     local dbData=_G[dbName]
     
     local lineData=dbData[dataId]
     return lineData[fieldIndex]
end
```

提供了一个lua类LuaToJavaBridge，方法getData的3个参数分别是：表名，表Id和字段编号  
require(dbName) 引入需要的lua文件

3.提供一个java调用lua的类JavaToLuaBridge

```
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.JsePlatform;

public class JavaToLuaBridge {

    private static LuaValue javaToLua;

    private JavaToLuaBridge() {

    }

    public static void init(String packagePath, String javaToLuaFile,
            String javaToLuaClass) {
        LuaValue _G = JsePlatform.standardGlobals();
        _G.get("package").set("path", packagePath);

        _G.get("dofile").call(LuaValue.valueOf(javaToLuaFile));
        javaToLua = _G.get(javaToLuaClass);
    }

    public static LuaValue getData(String dbName, int dataId, int fieldIndex) {
        LuaValue result = javaToLua.get("getData").call(
                LuaValue.valueOf(dbName), LuaValue.valueOf(dataId),
                LuaValue.valueOf(fieldIndex));
        return result;
    }
}
```

init方法，提供一个lua的运行环境，lua是弱语言，所有传递给lua，或者从lua获取的都是LuaValue对象  
_G.get(“package”).set(“path”, packagePath); 用来设置lua数据的位置，这里可以设置成**D:/Data/?.lua**  
_G.get(“dofile”).call(LuaValue.valueOf(javaToLuaFile));获取dofile的对象，然后加载LuaToJavaBridge.lua文件  
最后获取LuaToJavaBridge.lua文件中的类文件

4.测试

```
public static void main(String[] args) {
        JavaToLuaBridge.init("D:/Data/?.lua", "LuaToJavaBridge.lua",
                "LuaToJavaBridge");

        int id = JavaToLuaBridge.getData("TbTest", 1, 1).checkint();
        String name = JavaToLuaBridge.getData("TbTest", 1, 2).checkjstring();

        LuaTable table = JavaToLuaBridge.getData("TbTest", 1, 3).checktable();
        int len = table.length();
        for (int i = 1; i <= len; i++) {
            int tv = table.get(i).checkint();
            System.out.println(tv);
        }
        System.out.println("id = " + id + ",name = " + name);
    }
```

首先初始化，然后获取数据文件中的数据，对于int和string类型，分别调用checkint()和checkjstring()方法转化为对于的java类型；对于table类型需要checktable()，需要注意的是所有的lua表下标都是从1开始的。

在此期间遇到一个问题，让我迷惑了半天，刚刚还运行好好的，不一会就报下面这个错了：

```
Exception in thread "main" org.luaj.vm2.LuaError: LuaToJavaBridge.lua:1: unexpected symbol
    at org.luaj.vm2.LuaValue.error(Unknown Source)
    at org.luaj.vm2.lib.BaseLib$BaseLibV.invoke(Unknown Source)
    at org.luaj.vm2.lib.VarArgFunction.call(Unknown Source)
    at com.luaj.luajTest.JavaToLuaBridge.init(JavaToLuaBridge.java:20)
    at com.luaj.luajTest.JavaToLuaBridge.main(JavaToLuaBridge.java:32)
```

大意就是里面出现了意想不到的符号；  
原因：使用了记事本进行修改，记事本修改后保存的lua文件只是UTF-8编码，但是一般来说，lua是不支持有BOM的，lua文件应该保存为UTF-8无BOM类型，而windows记事本的UTF-8是有BOM的，这就会造成错误。所以，文件存储时格式一般选择UTF-8无BOM格式  
BOM: Byte Order Mark  
UTF-8 BOM又叫UTF-8 签名,其实UTF-8 的BOM对UFT-8没有作用,是为了支援UTF-16,UTF-32才加上的BOM,BOM签名的意思就是告诉编辑器当前文件采用何种编码,方便编辑器识别,但是BOM虽然在编辑器中不显示,但是会产生输出,就像多了一个空行。

**总结**  
总体来说使用起来还是挺方便的，以上只是一个简单的例子，涉及的数据类型也没有全，不过总体的结构可以参考一下；其实如果想更加熟练的使用luaj，还是要对lua本身比较熟悉。
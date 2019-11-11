**前言**  
最近生产环境有个老项目一直内存报警，不时的还出现内存泄漏，导致需要重启服务器，已经严重影响正常服务了。

**分析**  
1.dump内存文件  
liunx使用如下命令：

```
./jmap -dump:format=b,file=heap.hprof pid
```

2.使用Eclipse Memory Analysis进行分析

![](https://static.oschina.net/uploads/space/2017/0629/171331_pOtS_159239.jpg)

异常如下：

```
t org.apache.poi.xssf.usermodel.XSSFRow.<init>(Lorg/openxmlformats/schemas/spreadsheetml/x2006/main/CTRow;Lorg/apache/poi/xssf/usermodel/XSSFSheet;)V (XSSFRow.java:68)
at org.apache.poi.xssf.usermodel.XSSFSheet.initRows(Lorg/openxmlformats/schemas/spreadsheetml/x2006/main/CTWorksheet;)V (XSSFSheet.java:157)
at org.apache.poi.xssf.usermodel.XSSFSheet.read(Ljava/io/InputStream;)V (XSSFSheet.java:132)
at org.apache.poi.xssf.usermodel.XSSFSheet.onDocumentRead()V (XSSFSheet.java:119)
at org.apache.poi.xssf.usermodel.XSSFWorkbook.onDocumentRead()V (XSSFWorkbook.java:222)
at org.apache.poi.POIXMLDocument.load(Lorg/apache/poi/POIXMLFactory;)V (POIXMLDocument.java:200)
at org.apache.poi.xssf.usermodel.XSSFWorkbook.<init>(Ljava/io/InputStream;)V (XSSFWorkbook.java:179)
```

POI在加载Excel引发了内存泄漏，中间创建了大量的对象，占用了大量的内存

3.查看上传的Excel大小  
经查看发现很多Excel大小在9M的文件

4.查看代码POI读取Excel的方式  
发现使用的是用户模式，这样会占用大量的内存；POI提供了2中读取Excel的模式，分别是：  
**用户模式：**也就是poi下的usermodel有关包，它对用户友好，有统一的接口在ss包下，但是它是把整个文件读取到内存中的，  
对于大量数据很容易内存溢出，所以只能用来处理相对较小量的数据；  
**事件模式：**在poi下的eventusermodel包下，相对来说实现比较复杂，但是它处理速度快，占用内存少，可以用来处理海量的Excel数据。

经上面分析基本可以确定问题出在使用POI的用户模式去读取Excel大文件，导致内存泄漏。

**本地重现**  
下面模拟一个600kb大小的Excel（test.xlsx），分别用两种模式读取，然后观察内存波动；

1.需要引入的库maven：

```
<dependencies>
    <dependency>
        <groupId>org.apache.poi</groupId>
        <artifactId>poi-ooxml</artifactId>
        <version>3.6</version>
    </dependency>
    <dependency>
        <groupId>com.syncthemall</groupId>
        <artifactId>boilerpipe</artifactId>
        <version>1.2.1</version>
    </dependency>
</dependencies>
```

2.用户模式代码如下：

```
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
 
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
 
public class UserModel {
 
    public static void main(String[] args) throws InterruptedException {
        try {
            Thread.sleep(5000);
            System.out.println("start read");
            for (int i = 0; i < 100; i++) {
                try {
                    Workbook wb = null;
                    File file = new File("D:/test.xlsx");
                    InputStream fis = new FileInputStream(file);
                    wb = new XSSFWorkbook(fis);
                    Sheet sheet = wb.getSheetAt(0);
                    for (Row row : sheet) {
                        for (Cell cell : row) {
                            System.out.println("row:" + row.getRowNum() + ",cell:" + cell.toString());
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            Thread.sleep(1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```

3.事件模式代码如下：

```
import java.io.InputStream;
 
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.model.SharedStringsTable;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;
 
public class EventModel {
 
    public void processOneSheet(String filename) throws Exception {
        OPCPackage pkg = OPCPackage.open(filename);
        XSSFReader r = new XSSFReader(pkg);
        SharedStringsTable sst = r.getSharedStringsTable();
 
        XMLReader parser = fetchSheetParser(sst);
        InputStream sheet2 = r.getSheet("rId1");
        InputSource sheetSource = new InputSource(sheet2);
        parser.parse(sheetSource);
        sheet2.close();
    }
 
    public XMLReader fetchSheetParser(SharedStringsTable sst) throws SAXException {
        XMLReader parser = XMLReaderFactory.createXMLReader("org.apache.xerces.parsers.SAXParser");
        ContentHandler handler = new SheetHandler(sst);
        parser.setContentHandler(handler);
        return parser;
    }
 
    private static class SheetHandler extends DefaultHandler {
        private SharedStringsTable sst;
        private String lastContents;
        private boolean nextIsString;
 
        private SheetHandler(SharedStringsTable sst) {
            this.sst = sst;
        }
 
        public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException {
            if (name.equals("c")) {
                System.out.print(attributes.getValue("r") + " - ");
                String cellType = attributes.getValue("t");
                if (cellType != null && cellType.equals("s")) {
                    nextIsString = true;
                } else {
                    nextIsString = false;
                }
            }
            lastContents = "";
        }
 
        public void endElement(String uri, String localName, String name) throws SAXException {
            if (nextIsString) {
                int idx = Integer.parseInt(lastContents);
                lastContents = new XSSFRichTextString(sst.getEntryAt(idx)).toString();
                nextIsString = false;
            }
 
            if (name.equals("v")) {
                System.out.println(lastContents);
            }
        }
 
        public void characters(char[] ch, int start, int length) throws SAXException {
            lastContents += new String(ch, start, length);
        }
    }
 
    public static void main(String[] args) throws Exception {
        Thread.sleep(5000);
        System.out.println("start read");
        for (int i = 0; i < 100; i++) {
            EventModel example = new EventModel();
            example.processOneSheet("D:/test.xlsx");
            Thread.sleep(1000);
        }
    }
}
```

具体代码来源：[http://poi.apache.org/spreadsheet/how-to.html#xssf\_sax\_api](http://poi.apache.org/spreadsheet/how-to.html#xssf_sax_api)

4.设置VM arguments：-Xms100m -Xmx100m  
UserModel运行结果直接报OutOfMemoryError，如下所示：

```
Exception in thread "main" java.lang.OutOfMemoryError: GC overhead limit exceeded
    at java.lang.String.substring(String.java:1877)
    at org.apache.poi.ss.util.CellReference.separateRefParts(CellReference.java:353)
    at org.apache.poi.ss.util.CellReference.<init>(CellReference.java:87)
    at org.apache.poi.xssf.usermodel.XSSFCell.<init>(XSSFCell.java:105)
    at org.apache.poi.xssf.usermodel.XSSFRow.<init>(XSSFRow.java:68)
    at org.apache.poi.xssf.usermodel.XSSFSheet.initRows(XSSFSheet.java:157)
    at org.apache.poi.xssf.usermodel.XSSFSheet.read(XSSFSheet.java:132)
    at org.apache.poi.xssf.usermodel.XSSFSheet.onDocumentRead(XSSFSheet.java:119)
    at org.apache.poi.xssf.usermodel.XSSFWorkbook.onDocumentRead(XSSFWorkbook.java:222)
    at org.apache.poi.POIXMLDocument.load(POIXMLDocument.java:200)
    at org.apache.poi.xssf.usermodel.XSSFWorkbook.<init>(XSSFWorkbook.java:179)
    at zh.excelTest.UserModel.main(UserModel.java:23)
```

EventModel可以正常运行，使用Java VisualVM监控结果如下：  
![](https://static.oschina.net/uploads/space/2017/0629/171525_KMbH_159239.jpg)

UserModel模式下读取600kbExcel文件直接内存溢出，看了600kbExcel文件映射到内存中还是占用了不少内存；EventModel模式下可以流畅的运行。

5.设置VM arguments：-Xms200m -Xmx200m  
UserModel可以正常运行，使用Java VisualVM监控结果如下：

![](https://static.oschina.net/uploads/space/2017/0629/171602_glEi_159239.jpg)

EventModel可以正常运行，使用Java VisualVM监控结果如下：

![](https://static.oschina.net/uploads/space/2017/0629/171624_1j5r_159239.jpg)

UserModel模式和EventModel模式都可以正常运行，但是很明显UserModel模式回收内存更加频繁，而且在cpu的占用上更高。

**总结**  
通过简单的分析以及本地运行两种模式进行比较，可以看到UserModel模式下使用的简单的代码实现了读取，但是在读取大文件时CPU和内存都不理想；  
而EventModel模式虽然代码写起来比较繁琐，但是在读取大文件时CPU和内存更加占优。
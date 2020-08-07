package com.springboot.opencv;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * 动态链接库加载类
 * 
 * @author hui.zhao
 *
 */
public class NativeLoader {

    private static final String NATIVE_PATH = "opencv";

    /**
     * 加载native dll/so
     * 
     * @throws Exception
     */
    public static void loader() throws Exception {
        Enumeration<URL> dir = Thread.currentThread().getContextClassLoader().getResources(NATIVE_PATH);
        String systemType = System.getProperty("os.name");
        String ext = (systemType.toLowerCase().indexOf("win") != -1) ? ".dll" : ".so";
        while (dir.hasMoreElements()) {
            URL url = dir.nextElement();
            String protocol = url.getProtocol();
            if ("jar".equals(protocol)) {
                JarURLConnection jarURLConnection = (JarURLConnection) url.openConnection();
                JarFile jarFile = jarURLConnection.getJarFile();
                // 遍历Jar包
                Enumeration<JarEntry> entries = jarFile.entries();
                while (entries.hasMoreElements()) {
                    JarEntry jarEntry = entries.nextElement();
                    String entityName = jarEntry.getName();
                    if (jarEntry.isDirectory() || !entityName.startsWith(NATIVE_PATH)) {
                        continue;
                    }
                    if (entityName.endsWith(ext)) {
                        loadJarNative(jarEntry);
                    }
                }
            } else if ("file".equals(protocol)) {
                File file = new File(url.getPath());
                loadFileNative(file, ext);
            }

        }
    }

    private static void loadFileNative(File file, String ext) {
        if (null == file) {
            return;
        }
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (null != files) {
                for (File f : files) {
                    loadFileNative(f, ext);
                }
            }
        }
        if (file.canRead() && file.getName().endsWith(ext)) {
            try {
                System.load(file.getPath());
                System.out.println("加载native文件 :" + file + "成功!!");
            } catch (UnsatisfiedLinkError e) {
                System.out.println("加载native文件 :" + file + "失败!!请确认操作系统是X86还是X64!!!");
            }
        }
    }

    /**
     * 
     * @param jarEntry
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private static void loadJarNative(JarEntry jarEntry) throws IOException, ClassNotFoundException {
        File path = new File(".");
        String rootOutputPath = path.getAbsoluteFile().getParent() + File.separator;
        String entityName = jarEntry.getName();
        String fileName = entityName.substring(entityName.lastIndexOf("/") + 1);
        File tempFile = new File(rootOutputPath + File.separator + entityName);
        if (!tempFile.getParentFile().exists()) {
            tempFile.getParentFile().mkdirs();
        }
        if (tempFile.exists()) {
            tempFile.delete();
        }
        InputStream in = null;
        BufferedInputStream reader = null;
        FileOutputStream writer = null;
        try {
            in = NativeLoader.class.getResourceAsStream(entityName);
            if (in == null) {
                in = NativeLoader.class.getResourceAsStream("/" + entityName);
                if (null == in) {
                    return;
                }
            }
            NativeLoader.class.getResource(fileName);
            reader = new BufferedInputStream(in);
            writer = new FileOutputStream(tempFile);
            byte[] buffer = new byte[1024];

            while (reader.read(buffer) > 0) {
                writer.write(buffer);
                buffer = new byte[1024];
            }
        } finally {
            if (in != null) {
                in.close();
            }
            if (writer != null) {
                writer.close();
            }
        }
        try {
            System.load(tempFile.getPath());
            System.out.println("加载native文件 :" + tempFile + "成功!!");
        } catch (UnsatisfiedLinkError e) {
            System.out.println("加载native文件 :" + tempFile + "失败!!请确认操作系统是X86还是X64!!!");
        }

    }
}

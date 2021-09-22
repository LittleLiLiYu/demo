package com.example.demo.util;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ZipUtil {

    /**
     * 默认缓存大小 8192
     */
    public static final int DEFAULT_BUFFER_SIZE = 2 << 12;

    /**
     * 压缩
     *
     * @param zipFile  指定压缩包
     * @param file     需要压缩的文件（夹）
     * @param onlyList 如果指定压缩的是文件夹，是否只是压缩里面文件（不包含最外层文件夹）
     * @throws Exception
     */
    public static void zip(String zipFile, String file, boolean onlyList) throws Exception {
        // 如果指定压缩包已经存在，则先删除
        FileUtil.deleteFile(zipFile);

        File inputFile = new File(file);
        ZipOutputStream out = null;
        try {
            out = new ZipOutputStream(new FileOutputStream(zipFile));
            if (inputFile.isDirectory() && onlyList) {
                // 只压缩里面的
                for (File child : inputFile.listFiles()) {
                    zip(out, child, child.getName());
                }
            } else {
                zip(out, inputFile, inputFile.getName());
            }
        } finally {
            IOUtils.closeQuietly(out);
        }
    }

    private static void zip(ZipOutputStream out, File f, String base) throws Exception {
        if (f.isDirectory()) {// 文件夹
            File[] fl = f.listFiles();
            if (fl.length == 0) {// 空文件夹
                out.putNextEntry(new ZipEntry(base + File.separator));
            }
            for (int i = 0; i < fl.length; i++) {
                zip(out, fl[i], base + File.separator + fl[i].getName()); // 递归遍历子文件（夹）
            }
        } else {// 文件
            ZipEntry entry = new ZipEntry(base);

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            FileInputStream in = null;
            BufferedInputStream bi = null;
            try {
                // 将字节流写入当前zip目录
                in = new FileInputStream(f);
                bi = new BufferedInputStream(in);
                int b;
                while ((b = bi.read()) != -1) {
                    bos.write(b);
                }
                bos.flush();
                // 写入zip包
                byte[] bytes = bos.toByteArray();
                entry.setSize(bytes.length);
                out.putNextEntry(entry);
                out.write(bytes);
                out.closeEntry();
            } finally {
                IOUtils.closeQuietly(bi);
                IOUtils.closeQuietly(in);
                IOUtils.closeQuietly(bos);
            }
        }
    }

    /**
     * 解压, jdk自带实现类
     *
     * @param zipFile zip文件
     * @param destDir 解压到指定目录
     * @throws Exception
     */
    public static void unZip(String zipFile, String destDir) throws Exception {
        ZipInputStream zin = new ZipInputStream(new FileInputStream(zipFile));// 输入源zip路径
        BufferedInputStream bin = new BufferedInputStream(zin);
        try {
            File file = new File(destDir);
            ZipEntry entry = zin.getNextEntry();
            while (entry != null) {
                file = new File(file, entry.getName());
                if (entry.isDirectory()) {// 文件夹
                    if (!file.exists()) {
                        file.mkdirs();
                    }
                } else {
                    file = new File(destDir, entry.getName());
                    if (!file.getParentFile().exists()) {
                        file.getParentFile().mkdirs();
                    }
                    if (file.exists()) {
                        FileUtil.deleteFile(file.getAbsolutePath());
                    }
                    file.createNewFile();
                    FileOutputStream out = new FileOutputStream(file);
                    BufferedOutputStream bout = new BufferedOutputStream(out);
                    try {
                        int b;
                        while ((b = bin.read()) != -1) {
                            bout.write(b);
                        }
                        bout.flush();
                    } finally {
                        bout.close();
                        out.close();
                    }
                }
                entry = zin.getNextEntry();
            }
        } finally {
            bin.close();
            zin.close();
        }
    }

    /**
     * 合并压缩包
     *
     * @param destZip 合并后的压缩包
     * @param zips    待合并的压缩包
     * @throws Exception
     */
    public static void combineZips(String destZip, String... zips) throws Exception {
        String path = System.getProperty("java.io.tmpdir") + File.separator + "combineZipsTmp";
        // LOG.debug(path);
        FileUtil.deleteDirectory(path);
        new File(path).mkdirs();
        for (String zip : zips) {
            // LOG.debug("zip:{}", zip);
            unZip(zip, path);
        }
        zip(destZip, path, true);
        FileUtil.deleteDirectory(path);
    }

    /**
     * zip解压
     * @param srcFilePath        zip源文件
     * @param targetDirPath     解压后的目标文件夹
     * @throws RuntimeException 解压失败会抛出运行时异常
     */
    @Deprecated
    public static void unzip(String srcFilePath, String targetDirPath) throws RuntimeException {
        unzip(new File(srcFilePath), new File(targetDirPath));
    }

    /**
     * zip解压
     * @param srcFile        zip源文件
     * @param targetDir     解压后的目标文件夹
     * @throws RuntimeException 解压失败会抛出运行时异常
     */
    @Deprecated
    public static void unzip(File srcFile, File targetDir) throws RuntimeException {
        // 判断源文件是否存在
        if (!srcFile.exists()) {
            throw new RuntimeException(srcFile.getPath() + "所指文件不存在");
        }
        if (!targetDir.exists()) {
            targetDir.mkdirs();
        }
        if (!targetDir.isDirectory()) {
            targetDir.delete();
            targetDir.mkdirs();
        }
        // 开始解压
        ZipFile zipFile = null;
        try {
            zipFile = new ZipFile(srcFile);
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                File targetFile = new File(targetDir.getPath() + File.separator + entry.getName());
                // 如果是文件夹，就创建个文件夹
                if (entry.isDirectory()) {
                    targetFile.mkdirs();
                } else {
                    // 如果是文件，就先创建一个文件，然后用io流把内容copy过去
                    // 保证这个文件的父文件夹必须要存在
                    if(!targetFile.getParentFile().exists()){
                        targetFile.getParentFile().mkdirs();
                    }
                    targetFile.createNewFile();
                    // 将压缩文件内容写入到这个文件中
                    writeFromStream(getInputStream(zipFile, entry), targetFile);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("unzip error from ZipUtils", e);
        } finally {
            IOUtils.closeQuietly(zipFile);
        }
    }

    public static File unzip(File zipFile, String descDir) throws IOException {
        try (ZipArchiveInputStream inputStream = getZipFile(zipFile)) {
            File pathFile = new File(descDir);
            if (!pathFile.exists()) {
                pathFile.mkdirs();
            }
            ZipArchiveEntry entry = null;
            while ((entry = inputStream.getNextZipEntry()) != null) {
                System.out.println(entry.getName());
                if (entry.isDirectory()) {
                    File directory = new File(descDir, entry.getName());
                    directory.mkdirs();
                } else {
                    OutputStream os = null;
                    try {
                        os = new BufferedOutputStream(new FileOutputStream(new File(descDir, entry.getName())));
                        //输出文件路径信息
//                        LOG.info("解压文件的当前路径为:{}", descDir + entry.getName());
                        IOUtils.copy(inputStream, os);
                    } finally {
                        IOUtils.closeQuietly(os);
                    }
                }
            }
            final File[] files = pathFile.listFiles();
            if (files != null && files.length == 1 && files[0].isDirectory()) {
                // 说明只有一个文件夹
                FileUtils.copyDirectory(files[0], pathFile);
//                //免得删除错误， 删除的文件必须在/data/demand/目录下。
//                boolean isValid = files[0].getPath().contains("/data/www/");
//                if (isValid) {
//                    FileUtils.forceDelete(files[0]);
//                }
            }
            return pathFile;
        } catch (IOException e) {
            throw e;
        }
    }

    public static File unzip(InputStream inputStream, String descDir) throws IOException {
//        压缩文件内部包含中文, 所以这里使用 GBK
        try (ZipArchiveInputStream zipArchiveInputStream = new ZipArchiveInputStream(inputStream, "GBK")) {
            File pathFile = new File(descDir);
            if (!pathFile.exists()) {
                pathFile.mkdirs();
            }
            ZipArchiveEntry entry = null;
            while ((entry = zipArchiveInputStream.getNextZipEntry()) != null) {
                System.out.println(entry.getName());
                if (entry.isDirectory()) {
                    File directory = new File(descDir, entry.getName());
                    directory.mkdirs();
                } else {
                    OutputStream os = null;
                    try {
                        File tempFile = new File(descDir, entry.getName());
                        File tempDir = tempFile.getParentFile();
                        if (!tempDir.exists()) {
                            tempDir.mkdirs();
                        }
                        os = new BufferedOutputStream(new FileOutputStream(tempFile));
                        //输出文件路径信息
//                        LOG.info("解压文件的当前路径为:{}", descDir + entry.getName());
                        IOUtils.copy(zipArchiveInputStream, os);
                    } finally {
                        IOUtils.closeQuietly(os);
                    }
                }
            }
            final File[] files = pathFile.listFiles();
            if (files != null && files.length == 1 && files[0].isDirectory()) {
                // 说明只有一个文件夹
                FileUtils.copyDirectory(files[0], pathFile);
//                //免得删除错误， 删除的文件必须在/data/demand/目录下。
//                boolean isValid = files[0].getPath().contains("/data/www/");
//                if (isValid) {
//                    FileUtils.forceDelete(files[0]);
//                }
            }
            return pathFile;
        } catch (IOException e) {
            throw e;
        }
    }

    private static ZipArchiveInputStream getZipFile(File zipFile) throws IOException {
        System.out.println(zipFile);
        return new ZipArchiveInputStream(new BufferedInputStream(new FileInputStream(zipFile)), "GBK");
    }

    private static void writeFromStream(InputStream in, File outFile) throws IOException {
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(outFile);
            int len;
            byte[] buf = new byte[DEFAULT_BUFFER_SIZE];
            while ((len = in.read(buf)) != -1) {
                out.write(buf, 0, len);
            }
        } catch (IOException e) {
            throw e;
        } finally {
            IOUtils.closeQuietly(out);
            IOUtils.closeQuietly(in);
        }
    }

    private static InputStream getInputStream(ZipFile zipFile, ZipEntry entry) throws IOException {
        return zipFile.getInputStream(entry);
    }

    public static void main(String[] args) throws IOException {
//        System.setProperty("sun.zip.encoding", System.getProperty("sun.jnu.encoding"));
        String zip = "F:\\test\\罗江收费查询.zip";
        String target = "F:\\test\\unzip";
//        unZip(zip, target);
        unzip(new File(zip), target);
        System.out.println(System.getProperty("user.dir"));
    }
}

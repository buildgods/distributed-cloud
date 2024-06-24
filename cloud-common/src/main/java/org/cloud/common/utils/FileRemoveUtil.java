package org.cloud.common.utils;

import org.cloud.common.component.EnvComponent;
import org.springframework.util.FileSystemUtils;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.EnumSet;

public class FileRemoveUtil {

    /**
     * 删除项目所在目录的文件
     * @param fileName 文件名称
     * @param pathName 文件夹名称
     * @param sonName  子文件夹名
     * @return
     */
    public static boolean deleteFile(String fileName,String pathName,String sonName){
        // 文件路径
        String projectDirectory =System.getProperty("user.dir");
        // 根据图片名称删除本地存储的文件
        Path filePath = Paths.get(projectDirectory , pathName , sonName, fileName);
        return delete(filePath);

    }

    /**
     * 删除kkFileView的file包中的文件
     * @param fileName 文件名
     * @return
     */
    public static boolean deleteKKFile(String fileName){
        String directoryPath = EnvComponent.getDirectoryPath();
        // 根据文件名称删除本地存储的文件
        // 判断文件名后缀是不是以.pptx或者.docx结尾的，是文件进行重命名为.pdf结尾的
        if(fileName.endsWith(".pptx")|| fileName.endsWith(".docx")){
            fileName = replaceFileExtension(fileName,"pdf");
        } else if (fileName.endsWith(".xlsx")) {
            fileName = replaceFileExtension(fileName,"html");
        }
        Path directory = Paths.get(directoryPath, fileName);
        // 删除文件
        if(!delete(directory)) return false;
        // 去除文件名后缀，然后判断这个目录是不是文件夹
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex != -1) {
            fileName = fileName.substring(0, lastDotIndex);
        }
        // 获取去掉后缀的文件目录
        Path newDirectory = Paths.get(directoryPath, fileName);

        // 判断是不是文件夹
        if(Files.isDirectory(newDirectory)){
            // 是文件夹，删除整个文件夹
            try{
                Files.walkFileTree(newDirectory, EnumSet.noneOf(FileVisitOption.class), Integer.MAX_VALUE, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        Files.delete(file);
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                        Files.delete(dir);
                        return FileVisitResult.CONTINUE;
                    }
                });
            }catch (IOException e){
                e.printStackTrace();
                return false;
            }

        }
        return true;

    }

    /***
     * 删除文件的操作
     * @param filePath 要删除文件的绝对路径
     * @return
     */
    public static boolean delete(Path filePath){
        System.out.println(filePath);
        // 文件不存在
        if (!Files.exists(filePath)) {
            return false;
        }
        try {
            FileSystemUtils.deleteRecursively(filePath);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
    private static String replaceFileExtension(String fileName, String newExtension) {
        int lastDotIndex = fileName.lastIndexOf(".");
        if (lastDotIndex != -1) {
            return fileName.substring(0, lastDotIndex) + "." + newExtension;
        }
        return fileName;
    }
}

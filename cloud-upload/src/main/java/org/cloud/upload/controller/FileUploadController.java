package org.cloud.upload.controller;

import org.apache.commons.codec.digest.DigestUtils;
import org.cloud.common.enu.FileFolder;
import org.cloud.common.pojo.Result;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@RestController
public class FileUploadController {


    /**
     * 上传文件
     * @param file
     * @param pathName
     * @return
     * @throws IOException
     */
    @PostMapping("/upload")
    public Result<String> upload(MultipartFile file, String pathName) throws IOException {
        // 定义文件路径枚举
        String path = FileFolder.getPath(pathName);
        String fileName = UUID.randomUUID().toString();
        if(path.isEmpty())
            return Result.error("文件上传失败");
        // 把文件的内容存储到本地磁盘上
        String originalFilename = file.getOriginalFilename();
        // 保证文件的名字是唯一的，从而防止文件覆盖
        String filename = fileName + originalFilename.substring(originalFilename.lastIndexOf("."));
        // 获取文件路径
        String projectDirectory =System.getProperty("user.dir");
        // 在项目目录下的 uploads 文件夹内构建文件路径
        Path uploadsDirectory = Paths.get(projectDirectory, "uploads");
        Path filePath =  Paths.get(uploadsDirectory.toString(), path, filename) ;
        System.out.println(filePath.toString());
        // 如果目录不存在，则创建目录
        if (!Files.exists(filePath)) {
            Files.createDirectories(filePath);
        }
        try {
            file.transferTo(filePath.toFile());
            return Result.success(filename);
        }catch (Exception e){
            e.printStackTrace();
            return Result.error("文件上传失败");
        }


    }

    /**
     * 显示图片
     * produces = MediaType.IMAGE_JPEG_VALUE 定义页面显示的文件类型，不然后出现乱码问题
     * @param folderName
     * @param imageName
     * @return
     * @throws MalformedURLException
     */
    @GetMapping(value = "/images/{folderName}/{imageName}",produces = MediaType.IMAGE_JPEG_VALUE)
    public ResponseEntity<Resource> getImage(@PathVariable String folderName,@PathVariable String imageName) throws MalformedURLException {
        Path imagePath = Paths.get("uploads/").resolve(folderName).resolve(imageName);
        Resource resource = new UrlResource(imagePath.toUri());

        return ResponseEntity.ok().body(resource);
    }

    /**
     * 文件预览
     * @param folderName
     * @param imageName
     * @return 文件流
     * @throws IOException
     */
    @GetMapping("/files/{folderName}/{imageName}")
    public ResponseEntity<InputStreamResource> previewFile(@PathVariable String folderName,@PathVariable String imageName) throws IOException {
        Path filePath = Paths.get("uploads/").resolve(folderName).resolve(imageName);
        // 替换为实际的文件路径
        File file = new File(filePath.toUri());

        // 读取文件流
        InputStream inputStream = new FileInputStream(file);
        InputStreamResource resource = new InputStreamResource(inputStream);

        // 设置响应头
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + file.getName());
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE);

        // 构建响应实体
        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(file.length())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    @PostMapping("/uploadSliceFile")
    public Result uploadSliceFile(@RequestParam("pathName")String pathName,@RequestParam("sFile")MultipartFile sFile, @RequestParam("realFilename") String realFilename, @RequestParam("index") Integer index,@RequestParam("fileName") String fileName) throws IOException {
        // 定义文件路径枚举
        realFilename  = fileName + realFilename.substring(realFilename.lastIndexOf("."));
        Path filePath = getUploadFilePath(pathName);
        System.out.println(filePath.toString());

        String md5 = DigestUtils.md5Hex(realFilename);
        System.out.println(realFilename);
        System.out.println(md5);
        System.out.println("分片名: " + sFile.getOriginalFilename());
        // TODO 修改路径
        File dir = new File(filePath.toString() + "/" + md5);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File sFileWithIndex = new File(filePath.toString() + "/" + md5 + "/" + index);
        sFile.transferTo(sFileWithIndex);
        return Result.success();
    }

    @PostMapping("/mergeFragmentFile")
    public Result<String> mergeFragmentFile(@RequestParam("pathName")String pathName,@RequestParam String realFilename,@RequestParam("fileName") String fileName) throws IOException {

        realFilename  = fileName + realFilename.substring(realFilename.lastIndexOf("."));
        Path filePath = getUploadFilePath(pathName);
        System.out.println("-------开始合并文件");


        // 合并的文件
        RandomAccessFile raf = new RandomAccessFile(filePath.toString() + "/" + realFilename, "rw");

        // 获取分片所在文件夹
        String md5 = DigestUtils.md5Hex(realFilename);
        System.out.println(realFilename);
        System.out.println(md5);
        File file = new File(filePath.toString() + "/" + md5);
        File[] files = file.listFiles();
        int num = files.length;
        System.out.println(num);

        byte[] bytes = new byte[5 * 1024];

        // 合并分片
        for (int i = 0; i < num; i++) {
            File iFile = new File(file, String.valueOf(i));
            // 将每一个分片文件包装为缓冲流
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(iFile));
            int len = 0;
            // 将分片文件包装的流写入RandomAccessFile
            while ((len = bis.read(bytes)) != -1) {
                raf.write(bytes, 0, len);
            }
            bis.close();
        }

        // 删除分片所在文件夹的分片文件
        for (File tmpFile : files) {
            tmpFile.delete();
        }
        // 删除分片所在文件夹
        file.delete();

        raf.close();
        return Result.success(realFilename);
    }

    private static Path getUploadFilePath(String pathName) {
        // 定义文件路径枚举
        String path = FileFolder.getPath(pathName);
        // 文件路径
        String projectDirectory =System.getProperty("user.dir");
        // 在项目目录下的 uploads 文件夹内构建文件路径
        Path uploadsDirectory = Paths.get(projectDirectory, "uploads");
        Path filePath =  Paths.get(uploadsDirectory.toString(), path) ;
        return filePath;
    }

}

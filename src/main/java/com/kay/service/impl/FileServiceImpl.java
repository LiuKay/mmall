package com.kay.service.impl;

import com.google.common.collect.Lists;
import com.kay.service.FileService;
import com.kay.util.FTPUtil;
import java.io.File;
import java.io.IOException;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * Created by kay on 2018/3/21.
 */
@Service("iFileService")
@Slf4j
public class FileServiceImpl implements FileService {

    //private Logger log = LoggerFactory.getLogger(FileServiceImpl.class);

    /**
     * 文件上传
     *
     * @param file
     * @param path
     * @return 上传文件名称
     */
    @Override
    public String upload(MultipartFile file, String path) {
        //文件名
        //扩展名
        String fileName = file.getOriginalFilename();
        String fileExtensionName = fileName.substring(fileName.lastIndexOf(".") + 1);
        //上传文件名
        String uploadFileName = UUID.randomUUID().toString() + "." + fileExtensionName;

        log.info("开始长传文件,上传文件的文件名:{},上传路径:{},新文件名:{}", fileName, path, uploadFileName);

        //创建文件夹路径
        File fileDir = new File(path);
        if (!fileDir.exists()) {
            fileDir.setWritable(true);
            fileDir.mkdirs();    //mkdirs 递归创建，mkdir 只建一个，多级返回false
        }
        File targetFile = new File(path, uploadFileName);

        try {
            //上传文件到应用服务器
            file.transferTo(targetFile);

            //上传到FTP文件服务器
            FTPUtil.uploadFile(Lists.newArrayList(targetFile));

            //上传完后删除应用上的文件
            targetFile.delete();

        } catch (IOException e) {
            log.error("文件上传异常", e);
        }

        return targetFile.getName();
    }
}

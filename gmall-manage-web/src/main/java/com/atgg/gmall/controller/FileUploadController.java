package com.atgg.gmall.controller;

import org.apache.commons.lang3.StringUtils;
import org.csource.common.MyException;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@CrossOrigin
public class FileUploadController {
    @Value("${fileServer.url}")
    String fileUrl;

    @RequestMapping("fileUpload")
    public String fileUpload(MultipartFile file) throws IOException, MyException {

        String imgUrl = fileUrl;
        if (file != null) {


            String configFile  = this.getClass().getResource("/tracker.conf").getFile();

            ClientGlobal.init(configFile);

            TrackerClient trackerClient=new TrackerClient();
            TrackerServer trackerServer=trackerClient.getConnection();

            StorageClient storageClient=new StorageClient(trackerServer,null);
              //获取文件名
            String fileName= file.getOriginalFilename();
             //获取后缀名
            String extName = StringUtils.substringAfterLast(fileName, ".");
            String[] upload_file = storageClient.upload_file(file.getBytes(), extName, null);
            for (int i = 0; i < upload_file.length; i++) {
                String path = upload_file[i];
              //  System.out.println("s = " + path);
              imgUrl+="/"+path;
            }
        }
         return  imgUrl;

    }
}

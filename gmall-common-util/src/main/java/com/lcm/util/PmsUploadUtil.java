package com.lcm.util;

import org.csource.common.MyException;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public class PmsUploadUtil {

    public static String uploadImage(MultipartFile multipartFile) {

        String imgUrl = "http://192.168.42.182";

        String tracker = PmsUploadUtil.class.getResource("/tracker.conf").getPath();

        try {
            ClientGlobal.init(tracker);

            TrackerClient trackerClient = new TrackerClient();
            //获得一个trackerServer的实例
            TrackerServer trackerServer = trackerClient.getTrackerServer();
            //通过tracker获得一个连接客户端
            StorageClient storageClient = new StorageClient(trackerServer,null);

            //获取上传的二进制对象
            byte[] bytes = multipartFile.getBytes();

            //获取文件全名
            String originalFilename = multipartFile.getOriginalFilename();

            //截取图片类型
            String extName = originalFilename.substring(originalFilename.lastIndexOf(".")+1);

            String[] uploadInfos = storageClient.upload_file(bytes, extName, null);

            for (String uploadInfo : uploadInfos) {
                imgUrl += "/"+uploadInfo;
            }


        } catch (Exception e) {
            e.printStackTrace();
        }

        return imgUrl;
    }
}

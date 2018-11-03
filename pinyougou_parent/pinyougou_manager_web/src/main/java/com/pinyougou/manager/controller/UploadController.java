package com.pinyougou.manager.controller;

import com.pinyougou.FastDFSClient;
import entity.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 图片上传控制类
 */
@RestController
public class UploadController {

    @Value("${FILE_SERVER_URL}")
    private String FILE_SERVER_URL;

    @RequestMapping("upload")
    public Result upload(MultipartFile file){

        try {
            //获取图片员文件名
            String originalFilename = file.getOriginalFilename();
            //获取扩展名.不带 .
            String extName = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);

            //创建fastdfs客服端
            FastDFSClient fastDFSClient=new FastDFSClient("classpath:fdfs_client.conf");

            //上传文件到fastdfs
            String uploadFile = fastDFSClient.uploadFile(file.getBytes(), extName);

            //拼接文件url
            String url=FILE_SERVER_URL+uploadFile;

            //返回文件url
            return new Result(true,url);
        } catch (Exception e) {
            e.printStackTrace();
        }

       return new Result(false,"文件上传失败,请重试!");
    }
}

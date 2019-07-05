package com.feng.common;

import com.baidu.aip.ocr.AipOcr;
import org.json.JSONObject;

import java.util.HashMap;

public class Common {

    //设置APPID/AK/SK
    public static final String APP_ID = "16714630";
    public static final String API_KEY = "dbGlwI1ZXKNRkUEY6nTT16Dr";
    public static final String SECRET_KEY = "NnVNlGKbbjXosAj9XD0t4CjKKXjW5Oes";

    public static void main(String[] args){
        Invoker.setProvider(new UserDaoProvider());
        IUserDao userDao = Invoker.getInstance(IUserDao.class);
        System.out.println(userDao.getUserName("1213"));
//        method(args);
    }

    public static void method(String[] args){
        // 初始化一个AipOcr
        AipOcr client = new AipOcr(APP_ID, API_KEY, SECRET_KEY);

        // 可选：设置网络连接参数
        client.setConnectionTimeoutInMillis(2000);
        client.setSocketTimeoutInMillis(60000);

        // 传入可选参数调用接口
        HashMap<String, String> options = new HashMap<>();
//        options.put("detect_direction", "true");
//        options.put("probability", "true");

        // 调用接口
        String path = "D://test.jpg";
        JSONObject res = client.trainTicket(path, options);
        System.out.println(res.toString(2));
    }
}

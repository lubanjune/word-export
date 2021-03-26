package com.luban.configration;

import freemarker.cache.URLTemplateLoader;
import org.springframework.util.StringUtils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.stream.Collectors;

public class RemoteTemplateLoader extends URLTemplateLoader {
    //远程模板文件的存储路径（目录）
    private String remotePath;

    public RemoteTemplateLoader(String remotePath) {

        if (StringUtils.isEmpty(remotePath)) {
            throw new IllegalArgumentException("remotePath is null");
        }

        this.remotePath = canonicalizePrefix(remotePath);

        if (this.remotePath.indexOf('/') == 0) {
            this.remotePath = this.remotePath.substring(this.remotePath.indexOf('/') + 1);
        }

    }

    @Override
    protected URL getURL(String name) {

        if (name.contains("_zh_CN")) {
            String[] zh_cns = name.split("_zh_CN");
            name = Arrays.stream(zh_cns).collect(Collectors.joining());
        }

        if (name.contains("_en_US")) {
            String[] en_uses = name.split("_en_US");
            name = Arrays.stream(en_uses).collect(Collectors.joining());
        }

        String fullPath = this.remotePath + name;
        if ((this.remotePath.equals("/")) && (!isSchemeless(fullPath))) {
            return null;
        }
        //这个是针对不直接使用文件流形式进行访问和读取文件而使用的格式
        if (!this.remotePath.contains("streamFile") && this.remotePath.contains("webhdfs")) {
            fullPath = fullPath + "?op=OPEN";
        }
        URL url = null;
        try {
            url = new URL(fullPath);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return url;
    }


    private static boolean isSchemeless(String fullPath) {
        int i = 0;
        int ln = fullPath.length();

        if ((i < ln) && (fullPath.charAt(i) == '/')) i++;

        while (i < ln) {
            char c = fullPath.charAt(i);
            if (c == '/') return true;
            if (c == ':') return false;
            i++;
        }
        return true;
    }
}

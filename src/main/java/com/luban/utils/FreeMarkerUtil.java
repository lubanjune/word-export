package com.luban.utils;

import com.google.common.collect.Lists;
import com.luban.configration.RemoteTemplateLoader;
import com.luban.entity.UserAvatar;
import fr.opensagres.odfdom.converter.core.utils.StringUtils;
import fr.opensagres.xdocreport.core.document.SyntaxKind;
import fr.opensagres.xdocreport.document.IXDocReport;
import fr.opensagres.xdocreport.document.images.FileImageProvider;
import fr.opensagres.xdocreport.document.registry.XDocReportRegistry;
import fr.opensagres.xdocreport.template.IContext;
import fr.opensagres.xdocreport.template.TemplateEngineKind;
import fr.opensagres.xdocreport.template.formatter.FieldsMetadata;
import fr.opensagres.xdocreport.template.formatter.NullImageBehaviour;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.util.HtmlUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class FreeMarkerUtil {

    private static final Logger log = LoggerFactory.getLogger(FreeMarkerUtil.class);

    /**
     * @param dataMap
     * @param templateUrl  远程模板xml存放位置
     * @param outputStream 输出流 可以是response或者文件的输出流
     */
    public static void createRemoteDoc(Map<String, Object> dataMap, String templateUrl, OutputStream outputStream) {
        if (outputStream == null) {
            log.info("调用createDoc未传入输出流");
            return;
        }
        try {
            Configuration configuration = new Configuration(Configuration.VERSION_2_3_28);
            configuration.setDefaultEncoding("UTF-8");
            String url = templateUrl.substring(0, templateUrl.lastIndexOf("/"));
            String name = templateUrl.substring(templateUrl.lastIndexOf("/"));
            RemoteTemplateLoader templateLoader = new RemoteTemplateLoader(url);
            configuration.setTemplateLoader(templateLoader);
            Template template = configuration.getTemplate(name, "UTF-8");
            //生成docx
            Writer w = new OutputStreamWriter(outputStream, "utf-8");
            template.process(dataMap, w);
            w.flush();
            w.close();
        } catch (Exception e) {
            e.printStackTrace();
            log.info("生成word文档发生异常<{}>", e.getMessage());
        }
    }

    /**
     * 通过本地模板导出word文档
     *
     * @param data
     * @param response
     */
    public static void createLocalDoc(Map<String, Object> data, HttpServletResponse response) {
        try {
            response.setCharacterEncoding("utf-8");
            response.setContentType("application/msword");
            response.setHeader("Content-disposition", "attachment;filename=" + new String("会议签到表".getBytes("utf-8"), "ISO8859-1"));

            Configuration configuration = new Configuration(Configuration.VERSION_2_3_28);
            configuration.setDefaultEncoding("UTF-8");
            configuration.setDirectoryForTemplateLoading(new File("C:\\Users\\luban\\Desktop"));
            Template template = configuration.getTemplate("test.ftl");
            OutputStreamWriter writer = new OutputStreamWriter(response.getOutputStream(), "UTF-8");
            template.process(data, writer);
            writer.flush();
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
            log.info("生成word文档发生异常<{}>", e.getMessage());
        }
    }

    public static void createXdocreport() {
        try {
            //使用远程模板
//            InputStream inputStream = new URL(templateUrl).openConnection().getInputStream();
            File inputPath = new File("C:\\Users\\luban\\Desktop\\summary.docx");
            FileInputStream inputStream = new FileInputStream(inputPath);
            File outputPath = new File("C:\\Users\\luban\\Desktop\\test.docx");
            FileOutputStream outputStream = new FileOutputStream(outputPath);

            IXDocReport report = XDocReportRegistry
                    .getRegistry()
                    .loadReport(inputStream, TemplateEngineKind.Freemarker);

            // 设置特殊字段
            FieldsMetadata metadata = report.createFieldsMetadata();
            metadata.addFieldAsTextStyling("content", SyntaxKind.Html);
            metadata.addFieldAsImage("avatar", "user.avatar", NullImageBehaviour.RemoveImageTemplate);
            report.setFieldsMetadata(metadata);

            // 创建内容-text为模版中对应都变量名称
            String content = "&#x3c;p&#x3e;我在这里放了一段富文本&#x3c;/p&#x3e;" +
                    "&#x3c;p&#x3e;我准备测试富文本的处理&#x3c;/p&#x3e;";
            content = HtmlUtils.htmlUnescape(content);
            IContext context = report.createContext();
            context.put("name", "年终总结大会");
            context.put("time", "2021年3月26日");
            context.put("place", "线上");
            context.put("sponsor", "张三");
            context.put("content", content);
            //图片这里有三种填充方式，使用远程资源时可以使用ByteArrayImageProvider
            //使用本地资源使用FileImageProvider  类路径ClassPathImageProvider
            List<UserAvatar> users = Lists.newArrayList(
                    new UserAvatar("张三", "组织部", new FileImageProvider(new File("C:\\Users\\luban\\Desktop\\图片1.png"))),
                    new UserAvatar("李四", "宣传部", new FileImageProvider(new File("C:\\Users\\luban\\Desktop\\图片2.jpg"))));
            context.put("userList", users);
            // 生成文件
            report.process(context, outputStream);

            inputStream.close();
            outputStream.close();
        } catch (Exception e) {
            log.info("生成纪要文件发生异常：<{}>", e.getMessage());
        }

    }

    public static File createXmlDocLocalUrl(Map<String, Object> dataMap, String templateUrl, String zipUrl) {

        try {
            //获取临时文件路径前缀
            String prefix = getTempFileUrl(null);
            //获取远程xml模板
            Configuration configuration = new Configuration(Configuration.VERSION_2_3_28);
            configuration.setDefaultEncoding("UTF-8");
            String url = templateUrl.substring(0, templateUrl.lastIndexOf("/"));
            String name = templateUrl.substring(templateUrl.lastIndexOf("/"));
            RemoteTemplateLoader templateLoader = new RemoteTemplateLoader(url);
            configuration.setTemplateLoader(templateLoader);
            Template template = configuration.getTemplate(name, "UTF-8");
            //生成本地xml
            File localXmlFile = new File(prefix + ".xml");
            FileOutputStream localXmlOut = new FileOutputStream(localXmlFile);
            Writer w = new BufferedWriter(new OutputStreamWriter(localXmlOut), 1024);
            template.process(dataMap, w);
            localXmlOut.close();
            w.close();
            //根据远程xml和远程zip文件将本地xml转换成docx
            URL zip = new URL(zipUrl);
            InputStream remoteZipInput = zip.openConnection().getInputStream();
            ZipInputStream zipInputStream = ZipUtils.wrapZipInputStream(remoteZipInput);

            File localDocxFile = new File(prefix + ".docx");
            FileOutputStream localDocxOutput = new FileOutputStream(localDocxFile);
            ZipOutputStream zipOutputStream = ZipUtils.wrapZipOutputStream(localDocxOutput);

            FileInputStream localXmlInput = new FileInputStream(localXmlFile);

            ZipUtils.replaceItem(zipInputStream, zipOutputStream, localXmlInput);
            localDocxOutput.close();
            remoteZipInput.close();

            localXmlFile.delete();

            return localDocxFile;
        } catch (Exception e) {
            e.printStackTrace();
            log.info("生成word文档发生异常<{}>", e.getMessage());
        }

        return null;
    }

    public static String getTempFileUrl(String name) {

        String tempPath = System.getProperty("java.io.tmpdir");
        if (!tempPath.endsWith("/")) {
            tempPath = tempPath + "/";
        }

        if (StringUtils.isEmpty(name)) {
            name = "converttemp" + System.currentTimeMillis();
        }

        return tempPath + name;
    }

    public static void main(String[] args) {
        createXdocreport();
    }

}

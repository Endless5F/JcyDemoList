package com.android.javalib;

import com.alibaba.excel.support.ExcelTypeEnum;
import com.android.javalib.bean.Language;
import com.android.javalib.excel.EasyExcelUtil;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

public class MultiLanguage {
    // 翻译文件路径 -- 该文件需要为 2003 *.xls
    private String path = "D:\\translation.xls";
    private String outPathAndroid = "D:/values";
    private String outPathIOS = "D:/values";

    private List<String> outputNames = new ArrayList<>();
    private List<List<ConfigBean>> tempListList = new ArrayList<>();
    private List<List<ConfigBean>> finalListList = new ArrayList<>();

    @Test
    public void testReadExcelWithModel() {
        try (
                InputStream inputStream = new FileInputStream(path)
        ) {
            //读入文件,每一行对应一个 Model ,获取Model 列表
            List<Object> objectList = EasyExcelUtil.readExcelWithModel(inputStream, Language.class, ExcelTypeEnum.XLS, 0);
            List<Language> languageList = (List) objectList;

            processLanguageList(languageList);

            /**
             * 将行数据转换为列数据
             * 将处理过的tempListList数据，按照Excel列将每一种多语言所有的字段都添加进finalListList中
             * 即：tempListList 为行数据  ； finalListList为列数据
             * */
            for (int i = 0; i < outputNames.size(); i++) {
                List<ConfigBean> beanList = new ArrayList<>();
                for (int j = 0; j < tempListList.size(); j++) {
                    beanList.add(tempListList.get(j).get(i));
                }
                finalListList.add(beanList);
            }

            generateXML(finalListList, outputNames);
//            listList.forEach(System.out::println);
//            System.out.println(listList.size());
//            outputNames.forEach(System.out::println);
//            languageList.forEach(System.out::println);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 处理行数据
     * 处理 languageList集合数据：将除languageKey成员外的，其它属性按照顺序添加进集合，已备处理
     * */
    private void processLanguageList(List<Language> languageList) {
        for (int i = 0; i < languageList.size(); i++) {
            if (i == 0) {
                // 0 位置为Excel第一行：多语言种类
                outputNames.add(languageList.get(i).getValues_zh());
                outputNames.add(languageList.get(i).getValues_en());
                outputNames.add(languageList.get(i).getValues_en_rUS());
                outputNames.add(languageList.get(i).getValues_de());
                outputNames.add(languageList.get(i).getValues_it());
                outputNames.add(languageList.get(i).getValues_pt());
                outputNames.add(languageList.get(i).getValues_es());
                outputNames.add(languageList.get(i).getA1());
                outputNames.add(languageList.get(i).getA2());
                outputNames.add(languageList.get(i).getValues_fr());
                outputNames.add(languageList.get(i).getA3());
                outputNames.add(languageList.get(i).getA4());
                outputNames.add(languageList.get(i).getA5());
                outputNames.add(languageList.get(i).getValues_nb());
                outputNames.add(languageList.get(i).getValues_sv());
                outputNames.add(languageList.get(i).getValues_da());
                outputNames.add(languageList.get(i).getValues_fi());
                outputNames.add(languageList.get(i).getValues_tr());
                outputNames.add(languageList.get(i).getA6());
                outputNames.add(languageList.get(i).getA7());
                outputNames.add(languageList.get(i).getValues_ru());
                outputNames.add(languageList.get(i).getValues_vi());
                outputNames.add(languageList.get(i).getValues_th());
                outputNames.add(languageList.get(i).getA8());
                outputNames.add(languageList.get(i).getA9());
                outputNames.add(languageList.get(i).getValues_uz());
                outputNames.add(languageList.get(i).getValues_hi());
                outputNames.add(languageList.get(i).getValues_uk());
                outputNames.add(languageList.get(i).getValues_ms());
                outputNames.add(languageList.get(i).getA10());
                outputNames.add(languageList.get(i).getA11());
                outputNames.add(languageList.get(i).getValues_cs());
                outputNames.add(languageList.get(i).getValues_sk());
                outputNames.add(languageList.get(i).getValues_pl());
                outputNames.add(languageList.get(i).getValues_hu());
                outputNames.add(languageList.get(i).getValues_bg());
                outputNames.add(languageList.get(i).getA12());
                outputNames.add(languageList.get(i).getValues_id());
                outputNames.add(languageList.get(i).getValues_lv());
                outputNames.add(languageList.get(i).getValues_lt());
                outputNames.add(languageList.get(i).getValues_et());
                outputNames.add(languageList.get(i).getValues_hr());
                outputNames.add(languageList.get(i).getValues_sr());
                outputNames.add(languageList.get(i).getValues_sq());
                outputNames.add(languageList.get(i).getValues_mk());
                outputNames.add(languageList.get(i).getA13());
                outputNames.add(languageList.get(i).getValues_sl());
                outputNames.add(languageList.get(i).getValues_zh_rHK());
                outputNames.add(languageList.get(i).getValues_nl());
                outputNames.add(languageList.get(i).getA14());
                outputNames.add(languageList.get(i).getA15());
                outputNames.add(languageList.get(i).getA16());
                outputNames.add(languageList.get(i).getA17());
                outputNames.add(languageList.get(i).getA18());
                outputNames.add(languageList.get(i).getA19());
                outputNames.add(languageList.get(i).getA20());
                outputNames.add(languageList.get(i).getValues_zh_rTW());
            } else {
                // 每个词句的多语言
                List<ConfigBean> beanList = new ArrayList<>();
                beanList.add(new ConfigBean(languageList.get(i).getLanguageKey(), languageList.get(i).getValues_zh()));
                beanList.add(new ConfigBean(languageList.get(i).getLanguageKey(), languageList.get(i).getValues_en()));
                beanList.add(new ConfigBean(languageList.get(i).getLanguageKey(), languageList.get(i).getValues_en_rUS()));
                beanList.add(new ConfigBean(languageList.get(i).getLanguageKey(), languageList.get(i).getValues_de()));
                beanList.add(new ConfigBean(languageList.get(i).getLanguageKey(), languageList.get(i).getValues_it()));
                beanList.add(new ConfigBean(languageList.get(i).getLanguageKey(), languageList.get(i).getValues_pt()));
                beanList.add(new ConfigBean(languageList.get(i).getLanguageKey(), languageList.get(i).getValues_es()));
                beanList.add(new ConfigBean(languageList.get(i).getLanguageKey(), languageList.get(i).getA1()));
                beanList.add(new ConfigBean(languageList.get(i).getLanguageKey(), languageList.get(i).getA2()));
                beanList.add(new ConfigBean(languageList.get(i).getLanguageKey(), languageList.get(i).getValues_fr()));
                beanList.add(new ConfigBean(languageList.get(i).getLanguageKey(), languageList.get(i).getA3()));
                beanList.add(new ConfigBean(languageList.get(i).getLanguageKey(), languageList.get(i).getA4()));
                beanList.add(new ConfigBean(languageList.get(i).getLanguageKey(), languageList.get(i).getA5()));
                beanList.add(new ConfigBean(languageList.get(i).getLanguageKey(), languageList.get(i).getValues_nb()));
                beanList.add(new ConfigBean(languageList.get(i).getLanguageKey(), languageList.get(i).getValues_sv()));
                beanList.add(new ConfigBean(languageList.get(i).getLanguageKey(), languageList.get(i).getValues_da()));
                beanList.add(new ConfigBean(languageList.get(i).getLanguageKey(), languageList.get(i).getValues_fi()));
                beanList.add(new ConfigBean(languageList.get(i).getLanguageKey(), languageList.get(i).getValues_tr()));
                beanList.add(new ConfigBean(languageList.get(i).getLanguageKey(), languageList.get(i).getA6()));
                beanList.add(new ConfigBean(languageList.get(i).getLanguageKey(), languageList.get(i).getA7()));
                beanList.add(new ConfigBean(languageList.get(i).getLanguageKey(), languageList.get(i).getValues_ru()));
                beanList.add(new ConfigBean(languageList.get(i).getLanguageKey(), languageList.get(i).getValues_vi()));
                beanList.add(new ConfigBean(languageList.get(i).getLanguageKey(), languageList.get(i).getValues_th()));
                beanList.add(new ConfigBean(languageList.get(i).getLanguageKey(), languageList.get(i).getA8()));
                beanList.add(new ConfigBean(languageList.get(i).getLanguageKey(), languageList.get(i).getA9()));
                beanList.add(new ConfigBean(languageList.get(i).getLanguageKey(), languageList.get(i).getValues_uz()));
                beanList.add(new ConfigBean(languageList.get(i).getLanguageKey(), languageList.get(i).getValues_hi()));
                beanList.add(new ConfigBean(languageList.get(i).getLanguageKey(), languageList.get(i).getValues_uk()));
                beanList.add(new ConfigBean(languageList.get(i).getLanguageKey(), languageList.get(i).getValues_ms()));
                beanList.add(new ConfigBean(languageList.get(i).getLanguageKey(), languageList.get(i).getA10()));
                beanList.add(new ConfigBean(languageList.get(i).getLanguageKey(), languageList.get(i).getA11()));
                beanList.add(new ConfigBean(languageList.get(i).getLanguageKey(), languageList.get(i).getValues_cs()));
                beanList.add(new ConfigBean(languageList.get(i).getLanguageKey(), languageList.get(i).getValues_sk()));
                beanList.add(new ConfigBean(languageList.get(i).getLanguageKey(), languageList.get(i).getValues_pl()));
                beanList.add(new ConfigBean(languageList.get(i).getLanguageKey(), languageList.get(i).getValues_hu()));
                beanList.add(new ConfigBean(languageList.get(i).getLanguageKey(), languageList.get(i).getValues_bg()));
                beanList.add(new ConfigBean(languageList.get(i).getLanguageKey(), languageList.get(i).getA12()));
                beanList.add(new ConfigBean(languageList.get(i).getLanguageKey(), languageList.get(i).getValues_id()));
                beanList.add(new ConfigBean(languageList.get(i).getLanguageKey(), languageList.get(i).getValues_lv()));
                beanList.add(new ConfigBean(languageList.get(i).getLanguageKey(), languageList.get(i).getValues_lt()));
                beanList.add(new ConfigBean(languageList.get(i).getLanguageKey(), languageList.get(i).getValues_et()));
                beanList.add(new ConfigBean(languageList.get(i).getLanguageKey(), languageList.get(i).getValues_hr()));
                beanList.add(new ConfigBean(languageList.get(i).getLanguageKey(), languageList.get(i).getValues_sr()));
                beanList.add(new ConfigBean(languageList.get(i).getLanguageKey(), languageList.get(i).getValues_sq()));
                beanList.add(new ConfigBean(languageList.get(i).getLanguageKey(), languageList.get(i).getValues_mk()));
                beanList.add(new ConfigBean(languageList.get(i).getLanguageKey(), languageList.get(i).getA13()));
                beanList.add(new ConfigBean(languageList.get(i).getLanguageKey(), languageList.get(i).getValues_sl()));
                beanList.add(new ConfigBean(languageList.get(i).getLanguageKey(), languageList.get(i).getValues_zh_rHK()));
                beanList.add(new ConfigBean(languageList.get(i).getLanguageKey(), languageList.get(i).getValues_nl()));
                beanList.add(new ConfigBean(languageList.get(i).getLanguageKey(), languageList.get(i).getA14()));
                beanList.add(new ConfigBean(languageList.get(i).getLanguageKey(), languageList.get(i).getA15()));
                beanList.add(new ConfigBean(languageList.get(i).getLanguageKey(), languageList.get(i).getA16()));
                beanList.add(new ConfigBean(languageList.get(i).getLanguageKey(), languageList.get(i).getA17()));
                beanList.add(new ConfigBean(languageList.get(i).getLanguageKey(), languageList.get(i).getA18()));
                beanList.add(new ConfigBean(languageList.get(i).getLanguageKey(), languageList.get(i).getA19()));
                beanList.add(new ConfigBean(languageList.get(i).getLanguageKey(), languageList.get(i).getA20()));
                beanList.add(new ConfigBean(languageList.get(i).getLanguageKey(), languageList.get(i).getValues_zh_rTW()));
                tempListList.add(beanList);
            }
        }
    }

    private void generateXML(List<List<ConfigBean>> listList, List<String> outputNames) {
        for (int i = 0; i < listList.size(); i++) {
            try {
                /**just for android*/
                BuildXMLDoc(listList.get(i),  outPathAndroid + "/", outputNames.get(i));

                /**just for ios*/
                BuildFileDoc(listList.get(i), outPathIOS + "" + "/", outputNames.get(i));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 生产xml文件 list 数据 path 输出路径
     */
    private void BuildXMLDoc(List<ConfigBean> list, String path, String outPathName) throws IOException, JDOMException {
        Element root = new Element("resources");// 创建根节点，设置它属文件
        Document doc = new Document(root);// 将根节点添加到文档中
        String xmlName = "strings.xml";

        for (ConfigBean cbean : list) {
            if (cbean.getValue() != null && !"".equals(cbean.getValue()) && !"null".equals(cbean.getValue())) {
                Element elm = new Element("string");
                elm.setAttribute("name", cbean.getKey());
                // 敏感词汇替换
                String content = cbean.getValue()
                        .replace("'", "\\'")
                        .replace("@", "\\uff20")
                        .replace(":", "\\uff1a")
                        .replace("\"", "\\\"");
                elm.addContent(content);
                if (content != null && !"".equals(content)) {
                    root.addContent(elm);
                }
            }
        }
        Format format = Format.getPrettyFormat();
        XMLOutputter xmlout = new XMLOutputter(format);
        File fileFoot = new File(path);
        if (!fileFoot.exists()) {
            fileFoot.mkdir();
        }
        File file = new File(path + outPathName);
        if (!file.exists()) {
            file.mkdir();
        }
        xmlout.output(doc, new FileOutputStream(path + outPathName + "/" + xmlName));
    }

    private void BuildFileDoc(List<ConfigBean> list, String path, String outPathName) {
        String xmlName = outPathName + ".txt";
        for (ConfigBean cbean : list) {
            if (cbean.getValue() != null && !"".equals(cbean.getValue()) && !"null".equals(cbean.getValue())) {
                // 敏感词汇替换
                String content = cbean.getValue()
                        .replace("'", "\\'")
                        .replace("@", "\\uff20")
                        .replace(":", "\\uff1a")
                        .replace("\"", "\\\"");

                String info = "\"" + cbean.getKey() + "\"=" + "\"" + content + "\"" + ";" + "\n";

                writeTxtToFile(info, path, xmlName);
            }
        }
    }

    // 将字符串写入到文本文件中
    private void writeTxtToFile(String strcontent, String filePath, String fileName) {
        // 生成文件夹之后，再生成文件，不然会出错
        makeFilePath(filePath, fileName);

        String strFilePath = filePath + fileName;
        // 每次写入时，都换行写
        String strContent = strcontent + "\n";
        try {
            File file = new File(strFilePath);
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(strFilePath, true);
            OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
            osw.write(strContent);
            osw.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 生成文件
    private File makeFilePath(String filePath, String fileName) {
        File file = null;
        makeRootDirectory(filePath);
        try {
            file = new File(filePath + fileName);
            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }

    // 生成文件夹
    private static void makeRootDirectory(String filePath) {
        File file = null;
        try {
            file = new File(filePath);
            if (!file.exists()) {
                file.mkdir();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class ConfigBean {
        private String note;// 注释名
        private String key;// key
        private String value;// value

        public ConfigBean(String key, String value) {
            this.key = key;
            this.value = value;
        }

        public String getNote() {
            return note;
        }

        public void setNote(String note) {
            this.note = note;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return "ConfigBean{" +
                    "note='" + note + '\'' +
                    ", key='" + key + '\'' +
                    ", value='" + value + '\'' +
                    '}';
        }
    }
}

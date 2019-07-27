package com.android.javalib.excel;

import com.alibaba.excel.ExcelReader;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.metadata.BaseRowModel;
import com.alibaba.excel.metadata.Sheet;
import com.alibaba.excel.metadata.Table;
import com.alibaba.excel.support.ExcelTypeEnum;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @version V1.0
 * @author: lin_shen
 * @date: 2018/9/26
 * @Description: TODO
 */
public class EasyExcelUtil {

    /**
     * StringList 解析监听器
     */
    private static class StringExcelListener extends AnalysisEventListener {
        /**
         * 自定义用于暂时存储data
         * 可以通过实例获取该值
         */
        private List<List<String>> datas = new ArrayList<>();

        /**
         * 每解析一行都会回调invoke()方法
         * @param object
         * @param context
         */
        @Override
        public void invoke(Object object, AnalysisContext context) {
            List<String> stringList= (List<String>) object;
            //数据存储到list，供批量处理，或后续自己业务逻辑处理。
            datas.add(stringList);
            //根据自己业务做处理
        }

        @Override
        public void doAfterAllAnalysed(AnalysisContext context) {
            //解析结束销毁不用的资源
            //注意不要调用datas.clear(),否则getDatas为null
        }

        public List<List<String>> getDatas() {
            return datas;
        }
        public void setDatas(List<List<String>> datas) {
            this.datas = datas;
        }
    }

    /**
     * 使用 StringList 来读取Excel
     * @param inputStream Excel的输入流
     * @param excelTypeEnum Excel的格式(XLS或XLSX)
     * @return 返回 StringList 的列表
     */
    public static List<List<String>> readExcelWithStringList(InputStream inputStream,ExcelTypeEnum excelTypeEnum) {
        StringExcelListener listener = new StringExcelListener();
        ExcelReader excelReader = new ExcelReader(inputStream, excelTypeEnum, null, listener);
        excelReader.read();
        return  listener.getDatas();
    }

    /**
     * 使用 StringList 来写入Excel
     * @param outputStream Excel的输出流
     * @param data 要写入的以StringList为单位的数据
     * @param table 配置Excel的表的属性
     * @param excelTypeEnum Excel的格式(XLS或XLSX)
     */
    public static void writeExcelWithStringList(OutputStream outputStream, List<List<String>> data, Table table,ExcelTypeEnum excelTypeEnum) {
        //这里指定不需要表头，因为String通常表头已被包含在data里
        ExcelWriter writer = new ExcelWriter(outputStream, excelTypeEnum,false);
        //写第一个sheet, sheet1  数据全是List<String> 无模型映射关系,无表头
        Sheet sheet1 = new Sheet(0, 0);
        writer.write0(data, sheet1,table);
        writer.finish();
    }

    /**
     * 模型 解析监听器
     */
    private static class ModelExcelListener extends AnalysisEventListener {

        private List<Object> datas = new ArrayList<>();
        @Override
        public void invoke(Object object, AnalysisContext context) {
            datas.add(object);
        }
        @Override
        public void doAfterAllAnalysed(AnalysisContext context) {
        }
        public List<Object> getDatas() {
            return datas;
        }
        public void setDatas(List<Object> datas) {
            this.datas = datas;
        }
    }

    /**
     * 使用 模型 来读取Excel
     * @param inputStream Excel的输入流
     * @param clazz 模型的类
     * @param excelTypeEnum Excel的格式(XLS或XLSX)
     * @return 返回 模型 的列表(为object列表,需强转)
     */
    public static List<Object> readExcelWithModel(InputStream inputStream, Class<? extends BaseRowModel> clazz, ExcelTypeEnum excelTypeEnum,int headLineMun) {

        // 解析每行结果在listener中处理
        ModelExcelListener listener = new ModelExcelListener();
        ExcelReader excelReader = new ExcelReader(inputStream, excelTypeEnum, null, listener);
        //默认只有一列表头
        excelReader.read(new Sheet(1,headLineMun,clazz));
        return  listener.getDatas();

    }

    /**
     * 使用 模型 来写入Excel
     * @param outputStream Excel的输出流
     * @param data 要写入的以 模型 为单位的数据
     * @param table 配置Excel的表的属性
     * @param clazz 模型的类
     * @param excelTypeEnum Excel的格式(XLS或XLSX)
     */
    public static void writeExcelWithModel(OutputStream outputStream, List<? extends BaseRowModel> data, Table table, Class<? extends BaseRowModel> clazz, ExcelTypeEnum excelTypeEnum)  {
        //这里指定需要表头，因为model通常包含表头信息
        ExcelWriter writer = new ExcelWriter(outputStream, excelTypeEnum,true);
        //写第一个sheet, sheet1  数据全是List<String> 无模型映射关系
        Sheet sheet1 = new Sheet(1,0, clazz);
        writer.write(data, sheet1,table);
        writer.finish();
    }

}

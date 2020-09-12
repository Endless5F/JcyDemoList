package com.android.javalib.excel;

import com.alibaba.excel.metadata.Font;
import com.alibaba.excel.metadata.Table;
import com.alibaba.excel.metadata.TableStyle;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.android.javalib.bean.MainData;

import org.apache.poi.ss.usermodel.IndexedColors;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public class EasyExcelUtilTest {
    @Test
    public void testReadExcelWithStringList() {
        try (
                InputStream inputStream = new FileInputStream("C:\\Users\\jiaochengyun.ex\\Desktop\\preset_content.xls");
                OutputStream outputStream = new FileOutputStream("C:\\Users\\jiaochengyun.ex\\Desktop\\preset_content-副本.xls")
        ) {
            //读入文件,每一行对应一个List<String>
            List<List<String>> stringLists = EasyExcelUtil.readExcelWithStringList(inputStream, ExcelTypeEnum.XLS);

            //定义Excel正文背景颜色
            TableStyle tableStyle = new TableStyle();
            tableStyle.setTableContentBackGroundColor(IndexedColors.WHITE);

            //定义Excel正文字体大小
            Font font = new Font();
            font.setFontHeightInPoints((short) 10);

            tableStyle.setTableContentFont(font);

            Table table = new Table(0);
            table.setTableStyle(tableStyle);

            EasyExcelUtil.writeExcelWithStringList(outputStream, stringLists, table, ExcelTypeEnum.XLSX);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Test
    public void testReadExcelWithModelToExcel() {
        try (
                InputStream inputStream = new FileInputStream("D:\\preset_content.xls");
                OutputStream outputStream = new FileOutputStream("C:\\Users\\jiaochengyun.ex\\Desktop\\preset_content-副本.xlsx")
        ) {
            //读入文件,每一行对应一个 Model ,获取Model 列表
            List<Object> objectList = EasyExcelUtil.readExcelWithModel(inputStream, MainData.class, ExcelTypeEnum.XLS,1);
            List<MainData> etuInfoList = (List) objectList;
            etuInfoList.forEach(System.out::println);
            //定义Excel正文背景颜色
            TableStyle tableStyle = new TableStyle();
            tableStyle.setTableContentBackGroundColor(IndexedColors.WHITE);

            //定义Excel正文字体大小
            Font font = new Font();
            font.setFontHeightInPoints((short) 10);
            tableStyle.setTableContentFont(font);

            Table table = new Table(0);
            table.setTableStyle(tableStyle);

            EasyExcelUtil.writeExcelWithModel(outputStream, etuInfoList, table, MainData.class, ExcelTypeEnum.XLSX);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeFile(String str) {
        FileWriter writer;
        try {
            writer = new FileWriter("E:/token.txt");
            writer.write(str);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
package com.cpbpc.comms;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static com.cpbpc.comms.PunctuationTool.containHyphen;
import static com.cpbpc.comms.PunctuationTool.getHyphen;

public class SpreadSheetReader {
    private static java.util.logging.Logger logger = Logger.getLogger(SpreadSheetReader.class.getName());

    public static List<String> readVerseFromXlsx(File input) throws IOException, InvalidFormatException {
        List<String> result = new ArrayList<>();

        XSSFWorkbook xssfWorkbook = new XSSFWorkbook(input);
        int startRow = parseStartRowIndex();
        int endRow = parseEndRowIndex();
        int cellIndex = parseCellIndex();

//        wb.getSheetAt(0).getRow(9).getCell(4);
        XSSFSheet sheet = xssfWorkbook.getSheetAt(Integer.parseInt(AppProperties.getConfig().getProperty("sheet_index")));
        for( int i = startRow; i<=endRow; i++ ){
            String value = sheet.getRow(i).getCell(cellIndex).getStringCellValue();

            if( AppProperties.isChinese() ){
                String chapterWord = TextUtil.returnChapterWord(StringUtils.substring(value, 0, 1));
                if(containHyphen(value)){
                    String hyphen = getHyphen(value);
                    value = StringUtils.substring(value, 0, StringUtils.indexOf(value, hyphen)) + chapterWord
                            + StringUtils.substring(value, StringUtils.indexOf(value, hyphen)) + chapterWord;
                }else{
                    value += chapterWord;
                }
            }

            result.add(value);
        }

        return result;
    }

    private static int parseCellIndex() {
        String range = AppProperties.getConfig().getProperty("range");
        return CellReference.convertColStringToIndex(StringUtils.substring(range, 0, 1));
    }

    private static int parseEndRowIndex() {
        String range = AppProperties.getConfig().getProperty("range");
        String result = StringUtils.substring(StringUtils.split(range, "-")[1], 1);
        return Integer.parseInt(result)-1;
    }

    private static int parseStartRowIndex() {
        String range = AppProperties.getConfig().getProperty("range");
        String result = StringUtils.substring(StringUtils.split(range, "-")[0], 1);
        return Integer.parseInt(result)-1;
    }

}

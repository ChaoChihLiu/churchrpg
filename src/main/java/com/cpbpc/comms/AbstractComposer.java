package com.cpbpc.comms;

import com.amazonaws.services.s3.model.Tag;
import org.apache.commons.lang3.StringUtils;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.cpbpc.comms.PunctuationTool.replacePauseTag;
import static com.cpbpc.comms.PunctuationTool.replacePunctuationWithBreakTag;
import static com.cpbpc.comms.PunctuationTool.replaceRemPunctuationWithBreakTag;

public abstract class AbstractComposer {
    private static Logger logger = Logger.getLogger(AbstractComposer.class.getName());

    protected AbstractArticleParser parser;
    protected PhoneticIntf phonetic = ThreadStorage.getPhonetics();
    protected AbbreIntf abbr = ThreadStorage.getAbbreviation();
    protected VerseIntf verse = ThreadStorage.getVerse();
    
    public AbstractComposer(AbstractArticleParser parser) {
        this.parser = parser;
    }

    protected String wrapToPolly(String content) {

        return AWSUtil.toPolly(content);

    }

    protected String wrapToAzure(String content) {

        return AzureUtil.toTTS(content);

    }

    protected String wrapToAzure(String content, String voiceId) {

        return AzureUtil.toTTS(content, voiceId);

    }

    protected String processSentence(String content, boolean fixPronu, boolean replacePunc) {
        if( StringUtils.contains(content, "<") ){
            return content;
        }

        String result = "";

        if( replacePunc ){
            result = replacePunctuationWithBreakTag(abbr.convert(content));
        }else{
            result = abbr.convert(content);
        }

        if( fixPronu ){
            result = phonetic.convert(result);
        }
        result = replacePauseTag(result);

        return result;
    }

    protected String processSentence(String content, boolean fixPronu, String type) {
        if( StringUtils.contains(content, "<") ){
            return content;
        }
        String result = "";

        if( StringUtils.equalsIgnoreCase(type, "remembrance") ){
            result = replaceRemPunctuationWithBreakTag(abbr.convert(content));
        }else{
            result = abbr.convert(content);
        }

        if( fixPronu ){
            result = phonetic.convert(result);
        }
        result = replacePauseTag(result);

        return result;
    }

    protected String processSentence(String content, boolean fixPronu) {
        return processSentence(content, fixPronu, true);
    }

    public abstract List<ComposerResult> toTTS(boolean fixPronu, String publishDate);

    public String generatePLScript() {
        Map<String, String> scripts = splitPolly(false);
        Set<Map.Entry<String, String>> entries = scripts.entrySet();
        StringBuilder builder = new StringBuilder();
        for( Map.Entry<String, String> entry : entries ){
            String script = entry.getValue();
            builder.append(script);
        }

        return prettyPrintln(builder.toString());
    }

    protected abstract Map<String, String> splitPolly(boolean fixPronu);


//    private final Pattern redundantTagPattern = Pattern.compile("(<break\\s+time='800ms'/>\\.{0,}[\\n|\\r\\n]{0,}){1,}");
    private String redundantTagPattern = "(<break\\s+time='%d'/>\\.{0,}[\\n|\\r\\n]{0,}){1,}";
    protected Map<String, String> houseKeepRedundantTag(Map<String, String> scripts) {
        Set<Map.Entry<String, String>> entries = scripts.entrySet();
        for( Map.Entry<String, String> entry : entries ){
            String script = entry.getValue();
            StringBuffer result = new StringBuffer(script);
            for( int i = 1; i<=10; i++ ){
                Pattern p = Pattern.compile(String.format(redundantTagPattern, i*100));
                Matcher matcher = p.matcher(result);
                while( matcher.find() ){
                    String matched = matcher.group(0);
                    if(StringUtils.countMatches(matched, "break") > 1){
                        matcher.appendReplacement(result, matcher.group(1));
                    }
                }
//                matcher.appendTail(result);
            }
            scripts.put(entry.getKey(), result.toString());
        }

        return scripts;
    }

    private int script_line_limit = 10;
    protected List<String> grabAndSplitVerse(String content) {
        content = StringUtils.trim(content);
        List<String> result = new ArrayList<>();
        if(StringUtils.isEmpty(content) ){
            return result;
        }

        String[] splits = content.split(System.lineSeparator());
        int lineNumber = splits.length;
        int quotient = Math.floorDiv(lineNumber, script_line_limit);

        if( quotient <= 0 ){
            result.add(content);
            return result;
        }

        if( quotient == 1 ){
            int splitNumber = lineNumber/2;
            result.add(StringUtils.join(Arrays.copyOfRange(splits, 0, splitNumber), System.lineSeparator()));
            result.add(StringUtils.join(Arrays.copyOfRange(splits, splitNumber, lineNumber), System.lineSeparator()));
        }

        if( quotient > 1 ){
            for( int i=1; i<=quotient+1; i++ ){
                int start = (i-1)*script_line_limit;
                int end = i*script_line_limit;

                if( end <= lineNumber ){
                    result.add(StringUtils.join(Arrays.copyOfRange(splits, start, end), System.lineSeparator()));
                }else{
                    result.add(StringUtils.join(Arrays.copyOfRange(splits, start, lineNumber), System.lineSeparator()));
                }

            }
        }

        return result;
    }
    

    protected String prettyPrintln(String input) {
//        Source xmlInput = new StreamSource(new StringReader(input));
//        StringWriter stringWriter = new StringWriter();
//        StreamResult xmlOutput = new StreamResult(stringWriter);
//        TransformerFactory transformerFactory = TransformerFactory.newInstance();
//        transformerFactory.setAttribute("indent-number", 4);
//        transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
//        transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
//        Transformer transformer = transformerFactory.newTransformer();
//        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
//        transformer.setOutputProperty("omit-xml-declaration", "yes");
//        transformer.transform(xmlInput, xmlOutput);
//        return xmlOutput.getWriter().toString();
        return input.replaceAll("<break", System.lineSeparator()+"<break");
    }

    protected List<Tag> sendToTTS(String fileName, String content, String publishDate, int count){

        List<Tag> tags = new ArrayList<>();
        logger.info("use.polly is " + Boolean.valueOf((String) AppProperties.getConfig().getOrDefault("use.polly", "true")));
        if (Boolean.valueOf((String) AppProperties.getConfig().getOrDefault("use.polly", "false")) != true) {
            return tags;
        }

        logger.info("send to polly script S3 bucket!");
        if( StringUtils.isEmpty(this.parser.getArticle().getTiming()) ){
            tags.addAll( AWSUtil.putScriptToS3(fileName, content, getPublishMonth(publishDate), getPublishDate(publishDate), count) );
        }else{
            tags.addAll( AWSUtil.putScriptToS3(fileName, content, getPublishMonth(publishDate), getPublishDate(publishDate), this.parser.getArticle().getTiming()) );
        }
        return tags;
    }

    public String getPublishDate(String input){
        String publishDate = input.split("-")[2];
        return publishDate;
    }

    public String getPublishMonth(String input){
        String publishMonth = input.split("-")[0] + "_" + input.split("-")[1];
        return publishMonth;
    }

    protected String removeLineWhitespace( String content ){
        String result = "";
        for(char c : content.toCharArray()){
            if( c == ' ' ){
                continue;
            }
            result += c;
        }
        return result;
    }

    protected String findWeekDay(String startDate) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate date = LocalDate.parse(startDate, formatter);
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        if( StringUtils.equalsIgnoreCase(dayOfWeek.toString(), "Sunday") ){
            return "Lord's day";
        }

        return dayOfWeek.toString();
    }

}


package com.cpbpc.rpgv2;

import com.amazonaws.services.s3.model.Tag;
import com.cpbpc.comms.AWSUtil;
import com.cpbpc.comms.AppProperties;
import com.cpbpc.comms.ThreadStorage;
import org.apache.commons.lang3.StringUtils;

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

    protected String processSentence(String content, boolean fixPronu) {
        if( fixPronu ){
            return replacePauseTag(phonetic.convert(replacePunctuationWithBreakTag(abbr.convert(content))));
        }
        return replacePauseTag(replacePunctuationWithBreakTag(abbr.convert(content)));
    }

    protected abstract List<ComposerResult> toPolly(boolean fixPronu, String publishDate);

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


    private final Pattern redundantTagPattern = Pattern.compile("(<break\\s+time='800ms'/>\\.{0,}[\\n|\\r\\n]{0,}){1,}");
    protected Map<String, String> houseKeepRedundantTag(Map<String, String> scripts) {
        Set<Map.Entry<String, String>> entries = scripts.entrySet();
        for( Map.Entry<String, String> entry : entries ){
            String script = entry.getValue();
            StringBuffer result = new StringBuffer();
            Matcher matcher = redundantTagPattern.matcher(script);
            while( matcher.find() ){
                String matched = matcher.group(0);
                if(StringUtils.countMatches(matched, "break") > 1){
                    matcher.appendReplacement(result, matcher.group(1));
                }
            }
            matcher.appendTail(result);
            scripts.put(entry.getKey(), result.toString());
        }

        return scripts;
    }

    protected List<String> grabAndSplitVerse(String content) {
        content = StringUtils.trim(content);
        List<String> result = new ArrayList<>();
        if(StringUtils.isEmpty(content) ){
            return result;
        }

        String[] splits = content.split(System.lineSeparator());
        int lineNumber = splits.length;
        int quotient = Math.floorDiv(lineNumber, 10);
        int remainder = lineNumber%10;

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
                int start = (i-1)*10;
                int end = i*10;

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

    protected List<Tag> sendToPolly(String fileName, String content, String publishDate){

        List<Tag> tags = new ArrayList<>();
        logger.info("use.polly is " + Boolean.valueOf((String) AppProperties.getConfig().getOrDefault("use.polly", "true")));
        if (Boolean.valueOf((String) AppProperties.getConfig().getOrDefault("use.polly", "false")) != true) {
            return tags;
        }

        logger.info("send to polly script S3 bucket!");
        tags.addAll( AWSUtil.putScriptToS3(fileName, content, publishDate) );

        return tags;
    }

}


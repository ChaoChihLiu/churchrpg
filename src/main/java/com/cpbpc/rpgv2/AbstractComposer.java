package com.cpbpc.rpgv2;

import com.cpbpc.rpgv2.util.ThreadStorage;

import javax.xml.transform.TransformerException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.cpbpc.rpgv2.util.PauseTool.pause;
import static com.cpbpc.rpgv2.util.PauseTool.replacePunctuationWithPause;

public abstract class AbstractComposer {

    protected AbstractArticleParser parser;
    protected PhoneticIntf phonetic = ThreadStorage.getPhonetics();
    protected AbbreIntf abbr = ThreadStorage.getAbbreviation();
    protected VerseIntf verse = ThreadStorage.getVerse();

    public AbstractComposer(AbstractArticleParser parser) {
        this.parser = parser;
    }

    protected String wrapToPolly(String content) {

        return "<speak><prosody rate='" + AppProperties.getProperties().getProperty("speech_speed")
                + "' volume='" + AppProperties.getProperties().getProperty("speech_volume") + "'>"
                + content
                + "</prosody></speak>";

    }

    protected String processSentence(String content) {
        return replacePauseTag(replacePunctuationWithPause(phonetic.convert(content)));
    }

    protected abstract String toPolly();
    
    private final Pattern pattern = Pattern.compile("\\[pause(\\d{0,})\\]");
    protected String replacePauseTag( String input ){
        Matcher matcher = pattern.matcher(input);
        String result = input;
        while( matcher.find() ){
            String timespan = matcher.group(1);
            result = result.replace(matcher.group(0), pause(timespan));
        }

        return result;
    }

    protected String prettyPrintln(String input) throws TransformerException {
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

}

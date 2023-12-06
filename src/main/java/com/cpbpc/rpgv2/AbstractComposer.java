package com.cpbpc.rpgv2;

import com.cpbpc.comms.AWSUtil;
import com.cpbpc.comms.ThreadStorage;

import javax.xml.transform.TransformerException;

import static com.cpbpc.comms.PunctuationTool.replacePauseTag;
import static com.cpbpc.comms.PunctuationTool.replacePunctuationWithBreakTag;

public abstract class AbstractComposer {

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
            return replacePauseTag(replacePunctuationWithBreakTag(phonetic.convert(content)));
        }
        return replacePauseTag(replacePunctuationWithBreakTag(content));
    }

    protected abstract String toPolly(boolean fixPronu);
    

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

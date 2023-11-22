package com.cpbpc;

import org.apache.commons.lang3.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexExample {
    public static void main(String[] args) {

        String test = "4";

        test = StringUtils.substring(test, 0, StringUtils.length(test)-2)
                + " chapter "
                + StringUtils.substring(test, ((StringUtils.length(test)-2)<=0)?0:StringUtils.length(test)-2, ((StringUtils.length(test)-1)<=0)?1:StringUtils.length(test)-1);
        System.out.println(test);

        // The input string you want to search in
//        String input = "&nbsp;</option><option class=\"lang\" value=\"CCB\">&mdash;ZH&mdash;</option><option value=\"CCB\" >CCB</option><option value=\"CCBT\" >CCBT</option><option value=\"ERV-ZH\" >ERV-ZH</option><option value=\"CNVS\" >CNVS</option><option value=\"CNVT\" >CNVT</option><option value=\"CSBS\" >CSBS</option><option value=\"CSBT\" >CSBT</option><option value=\"CUVS\" >CUVS</option><option value=\"CUV\"  selected=\"selected\" >CUV</option><option value=\"CUVMPS\" >CUVMPS</option><option value=\"CUVMPT\" >CUVMPT</option><option value=\"RCU17SS\" >RCU17SS</option><option value=\"RCU17TS\" >RCU17TS</option></select><button>Update</button></div><div class=\"passage-text\"><div class='passage-content passage-class-0'><div class=\"version-CUV result-text-style-normal text-html\"> <p class=\"verse chapter-2\"><span id=\"zh-CUV-107698\" class=\"text Ps-34-1\"><span class=\"chapternum\">34 </span>（ 大 衛 在 亞 比 米 勒 面 前 裝 瘋 ， 被 他 趕 出 去 ， 就 作 這 詩 。 ） 我 要 時 時 稱 頌 耶 和 華 ； 讚 美 他 的 話 必 常 在 我 口 中 。</span></p> <p class=\"verse\"><span id=\"zh-CUV-107699\" class=\"text Ps-34-2\"><sup class=\"versenum\">2 </sup>我 的 心 必 因 耶 和 華 誇 耀 ； 謙 卑 人 聽 見 就 要 喜 樂 。</span></p> <p class=\"verse\"><span id=\"zh-CUV-107700\" class=\"text Ps-34-3\"><sup class=\"versenum\">3 </sup>你 們 和 我 當 稱 耶 和 華 為 大 ， 一 同 高 舉 他 的 名 。</span></p> <p class=\"verse\"><span id=\"zh-CUV-107701\" class=\"text Ps-34-4\"><sup class=\"versenum\">4 </sup>我 曾 尋 求 耶 和 華 ， 他 就 應 允 我 ， 救 我 脫 離 了 一 切 的 恐 懼 。</span></p> <p class=\"verse\"><span id=\"zh-CUV-107702\" class=\"text Ps-34-5\"><sup class=\"versenum\">5 </sup>凡 仰 望 他 的 ， 便 有 光 榮 ； 他 們 的 臉 必 不 蒙 羞 。</span></p> <p class=\"verse\"><span id=\"zh-CUV-107703\" class=\"text Ps-34-6\"><sup class=\"versenum\">6 </sup>我 這 困 苦 人 呼 求 ， 耶 和 華 便 垂 聽 ， 救 我 脫 離 一 切 患 難 。</span></p> <p class=\"verse\"><span id=\"zh-CUV-107704\" class=\"text Ps-34-7\"><sup class=\"versenum\">7 </sup>耶 和 華 的 使 者 在 敬 畏 他 的 人 四 圍 安 營 ， 搭 救 他 們 。</span></p> <p class=\"verse\"><span id=\"zh-CUV-107705\" class=\"text Ps-34-8\"><sup class=\"versenum\">8 </sup>你 們 要 嘗 嘗 主 恩 的 滋 味 ， 便 知 道 他 是 美 善 ； 投 靠 他 的 人 有 福 了 ！</span></p> <p class=\"verse\"><span id=\"zh-CUV-107706\" class=\"text Ps-34-9\"><sup class=\"versenum\">9 </sup>耶 和 華 的 聖 民 哪 ， 你 們 當 敬 畏 他 ， 因 敬 畏 他 的 一 無 所 缺 。</span></p> <p class=\"verse\"><span id=\"zh-CUV-107707\" class=\"text Ps-34-10\"><sup class=\"versenum\">10 </sup>少 壯 獅 子 還 缺 食 忍 餓 ， 但 尋 求 耶 和 華 的 甚 麼 好 處 都 不 缺 。</span></p>";

        // Define the regular expression pattern
//        String regex = "<sup class=\"versenum\">(\\d+)\\u00A0</sup>";
//        String regex = "(<span\\s{1,}class=\"chapternum\">34\\u00A0</span>)([^<>]*)(</span></p>)";

        // Create a Pattern object
//        Pattern pattern = Pattern.compile(regex);

        // Create a Matcher object
//        Matcher matcher = pattern.matcher(input);

        // Find and print all matching occurrences
//        while (matcher.find()) {
//            // The matched text is group 0
//            String match = matcher.group(2);
//            // The captured number is group 1
////            String number = matcher.group(1);
//            System.out.println("Matched: " + match);
////            System.out.println("Captured number: " + number);
//        }

//        String input = "<p class=\"chapter-2\"><span id=\"en-KJV-14333\" class=\"text Ps-31-1\"><span class=\"chapternum\">31&nbsp;</span>In thee, O <span style=\"font-variant: small-caps\" class=\"small-caps\">Lord</span>, do I put my trust; let me never be ashamed: deliver me in thy righteousness.</span></p>testtesttesttest\n" +
//                "    <p><span id=\"en-KJV-14341\" class=\"text Ps-31-9\"><sup class=\"versenum\">9&nbsp;</sup>Have mercy upon me, O <span style=\"font-variant: small-caps\" class=\"small-caps\">Lord</span>, for I am in trouble: mine eye is consumed with grief, yea, my soul and my belly.</span></p>\n" +
//                "    <p><span id=\"en-KJV-14343\" class=\"text Ps-31-11\"><sup class=\"versenum\">11&nbsp;</sup>I was a reproach among all mine enemies, but especially among my neighbours, and a fear to mine acquaintance: they that did see me without fled from me.</span></p>";
//
//        Pattern p = Pattern.compile("<sup\\s{1,}class=\"versenum\">11&nbsp;</sup>");
//        Matcher m = p.matcher(input);
//        int start = 0;
//        int end = 0;
//        if( m.find() ){
//            System.out.println(m.start());
//            System.out.println(m.end());
//            start = m.end();
//        }
//
//        p = Pattern.compile("</span></p>");
//        m = p.matcher(input);
//        if( m.find(start) ){
//            System.out.println(m.start());
//            System.out.println(m.end());
//            end = m.start();
//        }
//
//        System.out.println(input.substring(start, end));

//        System.out.println(input.indexOf("<span class=\"chapternum\">31&nbsp;</span>"));

//        String input = "<tbody>\n" +
//                "<tr>\n" +
//                "<td><em>十一月二十五日，主日</em><br />雅各书三章14节<br />加拉太书五章14-15节</td>\n" +
//                "<td style=\"text-align: right;\"><em></em><br /><em>“你们要谨慎……</em><br /><em>只怕要彼此消灭了。”</em></td>\n" +
//                "</tr>\n" +
//                "</tbody>\n" +
//                "</table>\n" +
//                "<p>&nbsp;</p>\n" +
//                "<p style=\"text-align: center;\"><strong>不可说谎抵挡真道</strong></p>";
//        String regex = "[一二三四五六七八九十百千零月日主礼拜，]{8,11}";

        String[] weekdays = new String[]{"LORD’S DAY",
                "MONDAY", "TUESDAY", "WEDNESDAY",
                "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"};
        String[] months = new String[]{"January", "February", "March",
                "April", "May", "June",
                "July", "August", "September",
                "October", "November", "December"};
        int max = 0;
        int min = 999;
        for (int i = 1; i < 32; i++) {
            for (String weekday : weekdays) {
                for (String month : months) {
                    String content = weekday + ", " + month + " ";
                    if (content.length() > max) {
                        max = content.length();
                    }
                    if (content.length() < min) {
                        min = content.length();
                    }
                }
            }
        }
        System.out.println("max " + max);
        System.out.println("min " + min);

        String input = "<div><span style=\"font-size: 12pt;\"><em>LORD’S DAY, JANUARY 7</em></span></div>\n" +
                "<div><span style=\"font-size: 12pt;\"><strong>Psalms 119 verse 10 to 11 </strong></span></div>\n" +
                "<p><span style=\"font-size: 12pt;\">Psalms 19 </span></p>\n" +
                "<p>&nbsp;</p>\n" +
                "<div><span style=\"font-size: 12pt;\"><em>“…and I shall be innocent from</em></span></div>\n" +
                "<p><span style=\"font-size: 12pt;\"><em>the great transgression.”</em></span></p>\n" +
                "<p>&nbsp;</p>\n" +
                "<div><span style=\"font-size: 12pt;\"><strong>SEEK GOD’S WORD</strong></span></div>\n" +
                "<div>&nbsp;</div>\n" +
                "<div style=\"text-align: justify;\"><span style=\"font-size: 12pt;\">Treasure hunters risk their lives to trek and find the most elusive and&nbsp;priceless gems or antiques in dangerous places. The death-defying&nbsp;pathways do not deter them from climbing the steep mountains or even&nbsp;diving into the deep. Not only does nature object to their covetous desires,&nbsp;even beasts also bar these men from getting near those secret caves and&nbsp;tombs. The dream of getting that greatest gem blinds their eyes to the jaws&nbsp;of death waiting for them on the trail. But, there is a far greater treasure&nbsp;that man must get, and it is beyond the valuation of man. Not one in&nbsp;this world can afford to purchase it, not even the combined assets of the&nbsp;world’s “trillionaires” (if there are such).</span></div>\n" +
                "<div style=\"text-align: justify;\">&nbsp;</div>\n" +
                "<div style=\"text-align: justify;\"><span style=\"font-size: 12pt;\"><span style=";
        String regex = "[A-Z,\\s’]{12,22}\\d{1,2}";

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);

        while (matcher.find()) {
            System.out.println("Found match: " + matcher.group());
        }


        // Input string
        input = "The quick brown fox jumps over the lazy dog.";

        // Define a regular expression pattern to find words
        pattern = Pattern.compile("\\b\\w+\\b");

        // Create a Matcher object using the pattern and the input string
        matcher = pattern.matcher(input);

        // Create a StringBuilder to build the modified result
        StringBuilder result = new StringBuilder();

        // Use the Matcher to find and replace words
        while (matcher.find()) {
            // Get the matched word
            String word = matcher.group();

            // Modify the word (e.g., convert to uppercase)
            String modifiedWord = word.toUpperCase();

            // Append the modified word to the result
            matcher.appendReplacement(result, modifiedWord);
        }

        // Append the remainder of the input string after the last match
        matcher.appendTail(result);

        // Print the modified result
        System.out.println(result.toString());


    }
}


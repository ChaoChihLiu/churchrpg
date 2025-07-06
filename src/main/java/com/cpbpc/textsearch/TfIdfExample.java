package com.cpbpc.textsearch;

import com.cpbpc.comms.TextUtil;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TfIdfExample {

    // Small constant to avoid IDF being zero
    private static final double MIN_IDF = 0.1;

    public static void main(String[] args) {
        List<Document> documents = Arrays.asList(
                new Document(1, TextUtil.removeHtmlTag(getText1())),
                new Document(2, TextUtil.removeHtmlTag(getText2())),
                new Document(3, TextUtil.removeHtmlTag(getText3()))
        );

        List<String> focusWords = Arrays.asList("salvation",
                "faith",
                "theology",
                "redemption",
                "belief",
                "doctrine",
                "atonement",
                "trust",
                "religious studies",
                "grace",
                "spirituality",
                "dogma",
                "justification",
                "confidence",
                "divinity",
                "repentance",
                "conviction",
                "creed");

        // Compute TF-IDF vectors for documents
        Map<Integer, Map<String, Double>> documentTfIdfVectors = computeTfIdfVectors(documents, focusWords);

        // Compute TF-IDF vector for the query
        Map<String, Double> queryTfIdfVector = computeQueryTfIdfVector(focusWords, documentTfIdfVectors);

        // Calculate relevance scores using cosine similarity
        List<RankingResult> rankingResults = relevanceRanking(documents, documentTfIdfVectors, queryTfIdfVector);

        double highestScore = 0;
        Document bestDoc = null;
        for (RankingResult result : rankingResults) {
            if( result.getRelevanceScore() > highestScore ){
                highestScore = result.getRelevanceScore();
                bestDoc = findDocument(documents, result.getDocumentId());
            }
            System.out.println("Document ID: " + result.getDocumentId() + ", Relevance Score: " + result.getRelevanceScore());
        }

        System.out.println(TextUtil.removeHtmlTag(bestDoc.content));
    }

    private static Document findDocument(List<Document> documents, int documentId) {

        for(Document doc : documents){
            if( doc.getId() == documentId ){
                return doc;
            }
        }

        return null;
    }

    public static Map<String, Double> computeTf(String document, List<String> focusWords) {
        Map<String, Integer> wordCount = new HashMap<>();
        String loweredDocument = document.toLowerCase();

        for (String focusWord : focusWords) {
            String loweredFocusWord = focusWord.toLowerCase();
//            int count = loweredDocument.split("\\b" + loweredFocusWord + "\\b", -1).length - 1;
            int count = loweredDocument.split(loweredFocusWord, -1).length - 1;
            wordCount.put(focusWord, count);
        }

        int totalWords = Arrays.asList(loweredDocument.split("\\s+")).size();

        Map<String, Double> tfScores = new HashMap<>();
        for (String word : focusWords) {
            tfScores.put(word, wordCount.getOrDefault(word, 0) / (double) totalWords);
        }
        return tfScores;
    }

    public static Map<String, Double> computeIdf(List<Document> documents, List<String> focusWords) {
        int totalDocs = documents.size();
        Map<String, Double> idfScores = new HashMap<>();
        for (String word : focusWords) {
            int containingDocs = 0;
            for (Document doc : documents) {
                if (doc.getContent().toLowerCase().contains(word.toLowerCase())) {
                    containingDocs++;
                }
            }
            double idf = Math.log((double) totalDocs / (1 + containingDocs));
            idfScores.put(word, Math.max(idf, MIN_IDF));
        }
        return idfScores;
    }

    public static Map<String, Double> computeTfIdf(Map<String, Double> tfScores, Map<String, Double> idfScores) {
        Map<String, Double> tfIdfScores = new HashMap<>();
        for (String word : tfScores.keySet()) {
            double tf = tfScores.get(word);
            double idf = idfScores.getOrDefault(word, MIN_IDF);
            tfIdfScores.put(word, tf * idf);
        }
        return tfIdfScores;
    }

    public static Map<Integer, Map<String, Double>> computeTfIdfVectors(List<Document> documents, List<String> focusWords) {
        Map<String, Double> idfScores = computeIdf(documents, focusWords);
        Map<Integer, Map<String, Double>> tfIdfVectors = new HashMap<>();

        for (Document doc : documents) {
            Map<String, Double> tfScores = computeTf(doc.getContent(), focusWords);
            Map<String, Double> tfIdfScores = computeTfIdf(tfScores, idfScores);
            tfIdfVectors.put(doc.getId(), tfIdfScores);
        }

        return tfIdfVectors;
    }

    public static Map<String, Double> computeQueryTfIdfVector(List<String> focusWords, Map<Integer, Map<String, Double>> documentTfIdfVectors) {
        // Compute the query vector as if it were a document
        Map<String, Double> idfScores = computeIdf(new ArrayList<>(), focusWords); // Dummy document list
        Map<String, Double> queryTfIdfVector = new HashMap<>();

        // Initialize query vector with zeros
        for (String word : focusWords) {
            double tf = 1.0 / focusWords.size(); // Simplistic TF for the query
            double idf = idfScores.getOrDefault(word, MIN_IDF);
            queryTfIdfVector.put(word, tf * idf);
        }
        return queryTfIdfVector;
    }

    public static double computeCosineSimilarity(Map<String, Double> vector1, Map<String, Double> vector2) {
        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        for (String key : vector1.keySet()) {
            double v1 = vector1.getOrDefault(key, 0.0);
            double v2 = vector2.getOrDefault(key, 0.0);
            dotProduct += v1 * v2;
            norm1 += v1 * v1;
            norm2 += v2 * v2;
        }

        norm1 = Math.sqrt(norm1);
        norm2 = Math.sqrt(norm2);

        return (norm1 > 0 && norm2 > 0) ? (dotProduct / (norm1 * norm2)) : 0.0;
    }

    public static List<RankingResult> relevanceRanking(List<Document> documents, Map<Integer, Map<String, Double>> documentTfIdfVectors, Map<String, Double> queryTfIdfVector) {
        List<RankingResult> rankings = new ArrayList<>();

        for (Map.Entry<Integer, Map<String, Double>> entry : documentTfIdfVectors.entrySet()) {
            int docId = entry.getKey();
            Map<String, Double> docVector = entry.getValue();
            double similarityScore = computeCosineSimilarity(docVector, queryTfIdfVector);
            rankings.add(new RankingResult(docId, similarityScore));
        }

        rankings.sort(Comparator.comparingDouble(RankingResult::getRelevanceScore).reversed());
        return rankings;
    }

    public static class Document {
        private final int id;
        private final String content;

        public Document(int id, String content) {
            this.id = id;
            this.content = content;
        }

        public int getId() {
            return id;
        }

        public String getContent() {
            return content;
        }
    }

    public static class RankingResult {
        private final int documentId;
        private final double relevanceScore;

        public RankingResult(int documentId, double relevanceScore) {
            this.documentId = documentId;
            this.relevanceScore = relevanceScore;
        }

        public int getDocumentId() {
            return documentId;
        }

        public double getRelevanceScore() {
            BigDecimal bd = new BigDecimal(relevanceScore);
            bd = bd.setScale(3, RoundingMode.HALF_EVEN);
            return bd.doubleValue();
        }
    }

    private static String getText1() {

        return "<div><span style=\"font-size: 12pt;\"><em>MONDAY, JULY 6</em></span></div>\n" +
                "<div><span style=\"font-size: 12pt;\"><strong>Acts 1:5</strong></span></div>\n" +
                "<p><span style=\"font-size: 12pt;\">John 14:16-18</span></p>\n" +
                "<p>&nbsp;</p>\n" +
                "<div><span style=\"font-size: 12pt;\"><em>“…for he dwelleth with you,</em></span></div>\n" +
                "<p><span style=\"font-size: 12pt;\"><em>and shall be in you.”</em></span></p>\n" +
                "<p>&nbsp;</p>\n" +
                "<div><span style=\"font-size: 12pt;\"><strong>PROMISED BAPTISM</strong></span></div>\n" +
                "<div>&nbsp;</div>\n" +
                "<div style=\"text-align: justify;\"><span style=\"font-size: 12pt;\">Many Christians today seek for a “second” baptism, a post-conversion&nbsp;experience to confirm that they are truly born-again believers. They say,&nbsp;“I was baptised by the Holy Spirit and after a series of tutoring I was able&nbsp;to speak in tongues!” However, the book of Acts makes no mention of&nbsp;the baptism of the Holy Spirit, for the Spirit of God does not baptise but&nbsp;Christ.</span></div>\n" +
                "<div style=\"text-align: justify;\">&nbsp;</div>\n" +
                "<div style=\"text-align: justify;\"><span style=\"font-size: 12pt;\"><span style=\"text-decoration: underline;\">Baptism with water</span>: John the Baptist’s main mission was to announce the&nbsp;coming of the Messiah. In John 1:29, he pointed to Jesus as the “<em>Lamb&nbsp;of God, which taketh away the sin of the world.</em>” His was the baptism of&nbsp;repentance for the remission of sins.</span></div>\n" +
                "<div style=\"text-align: justify;\">&nbsp;</div>\n" +
                "<div style=\"text-align: justify;\"><span style=\"font-size: 12pt;\"><span style=\"text-decoration: underline;\">Baptism with the Holy Spirit</span>: The promise was for the disciples to be&nbsp;baptised with the Holy Ghost (Acts 1:5) which is described as the Holy&nbsp;Spirit coming upon the believer. It was further clarified in Acts 2:4 as a&nbsp;filling with the Holy Spirit.</span></div>\n" +
                "<div style=\"text-align: justify;\">&nbsp;</div>\n" +
                "<div style=\"text-align: justify;\"><span style=\"font-size: 12pt;\">The disciples of the Lord Jesus Christ were already indwelt with the Spirit&nbsp;since they were already believers. Although Acts 1:5 appears to have the&nbsp;idea of a second baptism or a second blessing, the baptism mentioned&nbsp;among the disciples of Christ was about the filling of the Holy Spirit. Acts&nbsp;2:4 tells us that they were “<em>filled</em>” with the Holy Spirit. “This baptismal&nbsp;filling of the Spirit was not for salvation, but for service” (Khoo). The&nbsp;purpose of Jesus Christ’s promise to send the Holy Spirit was to instruct&nbsp;them in the doctrines they were to proclaim to the people and that they&nbsp;might be strengthened to withstand the conflicts which they would face.&nbsp;It is not a second baptism nor the gibberish and ecstatic utterances of&nbsp;the so-called “tongue-speaking” of today’s Charismatics. Rather, it is the&nbsp;Holy Spirit who would enable Christians to witness both near and far.</span></div>\n" +
                "<div style=\"text-align: justify;\">&nbsp;</div>\n" +
                "<div style=\"text-align: justify;\"><span style=\"font-size: 12pt;\">Have you truly believed in the Lord Jesus Christ? Are you filled with&nbsp;the Spirit of God? Are you able to speak forth the gospel of Jesus&nbsp;Christ to others?</span></div>\n" +
                "<div style=\"text-align: justify;\">&nbsp;</div>\n" +
                "<div style=\"text-align: justify;\"><span style=\"font-size: 12pt;\"><strong>THOUGHT:</strong> Am I baptised with the Holy Ghost?</span></div>\n" +
                "<div style=\"text-align: justify;\"><span style=\"font-size: 12pt;\"><strong>PRAYER:</strong> Father, please fill me with Thy Holy Spirit and let Him abide&nbsp;with me forever.</span></div>";

    }

    private static String getText2() {
        return "<p>My dear readers,</p>\n" +
                "<p>Billy Harichaikul entered FEBC in 2004, completed his Bachelor of Theology in 2008 and Master of Divinity in 2010.&nbsp;He returned to BDC to serve the Lord upon graduation in 2010 and has been serving till today, by the grace and mercy of God. He has written doctrinal papers that included: “On Bible Inspiration and Preservation”; “On Reformed Theology”; “On Biblical Separation”; “On the Millennium”; “On Church Government”; “On the History of the B-P Church of Singapore”; “On Hermeneutics”; and “On Pastoral Ministry”; as well as “On Personal Matters”: (a) Write a testimony of your salvation and call to fulltime ministry; (b) How can I contribute to the advancement of my church?; (c) A Resume of your background and service in churches that you have attended. Billy passed the oral examination presided by the ordination council comprising Rev Quek Suan Yew (chairman), Rev Clement Chew of Tabernacle BPC and Rev Lim Seh Beng of Calvary Jaya B-P Fellowship.</p>\n" +
                "<p><strong>A testimony of my salvation and call to fulltime ministry:</strong></p>\n" +
                "<p>I was brought up in a Christian family. My grandmother taught me about Jesus and prayed with me since I was very young. However, I had no interest in any spiritual or religious matters. As my father lay on his deathbed, he told me to repent of my sins and return to God. Upon hearing that, I was sad but continued to live in my sins. I was later baptized without any proper understanding of what it means to be saved. I felt pressured to do so. I just went through the motion. &nbsp;I believe I was not saved back then.</p>\n" +
                "<p>University life was my time to enjoy the world. During that time, one of the Youth For Christ (YFC) members from Campus Crusade approached me and shared with me the Gospel of Jesus Christ. He asked me if I will be in heaven after I died. I could not answer him. He then showed me the verse from Romans 10:9 “<em>That if thou shalt confess with thy mouth the Lord Jesus, and shalt believe in thine heart that God hath raised him from the dead, thou shalt be saved</em>”. This verse spoke to me, and I believed and was led to pray the sinner’s prayer. From then on, I believed and accepted Jesus Christ as my personal Lord and Saviour and was assured of my salvation.</p>\n" +
                "<p>However, I continued to live a sinful life. My spiritual life did not progress as I did not know what I should do after salvation. I thought I could live my life the way I wanted since I had received salvation, but deep in my heart I had no peace and knew that I was not right with God.</p>\n" +
                "<p>In my final year of studies, I was diagnosed with tuberculosis. I feared death and started to seek help and solutions, but found none. There was a huge burden and fear in my heart. My grandmother told me the exact words that my father told me on his deathbed: to pray, repent and return to God. I prayed and vowed to God that if He healed me, I would someday go back to Him and serve Him. Thank God for full recovery after six months of medication.</p>\n" +
                "<p>Despite God’s goodness and mercy upon my life, I forgot my vow and chased after the world again. There was no repentance, life change, or interest in spiritual matters. After graduation, I worked as a teacher in a missionary school. In the 3rd year of my career, my sister, who was studying at FEBC, kept calling me to study with her. I rejected her invitations because I did not want to waste my time there. I would instead do what I wanted. But God did not forget me even though I forgot my vow to Him.</p>\n" +
                "<p>That year, I felt no peace in my heart and was heavily burdened. I had no joy of salvation because of my disobedience. Everything seemed wrong with my life. I recalled my prayer and vow to the Lord when I was very sick. God called me to full-time service with the verse in Matthew 6:33 “<em>Seek ye first the kingdom of God and his righteousness, and all these things shall be added unto you.</em>” This verse spoke to me to put God first in everything, and He will do what is necessary for me according to His will.</p>\n" +
                "<p>After much struggle, I responded to the call, resigned from my secular job and joined my sister to study in FEBC. Thank God for leading me to FEBC, the God-honouring and faithful Bible college, where I learned the whole counsel of God’s Word, and that the Word of God and the testimony of Jesus Christ are the only things we have to live for. Truly, it was a great joy and blessing to have learned under many faithful servants of God during my study there.</p>\n" +
                "<p>In the 3rd year of my study at FEBC, my uncle encouraged me to help him with the children's hostel ministry back home in Thailand. I prayed and asked the Lord to show me where He wanted me to serve. My prayer was that wherever He leads me, I would go. The calling became clearer through the daily study of God’s Word and I came across Isaiah 6:8 “<em>Also I heard the voice of the Lord, saying, Whom shall I send, and who will go for us? Then said I, Here am I; send me.</em>” God laid a burden in my heart for the Lahu people who needed to hear and receive the truth of God’s Word.</p>\n" +
                "<p>From then on, I prayed and prepared to return and serve in my homeland. Thank God for answering prayers and leading me back to the BDC ministry that I used to reject and showed no interest, but man proposes and God disposes.</p>\n" +
                "<p>Looking back, I truly appreciate and am thankful for what God has done in my life. I had gone my own way many times, but He did not give up on me. Instead, He led and guided me. Truly, He is a gracious, merciful and loving God who has prepared me my entire life to know Him and serve Him full-time. I pray that my house and I will serve the Lord my whole life in His ministry with His enabling until He calls me home (Joshua 24:15).</p>\n" +
                "<p><strong>What are your ministries?</strong></p>\n" +
                "<p>Thank God for leading me to serve in Bethel Development Centre (BDC).</p>\n" +
                "<p>BDC ministers to the Lahu children in Chiang Rai through her hostel ministry, which is called the “Bethel Hostel Ministry” (BHM). By God’s grace, we provide them food, shelter and the Word of God to lead them to Christ.</p>\n" +
                "<p>BDC also ministers to adults such as the parents of the hostel children, the local pastors, evangelists, friends and loved ones through her Bible College ministry named “Bethel Bible College” (BBC). Thank God for sustaining us to teach God’s inerrant, infallible and perfectly preserved Word to the people here.</p>\n" +
                "<p>The goals of BDC ministry are:&nbsp;</p>\n" +
                "<ul>\n" +
                "<li><span style=\"font-size: 12pt;\">“To know Christ and to make Him known” and lead people to salvation.</span></li>\n" +
                "<li><span style=\"font-size: 12pt;\">To defend the Truth, warn and expose the false teachings and practices that contradict the Bible.&nbsp;</span></li>\n" +
                "<li><span style=\"font-size: 12pt;\">To be a living testimony for Christ.</span></li>\n" +
                "</ul>\n" +
                "<p>With God’s enabling and guidance from His Word, I will try my best to help the Lahu people to know God and grow in Him through the planned activities:</p>\n" +
                "<ul>\n" +
                "<li><span style=\"font-size: 12pt;\">Daily Bible Class for the adults attending Bethel Bible College</span></li>\n" +
                "<li><span style=\"font-size: 12pt;\">Daily devotion for the hostel children</span></li>\n" +
                "<li><span style=\"font-size: 12pt;\">Prayer meeting</span></li>\n" +
                "<li><span style=\"font-size: 12pt;\">Monthly Bible Study</span></li>\n" +
                "<li><span style=\"font-size: 12pt;\">Adult Bible Class</span></li>\n" +
                "<li><span style=\"font-size: 12pt;\">Lord's Day Morning Worship Service&nbsp;</span></li>\n" +
                "<li><span style=\"font-size: 12pt;\">Lord's Day Thai Worship Service</span></li>\n" +
                "<li><span style=\"font-size: 12pt;\">Annual Events such as Good Friday Service, Reformation Sunday Service, Christmas and Year End services, etc.</span></li>\n" +
                "</ul>\n" +
                "<p>I thank God for the privilege to serve in BDC, where my role of service is to manage the planning and running of the events and activities, the compound and building projects and oversee the physical and spiritual well-being of the hostel children and adults, especially by feeding them God's Word.</p>\n" +
                "<p>I covet your prayers that the Lord will help me, my family and other staff to serve Him faithfully to the best of our abilities as He enables in His ministry.</p>\n" +
                "<p><em>Yours faithfully in the Saviour’s Service</em></p>\n" +
                "<p><em>Rev Dr Quek Suan Yew, Pastor</em></p>";
    }

    private static String getText3() {
        return "<p style=\"text-align: center;\">How We Approach God Matters (Ecclesiastes 5:1-7)</p>\n" +
                "<p><strong>Introduction</strong></p>\n" +
                "<p>Going to the house of God for worship is the highlight of the week, a foretaste of worshipping God in heaven for eternity. How we approach and worship God reflects our heart condition and our relationship with God.&nbsp; Unlike the previous four chapters, God is mentioned six times in Ecclesiastes 5:1-7, which specifically warns us that how we approach God matters, for He is in heaven, the creator, ruler, and judge of all things, and we are mere creatures. &nbsp;&nbsp;&nbsp;&nbsp;</p>\n" +
                "<p><strong>Approach God in holiness to hear Him and His Word (Ecclesiastes 5:1)</strong></p>\n" +
                "<p><em><sup>1</sup></em><em> Keep thy foot when thou goest to the house of God, and be more ready to hear, than to give the sacrifice of fools: for they consider not that they do evil.</em></p>\n" +
                "<p>“<em>Keep thy foot</em>” means to keep the worship of God holy, for He is holy; and the house of God is holy, for God is in the midst during worship. Think of Moses meeting God at the burning bush. God told him to take off his shoes, for the ground that he stood on was holy ground (Exo 3:5). &nbsp;“<em>The house of God</em>” refers to the temple in Jerusalem that Solomon built nearly a thousand years before the time of Jesus, and the places designated for the worship of God in our time. Holiness in worship is crucial. Any contemporary songs, music, musical instruments, and the ways of the world should never be allowed in the worship of God. Worshippers should come with hearts prepared: with clean hands and a pure heart; a heart that is careful, discerning, and governed by the truth of God’s Word; a heart filled with gratitude and thanksgiving; a heart that is humble and teachable, ready to hear God’s Word.&nbsp; The word “<em>hear</em>” conveys a combined meaning of “pay attention” and “obey”. It is a “focused hearing” of God’s Word preached that leads to drawing closer to God, putting into remembrance, transforming hearts, obeying, and doing His Word. This is what God desires of His children. Worshipping God without “focused hearing”, with unconfessed and unrepented sin will be in vain even though the hymns may be sung wonderfully, and the tithes and offerings given generously. Solomon labelled them as “<em>the sacrifice of fools</em>”.</p>\n" +
                "<p><strong>Approach God with a guarded mouth and heart (Ecclesiastes 5:2-3)</strong></p>\n" +
                "<p><em><sup>2</sup></em><em> Be not rash with thy mouth, and let not thine heart be hasty to utter any thing before God: for God is in heaven, and thou upon earth: therefore let thy words be few. </em><em><sup>3</sup></em><em> For a dream cometh through the multitude of business; and a fool's voice is known by multitude of words.</em></p>\n" +
                "<p>A good understanding of God’s holiness, goodness, and greatness will govern how we approach Him in worship. God is in heaven, and we are on earth. This reminds us of His gloriousness and majesty, He is infinite and we are limited, He is the creator and we are His creatures. It is a wonderful privilege to worship Him. These understandings should cause us to be reverent and careful when we enter into His holy presence. Solomon counselled us to guard that our mouth be not rash and our heart not hasty - to be careful with what we say in our prayers and promises made before God. This divine rule applies to all prayers and supplications, not just corporate worship. Proverbs 20:25 condemns worshippers making hasty vows to God, “<em>It is a snare to the man who devoureth that which is holy, and after vows to make enquiry.</em>” Solomon further advised us “<em>let thy words be few</em>” - control every word and promise that originates from our heart and comes out from our mouth, for God holds us accountable for every word we say and all we do. A reverent worshipper is few of words and much in thoughts and meditation.</p>\n" +
                "<p>Verse 3 is saying: “A dream comes through much business (i.e. effort or activity), and a fool’s voice is known by his many careless words”. Careless words are likened to dreams that are of no real value. Solomon rightly described the human tendency to speak without thinking before God and others. It is foolish to speak too much and hear too little in God’s presence.</p>\n" +
                "<p><strong>Approach God with thoughtful vows (Ecclesiastes 5:4-7)</strong></p>\n" +
                "<p><em><sup>4</sup></em><em> When thou vowest a vow unto God, defer not to pay it; for he hath no pleasure in fools: pay that which thou hast vowed. </em><em><sup>5</sup></em><em> Better is it that thou shouldest not vow, than that thou shouldest vow and not pay. </em><em><sup>6</sup></em><em> Suffer not thy mouth to cause thy flesh to sin; neither say thou before the angel, that it was an error: wherefore should God be angry at thy voice, and destroy the work of thine hands? </em><em><sup>7</sup></em> <em>For in the multitude of dreams and many words there are also divers vanities</em><em>:</em><em> but fear thou God.</em></p>\n" +
                "<p>Solomon warned against making vows to God and then failing to keep them. God is not pleased with foolish persons who do so. It is better not to vow than to make a vow and not pay. Do not allow your mouth to cause you to sin, and do not say before the angel (messenger) that it was an error. God will surely be angry with your excuse and destroy (reject) your work.</p>\n" +
                "<p>In Israel, making vows was a common feature in Jewish worship and a serious matter. Vows were voluntary promises to God, but once made were unbreakable. Breaking them was a sin: “<em>When thou shalt vow a vow unto the Lord thy God, thou shalt not slack to pay it: for the Lord thy God will surely require it of thee; and it would be sin in thee.” </em>(Deut 23:21)<em>.</em> &nbsp;In our context, examples of vows are vows made during holy matrimony, adult baptism, infant baptism (by parents), New Year resolutions, promises before God and the Dean Burgeon Oath made on Dedication Sunday. Solomon rightly pointed out that it is important for God’s people to regard their failure to keep vows as a serious matter, and that great effort should be put into keeping vows and not regard the failure to keep them as simply an error or a thoughtless mistake. This divine requirement is applicable to promises made and contracts signed between men.</p>\n" +
                "<p>Please note verse 5 is not a prohibition of vows, or an excuse to never commit to anything, for there are legitimate vows such as marriage and membership vows. The teaching is to make vows thoughtfully and commit to fulfil them fully. In this context, public invitations are not biblical because they prompt worshippers to make impulsive hasty “vows” to God.</p>\n" +
                "<p>To sum up, verse 7 says, “<em>For in the multitude of dreams and many words there are also divers vanities</em>”, and the remedy is “<em>fear thou God</em>”. We are to fear God in awe of His greatness, power, justice and righteousness. Instead of many words, fear God. Fear is not cowering in terror. It is recognizing who God is and entering His presence with reverence and awe.</p>\n" +
                "<p><strong>Conclusion</strong></p>\n" +
                "<p>Considering the holiness, majesty and glory of God, we ought to worship Him in holiness. Come before His holy presence with heart and mind prepared, pay attention to hear and obey His Word. Watch that our mouths are not rash and guard our hearts so as not to be hasty in prayers and making promises to God. Vows to God (and man) are to be made carefully and thoughtfully. We are dealing with a God in heaven who is Omnipresent, Omniscient and Omnipotent, and He expects us to keep our vows, for He is a covenant keeping God. Failure to do so is a sin. Fear God, for He hates sin and unfaithfulness, and come before Him in awe and with reverence. AMEN. &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</p>";
    }

}



package info.ewai;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.speechlet.IntentRequest;
import com.amazon.speech.speechlet.LaunchRequest;
import com.amazon.speech.speechlet.SessionEndedRequest;
import com.amazon.speech.speechlet.SessionStartedRequest;
import com.amazon.speech.speechlet.SpeechletV2;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.json.SpeechletRequestEnvelope;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import com.amazon.speech.ui.Reprompt;
import com.amazon.speech.ui.SimpleCard;
import com.amazon.speech.ui.SsmlOutputSpeech;
import com.amazon.speech.ui.OutputSpeech;

public class OisixSpeechlet implements SpeechletV2 {
    private static final Logger log = LoggerFactory.getLogger(OisixSpeechlet.class);

    @Override
    public void onSessionStarted(SpeechletRequestEnvelope<SessionStartedRequest> requestEnvelope) {
        log.info("onSessionStarted requestId={}, sessionId={}", requestEnvelope.getRequest().getRequestId(),
                requestEnvelope.getSession().getSessionId());
        // any initialization logic goes here
    }

    @Override
    public SpeechletResponse onLaunch(SpeechletRequestEnvelope<LaunchRequest> requestEnvelope) {
        log.info("onLaunch requestId={}, sessionId={}", requestEnvelope.getRequest().getRequestId(),
                requestEnvelope.getSession().getSessionId());
        return getWelcomeResponse();
    }

    @Override
    public SpeechletResponse onIntent(SpeechletRequestEnvelope<IntentRequest> requestEnvelope) {
        IntentRequest request = requestEnvelope.getRequest();

        Intent intent = request.getIntent();
        String intentName = (intent != null) ? intent.getName() : null;

        log.info("onIntent requestId={}, sessionId={}, " + intentName, request.getRequestId(),
                requestEnvelope.getSession().getSessionId());

        if ("ReadTokushuIntent".equals(intentName)) {
            return getTokushuResponse();
        } else if ("ReadVipIntent".equals(intentName)) {
            return getVipResponse();
        } else if ("ReadSaleIntent".equals(intentName)) {
            return getSaleResponse();
        } else if ("AMAZON.HelpIntent".equals(intentName)) {
            return getHelpResponse();
        } else {
            return getAskResponse("オイシックス", "今週の特集は何？と言ってみてください。");
        }
    }

    @Override
    public void onSessionEnded(SpeechletRequestEnvelope<SessionEndedRequest> requestEnvelope) {
        log.info("onSessionEnded requestId={}, sessionId={}", requestEnvelope.getRequest().getRequestId(),
                requestEnvelope.getSession().getSessionId());
        // any cleanup logic goes here
    }

    private SpeechletResponse getWelcomeResponse() {
        String speechText = "オイシックスへようこそ！";
        return getAskResponse("オイシックス", speechText);
    }

    private SpeechletResponse getTokushuResponse() {
        log.info("getTokushuResponse(): start.");
        String speechText = "<speak>";

        try {
            Document document = Jsoup.connect("https://www.oisix.com/shop.g6--kit--course2_05w__html.htm").get();
            Elements elements = document.select(".head__navi_oisix__tokushu > .head__navi--list > li > a");
            log.info("list.size=" + elements.size());

            speechText += "<break time=\"2s\"/>今週の特集は" + elements.size() + "つあります。<break time=\"1s\"/>";
            int cnt = 1;
            for (Element e: elements) {
                Elements ps = e.getElementsByTag("p");
                speechText += cnt + "、";
                for (Element p: ps) {
                    speechText += "<break time=\"1s\"/>" + p.text();
                }
                speechText += "<break time=\"2s\"/>";
                cnt++;
            }
            speechText += "以上になります。</speak>";
        } catch (IOException e) {
            e.printStackTrace();
            speechText = "<speak>データの取得に失敗しました。</speak>";
        }

        SsmlOutputSpeech ssml = new SsmlOutputSpeech();
        ssml.setSsml(speechText);
        SimpleCard card = getSimpleCard("オイシックス", ssml.getSsml());

        log.info("getTokushuResponse(): end.");
        return SpeechletResponse.newTellResponse(ssml, card);
    }

    private SpeechletResponse getVipResponse() {
        String speechText = "<speak>";

        try {
            Document document = Jsoup.connect("https://www.oisix.com/shop.g6--shopping--special_vip__html.htm").get();
            Elements elements = document.select("#centercontent .shouhintag .kounyuudai__KikakuBig .tokuItem a");
            System.out.println("size=" + elements.size());
            int cnt = 1;

            speechText += elements.size() + "商品あります。<break time=\"2s\"/>";

            for (Element e: elements) {
                speechText += cnt + "<break time=\"1s\"/>" + e.text() + "<break time=\"1s\"/>";
                cnt++;
            }
            speechText += "<break time=\"2s\"/>";
            speechText += "以上になります。</speak>";
        } catch (IOException e) {
            e.printStackTrace();
            speechText = "<speak>データの取得に失敗しました。</speak>";
        }

        SsmlOutputSpeech ssml = new SsmlOutputSpeech();
        ssml.setSsml(speechText);
        SimpleCard card = getSimpleCard("オイシックス", ssml.getSsml());
        return SpeechletResponse.newTellResponse(ssml, card);
    }

    private SpeechletResponse getSaleResponse() {
        String speechText = "<speak>";

        try {
            Document document = Jsoup.connect("https://www.oisix.com/shop.g6--tokushuu--toku_140410outlet__html.htm").get();
            Elements elements = document.select("#centercontent .frame_set .set_txt strong");

            int size = 0;
            for (Element e: elements) {
                if (e.text().indexOf("30%OFF") > -1) size++;
            }
            speechText += size + "商品あります。<break time=\"2s\"/>";
            int cnt = 1;
            for (Element e: elements) {
                if (e.text().indexOf("30%OFF") > -1) {
                    speechText += cnt + "<break time=\"1s\"/>" + e.text();
                    cnt++;
                }
            }
            speechText += "<break time=\"2s\"/>";
            speechText += "以上になります。</speak>";
        } catch (IOException e) {
            e.printStackTrace();
            speechText = "<speak>データの取得に失敗しました。</speak>";
        }

        SsmlOutputSpeech ssml = new SsmlOutputSpeech();
        ssml.setSsml(speechText);
        SimpleCard card = getSimpleCard("オイシックス", ssml.getSsml());
        return SpeechletResponse.newTellResponse(ssml, card);
    }

    private SpeechletResponse getHelpResponse() {
        String speechText = "今週の特集を教えて！と言って見てください。";
        return getAskResponse("オイシックス特集", speechText);
    }

    private SimpleCard getSimpleCard(String title, String content) {
        SimpleCard card = new SimpleCard();
        card.setTitle(title);
        card.setContent(content);

        return card;
    }

    private PlainTextOutputSpeech getPlainTextOutputSpeech(String speechText) {
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText(speechText);

        return speech;
    }

    private Reprompt getReprompt(OutputSpeech outputSpeech) {
        Reprompt reprompt = new Reprompt();
        reprompt.setOutputSpeech(outputSpeech);

        return reprompt;
    }

    private SpeechletResponse getAskResponse(String cardTitle, String speechText) {
        SimpleCard card = getSimpleCard(cardTitle, speechText);
        PlainTextOutputSpeech speech = getPlainTextOutputSpeech(speechText);
        Reprompt reprompt = getReprompt(speech);

        return SpeechletResponse.newAskResponse(speech, reprompt, card);
    }
}

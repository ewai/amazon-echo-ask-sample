package info.ewai;

import java.util.HashSet;
import java.util.Set;

import com.amazon.speech.speechlet.lambda.SpeechletRequestStreamHandler;

public final class OisixSpeechletRequestStreamHandler extends SpeechletRequestStreamHandler {
    private static final Set<String> supportedApplicationIds;
    static {
        supportedApplicationIds = new HashSet<String>();
        supportedApplicationIds.add(System.getenv("ALEXA_APP_ID"));
    }

    public OisixSpeechletRequestStreamHandler() {
        super(new OisixSpeechlet(), supportedApplicationIds);
    }
}

package com.example.AITestMoodul.advisors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.core.Ordered;

import java.text.Normalizer;
import java.util.Map;
import java.util.regex.Pattern;

@Slf4j
public class RegexJailbreakAdvisor implements CallAdvisor {

  private static final int MAX_MESSAGE_LENGTH = 1_000;

  // Lihtsad jailbreak-katsed ("ignore your instructions", "you are now ...", "DAN mode") lastakse
  // meelega läbi - Insultbot roastib neid süsteemiprompti järgi. Kui mudel siiski kuuletub,
  // püüab selle kinni LlmJudgeSafetyAdvisori väljundi kontroll.
  // Siin blokeeritakse ainult rünnakud, mida pole mõtet roastida: prompti väljameelitamine,
  // võltsitud rollimärgendid, vastuse tüübi ette dikteerimine ja kodeeritud sisu.
  private static final Map<String, Pattern> PATTERNS = Map.ofEntries(
          Map.entry("prompt-extraction", Pattern.compile(
                  "(reveal|show|print|repeat|output|tell me)\\s+(me\\s+)?(your|the)\\s+(system\\s+)?(prompt|instructions|rules)")),
          Map.entry("fake-role-markers", Pattern.compile(
                  "(<\\|?(im_start|im_end|system|endoftext)\\|?>|\\[/?inst]|^\\s*(system|assistant)\\s*:|###\\s*(system|instruction))",
                  Pattern.MULTILINE)),
          Map.entry("output-manipulation", Pattern.compile(
                  "(set|mark|classify|label)\\s+(the\\s+|this\\s+|your\\s+)?(type|response|answer)\\s+(to|as)\\b")),
          Map.entry("encoded-payload", Pattern.compile(
                  "[a-z0-9+/]{120,}={0,2}")) // pikk base64-laadne plokk
  );

  private static final Pattern INVISIBLE_CHARS = Pattern.compile("[\\u200B-\\u200F\\u202A-\\u202E\\u2060-\\u2064\\uFEFF]");

  @Override
  public ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain) {
    UserMessage userMessage = request.prompt().getUserMessage();
    String message = userMessage == null ? "" : userMessage.getText();

    if (message.length() > MAX_MESSAGE_LENGTH) {
      throw block("too-long", "Message length " + message.length() + " exceeds " + MAX_MESSAGE_LENGTH);
    }

    String normalized = normalize(message);
    PATTERNS.forEach((name, pattern) -> {
      if (pattern.matcher(normalized).find()) {
        throw block(name, "Matched jailbreak pattern '" + name + "'");
      }
    });

    return chain.nextCall(request);
  }

  static String normalize(String text) {
    String nfkc = Normalizer.normalize(text, Normalizer.Form.NFKC);
    return INVISIBLE_CHARS.matcher(nfkc).replaceAll("")
            .toLowerCase()
            .replaceAll("\\h+", " "); // reavahetused jäävad alles, et ^system: muster töötaks
  }

  private GuardrailViolationException block(String rule, String reason) {
    log.warn("[RegexJailbreak] blocked rule={} reason={}", rule, reason);
    return new GuardrailViolationException(getName() + ":" + rule, GuardrailViolationException.Stage.INPUT, reason);
  }

  @Override
  public String getName() {
    return "regex-jailbreak-guard";
  }

  @Override
  public int getOrder() {
    return Ordered.HIGHEST_PRECEDENCE + 50;
  }
}

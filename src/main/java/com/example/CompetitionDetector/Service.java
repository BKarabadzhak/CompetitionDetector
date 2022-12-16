package com.example.CompetitionDetector;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class Service {

    @Value("${receivers}")
    private String[] receivers;
    private static final int TIME_TO_WAIT_IN_MILLISECONDS = 5000;
    public static final String SUBJECT = "Some competition was announced by Sofia Karting Ring";
    public static final String BODY = "Check website for updates: ";
    public static final String URL = "https://www.sodiwseries.com/en-gb/tracks/sofia-karting-ring-328.html";
    private static Collection<Node> competitionInfo;
    private final EmailService emailService;

    // All logs from the console are saved in the logs folder at the root level after the application stops.
    private final Logger logger = LoggerFactory.getLogger(Service.class);

    public static void initCompetitionInfo() throws IOException {
        competitionInfo = fetchCompetitionInfo();
    }

    public void sendEmail() {
        try {
            Collection<Node> updatedInfo = fetchCompetitionInfo();
            if (competitionInfo.size() != updatedInfo.size()) {
                logger.info("Updated number of competitions is {}", updatedInfo.size());

                if (updatedInfo.size() > competitionInfo.size()) {
                    fetchEmailDetails(SUBJECT, BODY + URL).forEach(emailService::sendEmail);
                    Thread.sleep(TIME_TO_WAIT_IN_MILLISECONDS);
                    //One more time to get more notifications
                    fetchEmailDetails(SUBJECT, BODY + URL).forEach(emailService::sendEmail);
                }

                competitionInfo = updatedInfo;
            }
        } catch (IOException e) {
            logger.error("Exception while checking for updates", e);
        } catch (InterruptedException e) {
            logger.error("Exception while sleeping", e);
            Thread.currentThread().interrupt();
        } catch (MailException e) {
            logger.error("Exception while sending emails", e);
        }
    }

    private static Collection<Node> fetchCompetitionInfo() throws IOException {
        // Parse HTML page and fetch a list of available competitions
        final Document doc = Jsoup.connect(URL).get();
        final Element element = doc.getElementById("track-map-table");
        final Node bodyTable = element.childNode(1);
        return bodyTable.childNodes();
    }

    private Collection<EmailDetails> fetchEmailDetails(String subject, String body) {
        return Arrays.stream(receivers).map(receiver -> new EmailDetails(receiver, subject, body))
                .collect(Collectors.toList());
    }
}

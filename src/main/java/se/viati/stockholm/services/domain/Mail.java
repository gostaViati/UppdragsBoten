package se.viati.stockholm.services.domain;

public class Mail {

    public final String id;
    public final String subject;
    public final String body;

    public Mail(String id, String subject, String body) {
        this.id = id;
        this.subject = subject;
        this.body = body;
    }
}

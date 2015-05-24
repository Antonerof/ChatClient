package message;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import com.sun.xml.ws.util.NoCloseInputStream;

@XmlRootElement(name = "message")
public class Message implements Serializable {
    private static final long serialVersionUID = 1L;

    private Date date = new Date();
    private String from;
    private String to;
    private String text;
    private int idRoom;

    public Message(String from, String to, String text) {
        this.from = from;
        this.to = to;
        this.text = text;
    }

    public Message() {
    }

    @Override
    public String toString() {
        return new StringBuilder().append("[").append(date.toString())
                .append(", From: ").append(from).append(", To: ").append(to)
                .append("] ").append(text).toString();
    }

    public int send(String url) throws Exception {
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        con.setRequestMethod("POST");
        con.setDoOutput(true);

        OutputStream os = con.getOutputStream();
        this.writeToStreamXML(os);
        os.flush();
        os.close();

        return con.getResponseCode();
    }

    public void writeToStreamXML(OutputStream outputStream) {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(Message.class);
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.marshal(this, outputStream);
        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }

    public static Message readFromStreamXML(InputStream in) {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(Message.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            if (in.available() > 0) {
                return (Message) unmarshaller.unmarshal(new NoCloseInputStream(in));
            }
            else return null;
        } catch (JAXBException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public int getIdRoom() {
        return idRoom;
    }

    @XmlElement
    public void setIdRoom(int idRoom) {
        this.idRoom = idRoom;
    }

    public Date getDate() {
        return date;
    }

    @XmlElement
    public void setDate(Date date) {
        this.date = date;
    }

    public String getFrom() {
        return from;
    }

    @XmlElement
    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    @XmlElement
    public void setTo(String to) {
        this.to = to;
    }

    public String getText() {
        return text;
    }

    @XmlElement
    public void setText(String text) {
        this.text = text;
    }
}

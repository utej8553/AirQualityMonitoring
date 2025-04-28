package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.List;
import javax.mail.*;
import javax.mail.internet.*;
import com.fazecast.jSerialComm.SerialPort;
import org.json.JSONArray;
import org.json.JSONObject;

public class SensorDashboard extends JPanel {
    private static final String API_KEY = "AIzaSyALEmHlwdPmoETiPYRNbse5xAxrY7hTD_4";
    private static float[] tempData = new float[20];
    private static float[] humidData = new float[20];
    private static float[] airQualData = new float[20];
    private static float[] currentData = airQualData;
    private static String currentType = "Air Quality";

    private static List<List<Integer>> sensorRecords = new ArrayList<>();
    private static SerialPort serialPort;
    private static SensorDashboard panel;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(SensorDashboard::launchUI);
    }

    public static void launchUI() {
        initializeSerialPort();

        JFrame frame = new JFrame("Sensor Dashboard");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 700);
        frame.setLocationRelativeTo(null);

        panel = new SensorDashboard();
        frame.add(panel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        String[] types = {"Temperature", "Humidity", "Air Quality"};

        for (String type : types) {
            JButton btn = new JButton(type);
            btn.addActionListener(e -> {
                currentType = type;
                currentData = switch (type) {
                    case "Temperature" -> tempData;
                    case "Humidity" -> humidData;
                    default -> airQualData;
                };
                panel.repaint();
            });
            buttonPanel.add(btn);
        }

        JButton qnaBtn = new JButton("Ask AI");
        qnaBtn.addActionListener(SensorDashboard::openQnA);
        buttonPanel.add(qnaBtn);

        frame.add(buttonPanel, BorderLayout.NORTH);
        frame.setVisible(true);
    }

    private static void openQnA(ActionEvent e) {
        SwingUtilities.invokeLater(() -> {
            JFrame qnaFrame = new JFrame("Ask AI");
            qnaFrame.setSize(600, 400);
            qnaFrame.setLayout(new BorderLayout());

            JTextArea contentArea = new JTextArea("");
            JTextArea answerArea = new JTextArea();
            answerArea.setEditable(false);

            String[] operations = {"summarize"};
            JComboBox<String> operationBox = new JComboBox<>(operations);

            JButton askButton = new JButton("Ask");

            askButton.addActionListener(a -> {
                String operation = (String) operationBox.getSelectedItem();
                String userContent = contentArea.getText();
                String answer = callGeminiAPI(sensorRecords, userContent, operation);
                answerArea.setText(answer);
            });

            JPanel topPanel = new JPanel(new BorderLayout());
            topPanel.add(operationBox, BorderLayout.WEST);
            topPanel.add(new JScrollPane(contentArea), BorderLayout.CENTER);

            qnaFrame.add(topPanel, BorderLayout.NORTH);
            qnaFrame.add(new JScrollPane(answerArea), BorderLayout.CENTER);
            qnaFrame.add(askButton, BorderLayout.SOUTH);

            qnaFrame.setLocationRelativeTo(null);
            qnaFrame.setVisible(true);
        });
    }

    private static String callGeminiAPI(List<List<Integer>> sensorData, String userInput, String operation) {
        if (API_KEY == null || API_KEY.isEmpty()) {
            return "❌ API key not set!";
        }

        List<List<Integer>> cleanedData = sensorData.size() > 3 ? sensorData.subList(3, sensorData.size()) : sensorData;

        StringBuilder dataText = new StringBuilder();
        for (List<Integer> row : cleanedData) {
            dataText.append(row.toString()).append("\n");
        }

        String prompt = buildPrompt(operation, dataText.toString(), userInput);

        JSONObject requestBody = new JSONObject()
                .put("contents", new JSONArray().put(new JSONObject()
                        .put("parts", new JSONArray().put(new JSONObject().put("text", prompt)))));

        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=" + "API_KEY"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) return "Error: " + response.body();

            JSONObject responseJson = new JSONObject(response.body());
            JSONArray parts = responseJson
                    .getJSONArray("candidates")
                    .getJSONObject(0)
                    .getJSONObject("content")
                    .getJSONArray("parts");

            return parts.getJSONObject(0).getString("text");

        } catch (Exception e) {
            e.printStackTrace();
            return "Failed: " + e.getMessage();
        }
    }

    private static String buildPrompt(String operation, String sensorData, String userInput) {
        StringBuilder prompt = new StringBuilder();

        switch (operation) {
            case "summarize" -> prompt.append("You are having a data in the form: {temperature, humidity, airQuality}, ignore the first value because that might be wrong!,just answer the questions provided politely: do not provide extra information\n\n");
            default -> throw new IllegalArgumentException("Unknown operation: " + operation);
        }

        prompt.append("Data:\n").append(sensorData);
        prompt.append("\nQuestion: :\n").append(userInput);

        return prompt.toString();
    }

    private static void updateGraph(float t, float h, float a) {
        System.arraycopy(tempData, 1, tempData, 0, 19);
        System.arraycopy(humidData, 1, humidData, 0, 19);
        System.arraycopy(airQualData, 1, airQualData, 0, 19);
        tempData[19] = t;
        humidData[19] = h;
        airQualData[19] = a;

        sensorRecords.add(List.of((int)t, (int)h, (int)a));
        panel.repaint();
    }

    private static void initializeSerialPort() {
        SerialPort[] ports = SerialPort.getCommPorts();
        if (ports.length == 0) {
            System.out.println("No serial ports available.");
            return;
        }
        serialPort = ports[0];
        serialPort.setBaudRate(9600);

        if (serialPort.openPort()) {
            System.out.println("Connected to: " + serialPort.getSystemPortName());
            new Thread(SensorDashboard::readSerialData).start();
        } else {
            System.out.println("Failed to open port.");
        }
    }

    private static void readSerialData() {
        StringBuilder buffer = new StringBuilder();
        while (true) {
            try {
                if (serialPort.bytesAvailable() > 0) {
                    byte[] data = new byte[serialPort.bytesAvailable()];
                    serialPort.readBytes(data, data.length);
                    buffer.append(new String(data));

                    while (buffer.toString().contains("\n")) {
                        String line = buffer.substring(0, buffer.indexOf("\n")).trim();
                        buffer.delete(0, buffer.indexOf("\n") + 1);
                        System.out.println("From Arduino: " + line);

                        String[] parts = line.split(",");
                        if (parts.length == 3) {
                            try {
                                float t = Float.parseFloat(parts[0].trim());
                                float h = Float.parseFloat(parts[1].trim());
                                float a = Float.parseFloat(parts[2].trim());
                                updateGraph(t, h, a);
                                if (a > 300) sendMail("⚠️ Alert! Poor Air Quality: AQI = " + a);
                            } catch (NumberFormatException ignore) {}
                        }
                    }
                }
                Thread.sleep(100);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void sendMail(String msg) {
        String from = "sender@gmail.com";
        String pass = "sender_password";
        String to = "receiver@gmail.com";

        Properties p = new Properties();
        p.put("mail.smtp.auth", "true");
        p.put("mail.smtp.starttls.enable", "true");
        p.put("mail.smtp.host", "smtp.gmail.com");
        p.put("mail.smtp.port", "587");

        Session session = Session.getInstance(p, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(from, pass);
            }
        });

        try {
            Message m = new MimeMessage(session);
            m.setFrom(new InternetAddress(from));
            m.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
            m.setSubject("Sensor Alert 🚨");
            m.setText(msg);
            Transport.send(m);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        setBackground(Color.BLACK);

        int width = getWidth();
        int height = getHeight();
        int graphHeight = height - 80;
        int barWidth = Math.max(8, (width - 100) / currentData.length);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString("📈 " + currentType, 20, 30);

        for (int i = 0; i <= 5; i++) {
            int y = height - 50 - i * (graphHeight / 5);
            g.setColor(Color.DARK_GRAY);
            g.drawLine(60, y, width - 40, y);

            int maxVal = currentType.equals("Air Quality") ? 500 : 100;
            g.setColor(Color.WHITE);
            g.drawString(String.valueOf((i * maxVal) / 5), 20, y + 5);
        }

        Color barColor = switch (currentType) {
            case "Humidity" -> new Color(137, 255, 255);
            case "Temperature" -> new Color(255, 165, 0);
            default -> new Color(255, 99, 132);
        };

        for (int i = 0; i < currentData.length; i++) {
            float val = currentData[i];
            float maxVal = currentType.equals("Air Quality") ? 500f : 100f;
            int h = (int) ((val / maxVal) * graphHeight);
            int x = 60 + i * (barWidth + 4);
            int y = height - h - 50;

            g.setColor(barColor);
            g.fillRoundRect(x, y, barWidth, h, 10, 10);
        }
    }
}

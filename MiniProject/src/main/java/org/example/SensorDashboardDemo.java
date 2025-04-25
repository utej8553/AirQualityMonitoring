package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.*;
import javax.swing.Timer;

public class SensorDashboardDemo extends JPanel {
    private static float[] tempData = new float[20];
    private static float[] humidData = new float[20];
    private static float[] airQualData = new float[20];
    private static float[] currentData = airQualData;
    private static String currentType = "Air Quality";

    private static Timer timer;
    private static SensorDashboardDemo panel;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(SensorDashboardDemo::launchUI);
    }

    public static void launchUI() {
        JFrame frame = new JFrame("Sensor Dashboard Demo");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 700);
        frame.setLocationRelativeTo(null);

        panel = new SensorDashboardDemo();
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

        // AI Button (can be expanded with functionality later)
        JButton qnaBtn = new JButton("Ask AI");
        qnaBtn.addActionListener(SensorDashboardDemo::openQnA);
        buttonPanel.add(qnaBtn);

        frame.add(buttonPanel, BorderLayout.NORTH);
        frame.setVisible(true);

        // Simulating sensor data update every second
        timer = new Timer(1000, e -> generateRandomData());
        timer.start();
    }

    private static void openQnA(ActionEvent e) {
        SwingUtilities.invokeLater(() -> {
            JFrame qnaFrame = new JFrame("Ask a Question");
            qnaFrame.setSize(600, 300);
            qnaFrame.setLayout(new BorderLayout());

            JTextArea questionBox = new JTextArea("Enter your question here...");
            JTextArea answerBox = new JTextArea();
            answerBox.setEditable(false);
            JButton askBtn = new JButton("Ask");

            askBtn.addActionListener(a -> {
                // Placeholder for AI QnA functionality
                String question = questionBox.getText();
                answerBox.setText("AI Answer to: " + question);
            });

            qnaFrame.add(new JScrollPane(questionBox), BorderLayout.NORTH);
            qnaFrame.add(new JScrollPane(answerBox), BorderLayout.CENTER);
            qnaFrame.add(askBtn, BorderLayout.SOUTH);

            qnaFrame.setLocationRelativeTo(null);
            qnaFrame.setVisible(true);
        });
    }

    private static void generateRandomData() {
        // Generate random sensor data to simulate real sensor readings
        float t = 20 + (float) (Math.random() * 10);  // Simulating Temperature between 20-30
        float h = 30 + (float) (Math.random() * 50);  // Simulating Humidity between 30-80
        float a = (float) (Math.random() * 500);      // Simulating Air Quality between 0-500

        updateGraph(t, h, a);
    }

    private static void updateGraph(float t, float h, float a) {
        System.arraycopy(tempData, 1, tempData, 0, 19);
        System.arraycopy(humidData, 1, humidData, 0, 19);
        System.arraycopy(airQualData, 1, airQualData, 0, 19);
        tempData[19] = t;
        humidData[19] = h;
        airQualData[19] = a;
        panel.repaint();
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

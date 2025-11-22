package flowroute;

import javax.swing.*;
import java.awt.*;

public class start extends JFrame {
    public start() {
        super("Start Page");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(720, 750);
        setLocationRelativeTo(null);

        JLayeredPane layeredPane = getLayeredPane();

        // 배경 이미지
        ImageIcon startImage = new ImageIcon("src/images/start_background.png");
        JLabel background = new JLabel(startImage);
        background.setBounds(0, 0, 720, 750);
        layeredPane.add(background, JLayeredPane.DEFAULT_LAYER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.setBounds(0, 0, 720, 750);
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));

        buttonPanel.add(Box.createVerticalGlue());

        JButton startButton = new JButton("");
        startButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        startButton.setOpaque(false);
        startButton.setContentAreaFilled(false);
        startButton.setBorderPainted(false);
        startButton.setFocusPainted(false);
        startButton.setFont(new Font("Arial", Font.BOLD, 18));
        startButton.setForeground(Color.WHITE);
        startButton.setPreferredSize(new Dimension(300, 200));
        startButton.setMaximumSize(new Dimension(300, 200));

        startButton.addActionListener(e -> {
            dispose(); // 창 닫고
            new login(); // 로그인 창으로 이동
        });

        buttonPanel.add(Box.createVerticalStrut(150));
        buttonPanel.add(startButton);
        buttonPanel.add(Box.createVerticalGlue());

        layeredPane.add(buttonPanel, JLayeredPane.PALETTE_LAYER);

        setVisible(true);
    }

    public static void main(String[] args) {
        new start();
    }
}

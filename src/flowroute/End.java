package flowroute;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class End extends JPanel {

    public End(String currentUser, int currentScore, JFrame parentFrame) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(new Color(20, 44, 70));

        int highscore = 0;

        // DB에서 highscore 가져오기 및 갱신
        try (Connection con = DBConnect.getConnection()) {

            if (con == null) {
                System.out.println("DB 연결 실패");
            }

            String sql = "SELECT COALESCE(high_score, 0) AS high_score FROM users WHERE user_id=?";
            PreparedStatement pstmt = con.prepareStatement(sql);
            pstmt.setString(1, currentUser);

            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                highscore = rs.getInt("high_score");
            }

            if (currentScore > highscore) {
                String updateSql = "UPDATE users SET high_score=? WHERE user_id=?";
                PreparedStatement updatePstmt = con.prepareStatement(updateSql);
                updatePstmt.setInt(1, currentScore);
                updatePstmt.setString(2, currentUser);
                int updatedRows = updatePstmt.executeUpdate();
                if (updatedRows > 0) {
                    highscore = currentScore;
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        // 점수
        JLabel scoreLabel = new JLabel("Score: " + currentScore);
        scoreLabel.setForeground(Color.WHITE);
        scoreLabel.setFont(new Font("Arial", Font.BOLD, 30));
        scoreLabel.setAlignmentX(CENTER_ALIGNMENT);

        JLabel highLabel = new JLabel("High Score: " + highscore);
        highLabel.setForeground(Color.YELLOW);
        highLabel.setFont(new Font("Arial", Font.BOLD, 30));
        highLabel.setAlignmentX(CENTER_ALIGNMENT);

        Color buttonColor = Color.decode("#2E688C");

        JButton retryBtn = new JButton("Retry");
        retryBtn.setFont(new Font("Arial", Font.BOLD, 18));
        retryBtn.setBackground(buttonColor);
        retryBtn.setForeground(Color.WHITE);
        retryBtn.setFocusPainted(false);
        retryBtn.setAlignmentX(CENTER_ALIGNMENT);
        retryBtn.setMaximumSize(new Dimension(200, 50));

        JButton mainBtn = new JButton("Main Menu");
        mainBtn.setFont(new Font("Arial", Font.BOLD, 18));
        mainBtn.setBackground(buttonColor);
        mainBtn.setForeground(Color.WHITE);
        mainBtn.setFocusPainted(false);
        mainBtn.setAlignmentX(CENTER_ALIGNMENT);
        mainBtn.setMaximumSize(new Dimension(200, 50));

        // 현재 게임창 닫고 새 GameFrame 생성
        retryBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                parentFrame.dispose();
                new GameFrame(currentUser);
            }
        });

        // start 화면 띄우고 게임 창 닫기
        mainBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                parentFrame.dispose();
                new start();
            }
        });

        add(Box.createVerticalGlue());
        add(scoreLabel);
        add(Box.createVerticalStrut(10));
        add(highLabel);
        add(Box.createVerticalStrut(30));
        add(retryBtn);
        add(Box.createVerticalStrut(10));
        add(mainBtn);
        add(Box.createVerticalGlue());
    }
}
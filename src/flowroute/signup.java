package flowroute;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class signup extends JFrame {
    public signup() {
        super("Sign Up Page");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(720, 750);
        setLocationRelativeTo(null);

        // 메인 패널
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.white);
        panel.add(Box.createVerticalGlue());

        // 회원가입 제목
        JLabel title = new JLabel("Sign Up", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 40));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(title);
        panel.add(Box.createVerticalStrut(30));

        // 아이디 입력 패널
        JPanel idPanel = new JPanel();
        idPanel.setLayout(new BoxLayout(idPanel, BoxLayout.X_AXIS));
        idPanel.setBackground(Color.white);
        idPanel.setMaximumSize(new Dimension(300, 35));
        idPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel idLabel = new JLabel("ID");
        idLabel.setPreferredSize(new Dimension(60, 30));
        JTextField idField = new JTextField();
        idField.setMaximumSize(new Dimension(250, 30));

        idPanel.add(idLabel);
        idPanel.add(Box.createHorizontalStrut(10));
        idPanel.add(idField);

        // 패스워드 입력 패널
        JPanel pwPanel = new JPanel();
        pwPanel.setLayout(new BoxLayout(pwPanel, BoxLayout.X_AXIS));
        pwPanel.setBackground(Color.white);
        pwPanel.setMaximumSize(new Dimension(300, 35));
        pwPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel pwLabel = new JLabel("PW");
        pwLabel.setPreferredSize(new Dimension(60, 30));
        JPasswordField pwField = new JPasswordField();
        pwField.setMaximumSize(new Dimension(250, 30));

        pwPanel.add(pwLabel);
        pwPanel.add(Box.createHorizontalStrut(10));
        pwPanel.add(pwField);

        // 회원가입 버튼
        JButton signupButton = new JButton("Sign Up");
        signupButton.setFont(new Font("Arial", Font.BOLD, 18));
        signupButton.setBackground(new Color(20, 44, 70));
        signupButton.setForeground(Color.WHITE);
        signupButton.setFocusPainted(false);
        signupButton.setPreferredSize(new Dimension(200, 50));
        signupButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        signupButton.setMaximumSize(new Dimension(200, 50));

        signupButton.addActionListener(e -> {
            String id = idField.getText();
            String pw = new String(pwField.getPassword());

            if (id.isEmpty() || pw.isEmpty()) {
                JOptionPane.showMessageDialog(this, "아이디와 비밀번호를 입력하세요.", "입력 오류", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try (Connection con = DBConnect.getConnection()) {
                PreparedStatement check = con.prepareStatement("SELECT * FROM users WHERE user_id=?");
                check.setString(1, id);
                ResultSet rs = check.executeQuery();

                if (rs.next()) {
                    JOptionPane.showMessageDialog(this, "이미 존재하는 아이디입니다.", "회원가입 실패", JOptionPane.ERROR_MESSAGE);
                } else {
                    PreparedStatement pstmt = con.prepareStatement("INSERT INTO users(user_id, user_pw) VALUES(?, ?)");
                    pstmt.setString(1, id);
                    pstmt.setString(2, pw);
                    pstmt.executeUpdate();

                    JOptionPane.showMessageDialog(this, "회원가입 성공!");
                    dispose(); // 회원가입 창 닫기
                    new login(); // 로그인 창 열기
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "DB 연결 오류 발생", "오류", JOptionPane.ERROR_MESSAGE);
            }
        });
        panel.add(idPanel);
        panel.add(Box.createVerticalStrut(15));
        panel.add(pwPanel);
        panel.add(Box.createVerticalStrut(30));
        panel.add(signupButton);
        panel.add(Box.createVerticalGlue());

        // 중앙 배치용 wrapper
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setBackground(Color.white);
        wrapper.add(panel);

        add(wrapper);
        setVisible(true);
    }

    public static void main(String[] args) {
        new signup();
    }
}

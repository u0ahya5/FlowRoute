package flowroute;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class login extends JFrame {
	public login() {
		super("Login Page");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setSize(720, 750);
		setLocationRelativeTo(null);

		// 메인 패널
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.setBackground(Color.white);
		panel.add(Box.createVerticalGlue());

		// 로그인 제목
		JLabel title = new JLabel("Login", SwingConstants.CENTER);
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
		idLabel.setPreferredSize(new Dimension(40, 30));
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
		pwLabel.setPreferredSize(new Dimension(40, 30));
		JPasswordField pwField = new JPasswordField();
		pwField.setMaximumSize(new Dimension(250, 30));

		pwPanel.add(pwLabel);
		pwPanel.add(Box.createHorizontalStrut(10));
		pwPanel.add(pwField);

		// 로그인 버튼
		JButton loginButton = new JButton("Login");
		loginButton.setFont(new Font("Arial", Font.BOLD, 18));
		loginButton.setBackground(new Color(20, 44, 70));
		loginButton.setForeground(Color.WHITE); // 글씨 색 지정
		loginButton.setFocusPainted(false); // 클릭 시 생기는 테두리 삭제
		loginButton.setPreferredSize(new Dimension(200, 50)); // 버튼 크기 지정
		loginButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		loginButton.setMaximumSize(new Dimension(200, 50));
		
		// 회원가입 버튼
        JButton signupButton = new JButton("Sign Up");
        signupButton.setFont(new Font("Arial", Font.BOLD, 16));
        signupButton.setBackground(new Color(20, 44, 70));
        signupButton.setForeground(Color.WHITE);
        signupButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        signupButton.setMaximumSize(new Dimension(200, 40));

        // 로그인 로직
        loginButton.addActionListener(e -> {
        	String id = idField.getText();
        	String pw = new String(pwField.getPassword());
        	
        	if(id.isEmpty() || pw.isEmpty()) {
        		JOptionPane.showMessageDialog(this, "아이디와 비밀번호를 입력하세요");
        		return;
        	}
        	
        	try(Connection con = DBConnect.getConnection()){
        		if(con == null) {
        			JOptionPane.showMessageDialog(this, "DB 연결 실패함");
        			return;
        		}
        		
        		String sql = "SELECT * FROM users WHERE user_id = ? AND user_pw = ?";
        		PreparedStatement pstmt = con.prepareStatement(sql);
        		pstmt.setString(1, id);
        		pstmt.setString(2, pw);
        		ResultSet rs = pstmt.executeQuery();
        		
        		if(rs.next()) {
        			JOptionPane.showMessageDialog(this, "로그인 성공!");
        			dispose();
        			new GameFrame(id);
        		} else {
        			JOptionPane.showMessageDialog(this, "아이디 또는 비밀번호가 잘못되었습니다.");
        		}
        	} catch(SQLException ex) {
        		ex.printStackTrace();
        		JOptionPane.showMessageDialog(this, "로그인 중 오류가 발생했습니다.");
        	}
        });
        
        // 회원가입 버튼 클릭 시
        signupButton.addActionListener(e -> {
        	dispose();
        	new signup();
        });

		panel.add(idPanel);
		panel.add(Box.createVerticalStrut(15));
		panel.add(pwPanel);
		panel.add(Box.createVerticalStrut(30));
		panel.add(loginButton);
		panel.add(Box.createVerticalStrut(10));
		panel.add(signupButton);
		panel.add(Box.createVerticalGlue());

		JPanel wrapper = new JPanel(new GridBagLayout());
		wrapper.setBackground(Color.white);
		wrapper.add(panel);

		add(wrapper);
		setVisible(true);
	}

	public static void main(String[] args) {
		new login();
	}
}
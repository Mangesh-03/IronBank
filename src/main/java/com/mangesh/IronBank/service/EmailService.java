package com.mangesh.IronBank.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendOtpEmail(String toEmail, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("IronBank — Your OTP Verification Code");
        message.setText(
                "Your OTP is: " + otp + "\n\n" +
                        "This OTP expires in 10 minutes.\n" +
                        "Do not share this with anyone."
        );
        mailSender.send(message);
    }

    public void welCome(String toEmail) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Welcome to IronBank");
        message.setText(
                "Dear Valued Customer,\n" +
                        "\n" +
                        "Welcome to IronBank! We are honored that you have chosen us as your trusted financial partner.  " +
                        "and we are committed to providing you with secure, seamless, and " +
                        "personalized banking solutions to help you achieve your financial goals. \n" +
                        "\n" +
                        "To get started, please log in to our mobile app or online portal using your credentials. " +
                        "Should you need any assistance, our dedicated support team is here for you 24/7. \n" +
                        "\n" +
                        "Thank you for trusting us with your financial journey. We look forward to serving you!\n" +
                        "\n" +
                        "Warm regards,\n" +
                        "The IronBank Team"
        );
        mailSender.send(message);
    }

    public void accountCreated(String email,String accountNumber)
    {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Welcome to IronBank");
        message.setText(
                "Dear Valued Customer,\n\n" +
                        "Your IronBank account has been successfully created!\n\n" +
                        "Account Number: " + accountNumber + "\n\n" +
                        "Please keep your account number safe. " +
                        "Do not share it with anyone.\n\n" +
                        "If you did not request this, please contact us immediately.\n\n" +
                        "Warm regards,\n" +
                        "The IronBank Team"
        );
        mailSender.send(message);
    }
}

package com.kamel.practice.service

import org.springframework.core.io.ClassPathResource
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service

@Service
class EmailSenderService(private val emailSender: JavaMailSender) {

    fun sendEmail(to: String, subject: String, body: String) {
        // val message = SimpleMailMessage()
        val mimeMessage = emailSender.createMimeMessage()
        val helper = MimeMessageHelper(mimeMessage, true)
        helper.setTo(to)
        helper.setSubject(subject)
        helper.setText(body, true) // true = HTML content

        val imageResource = ClassPathResource("static/me.jpg")
        helper.addAttachment("me.jpg", imageResource)
//        message.setTo(to)
//        message.subject = subject
//        message.text = body
        emailSender.send(mimeMessage)
        //emailSender.send(message)
    }
}
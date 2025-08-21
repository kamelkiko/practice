package com.kamel.practice.domain.service.email

import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Service

@Service
class EmailService(
    private val emailSender: JavaMailSender
) {
    fun sendEmail(
        to: String,
        subject: String,
        body: String
    ) {
        val simpleMailMessage = SimpleMailMessage()
        simpleMailMessage.setTo(to)
        simpleMailMessage.subject = subject
        simpleMailMessage.text = body
        emailSender.send(simpleMailMessage)
    }
}
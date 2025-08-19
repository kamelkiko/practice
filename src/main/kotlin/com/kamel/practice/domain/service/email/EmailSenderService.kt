package com.kamel.practice.domain.service.email

import org.springframework.core.io.Resource
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service

@Service
class EmailSenderService(private val emailSender: JavaMailSender) {

    fun sendEmailWithAttachment(to: String, subject: String, body: String, resource: Resource) {
        val mimeMessage = emailSender.createMimeMessage()
        val helper = MimeMessageHelper(mimeMessage, true)
        helper.setTo(to)
        helper.setSubject(subject)
        helper.setText(body, false)
        helper.addAttachment(resource.filename ?: "Unknown", resource)
        emailSender.send(mimeMessage)
    }

    fun sendEmail(to: String, subject: String, body: String) {
        val simpleMailMessage = SimpleMailMessage()
        simpleMailMessage.setTo(to)
        simpleMailMessage.subject = subject
        simpleMailMessage.text = body
        emailSender.send(simpleMailMessage)
    }
}